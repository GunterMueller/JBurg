import burmTest.TestINode;

/**
 *  This class contains the reducer logic
 *  driven by tests/arithmetic/ExplicitReducer.jbg
 */
public class ExplicitReducer
{
    /**
     *  State switch used to verify prologue processing;
     *  flip it to true before reducing an INT.
     */
    private boolean readyForInt = false;

    /**
     *  Reduce the two sides of an addition
     *  to their sum.
     *  Note that the reduce routine parameters
     *  don't accept the node, since we know 
     *  everything about the node we need by the 
     *  simple fact that we're adding; also note 
     *  that  javac is unboxing and boxing for us.
     */
    public Integer reduceAdd(int lhs, int rhs)
    {
        return lhs + rhs;
    }

    /**
     *  Reduce an INT token to an Integer constant.
     *  Note the test of the readyForInt switch;
     *  this has nothing to do  with reducing 
     *  integer values, it's  a test to ensure 
     *  the prologue code ran properly.
     */
    public Integer reduceInt(TestINode node)
    {
        if ( this.readyForInt )
            this.readyForInt = false;
        else
            throw new IllegalStateException("readyForInt precondition not set -- Prologue code didn't run?");

        return Integer.parseInt(node.getUserObject().toString());
    }

    /**
     *  Prologue of an INT-to-Integer reduction;
     *  set the switch to tell the reduction logic
     *  the prologue ran and it's OK to proceed.
     */
    public void prologueInt()
    {
        this.readyForInt = true;
    }
}
