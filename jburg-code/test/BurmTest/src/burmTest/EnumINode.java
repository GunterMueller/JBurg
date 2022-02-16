package burmTest;

import java.util.Vector;

/**
 *  A fairly generalized i-node class for testing.
 */
public class EnumINode
{
    /**
     *  Opcode.
     */
    EnumOpcodes opcode;
    /**
     *  Child nodes.  Vector added as necessary.
     */
    Vector<EnumINode> children;

    /**
     *  The node's semantic content, if it has any.
     *  In the arithmetic tests, it's an Integer.
     */
    Object userObject;

    public EnumINode(EnumOpcodes opcode, Object user_object)
    {
        this.opcode = opcode;
        this.userObject = user_object;
    }

    public int getArity()
    {
        if ( null == children )
            return 0;
        else
            return children.size();
    }

    public EnumINode getNthChild(int idx)
    {
        return children.elementAt(idx);
    }

    public EnumOpcodes getOperator()
    {
        return this.opcode;
    }

    public Object getUserObject()
    {
        return this.userObject;
    }

    public void setUserObject(Object user_object)
    {
       this.userObject = user_object;
    }

    public void addChild(EnumINode child)
    {
        if ( null == this.children )
            this.children = new Vector<EnumINode>();

        this.children.add(child);
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer("<Node");
        addAttribute(result, "operator", this.opcode);
        if ( this.userObject != null )
            addAttribute(result, "userObject", this.userObject);

        if ( this.children != null )
        {
            result.append("> ");
            for ( EnumINode kid: this.children)
            {
                result.append(kid.toString());
            }
            result.append(" </Node>");
        }
        else
            result.append("/>");

        return result.toString();
    }

    void addAttribute(StringBuffer buffer, String attr_name, Object attr_value)
    {
        buffer.append(" ");
        buffer.append(attr_name);
        buffer.append("=\"");
        buffer.append(attr_value.toString());
        buffer.append("\"");
    }
}
