package jburg.burg;

/**
 *  A JBurgProduction is a view of a pattern-matching rule (JBurgRule) 
 *  or a nonterminal transformation rule (ClosureRecord) that exposes
 *  characteristics important in their static analysis.
 */
public interface JBurgProduction
{
    /**
     *  @return the nonterminal this production produces.
     */
    public String getGoalState();

    /**
     *  @return the code snippet that computes
     *    or retrieves this production's 
     *    (potentially) cached code.
     */
    public String getCachedCost();

    /**
     *  @return this production's reduce action.
     */
    public JBurgReduceAction getReduceAction();

    /**
     *  Is this production's cost constant in the
     *  context where it's to be invoked?
     *  @param productions - the set of productions at the
     *    point where this productions's cost is required.
     */
    public boolean computesConstantCost(Multimap<String, JBurgProduction> productions);

    /**
     *  Get the constant cost of a production.
     *  @param productions - the set of productions at the
     *    point where this production's cost is required.
     *  @return the cost, with overflow-safe addition.
     *  @pre computesConstantCost(productions) must be true.
     */
    public int getConstantCost(Multimap<String, JBurgProduction> productions);
}
