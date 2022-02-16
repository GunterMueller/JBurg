package jburg.burg;

/**
 *  A ClosureGraph holds the graph of closures that
 *  can be reached from a starting nonterminal.
 */
class ClosureGraph extends ClosuresByNonterminal
{
    ClosureGraph(JBurgGenerator generator)
    {
        this.generator = generator;
    }

    private final JBurgGenerator generator;

    /**
     *  Sweep the set of nonterminal-to-nonterminal rules and
     *  add any that can be produced by the most recently
     *  added nonterminal.
     *  @param currentNT - the most recently added nonterminal.
     */
    void addClosures(String currentNT)
    {
        addClosures(currentNT, currentNT);
    }

    /**
     *  Sweep the set of nonterminal-to-nonterminal rules and
     *  add any that can be produced by the most recently
     *  added nonterminal.
     *  @param currentNT - the most recently added nonterminal.
     *  @param patternNT - the nonterminal produced by the pattern match.
     */
    void addClosures(String currentNT, String patternNT)
    {
        if ( generator.closureSets.containsKey(currentNT) )
        {
            for ( ClosureRecord closure: generator.closureSets.get(currentNT) )
            {
                String newNT = closure.getGoalState();

                //  Can't replace the pattern with a closure.
                if ( !newNT.equals(patternNT) )
                {
                    if ( ! this.containsKey ( newNT ) )
                    {
                        super.addClosure(newNT, closure);
                        addClosures(newNT);
                    }
                    else
                    {
                        //  Add this closure, but its consequent
                        //  closures are already in the set so
                        //  it's not necessary to add them.
                        super.addClosure(newNT, closure);
                    }
                }
            }
        }
    }
}
