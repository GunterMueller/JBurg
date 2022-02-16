package burmTest;

import java.util.Vector;

/**
 *  A subclass of TestINode that expects a
 *  literal integer user object and provides
 *  a typed access routine to that data.
 */
public class LiteralIntNode extends TestINode
{
    private int value;

    public LiteralIntNode(Integer opcode, String user_object)
    {
        super(opcode, user_object);
        this.value = Integer.parseInt(user_object);
    }

    public int getValue()
    {
        return this.value;
    }
}
