package jburg.burg.inode;

public class Antlr2CppAdapter 
	implements InodeAdapter
{
	public boolean accept(String inodeClassName)
	{
		return inodeClassName.equals("antlr::AST");
	}

	public Object genGetArity(Object stem, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "getArityOf", stem);
	}

	/**
	 *  @return an expression that fetches a child node at the specified index.
	 */
	public Object genGetNthChild(Object stem, Object index, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "getNthChild", stem, index);
	}

	public Object genGetOperator( Object node, jburg.emitter.EmitLang emitter)
	{
		return emitter.genAccessMember(node, "getType");
	}
}

