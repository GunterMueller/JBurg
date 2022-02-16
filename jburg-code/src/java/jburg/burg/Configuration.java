package jburg.burg;

import jburg.burg.inode.InodeAdapter;
import jburg.burg.inode.InlineInodeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Configuration settings from the grammar and command line;
 * shared by the generator and emitters.
 */
public class Configuration
{
    /**
     * When not null, the annotation get/set methods
     * i-nodes use to self-manage their annotations.
     */
    public AnnotationAccessor annotationAccessor;

	/** 
	 *  I-node adapter class name.  The adapter is instantiated by name.
	 *  @note: specified as an alternative to the I-node class' name,
	 *  which selects a builtin adapter.
	 */
	public String iNodeAdapterClass;

    /**
     *  When set, generate a label() function
     *  that discards null i-nodes.
     */
	public boolean generateNullTolerantLabeller = false;

    /**
     * Cache closure computations if the number of
     * elements in the computation exceeds this threshold.
     */
    public int closureCacheThreshold = 7;
    
    /**
     * Volatile cost functions.
     */
    public List<String> volatileCostFunctions = new ArrayList<String>();

    /**
     *  The type of the INodes' opcode.  Defaults to int but
     *  can be an enum for maintainability.
     */
    public String opcodeType = "int";
    
	/**  Name of the initial parameter to the label routine. */
	public String initialParamName = "to_be_labelled";
	
    /**
     * The type of the generated nonterminals.  Defaults to int
     * but can be an enum for maintainability and error detection.
     */
    public String ntType = "int";
    
	/** I-node adapter to use */
	public InodeAdapter iNodeAdapter;
	
	/** Name of the node in the reducer */
	public String reducerNodeName = "__p";

	/** Name of the stack of reduced values */
	public String reducedValuesName = "__reducedValues";

    /**
     *  The name of the i-node class that's being labeled and reduced.
     */
    public String iNodeClass = null;

    /** The package name of the generated reducer. */
    public String packageName = null;

    /**  The name of the generated BURM's base class. */
    public String baseClassName = null;

    /**  The name of the annotation's base class. */
    public String annotationBaseClassName = null;

    /**
     * Memory allocator to use; only some
     * targets support this.
     */
    public String allocator = null;

    /**  Default error handler.  null means use hard-coded default, i.e., throw an exception. */
    public String defaultErrorHandler = null;

    /** Return types declared in the specification. */
    public Set<String> returnTypes = new HashSet<String>();

    /** Initialization sequence for static annotations. */
    public String initStaticAnnotation = null;

    /**  Command-line options.  */
    public Options options;

    /** Name of the 'wildcard' state, which accepts
     *  unmatched constructs, or null if there is no
     *  wildcard state.
     */
    public String wildcardState = null;

    /**
     *  The goal states' names become symbolic constants
     *  in the generated reducer or the nonterminal enumeration.
     */
    public final Set<String>    goalStateNames = new HashSet<String>();

    /**
     * operator mapped to node type. This map is populated by JBurg.NodeType
     * directives.
     * <p>
     * For example, an operator named IdentifierID would be associated with
     * {@code IIdentifierNode*} in this map if the input contained
     * {@code JBurg.NodeType IdentifierID = IIdentifierNode*;
     */
    public final Map<String, String> opcodeNodeTypes = new HashMap<String, String>();
    
    /**  The names of any interfaces that the generated BURM implements.  */
    public final List<String> interfaceNames = new ArrayList<String>();

    /** blocks of code to be added into the class verbatim */
    public final List<String> inclassCode = new ArrayList<String>();

    /**
     *  Cost functions defined by the specification.
     *  Note that cost functions may also be embedded
     *  in the inline code blocks in a specification.
     */
    public final List<Object> costFunctions = new ArrayList<Object>();

    /**  Header code, copied as-is into the reducer. */
    public String headerBlock = null;

    /** 
     * If the pattern-matcher generator fails, dump its annotation tree here.
     * Note: this is only enabled (or useful) when debugging JBurg's own BURM. 
     */
    static public String patternMatcherDumpFile = null;

    /**
     * Name for the language to emit code in (default is assumed to be Java)
     */
    public String emitLanguageName = null;
    
    /** Code emitter to use (selected using the language name) */

    /**
     *  The return types for specific states.
     */
    Map<String, String> returnTypeTable = new HashMap<String, String>();

    /**
     *  The default return type of the reducer functions.
     *  If this is defaulted to null, the generated reducer will return nodes of the iNodeClass.
     */
    String defaultReturnType = null;

    final static String STRICT_RETURN_TYPE = "-Strict state types-";

	/**
	 *  @return the return type for a specific state.
	 */
	public String getReturnType(String stateName)
	{
		if (returnTypeTable.containsKey(stateName)) {
            return returnTypeTable.get(stateName).toString();
        } else if (this.defaultReturnType != STRICT_RETURN_TYPE) {
			return defaultReturnType;
        } else {
            throw new IllegalArgumentException(String.format("No return type for state %s",stateName));
        }
	}
	
    /**  
     *  Caller's logging interface.  If defaulted to null, informational and
     *  error messages go to System.out and System.err respectively.
     */
    Logger logger = null;
    
    /**
     * @return the current Logger implementation;
     * if no Logger was specified, constructs
     * a Logger that emits messages to stdout/stderr.
     */
    public Logger getLogger()
    {
        if (logger == null)
            logger = new Logger(true, true, true);
        return logger;
    }

    public String getAnnotationInterfaceName()
    {
        return options.annotationInterfaceName;
    }

    Configuration(Options options)
    {
        this.options = options;
    }
}
