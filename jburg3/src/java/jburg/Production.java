package jburg;

import jburg.semantics.HostRoutine;

/**
 * A Production represents a transformation
 * of an input to an output nonterminal,
 * either by directly matching a pattern to
 * the initial input (a PatternMatcher) or by 
 * further transforming an initial nonterminal
 * to a new nonterminal (a Closure).
 */
public abstract class Production<Nonterminal>
{
    /**
     * The Nonterminal "goal state" this Production produces.
     */
    final Object   target;
    public Object  getNonterminal() { return target; }


    /**
     * A figure of merit used to evaluate this Production's
     * usefulness compared to other Productions; lower figures
     * are better, but other than that the semantics of "cost"
     * are up to the application that generates the production table.
     * Range 0..Integer.MAX_VALUE-1.
     */
    final int   ownCost;
    public int  getCost() { return ownCost; }

    /**
     * Set if the production's final dimension can extend
     * to cover variadic tails of a pattern-matched subtree.
     */
    final boolean        isVarArgs;
    public final boolean getIsVarArgs() { return isVarArgs; }


    /**
     * A semantic predicate method that guards this production,
     * or null if the production is evaluated solely by cost.
     */
    HostRoutine         predicate;
    public HostRoutine  getPredicate() { return predicate; }
    public final static HostRoutine NO_PREDICATE = null;

    /**
     * A reducer method to call before deriving the subtree's
     * children (or advancing from a closure to the source
     * derivation of the subtree). Null if no such callback
     * is required.
     */
    HostRoutine         preCallback;
    public HostRoutine  getPreCallback() { return preCallback; }
    public final static HostRoutine NO_PRECALLBACK = null;

    /**
     * A reducer method to call after deriving the subtree's
     * children (or continuing after producing a closure's
     * source nonterminal). Null if no such callback
     * is required.
     */
    HostRoutine         postCallback;
    public HostRoutine  getPostCallback() { return postCallback; }
    public final static HostRoutine NO_POSTCALLBACK = null;

    /**
     * Construct a production.
     * @param target        the target nonterminal.
     * @param ownCost       the production's figure of merit.
     * @param isVarArgs     true if the production can cover a subtree's variadic tail.
     * @param predicate     the semantic predicate guarding this production, or null.
     * @param preCallback   the pre-derivation callback, or null.
     * @param postCallback  the post-derivation callback, or null.
     */
    Production(Object target, int ownCost, boolean isVarArgs, HostRoutine predicate, HostRoutine preCallback, HostRoutine postCallback)
    {
        this.target         = target;
        this.ownCost        = ownCost;
        this.isVarArgs      = isVarArgs;
        this.predicate      = predicate;
        this.preCallback    = preCallback;
        this.postCallback   = postCallback;
    }

    /**
     * @return true if this production has a semantic predicate.
     */
    boolean hasPredicate()
    {
        return this.predicate != null;
    }

    /**
     * Get the decorated callback name of a HostRoutine.
     * @param callback  the callback of interest.
     * @pre callback must be a pre or post callback
     * method of this Production.
     * @return a decorated name for debugging.
     * @throws IllegalStateException if the method
     * specified is not a pre or post callback.
     */
    protected String getCallbackName(HostRoutine callback)
    {
        if (callback != null) {
            if (callback == this.preCallback) {
                return String.format("preCallback:%s", callback.getName());
            } else if (callback == this.postCallback) {
                return String.format("postCallback:%s", callback.getName());
            } else {
                throw new IllegalStateException(String.format("Unknown callback method %s", callback));
            }
        } else {
            return "";
        }
    }

    public String getDescription()
    {
        return toString();
    }
}
