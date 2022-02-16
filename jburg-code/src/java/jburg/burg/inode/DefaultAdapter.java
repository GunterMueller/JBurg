package jburg.burg.inode;

public class DefaultAdapter
    implements InodeAdapter
{
	public boolean accept(String inodeClassName)
	{
		//  This adapter is directly instantiated
		//  by the generator if all other adapters fail.
		return false;
	}

	public Object genGetArity(Object stem, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(stem, "getArity");
	}

	public Object genGetNthChild(Object stem, Object index, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(stem, "getNthChild", index );
	}

	public Object genGetOperator(Object stem, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(stem, "getOperator");
	}

    /** Covert interface: shut off StringTemplate interrogation of this node's operator characteristics. */
    public Object getOperatorParameterName = null;
}
