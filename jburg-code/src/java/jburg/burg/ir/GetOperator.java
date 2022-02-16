package jburg.burg.ir;

/**
 * GetOperator carries information necessary to generate
 * a call to get an inode's operator, and serves (via its
 * class type) as a prompt to the code generator to
 * generate such a call.
 */
public class GetOperator
{
    /**
     * @param memberExpression an expression to access
     * the inode whose operator is to be computed.
     */
    public GetOperator(Object memberExpression)
    {
        this.memberExpression = memberExpression;
    }

    public final Object memberExpression;
}
