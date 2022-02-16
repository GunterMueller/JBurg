package jburg.emitter;

import java.io.PrintStream;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jburg.burg.AnnotationAccessor;
import jburg.burg.Configuration;
import jburg.burg.JBurgRule;
import jburg.burg.JBurgPatternMatcher;

import jburg.burg.ir.*;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;

/**
 * An emitter that uses string templates to build up constructs.
 */
@SuppressWarnings({"nls","rawtypes"})
public abstract class TemplateBasedEmitter implements EmitLang
{
    /**
     * Construct an emitter and load its templates.
     */
	protected TemplateBasedEmitter(String templateGroupName)
    {
        this.templates = new TemplateGroup("templates", templateGroupName + "Snippets");
        this.boilerplate = new TemplateGroup("templates", templateGroupName + "Boilerplate");

        registerRenderer(Integer.class,             new ModifiersRenderer());
        registerRenderer(Class.class,               new ExceptionRenderer());
        registerRenderer(FormalParameter.class,     new ParametersRenderer());
        registerRenderer(GetNthChild.class,         new GetNthChildRenderer());
        registerRenderer(GetArity.class,            new GetArityRenderer());
        registerRenderer(GetOperator.class,         new GetOperatorRenderer());

        setDefaultAttribute("staticsSuffix", this.staticsSuffix);

        // Assume the nonterminal names are ints.
        setNtType("int");

        // Assume the operator type is int.
        setOpcodeType("int");
	}

    @Override
	public abstract boolean accept(String langName);
    
	/** I-node adapter in use. */
	protected jburg.burg.inode.InodeAdapter inodeAdapter;
	
    /** The source tree's node type. */
    protected String iNodeType;

	/** Prefix to internal BURM names. */
	protected String internalPrefix = "__";

    /** Operator type, defaults to int */
    protected String operatorType;

    /** Nonterminal type, defaults to int */
    protected String ntType;

    /** A for-loop header to iterate over the nonterminals. */
    protected Object ntIterator;

    /** The name of the i'th state */
    protected Object ntStateName;

    /** Enumerated nonterminals don't get decorated. */
    protected boolean enumeratedNonterminals = false;

    /** The annotation accessor in use. */
    protected AnnotationAccessor annotationAccessor;

    /** The storage allocator in use, or null for none. */
    protected Object storageAllocator;

    /** The Configuration in use. */
    protected Configuration configuration;

    /**
     * The string templates that build up the pattern matchers and dynamic programming engine.
     */
    protected final TemplateGroup templates;

    /**
     * String templates for largely pre-written source, copied into the generated code.
     */
    protected final TemplateGroup boilerplate;

    /**
     * Annotation classes whose static analysis indicates they
     * can be emitted as static singletons.
     */
    protected List<String> singletonAnnotations = new ArrayList<String>();

    /**
     * Pseudorandom suffix for static data, in case two BURMs live in one load module.
     */
    protected int staticsSuffix = Math.abs(new java.util.Random().nextInt());

    /**
     * When set, the emitter may emit debugging support.
     */
    protected boolean debugMode;

    @Override
    public void setOpcodeType(String operator_type)
    {
        this.operatorType = operator_type;
        setDefaultAttribute("operatorType", operator_type);
    }

    @Override
    public String getOpcodeType()
    {
        return this.operatorType;
    }

    public void setConfiguration(Configuration config)
    {
        this.configuration = config;
        setDefaultAttribute("configuration",config);
        this.templates.setConfiguration(config);
        this.boilerplate.setConfiguration(config);
    }

    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    public void setNtType(String nt_type)
    {
        this.ntType = nt_type;
        setDefaultAttribute("ntType", nt_type);

        this.enumeratedNonterminals = !(nt_type.equals("int"));

        if ( this.enumeratedNonterminals )
        {
            this.ntIterator = getTemplate("ntIteratorEnumerated");
            this.ntStateName = getTemplate("ntStateNameEnumerated");
        } else {
            this.ntIterator = getTemplate("ntIteratorInt");
            this.ntStateName = getTemplate("ntStateNameInt");
        }
    }

    public void setAnnotationAccessor(AnnotationAccessor annotationAccessor)
    {
        this.annotationAccessor = annotationAccessor;
        setDefaultAttribute("annotationAccessor", annotationAccessor);
    }
	
	protected String reducerStack()
	{
	    return internalPrefix + "reducedValues";
	}

    public void setAllocator(Object storageAllocator)
    {
        this.storageAllocator = storageAllocator;
        setDefaultAttribute("storageAllocator", storageAllocator);
    }
	
	protected String subgoalArray()
	{
	    return internalPrefix + "_subgoals_by_rule";
	}

    protected void registerRenderer(Class<?> clazz, AttributeRenderer renderer)
    {
        templates.registerRenderer(clazz, renderer);
        boilerplate.registerRenderer(clazz, renderer);
    }

    @Override
    public void setClassName(String className)
    {
        setDefaultAttribute("className", className);
    }

    @Override
    public void setDebugMode(Boolean debugMode)
    {
        this.debugMode = debugMode;
        if (debugMode) {
            setDefaultAttribute("debugMode", debugMode);
        } else {
            setDefaultAttribute("debugMode", null);
        }
    }
	
    @Override
    public StringTemplate getTemplate(String name, Object ... attrValue)
    {
        return this.templates.getTemplate(name, attrValue);
    }

    @Override
    public void emitHeader(
        String packageName,
        String headerBlock,
        String baseClassName,
        List<String> interfaceNames,
        PrintStream output
        )
	{
		if (packageName != null)
			output.print(getTemplate( "packageHeader", "packageName", packageName ));

		if (headerBlock != null) {
			//  Strip off the enclosing "{" and "}".
			output.print(headerBlock.substring(1,
						 headerBlock.length() - 2));
			output.print("\n");
		}

        output.print(getTemplate("classHeader", "className", this.configuration.options.className, "baseClassName", baseClassName, "interfaceNames", interfaceNames ));

        // The entire class is too large to enclose in a block.
		output.println(getTemplate("beginEmitterBody", "reducerStack", reducerStack()));

        if (configuration.ntType.equals("int")) {
            output.print(getTemplate("nonterminalConstants", "nStates", configuration.goalStateNames.size()));
        }

        if (this.annotationAccessor != null) {
            output.print(getTemplate("annotationAccessor"));
        }
	}
    
    /**
     *  Emit the static subgoal arrays.
     *  This method is suitable for hosts that
     *  support static array literals; see the
     *  C++ emitter for a solution to a less
     *  hospitable environment.
     */
    public void emitSubgoalLookupTables(int max_action, Map<Integer, Vector<JBurgPatternMatcher>> rules_by_action, PrintStream output)
    {
        output.println();
        StringTemplate outerBlock = getTemplate("arrayLiteral");
        StringTemplate subgoalTableHeader = getTemplate(
            "subgoalTableHeader",
            "subgoalArrayName", subgoalArray(),
            "declContents", outerBlock
        );
        for ( int i = 0; i <= max_action; i++ )
        {
            if ( rules_by_action.containsKey(i))
            {
                StringTemplate innerBlock = getTemplate("arrayLiteral");
                // TODO: Parameterize the function
                outerBlock.setAttribute("contents",innerBlock);
                
                //  Emit the subgoals in reverse order so they are reduced and pushed
                //  onto the stack in the correct order.
                Vector<JBurgPatternMatcher> matchers = rules_by_action.get(i); 
                for ( int j = matchers.size() -1; j >= 0; j--  )
                {
                    JBurgPatternMatcher matcher = matchers.elementAt(j);
                    Vector<JBurgPatternMatcher.PathElement> accessPath = matcher.generateAccessPath();
                    innerBlock.setAttribute("contents",
                        getTemplate(
                            "subgoalDeclaration",
                            "subgoal", genGetGoalState(matcher.getSubgoal()),
                            "initialPosition",
                                matcher.isNary() ?
                                    getTemplate("subgoalInitialNary", "position", matcher.getPositionInParent()):
                                    getTemplate("subgoalInitialFixed"),
                            "accessPath",
                                accessPath.size() > 0?
                                    accessPath:
                                    null
                        )
                    );
                }
            }
            else
            {
                outerBlock.setAttribute("contents",getTemplate("subgoalMissing"));
            }
        }
        output.println(subgoalTableHeader);
    }


    @Override
	public void emitTrailer(
			Set<String>             goalStateNames, 
			Map<String, Object>     burm_properties, 
            Object                  default_error_handler,
            Map<Integer,Object>     prologue_blocks,
			PrintStream             output
		)
	{
        String problemTree = "__problemTree";

        StringTemplate trailer = boilerplate.getTemplate(
            "trailer",
            "defaultErrorHandler", default_error_handler,
            "problemTree", problemTree,
            "ntIterator", this.ntIterator,
            "getNthChildI", new GetNthChild("node","i"),
            "subParentArity", new GetArity("sub_parent"),
            "wildcardGoal",
                enumeratedNonterminals?
                    getTemplate("ntMissingEnumerated"):
                    getTemplate("ntMissingInt")
        );

        // Emit prologue blocks if they were specified.
        if ( prologue_blocks.size() > 0 )
        {
            ArrayList<StringTemplate> prologueCases = new ArrayList<StringTemplate>();
            trailer.setAttribute("prologueCases", prologueCases);

            for ( Integer rule: prologue_blocks.keySet() )
            {
                prologueCases.add(
                    getTemplate(
                        "switchCase",
                        "label", rule,
                        "contents", prologue_blocks.get(rule)
                    )
                );
            }
        }

		//  Emit BURM properties and their get/set methods.
        for ( Map.Entry entry: burm_properties.entrySet() )
		{

			//  Convert the property's name to canonical form, for inclusion
			//  in the get/set method names.
			StringBuffer canonicalName = new StringBuffer(entry.getKey().toString().toLowerCase());
			canonicalName.setCharAt(0, Character.toUpperCase( canonicalName.charAt( 0 )));

            output.println(
                getTemplate(
                    "BURMProperty",
                    "propertyType", entry.getValue(),
                    "propertyName", entry.getKey(),
                    "canonicalName", canonicalName
                )
            );
		}

        output.println(trailer);

		// Emit dump routines.
        output.println(boilerplate.getTemplate(
                "debuggingSupport",
                "problemTree", problemTree,
                "stateNames", goalStateNames,
                "ntStateName", this.ntStateName,
                "ntIterator", this.ntIterator,

                // FIXME: expand the embedded node.getNode() call.
                "getOperator", new GetOperator("node.getNode()")
            )
        );

		//  Emit the JBurgAnnotation classes.
		emitAnnotation(output);

        output.print(getTemplate(
                "defineSingletonAnnotations",
                "singletonAnnotations", singletonAnnotations
            )
        );

        //  Close the class definition.
        output.println( genEndClass() );
	}

	@Override
    public Object genActionRoutineParameter(Object stackName, Object paramType, Object paramName)
	{
        return getTemplate(
            "actionRoutineParameter",
            "paramType", paramType,
            "paramName", paramName,
            "initialValue", genPopFromStack(stackName, paramType)
        );
	}

    @Override
    public Object genVariadicActionRoutineParameter(Object stackName, Object paramType, Object paramName)
	{
        return genActionRoutineParameter(stackName, paramType, paramName);
	}	

    @Override
    public Object genPopFromStack(Object stackName, Object valueType)
    {
        return getTemplate(
            "popFromStack",
            "valueType", valueType,
            "stackName", stackName
        );
    }

	@Override
    public Object genPushToStack(Object stackName, Object value )
	{
        return getTemplate(
            "pushToStack",
            "stackName", stackName,
            "value", value
        );
	}

	@Override
    public Object genCheckPtr(Object expr, boolean checkForNull) {
        return getTemplate(
            checkForNull? "isNotNull": "isNull",
            "expr", expr
        );
	}

	@Override
    public Object genAccessMember(Object parentName, Object memberName)
	{
		return getTemplate(
            "memberAccess",
            "stem", parentName,
            "member", memberName
        );
	}

	@Override
    public Object genCallMethod(Object parentName, Object methodName, Object ... params)
	{
		return getTemplate(
            "callMethod",
            "params", params,
            "nameElements", parentName,
            "nameElements", methodName
        );
	}
	
	public void setInodeAdapter(jburg.burg.inode.InodeAdapter adapter)
	{
		this.inodeAdapter = adapter;
		setDefaultAttribute("iNodeAdapter", adapter);
	}
	
	@Override
	public void emitInclass(List<? extends Object> inclassBlocks, PrintStream output) 
	{
		output.println(getTemplate("adHocDirectives", "directives", inclassBlocks));
	}

	@Override
    public Object genCmpEquality(Object lhs, Object rhs, boolean testEquals)
	{
        Object result = testEquals?
            getTemplate("equals", "lhs", lhs, "rhs", rhs):
            getTemplate("notEquals", "lhs", lhs, "rhs", rhs);

		return result.toString();
	}

	@Override
    public Object genLogicalAnd(Object lhs, Object rhs)
	{
	   if (lhs != null && rhs != null) {
           return getTemplate(
               "logicalAnd",
               "lhs", lhs,
               "rhs", rhs
           );
       } else if (lhs == null) {
	       return rhs;
       } else {
	       return lhs;
       }
	}

	@Override
    public Object genCmpLess( Object lhs, Object rhs )
	{
        // FIXME: This is lhs > rhs, i.e., greaterThan
        return getTemplate("greaterThan", "lhs", lhs, "rhs", rhs);
	}

	@Override
    public Object genCmpGtEq(Object lhs, Object rhs)
	{
        return getTemplate("greaterThanOrEquals", "lhs", lhs, "rhs", rhs);
	}
	@Override
    public Object genNot( Object operand )
	{
        return getTemplate("not", "operand", operand);
	}

	@Override
    public Object genGetGoalState(Object p)
	{
       String templateName = enumeratedNonterminals ?  "goalStateEnumerated": "goalStateInteger";
       return getTemplate(templateName, "nt", genGetUndecoratedGoalState(p));
	}

    @Override
    public Object genGetUndecoratedGoalState( Object p )
    {
       Object rawNT = p instanceof JBurgRule ?  ((JBurgRule)p).getGoalState(): p;

       return enumeratedNonterminals ?
           rawNT:
           getTemplate("goalStateUndecorated", "nt", rawNT);
    }

	@Override
    public Object genComment( Object text )
	{
        return getTemplate("comment", "text", text);
	}

	@Override
    public Object genAddition( Object a1, Object a2 )
	{
		if ( a1 == null || a1.equals("0") )
			return a2;
		if ( a2 == null || a2.equals("0") )
			return a1;
		else
            return getTemplate("add", "lhs", a1, "rhs", a2);
	}

	@Override
    public Object genAssignment( Object lvar, Object rvalue )
	{
        return getTemplate("assign", "lvar", lvar, "rvalue", rvalue);
	}

	@Override
    public Object genCast(Object newClass, Object target)
	{
		return getTemplate("cast", "newClass", newClass, "target", target);
	}

	@Override
    public Object genLocalVar ( Object type, Object name, Object initializer )
	{
		return getTemplate(
                "declareInstanceField",
                "name", name,
                "type", type,
                "initializer", initializer
        );
	}

	@Override
    public Object declareMethod( int modifiers, Object returnClass, Object name, FormalParameter[] formals, Class[] exceptions )
	{       
		return getTemplate(
            "declareMethod",
            "modifiers",  modifiers,
            "returnType", returnClass,
            "name",       name,
            "parameters", formals,
            "exceptions", exceptions
        );
    }

	@Override
    public Object genThrow( Object diagnostic )
	{
		return getTemplate("throwDiagnostic", "diagnostic", diagnostic);
	}

	@Override
    public Object genReturnValue( Object value )
	{
		return getTemplate( "returnValue", "value", value);
	}

    public Object genEndClass()
	{
		return getTemplate("endEmitterBody");
	}

	@Override
    public Object genInstanceField ( int modifiers, Object type, Object name, Object initializer )
	{
		return getTemplate(
                "declareInstanceField",
                "modifiers",    modifiers,
                "name",         name,
                "type",         type,
                "initializer",  initializer
        );
	}

	@Override
    public Object genMaxIntValue()
	{
		return getTemplate("maxIntValue");
	}

	@Override
    public Object genNewObject( Object type, Object ... parameters )
	{
        return getTemplate(
            "newObject",
            "type", type,
            "parameters", parameters
        );
	}

	@Override
    public Object genNullPointer()
	{
		return getTemplate("nullPointer");
	}

	@Override
    public Object genWhileLoop( Object test_condition )
	{
		return getTemplate(
            "whileHeader",
            "test", test_condition
        );
	}

	@Override
    public Object genNaryContainerType(Object contentsType)
	{
        return getTemplate("declareContainer", "contentsType", contentsType);
	}

    @Override
    public Object defineContainer(Object contentsType)
    {
        return getTemplate(
            "newObject",
            "type", genNaryContainerType(configuration.options.annotationInterfaceName)
        );
    }
	
    @Override
	public void setINodeType(String inode_type)
	{
	    this.iNodeType = inode_type;
	    setDefaultAttribute("iNodeClass", inode_type);
	}

    @Override
    public String getINodeType()
    {
	    return this.iNodeType;
    }

    @Override
    public Object genOverflowSafeAdd(Object expr1, Object expr2)
    {
		if ( expr1 == null || expr1.equals("0") )
			return expr2.toString();
		else if ( expr2 == null || expr2.equals("0") )
			return expr1.toString();
        else
            return getTemplate("normalizedAdd", "expr1", expr1, "expr2", expr2);
    }

    /**
     * Set a default attribute in the template manager.
     * @param key the attribute's name.
     * @param value the attribute's value.
     */
    public void setDefaultAttribute(String key, Object value)
    {
        this.templates.setDefaultAttribute(key, value);
        this.boilerplate.setDefaultAttribute(key, value);
    }

    public StringTemplate getConstructorBody(Object decl, Object baseClass, Object ... superParameters)
    {
        return getTemplate(
            "constructorBody",
            "decl", decl,
            "baseClassName", baseClass,
            "superParameters", "burm,node"
        );
    }

    public StringTemplate getNarySubtreeAdd()
    {
        return getTemplate("narySubtreeAdd");
    }

    public void addSingletonAnnotationClass(String className)
    {
        this.singletonAnnotations.add(className);
    }

    public Object getStringIntConcat(Object strArg, Object intArg)
    {
        return getTemplate("stringIntConcat", "strArg", strArg, "intArg", intArg);
    }

    public Object getReferenceType(Object baseType)
    {
        return getTemplate("getReferenceType", "baseType", baseType);
    }

    @Override
    public Object getAddChild()
    {
        return getTemplate(
                    "callMethod",
                    "nameElements", "narySubtrees",
                    "nameElements", getNarySubtreeAdd(),
                    "params", "child"
                );
    }

	protected void emitAnnotation(PrintStream output) 
	{
        output.println(
            boilerplate.getTemplate(
                "annotation",
                "getOperator",      new GetOperator("m_node"),
                "getNthChildIdx",   new GetNthChild("result", "idx")
            )
        );
	}

    class ModifiersRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale) {

            if ("modifiers".equalsIgnoreCase(formatString) && o instanceof Integer ) {

                StringBuffer result = new StringBuffer();
                int modifiers = (Integer)o;

                decodeModifiers(modifiers, result);
                return result.toString();

            } else {
                return o.toString();
            }
        }
    }

    abstract protected void decodeModifiers(int modifiers, StringBuffer result);

    class ParametersRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale)
        {
            FormalParameter formal = (FormalParameter) o;

            return getTemplate("formalParameter",
                "type", formal.type,
                "name", formal.name
            ).toString();
        }
    }

    class GetNthChildRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale)
        {
            GetNthChild nthChild = (GetNthChild) o;
            return inodeAdapter.genGetNthChild(nthChild.memberExpression, nthChild.indexExpression, TemplateBasedEmitter.this).toString();
        }
    }

    class GetArityRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale)
        {
            GetArity arity = (GetArity) o;
            return inodeAdapter.genGetArity(arity.memberExpression, TemplateBasedEmitter.this).toString();
        }
    }

    class GetOperatorRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale)
        {
            GetOperator operator = (GetOperator) o;
            return inodeAdapter.genGetOperator(operator.memberExpression, TemplateBasedEmitter.this).toString();
        }
    }

    class ExceptionRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale) {
            return ((Class)o).getName();
        }
    }
}
