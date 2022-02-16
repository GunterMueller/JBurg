package jburg;

import jburg.semantics.HostRoutine;
import java.util.*;

/**
 * A State represents a vertex in the transition table.
 * Vertices represent an equivalence class of input nodes,
 * each of which has the same opcode/arity; an input node
 * must match one of the pattern-matching productions in
 * the state. The state may also be able to produce other
 * nonterminals via nonterminal-to-nonterminal closures.
 *
 * <p>Store State objects in hashed associative containers;
 * State objects' hash and equality semantics are set up
 * to weed out duplicate states.
 */
public class State<Nonterminal, NodeType>
{
    /**
     * The state's number. This number is set
     * by the production table when it places
     * a state in its table of unique states.
     */
    int number = -1;

    /** "Typedef" a map of costs by nonterminal. */
    @SuppressWarnings("serial")
	public class CostMap extends HashMap<Object,Long> {}
    /** "typedef" a map of Productions keyed by Nonterminal. */
    @SuppressWarnings("serial")
	public class ProductionMap extends HashMap<Object, Production<Nonterminal>> {}
    /** "typedef" a map of Closures by Nonterminal. */
    @SuppressWarnings("serial")
	public class ClosureMap    extends HashMap<Object, Closure<Nonterminal>> {}

    /**
     * This state's non-closure productions.
     */
    ProductionMap  nonClosureProductions = new ProductionMap();
    public ProductionMap  getPatterns() { return nonClosureProductions; }

    /**
     * Cost of each pattern match.
     */
    CostMap patternCosts = new CostMap();
    public CostMap getCostMap() { return patternCosts; }

    /**
     * This state's closures, i.e., nonterminal-to-nonterminal productions.
     */
    ClosureMap  closures = new ClosureMap();
    public ClosureMap getClosures() { return closures; }

    /**
     * The node type of this state; used while projecting
     * representer states, which are unique for a particular
     * tuple of (NodeType, nt=cost*).
     */
    final NodeType  nodeType;

    /**
     * This state's arity kind: unknown, fixed-arity, or variadic.
     * The state's arity kind is set by its first production, and
     * subsequent productions must be of the same arity kind.
     */
    ArityKind arityKind = null;

    /**
     * This state's predicate methods. Nodes belonging to this
     * state's equivalence class satisfy all the predicates.
     */
    final List<HostRoutine> predicates = new ArrayList<HostRoutine>();
    public List<HostRoutine> getPredicates() { return predicates; }

    /**
     * Closure chains' pre-reduction callback actions.
     * Denormalized for the string templates' use.
     */
    public final Map<Object,List<Production<Nonterminal>>> closurePreProductions = new HashMap<Object,List<Production<Nonterminal>>>();

    /**
     * Closure chains' post-reduction callback actions.
     * Denormalized for the string templates' use.
     */
    public final Map<Object,List<Production<Nonterminal>>> closurePostProductions = new HashMap<Object,List<Production<Nonterminal>>>();

    /**
     * The nonterminals that start closure chains.
     * Denormalized for the string templates' use.
     */
    public final Map<Object,Object> closurePatternPrecursor = new HashMap<Object,Object>();
    boolean isFinished;

    /**
     * Construct a state that characterizes non-null nodes.
     * @param nodeType the node type of the nodes.
     */
    State(NodeType nodeType)
    {
        assert nodeType != null: "Add null pointer productions' states using the no-args State() constructor";
        this.nodeType = nodeType;
    }

    /**
     * Construct a state that characterizes null pointers, or the error state.
     */
    State()
    {
        this.nodeType = null;
    }

    /**
     * Construct a state based on a source state.
     * @param source    the source state.
     */
    State(State<Nonterminal, NodeType> source)
    {
        this.nodeType = source.nodeType;
        this.arityKind = source.arityKind;
        this.nonClosureProductions.putAll(source.nonClosureProductions);
        this.patternCosts.putAll(source.patternCosts);
        this.closures.putAll(source.closures);
        this.predicates.addAll(source.predicates);
    }

    /**
     * Construct a state based on a source state, with a new predicate.
     * @param source    the source state.
     * @param predicate the predicate.
     */
    @SuppressWarnings("unchecked")
    State(State<Nonterminal, NodeType> source, HostRoutine predicate)
    {
        this(source);
        // Add the new predicate, and sort the predicate
        // list into its canonical form by hash code.
        assert !this.predicates.contains(predicate);
        this.predicates.add(predicate);
        Collections.sort(this.predicates);
    }

    /**
     * Add a non-closure production to this state.
     * This production may displace a previously
     * added production.
     * @param p     the production.
     * @param cost  the cost of this production. The
     * cost must be the best cost known so far.
     */
    void setNonClosureProduction(Production<Nonterminal> p, long cost)
    {
        assert cost < getCost(p.target);
        assert !(p instanceof Closure): "use addClosure to add closures";

        patternCosts.put(p.target.toString(), cost);
        nonClosureProductions.put(p.target.toString(), p);

        if (arityKind == null) {
            arityKind = p.isVarArgs? ArityKind.Variadic:ArityKind.Fixed;
        } else if (isVarArgs() != p.isVarArgs) {
            throw new UnsupportedOperationException("Cannot mix variadic and fixed-arity productions");
        }
    }

    /**
     * Finish compilation of a state; create denormalized lists of pre and post productions
     * for the string template's use as it creates closure chains.
     */
    void finishCompilation()
    {
        if (!this.isFinished) {

            for (Object nt: getNonterminals()) {
                closurePreProductions.put(nt, getProductionsFor(nt, ClosureProductionsType.PreCallback));
                List<Production<Nonterminal>> postProductionsForNt = getProductionsFor(nt, ClosureProductionsType.PostCallback);
                Collections.reverse(postProductionsForNt);
                closurePostProductions.put(nt, postProductionsForNt);
                setClosurePrecursor(nt);
            }

            this.isFinished = true;
        }
    }

    /**
     * @return the number of pattern matching productions in this state.
     */
    int size()
    {
        assert(nonClosureProductions.size() == patternCosts.size());
        return nonClosureProductions.size();
    }

    /**
     * @return true if this state has no pattern matching productions.
     */
    boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Do nodes labeled with this state satisfy a particular predicate?
     * @predicate   the predicate of interest.
     * @return true if a node labeled with this state is
     * known to satisfy the given predicate.
     */
    boolean satisfiesPredicate(HostRoutine predicate)
    {
        return this.predicates.contains(predicate);
    }

    /**
     * Get the cost of a nonterminal; this may require
     * navigation of a chain of closure productions back
     * to the pattern-matching production.
     * @return the aggregated cost of productions that
     * produce the given nonterminal, or Integer.MAX_VALUE
     * if there is no production for this nonterminal.
     * Costs are returned as longs (and computed as longs)
     * so that they don't overflow.
     */
    long getCost(Object nt)
    {
        nt = nt.toString();

        if (patternCosts.containsKey(nt)) {
            return patternCosts.get(nt);

        } else if (closures.containsKey(nt)) {
            // Traverse the chain of closures.
            Closure<Nonterminal> closure = closures.get(nt);
            long closedCost = closure.ownCost + getCost(closure.source);
            assert closedCost < Integer.MAX_VALUE;
            return closedCost;

        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Get the Production for a nonterminal.
     * @param goal  the Nonterminal to be produced.
     * @return the corresponding Production, which
     * may be a pattern matcher or a closure.
     * @throws IllegalArgumentException if this state
     * has no production for the specified nonterminal.
     */
    Production<Nonterminal> getProduction(Object goal)
    {
        goal = goal.toString();

        if (nonClosureProductions.containsKey(goal)) {
            return nonClosureProductions.get(goal);
        } else if (closures.containsKey(goal)) {
            return closures.get(goal);
        } else {
            throw new IllegalArgumentException(String.format("%s not produced by %s", goal, this));
        }
    }

    enum ClosureProductionsType { PreCallback, PostCallback };

    /**
     * Get all the productions this State uses
     * to transform an intput to a given nonterminal goal.
     * @param nt    the nonterminal.
     * @return a list of the productions to run.
     */
    List<Production<Nonterminal>> getProductionsFor(Object goal, ClosureProductionsType type)
    {
        List<Production<Nonterminal>> result = new ArrayList<Production<Nonterminal>>();

        while (!nonClosureProductions.containsKey(goal)) {
            assert(closures.containsKey(goal));
            Closure<Nonterminal> c = closures.get(goal);

            if (type == ClosureProductionsType.PreCallback &&  c.getPreCallback() != null) {
                result.add(c);
            } else if (type == ClosureProductionsType.PostCallback &&  c.getPostCallback() != null) {
                result.add(c);
            }
            goal = c.getSource();
        }

        return result;
    }

    /**
     * Cache the nonterminal that starts a closure chain;
     * this is the nonterminal that the reducer first reduces
     * via a pattern matching production.
     */
    private void setClosurePrecursor(Object goal)
    {
        if (!nonClosureProductions.containsKey(goal)) {
            Object precursor = goal;

            while (!nonClosureProductions.containsKey(precursor) && closures.containsKey(precursor)) {
                precursor = closures.get(precursor).getSource();
            }

            assert nonClosureProductions.containsKey(precursor);
            closurePatternPrecursor.put(goal, precursor);
        }
    }

    /**
     * Get all the non-closure (i.e., pattern match) productions.
     */
    Collection<Production<Nonterminal>> getNonClosureProductions()
    {
        return nonClosureProductions.values();
    }

    /**
     * Does this State accept variadic arguments?
     * @return true if all productions in the state are variadic.
     */
    boolean isVarArgs()
    {
        assert arityKind != null;
        return arityKind == ArityKind.Variadic;
    }

    /**
     * Add a closure to the closure map if it's the best alternative seen so far.
     * @return true if the closure is added to the map.
     */
    boolean addClosure(Closure<Nonterminal> closure)
    {
        // Don't replace patterns with closures.
        // TODO: Analyze this and find the cases where it's appropriate.
        if (!patternCosts.containsKey(closure.target)) {
            // The cost of a closure is its own cost,
            // plus the cost of producing its antecedent.
            long closureCost = closure.ownCost + getCost(closure.source);

            if (closureCost < this.getCost(closure.target)) {
                closures.put(closure.target, closure);
                return true;
            }
        }

        return false;
    }

    String getClosureRationale(Closure<Nonterminal> closure)
    {
        if (patternCosts.containsKey(closure.target)) {
            return String.format("Incumbent pattern for %s", closure.target);
        } else if (getCost(closure.source) == Integer.MAX_VALUE) {
            return String.format("Missing antecedent %s", closure.source);
        } else {
            long closureCost = closure.ownCost + getCost(closure.source);
            if (closureCost >= getCost(closure.target)) {
                return String.format("Closure cost %d beat by existing %d", closureCost, getCost(closure.target));
            } else {
                return String.format("Incumbent Closure cost %d beats %d", getCost(closure.target), closureCost);
            }
        }
    }

    /**
     * Get all closures that depend on a given pattern-matching production.
     * @param needle the production of interest.
     * @return the set of closures that depend on the needle.
     */
    List<Closure<Nonterminal>> getClosuresTo(Production<Nonterminal> needle)
    {
        List<Closure<Nonterminal>> result = new ArrayList<Closure<Nonterminal>>();

        for (Closure<Nonterminal> c: this.closures.values()) {

            Closure<Nonterminal> current = c;

            while (this.closures.containsKey(current.source)) {
                current = this.closures.get(current.source);
            }

            Production<Nonterminal> pattern = nonClosureProductions.get(current.source);
            assert pattern != null;

            if (pattern == needle) {
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Marshal nonterminals produced by both
     * pattern matchers and closures.
     * @return the set of nonterminals produced.
     */
    public Set<Object> getNonterminals()
    {
        // We could use a cheaper data structure, e.g.,
        // List<Object>, but returning a set makes
        // the semantics of this operation clear.
        Set<Object> result = new HashSet<Object>();

        for (Object patternNonterminal: nonClosureProductions.keySet()) {
            result.add(patternNonterminal);
        }

        for (Object closureNonterminal: closures.keySet()) {
            // A closure should never occlude a pattern match.
            assert !result.contains(closureNonterminal);
            result.add(closureNonterminal);
        }

        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(String.format("State %d %s", this.number, this.nodeType));

        if (!this.isFinished) {
            buffer.append(" !unfinished!");
        }

        if (nonClosureProductions.size() > 0) {
            buffer.append("[");
            boolean didFirst = false;
            for (Object nt: nonClosureProductions.keySet()) {
                Production<Nonterminal> p = nonClosureProductions.get(nt);

                if (didFirst) {
                    buffer.append(",");
                } else {
                    didFirst = true;
                }
                buffer.append(String.format("%s=%s", nt, p));
            }
            buffer.append("]");

            if (closures.size() > 0) {
                for (Object nt: closures.keySet()) {
                    buffer.append(String.format(", %s=%s", nt, closures.get(nt).source));
                }
            }
        }

        return buffer.toString();
    }

    public String getDescription()
    {
        StringBuilder buffer = new StringBuilder();

        if (nonClosureProductions.size() > 0) {

            int nDone = 0;

            for (Object nt: nonClosureProductions.keySet()) {
                Production<Nonterminal> p = nonClosureProductions.get(nt);

                if (buffer.length() > 0) {
                    buffer.append("\n");
                }

                buffer.append(String.format("%s=%s", nt, p.getDescription()));

                // Append closures chained from this nonterminal.
                appendClosures(nt, 4, buffer, false);
            }
        }

        return buffer.toString();
    }

    private void appendClosures(Object nt, int padding, StringBuilder buffer, boolean wroteSeparator)
    {
        for (Closure<Nonterminal> closure: closures.values()) {

            if (closure.getSource().equals(nt)) {
                if (!wroteSeparator) {
                    buffer.append("\n    ---- closures ----");
                    wroteSeparator = true;
                }
                buffer.append(String.format("\n%" + String.valueOf(padding) + "s%s => %s", "", nt, closure.getNonterminal()));
                appendClosures(closure.getNonterminal(), padding+4, buffer, wroteSeparator);
            }
        }
    }

    public int getStateNumber()
    {
        return this.number;
    }

    /**
     * Define a state's hash code in terms of its
     * node type's hash code, its pattern map's
     * hash code, and its predicate list's hash code.
     *
     * <p> <strong>Using the cost map's hash code is invalid,</strong>
     * since subsequent iterations may produce states that
     * are identical except that they cost more due to closures,
     * so computations based on the cost map diverge.
     *
     * <p>However, two states with the same pattern map
     * will also have the same cost map after closure,
     * so the pattern map is a valid choice for hashing.
     *
     * @return this state's node type's hashCode(),
     * concatenated with the pattern map's hashCode()
     * and the predicate list's hashCode().
     */
    @Override
    public int hashCode()
    {
        int nodeHash = nodeType != null? nodeType.hashCode(): 0;
        return nodeHash * 31 + predicates.hashCode() * 31 + nonClosureProductions.hashCode();
    }

    /**
     * Two states are equal if their node types,
     * pattern maps, and predicate guards are equal.
     * @param o the object to compare against.
     * @return true if o is a State and equal to this State.
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof State) {
            State<?,?> s = (State<?,?>)o;

            if (this.nodeType == s.nodeType) {
                return this.nonClosureProductions.equals(s.nonClosureProductions) && this.predicates.equals(s.predicates);
            } else if (this.nodeType != null && s.nodeType != null) {
                return
                    this.nodeType.equals(s.nodeType) &&
                    this.nonClosureProductions.equals(s.nonClosureProductions) &&
                    this.predicates.equals(s.predicates)
                    ;
            }
        }

        return false;
    }
}
