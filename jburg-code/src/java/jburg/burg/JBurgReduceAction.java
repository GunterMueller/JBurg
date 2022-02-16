package jburg.burg;

import java.util.ArrayList;

/**
 *  JBurgReduceAction holds action code fragments and their associated "parameters."
 *  A rule specifies "parameters" by naming individual subgoal states within a pattern
 *  rule specifiction, for example,
 *  <xmp>integer = PLUS ( integer i1, integer i2 )</xmp>
 *  In this example, i1 and i2 are the action routine's "parameters."  Since the action
 *  routines all share a common signature, the parameters are passed via the reducer's
 *  reduced values stack.
 */
class JBurgReduceAction
{
    /** The reduction that triggers this action. */
    private final JBurgRule rule;

    /** The parent generator. */
    private final JBurgGenerator generator;
    
    /**
     * The non-terminal state produced by this action, e.g., expression from
     * <xmp>expression = ADD(expression l, expression r)</xmp>
     */
    private final String m_state;

    /**
     *  The antecedent reduction of a nonterminal-to-nonterminal rule.
     */
    private String antecedentState;
    
    /**
     *  The operator ID of a pattern rule.
     */
    private String m_operator;
    
    /**
     * Names and types of the routine's parameters.
     * Types are given as their non-terminal state names; toString()
     * translates these BURM-centric types into the corresponding
     * types in the target language using the mapping set up by
     * the ReturnType directives in the input specification.
     */
    private ArrayList<ParameterDescriptor> m_parameterList = new ArrayList<ParameterDescriptor>();

    /**
     *  This action routine's index, assigned in entry order.
     *  The action routines are enumerated as action_1(JBurgNode p), action_2(JBurgNode p),
     *  etc. in the generated reducer.
     */
    int index;
    
    /**
     * Track name-to-subtree mappings to be emitted.
     */
    class NamedSubtree
    {
        Object path;
        Object name;
        
        NamedSubtree(Object path, Object name)
        {
            this.path = path;
            this.name = name;
        }
    }
    /**
     *  Saved name-to-subtree mappings.
     */
    ArrayList<NamedSubtree> namedChildNodes = new ArrayList<NamedSubtree>();
    
    /**
     * Construct a reduce action.
     * @param strActionCode - the action's implementation
     * code, in a target-specific language 
     */
    public JBurgReduceAction(JBurgRule rule, String resultState, JBurgGenerator generator)
    {
        this.rule  = rule;
        this.m_state = resultState;
        this.generator = generator;
    }

    /**
     * Add a parameter to the action's parameter list.
     */
    public void addParameter(String parmName, String parmState, ParameterDescriptor.ArityType arityType)
        throws Exception
    {
        m_parameterList.add(new ParameterDescriptor(parmName, parmState, arityType));
    }

    /**
     *  Return this action's index (the N constituent of its generated name, action_N).
     */
    public int getIndex()
    {
        return this.index;
    }

    /**
     *  Set this action's index (the N constituent of its generated name, action_N).
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * @return the non-terminal state this reduction derives.
     */
    public String getState()
    {
        return this.m_state;
    }

    /**
     *  @param operator the operator at the root of 
     *    the matched subtree.
     */
    public void setOperator(String operator)
    {
        this.m_operator = operator;
    }

    /**
     * @return the operator at the root of 
     *    the matched subtree.
     */
    public String getOperator()
    {
        return m_operator;
    }

    /**
     *  @return the reduction action's code, with
     *  prologue logic to pop its parameters off the stack.
     */
    public String emit()
    {
        // TODO: Make this a StringTemplate
        String result = "";

        for ( ParameterDescriptor next_param: m_parameterList ) {

            String paramName = next_param.paramName;
            String paramType = generator.config.getReturnType(next_param.paramState);

            if ( next_param.arityType == ParameterDescriptor.ArityType.Variable ) {
                paramType = generator.getCodeEmitter().getTemplate("declareVariadicReturnType", "contentsType", paramType).toString();
                result += generator.getCodeEmitter().genVariadicActionRoutineParameter(generator.config.reducedValuesName, paramType, paramName);
            } else {
                result += generator.getCodeEmitter().genActionRoutineParameter(generator.config.reducedValuesName, paramType, paramName);
            }
        }
        
        for ( NamedSubtree named_child: this.namedChildNodes )
        {
            result += "\n\t" + generator.getCodeEmitter().genLocalVar(generator.config.iNodeClass, named_child.name, named_child.path) + ";";
        }

        // convert \r\n to \n so the generated
        // files have consistent line endings.
        String[] actionlines = this.rule.getReductionCode().split("\n");
        for( int l = 0; l < actionlines.length; ++l )
        {
            if(actionlines[l].endsWith("\r"))
            {
                actionlines[l] = actionlines[l].substring(0,actionlines[l].length()-1);
            }
            result += "\n" + actionlines[l];
        }

        return result;
    }

    /**
     *  Track a name-to-subtree mapping.  These are unreduced I-nodes,
     *  typically a terminal identified by pattern match.
     *  @param path - the path from the root node to the subtree.
     *  @param name - the name to give to the subtree.
     */
    public void addNamedSubtree(Object path, Object name)
    {
        namedChildNodes.add(new NamedSubtree(path, name));
    }

    /**
     *  Set this reduction's antecedent state, which
     *  determines what reduction needs to run before
     *  this one to transform one nonterminal to another.
     */
    public void setAntecedentState(String antecedentState)
    {
        this.antecedentState = antecedentState;
    }

    /**
     *  @return true if this rule has an antecedent state,
     *    which also means it's a nonterminal-to-nonterminal rule.
     */
    public boolean hasAntecedentState()
    {
        return getAntecedentState() != null;
    }

    /**
     *  @return this rule's antecedent state,
     *    or null if no antecedent is present.
     */
    public String getAntecedentState()
    {
        return this.antecedentState;
    }
}
