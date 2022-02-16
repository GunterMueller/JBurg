package jburg.burg.inode;

public class Antlr2JavaAdapter
	implements InodeAdapter, InodeAuxiliarySupport
{
	/**  The fully qualified name of the AST. */
	public static final String s_iNodeType = "antlr.collections.AST";

	public boolean accept(String iNodeClass)
	{
		return s_iNodeType.equals(iNodeClass);
	}

	public Object genGetNthChild( Object node, Object idx, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "getNthChild", node, idx);
	}

	public Object genGetArity(Object node, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(null, "getArity", node);
	}

	public Object genGetOperator( Object node, jburg.emitter.EmitLang emitter)
	{
		return emitter.genCallMethod(node, "getType");
	}

    public void emitAuxiliarySupport(jburg.emitter.EmitLang emitter, java.io.PrintStream output)
	{
		//  FIXME: Use structured statements!
		output.print("\n\tprivate int getArity(" + s_iNodeType + " n)");
		output.print("\n\t{");

		output.print("\n\t\tint result = 0;");
		output.print("\n\t\t" + s_iNodeType + " cursor = n.getFirstChild();");
		output.print("\n\t\twhile (cursor != null) {");
		output.print("\n\t\t\tresult++;");
		output.print("\n\t\t\tcursor = cursor.getNextSibling();");
		output.print("\n\t\t}");
		output.print("\n\treturn result;");

		output.print("\n\t}");

		output.print("\n\n\tprivate " + s_iNodeType + " getNthChild(" + s_iNodeType + " n, int idx)");

		output.print("\n\t{");

		output.print("\n\t\t" + s_iNodeType + " result = n.getFirstChild();");
		output.print("\n\t\twhile (result != null && idx != 0) {");
		output.print("\n\t\t\tidx--;");
		output.print("\n\t\t\tresult = result.getNextSibling();");
		output.print("\n\t\t}");
		output.print("\n\treturn result;");

		output.print("\n\t}");
	}
}
