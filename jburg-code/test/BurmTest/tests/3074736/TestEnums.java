import burmTest.EnumINode;
import static burmTest.EnumOpcodes.*;

public class TestEnums
{
    public static void main(String[] args) throws Exception
    {
        EnumINode root = new EnumINode(ADD, null);
        root.addChild(new EnumINode(INT, new Integer(1)));
        root.addChild(new EnumINode(INT, new Integer(2)));

        EnumOpcodesEmitter burm = new EnumOpcodesEmitter();
        burm.burm(root);

        Integer result = (Integer)burm.getResult();
        if ( result.intValue() == 3 )
            System.err.println("Succeeded: 3074736 (enum opcodes)");
        else
        {
            System.err.println("FAILED: 3074736 (enum opcodes)");
            System.err.println(".. output mismatch: expected 3, actual " + result);
        }


    }
}
