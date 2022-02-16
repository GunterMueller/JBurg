package jburg.burg.inode;

import org.antlr.stringtemplate.*;

public class InlineInodeAdapter
	implements InodeAdapter, InodeAuxiliarySupport
{
    public final String getOperator;
    public final String getOperatorParameterName;

    public final String getNthChild;
    public final String getNthChildParameterName;
    public final String getNthChildIndex;

    public final String getArity;
    public final String getArityParameterName;

    public InlineInodeAdapter(
        String getOperator, String getOperatorParameterName,
        String getNthChild,String getNthChildParameterName, String getNthChildIndex,
        String getArity, String getArityParameterName)
    {
        this.getOperator = getOperator;
        this.getNthChild = getNthChild;
        this.getArity    = getArity;

        this.getOperatorParameterName = getOperatorParameterName;
        this.getNthChildParameterName = getNthChildParameterName;
        this.getNthChildIndex         = getNthChildIndex;
        this.getArityParameterName    = getArityParameterName;
    }

	public boolean accept(String inodeClassName)
	{
        // This adapter is explicitly instantiated.
		return false;
	}

	public Object genGetArity(Object stem, jburg.emitter.EmitLang emitter)
	{
        // TODO: Namespace support
		return emitter.genCallMethod(null, "_jburg_getChildCount", stem);
	}

	/**
	 *  @return an expression that fetches a child node at the specified index.
	 */
	public Object genGetNthChild(Object stem, Object index, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "_jburg_getNthChild", stem, index );
	}

	public Object genGetOperator( Object node, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "_jburg_getOperator", node);
	}

    public void emitAuxiliarySupport(jburg.emitter.EmitLang emitter, java.io.PrintStream output)
    {
        output.println(emitter.getTemplate("inlineAdapter", "specification", this));
    }
}
