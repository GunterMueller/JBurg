package jburg.burg.inode;

/**
 * The Antlr3JavaAdapter is called by the JBurg code generator as it
 * generates the BURM; the adapter translates implementation and target
 * agnostic operations into code snippets appropriate for the ANTLR3
 * Tree class.
 */
public class Antlr3JavaAdapter implements InodeAdapter {

    /**  The fully qualified name of the AST. */
    public static final String s_iNodeType = "org.antlr.runtime.tree.Tree";

    /**
     * Is this the correct INodeAdapter for the INodeType in the specification?
     * @param iNodeClass the qualified name of the AST/inode class being processed.
     * @return true if iNodeClass matches the ANTLR3 class name.
     */
    public boolean accept(String iNodeClass) {
        return iNodeClass.equals(s_iNodeType);
    }

    /**
     * Generate a call to get the number of children a node has (its arity).
     * @param node an expression that will generate a path to the node.
     * @param emitter the target-specific code emitter.
     * @return a code snippet that calls node.getChildCount()
     */
    public Object genGetArity(Object node, jburg.emitter.EmitLang emitter) {
        return emitter.genCallMethod(node, "getChildCount");
    }

    /**
     * Generate a call to get the n'th child of a node.
     * @param node an expression that will generate a path to the node.
     * @param index an expression that computes the index of the desired child.
     * @param emitter the target-specific code emitter.
     * @return a code snippet that calls node.getChild(index)
     */
    public Object genGetNthChild( Object node, Object index, jburg.emitter.EmitLang emitter) {
        return emitter.genCallMethod( node, "getChild", index );
    }

    /**
     * Generate a call to get the operator (the type) of a node.
     * @param node an expression that will generate a path to the node.
     * @param emitter the target-specific code emitter.
     * @return a code snippet that calls node.getType()
     */
    public Object genGetOperator( Object node, jburg.emitter.EmitLang emitter) {
        return emitter.genCallMethod(node, "getType");
    }
}
