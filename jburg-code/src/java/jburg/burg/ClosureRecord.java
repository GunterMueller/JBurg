package jburg.burg;

import java.util.List;

/**
 *  ClosureRecord tracks the target state, cost, and action code associated
 *  with a reduction from an antecedent nonterminal to a target nonterminal state.
 */
class ClosureRecord implements JBurgProduction
{
    private final JBurgRule rule;

    private final JBurgGenerator generator;

    public ClosureRecord(JBurgRule rule, JBurgGenerator generator) throws Exception
    {
        this.rule = rule;
        this.generator = generator;
    }

    /**
     *  Get the closure's uncached cost.
     */
    public String getCost(String baseNT)
    {
        return this.rule.getCost(baseNT);
    }

    /**
     *  Get the closure's (potentially) cached cost.
     */
    @Override
    public String getCachedCost()
    {
        return getCachedCost(generator.config.reducerNodeName);
    }

    public String getCachedCost(String nodeName)
    {
        String cost = this.rule.getCost(nodeName);

        //  The cost function result will be cached;
        //  return a unique accessor method name.
        if ( costComputedViaFunction() )
            return generator.getCodeEmitter().genCallMethod(null, String.format("getCostFunctionResult_%h", cost)).toString();
        else
            return cost;
    }

    /**
     *  @return the rule's costComputedViaFunction() result.
     */
    public boolean costComputedViaFunction()
    {
        return rule.costComputedViaFunction();
    }

    /**
     *  @return the rule's getConstantCost() result.
     */
    public int getConstantCost()
    {
        return this.rule.getConstantCost();
    }

    /**
     *  @return the rule's hasConstantCost() result.
     */
    public boolean hasConstantCost()
    {
        return this.rule.hasConstantCost();
    }

    /**
     *  @return the rule's getAntecedentState() result.
     */
    public String getAntecedentState()
    {
        return this.rule.getAntecedentState();
    }

    /**
     *  @return the rule's getGoalState() result.
     */
    @Override
    public String getGoalState()
    {
        return this.rule.getGoalState();
    }

    /**
     *  @return this closure's reduce action.
     */
    @Override
    public JBurgReduceAction getReduceAction()
    {
        return rule.getReduceAction();
    }

    /**
     *  Closure records are equal if they have the same goal state, action, and cost.
     *  @see java.lang.Object#equals
     *
     * @param o -- the object to test for equality.
     * @return true if o is a ClosureRecord and it equals this one.
     *
     */
    @Override
    public boolean equals(Object o)
    {
        boolean result = false;

        if (o instanceof ClosureRecord)
        {
            ClosureRecord cuz = (ClosureRecord) o;

            result = getGoalState().equals(cuz.getGoalState());
            result &= getReduceAction().equals(cuz.getReduceAction());
            result &= this.rule.getCost(generator.config.reducerNodeName).equals(cuz.rule.getCost(generator.config.reducerNodeName));
        }

        return result;
    }

    /**
     *  @return true if the cost of this closure is known to be zero.
     */
    public boolean costIsZero()
    {
        return hasConstantCost() && getConstantCost() == 0;
    }

    /**
     *  @return the computation of this closure's cost,
     *    which is somewhat error-prone when open coded.
     */
    public String getCostComputation()
    {
        return getCostComputation(
            null, 
            generator.getCodeEmitter().genCallMethod(
                null,
                "getCost",
                generator.getNonterminal(this.getAntecedentState())
            ).toString()
        );
    }

    /**
     * @param stem the stem of the cached cost computation.
     * @param antecedentCost the caller's computation of this
     * closure's antcedent cost.
     * @return
     */
    public String getCostComputation(String stem, String antecedentCost)
    {
        if ( this.costIsZero() ) {
            return antecedentCost;
        } else {
            return generator.getCodeEmitter().genOverflowSafeAdd(this.getCachedCost(), antecedentCost).toString();
        }
    }

    /**
     *  Precompute the cost of this closure if possible.
     *  @param productions - the set of productions at the
     *    point where the closure's cost is required.  If
     *    the closure derives a single pattern-match 
     *    rule that has a constant cost, and the closure
     *    itself has a constant cost, then the cost
     *    can be precomputed.
     *  @return the precomputed cost, or the result of
     *    calling {@link getCostComputation()} to get
     *    a non-constant cost.
     */
    public String getCostComputation(Multimap<String, JBurgProduction> productions)
    {
        //  A closure back to a pattern-match with constant cost can be precomputed.
        if ( this.computesConstantCost(productions) )
        {
            return Integer.toString(getConstantCost(productions));
        }
        else
        {
            //  Return the naive cost computation.
            return getCostComputation();
        }
    }

    /**
     *  Get the constant cost of a closure.
     *  @param productions - the set of productions at the
     *    point where the closure's cost is required.
     *  @return the cost, with overflow-safe addition.
     *  @pre computesConstantCost(productions) must be true.
     */
    @Override
    public int getConstantCost(Multimap<String, JBurgProduction> productions)
    {
        long constantAntecedent;
        JBurgProduction production = productions.get(this.getAntecedentState()).get(0);

        if ( production instanceof JBurgRule )
            constantAntecedent = ((JBurgRule)production).getConstantCost();
        else
            constantAntecedent = ((ClosureRecord)production).getConstantCost(productions);

        //  Integer-overflow safe addition.
        @SuppressWarnings("cast")
        long accum = (long)this.getConstantCost() + constantAntecedent;

        if ( accum < Integer.MAX_VALUE )
            return (int) accum;
        else
            return Integer.MAX_VALUE;
    }

    /**
     *  Does this closure compute a constant cost
     *  in the context of the given productions?
     *  @param productions - the set of productions at the
     *    point where the closure's cost is required.  If
     *    the closure derives a single pattern-match 
     *    rule that has a constant cost, and the closure
     *    itself has a constant cost, then the cost
     *    can be precomputed.
     *  @return true if the computed cost is constant.
     */
    @Override
    public boolean computesConstantCost(Multimap<String, JBurgProduction> productions)
    {
        boolean result = false;
        if ( this.hasConstantCost() )
        {
            List<JBurgProduction> antecedents = productions.get(this.getAntecedentState());

            if ( antecedents.size() == 1 )
            {
                JBurgProduction production = antecedents.get(0);

                if ( production instanceof JBurgRule )
                    result = ((JBurgRule)production).hasConstantCost();
                else
                    result = ((ClosureRecord)production).computesConstantCost(productions);
            }
        }

        return result;
    }

    public String toString()
    {
        return String.format("Closure{%s=%s:%s}", getGoalState(), getAntecedentState(), getCostComputation());
    }
}
