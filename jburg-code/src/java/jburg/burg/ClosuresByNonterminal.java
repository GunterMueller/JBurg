package jburg.burg;

/**
 *  ClosuresByNonterminal is a convenience class that holds
 *  closure sets grouped by nonterminal; the NT may
 *  be the production or the antecedent, depending on usage.
 */
class ClosuresByNonterminal extends Multimap<String, ClosureRecord>
{
    /**
     *  Add a closure record.
     *  @param nt - the nonterminal that indexes this closure.
     *    May be the production or the antecedent.
     */
    public void addClosure(String nt, ClosureRecord closure)
    {
        if ( ! this.getSet(nt).contains(closure) )
            this.getSet(nt).add(closure);
    }
}
