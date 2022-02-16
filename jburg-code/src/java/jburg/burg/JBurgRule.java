package jburg.burg;

import antlr.collections.AST;
import static jburg.parser.JBurgTokenTypes.*;
import static jburg.burg.JBurgUtilities.*;

import jburg.emitter.EmitLang;

import java.io.PrintWriter;
import java.util.Vector;

/**
 *  JBurgRule contains an AST that represents a rule, 
 *  its associated JBurgReduceAction, and the rule's
 *  static analysis characteristics.
 */
public class JBurgRule implements JBurgProduction
{
    /**
     *  The parsed rule.
     */
    final AST m_AST;

    final private JBurgGenerator generator;

    /**
     *  Patterns that have been optimized out are represented
     *  by this not-likely-to-compile sequence.
     */
    final private static String nilPattern = "-null-";
    
    /**
     *  If the rule is a pattern rule, its pattern matcher.
     */
    JBurgPatternMatcher patternMatcher = null;
    
    /**
     * The rule's reduction action.
     */
    JBurgReduceAction reduceAction;

    public JBurgRule(AST n, JBurgGenerator generator)
    {
        m_AST = n;
        this.generator = generator;
        
        if ( m_AST.getType() == PATTERN_RULE ) {

            AST pattern_root = m_AST.getFirstChild().getNextSibling().getFirstChild();

            try {
                this.patternMatcher = generateMatcher(pattern_root);
            } catch ( Exception ex ) {
                generator.config.getLogger().exception("Building pattern recognizer for " + m_AST.toStringTree(), ex);
            }
        }
    }

    /**
     * @param baseNode - the path to the base of the subtree.
     * @return the subtree's cost specification.
     */
    public String getCost(String baseNode)
    {
        if ( m_AST.getType() != SIMPLE_TRANSFORMATION_RULE )
        {
            AST cost_spec = m_AST.getFirstChild().getNextSibling().getNextSibling();

            String costText = cost_spec.getFirstChild().getText();

            if (costComputedViaFunction()) {
                if ( cost_spec.getFirstChild().getNextSibling() != null ) {
                    return generator.emitExpression(getASTByType(m_AST, PROCEDURE_CALL)).toString();
                } else {
                    return generator.getCodeEmitter().genCallMethod(null, costText, baseNode).toString();
                }
            } else {
                return costText;
            }
        } else {
            // Simple transformation rules don't cost anything.
            return "0";
        }
    }
    
    /** 
     *  @return true if the rule has a constant cost.
     */
    public boolean hasConstantCost()
    {
        if ( m_AST.getType() != SIMPLE_TRANSFORMATION_RULE )
        {
            AST cost_spec = m_AST.getFirstChild().getNextSibling()
                    .getNextSibling();

            String costText = cost_spec.getFirstChild().getText();

            boolean ownCostConstant =
                cost_spec.getType() == LITERAL_COST_SPEC ||
                generator.manifestConstants.containsKey(costText);
            
            return ownCostConstant &&
                 (this.isTerminalPattern() || this.patternMatcher == null);
        }
        else
        {
            // Simple transformation rules all cost 0.
            return true;
        }
    }
    
    /**
     *  @return the rule's cost.
     *  @throws IllegalStateException if the cost isn't constant.
     */
    public Integer getConstantCost()
    {
        if ( m_AST.getType() != SIMPLE_TRANSFORMATION_RULE )
        {
            AST cost_spec = m_AST.getFirstChild().getNextSibling()
                    .getNextSibling();

            String costText = cost_spec.getFirstChild().getText();

            if ( cost_spec.getType() == LITERAL_COST_SPEC )
            {
                return new Integer(costText);
            }
            else if ( generator.manifestConstants.containsKey(costText) )
            {
                return generator.manifestConstants.get(costText);
            }
            else
            {
                throw new IllegalStateException("non constant cost: " + costText);
            }
        }
        else
        {
            // Simple transformation rules don't cost anything.
            return 0;
        }
    }

    /**
     *  @return true if the cost specification is a function call.
     */
    public boolean costComputedViaFunction()
    {
        boolean result = false;

        if ( m_AST.getType() != SIMPLE_TRANSFORMATION_RULE )
            result = m_AST.getFirstChild().getNextSibling().getNextSibling().getType() == PROCEDURE_CALL;

        return result;
    }

    /**
     *  @return the rule's cached cost.
     */
    @Override
    public String getCachedCost()
    {
        return getCachedCost(generator.config.reducerNodeName);
    }
    
    /**
     * @param inodePath the expression that computes the reference to the i-node.
     */
    public String getCachedCost(String inodePath)
    {
        if ( this.costComputedViaFunction() && this.isStableCostFunction() )
            return String.format("cachedCost_%h()", getCost(generator.config.reducerNodeName));
        else
            return getCost(inodePath);

    }

    /**
     * @return true if the rule's cost is not volatile.
     */
    public boolean isStableCostFunction()
    {
        return generator.isStableCostFunction(getCost(generator.config.reducerNodeName));
    }

    /**
     *  @return the rule's reduction action.
     */
    @Override
    public JBurgReduceAction getReduceAction()
    {
        if ( null == this.reduceAction )
        {
            throw new IllegalStateException( "getReduceAction() has no action to return." );
        }

        return this.reduceAction;
    }

    /**
     *  @return the antecedent state of a nonterminal-to-nonterminal
     *    reduction, i.e., the state that the subtree must be reduced
     *    to before this reduction can run.
     */
    public String getAntecedentState()
    {
         if ( m_AST.getType() == SIMPLE_TRANSFORMATION_RULE )
         {
             return m_AST.getFirstChild().getNextSibling().getText();
         }
         else if ( m_AST.getType() == TRANSFORMATION_RULE )
         {
             return m_AST.getFirstChild().getNextSibling().getText();
         }
         else
             throw new IllegalStateException(String.format("no antecedent for %s", m_AST.getType()));
    }

    /**
     *  @return the node ID of the node at the root of the subtree.
     */
    public String getOperator()
    {
        int type = m_AST.getType();

        if ( PATTERN_RULE == type )
        {
            return m_AST.getFirstChild().getNextSibling().getFirstChild().getFirstChild().getText();
        }
        else
        {
            //  A transformation rule.
            return m_AST.getFirstChild().getNextSibling().getText();
        }
    }

    /**
     *  @return the nonterminal produced by this reduction.
     */
    @Override
    public String getGoalState()
    {
        return m_AST.getFirstChild().getText();
    }

    /**
     *  Set this rule's associated action code.
     */
    public void setReduceAction(JBurgReduceAction reduceAction)
    {
        this.reduceAction = reduceAction;
    }

    /**
     *  @return true if this node has no children (a "leaf" node).
     */
    public boolean isTerminalPattern()
    {
        return this.isFixedArity() && this.patternMatcher.getNominalArity() == 0;
    }

    /**
     *  @return true if this rule's pattern has a fixed number of "operand" subtrees.
     */
    public boolean isFixedArity()
    {
        return this.patternMatcher != null && !this.patternMatcher.hasNaryTail();
    }

    /**
     *  @return true if this rule needs an out-of-line method to check its cost.
     */
    public boolean needsExplicitCostCheck()
    {
        if ( this.isTerminalPattern() ) 
            return false;
        else if ( this.patternMatcher.hasNaryTail() )
            return  this.patternMatcher.getNominalArity() != this.patternMatcher.getMinimumNaryChildCount();
        else
            return true;
    }

    /**
     *  Is this production's cost constant in the
     *  context where it's to be invoked?
     *  @param productions - the set of productions at the
     *    point where this productions's cost is required.
     */
    @Override
    public boolean computesConstantCost(Multimap<String, JBurgProduction> productions)
    {
        return hasConstantCost();
    }

    /**
     *  Get the constant cost of a production.
     *  @param productions - the set of productions at the
     *    point where this production's cost is required.
     *  @return the cost, with overflow-safe addition.
     *  @pre computesConstantCost(productions) must be true.
     */
    @Override
    public int getConstantCost(Multimap<String, JBurgProduction> productions)
    {
        return getConstantCost();
    }

    @Override
    public String toString()
    {
        return m_AST.toStringTree();
    }

    /**
     * @return this rule's reduction logic
     * as platform-specific code.
     */
    public String getReductionCode()
    {
        EmitLang emitter = generator.getCodeEmitter();

        if ( m_AST.getType() == SIMPLE_TRANSFORMATION_RULE ) {

            return emitter.getTemplate(
                "exprStmt",
                "expr", emitter.getTemplate(
                    "returnValue",
                    "value",    generator.getCodeEmitter().genPopFromStack(
                        generator.config.reducedValuesName,
                        generator.config.getReturnType(this.getGoalState())
                    )
                )
            ).toString();

        } else if ( hasASTOfType(m_AST, EXPLICIT_REDUCTION ) ) {

            AST reduction = getASTByType(m_AST, EXPLICIT_REDUCTION);

            return emitter.getTemplate(
                "exprStmt",
                "expr", emitter.getTemplate(
                    "returnValue",
                    "value",    generator.emitExpression(getASTByType(reduction, PROCEDURE_CALL))
                )
            ).toString();

        } else if ( hasASTOfType(m_AST, REDUCTION_ACTION) ) {

            AST reduction = getASTByType(m_AST, REDUCTION_ACTION);
            return getCodeBlock(reduction);

        } else {
            throw new IllegalStateException("A reduction must specify a reduction function");
        }
    }

    /**
     * @return a unique identifier for this rule.
     */
    public String getUniqueId()
    {
        return String.format("Rule_%h", this);
    }

    /**
     * Translate a pattern specification into a pattern matcher.
     * @param pattern_root the root of the pattern.
     */
	JBurgPatternMatcher generateMatcher(AST pattern_root)
	throws Exception
	{   
        JBurgPatternEncoder patternBURM = new JBurgPatternEncoder();

        //  As we traverse the subtree, we may find parameterized subtrees,
        //  for example, in ADD(expr lhs, expr rhs) lhs and rhs are paramterized
        //  subtrees.  These subtrees play several parts in the computation of the
        //  locally-optimal reduction:
        //  -  They contribute to the rule's computed cost.
        //  -  The reduction's action code may refer to these elements by name.
        //  -  If the rule is of the form OP(nttype1 a [, nttype2 b]), then the rule
        //     must enforce this reduce-time goal state on its subtrees' reductions.
        patternBURM.setSubgoals(new Vector<JBurgPatternMatcher>());
        
        //  There may also be named terminals in the pattern;
        //  as a convenience, record these so that they
        //  can be used by name in the reduction.
        patternBURM.setNamedterminals(new Vector<JBurgPatternMatcher>());
        
        try
        {
            patternBURM.burm( pattern_root );
        }
        catch (IllegalStateException burm_error )
        {
            if ( generator.config.patternMatcherDumpFile != null )
            {
                //  Dump the BURM's debugging info.
                patternBURM.dump(new PrintWriter(generator.config.patternMatcherDumpFile));
            }
            
            throw burm_error;
        }
        
        JBurgPatternMatcher recognizer = (JBurgPatternMatcher) patternBURM.getResult();
        recognizer.setParameterizedSubtrees(patternBURM.getSubgoals());
        recognizer.setNamedSubtrees(patternBURM.getNamedterminals());

        return recognizer;
	}
}
