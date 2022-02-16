package jburg.burg.ir;

/**
 * GetArity carries information necessary to generate
 * a call to get an inode's arity, and serves (via its
 * class type) as a prompt to the code generator to
 * generate such a call.
 */
public class GetArity
{
    /**
     * @param memberExpression an expression to access
     * the inode whose arity is to be computed.
     */
    public GetArity(Object memberExpression)
    {
        this.memberExpression = memberExpression;
    }

    public final Object memberExpression;
}
