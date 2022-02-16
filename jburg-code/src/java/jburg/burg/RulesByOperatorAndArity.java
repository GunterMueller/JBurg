package jburg.burg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *  RulesByOperatorAndArity sorts JBurgRules into equivalence classes,
 *  where the equivalence relation is a structural one that means the
 *  rules can share an annotation object.
 */
class RulesByOperatorAndArity
{
    RulesByOperatorAndArity(JBurgGenerator generator)
    {
        this.generator = generator;
    }
    
    private final JBurgGenerator generator;

    /**
     *  Unsorted rules, keyed by operator.
     */
    Multimap<String, JBurgRule> unsortedRules = new Multimap<String, JBurgRule>();

    /**
     * Rules partitioned into equivalence classes and managed by
     * an AnnotationSemantics instance. Keyed by operator.
     */
    Map<String, List<AnnotationSemantics>> annotationSemantics = null;

    private boolean analyzed()
    {
        return this.annotationSemantics != null;
    }

    /**
     * Add the contents of a list of rules to the store.
     */
    public void addAll(List<JBurgRule> rules)
    {
        assert !analyzed();

        for (JBurgRule rule: rules) {
            addRule(rule);
        }
    }

    /**
     * Add a single rule to the store.
     */
    public void addRule(JBurgRule rule)
    {
        assert !analyzed();
        this.unsortedRules.addToSet(rule.getOperator(), rule);
    }

    /**
     * @return the set of operators in the store.
     */
    public Set<String> getOperators()
    {
        assert analyzed();
        return annotationSemantics.keySet();
    }

    /**
     * Perform semantic analysis.
     */
    public void analyze()
    {
        assert !analyzed();
        this.annotationSemantics = new TreeMap<String, List<AnnotationSemantics>>();

        for ( String op: unsortedRules.keySet() ) {
            annotationSemantics.put(op, partition(this.unsortedRules.get(op)));
        }
    }

    /**
     * Get an equivalence classes' semantics for an operator.
     * @param operator the operator of interest.
     * @pre analyze() must have been called.
     */
    public List<AnnotationSemantics> getAnnotationSemantics(String operator)
    {
        assert analyzed();
        return annotationSemantics.get(operator);
    }

    /**
     * Partition a list of rules into equivalence classes.
     * @param unsorted_rules all rules for a given operator.
     * TODO: Use a parameterized pattern matcher to allow finer grained sharing.
     */
    private List<AnnotationSemantics> partition(Collection<JBurgRule> unsorted_rules)
    {
        //  Find the minumum arity of all n-ary patterns;
        //  any rule with arity >= this limit gets sorted
        //  into the "variable-arity" bucket.
        int arity_requires_variable = Integer.MAX_VALUE;

        for ( JBurgRule rule: unsorted_rules )
            if ( rule.patternMatcher.hasNaryness() )
                arity_requires_variable = Math.min(arity_requires_variable, rule.patternMatcher.getNominalArity());

        Multimap<Integer, JBurgRule> rules = new Multimap<Integer, JBurgRule>(); 

        for ( JBurgRule rule: unsorted_rules ) {
            //  All n-ary patterns and any fixed-arity patterns that
            //  overlap with n-ary patterns go into a common annotation;
            //  fixed-arity patterns of arity less than the smallest
            //  n-ary arity can't be confused with an n-ary pattern
            //  and can go in their own annotation.
            rules.addToSet(
                Math.min(rule.patternMatcher.getNominalArity(), arity_requires_variable),
                rule
            );
        }

        ArrayList<AnnotationSemantics> result = new ArrayList<AnnotationSemantics>();

        for (List<JBurgRule> annotationRules: rules.values()) {
            result.add(new AnnotationSemantics(this.generator, annotationRules));
        }

        return result;
    }

    public List<AnnotationSemantics> getAllAnnotations()
    {
        List<AnnotationSemantics> result = new ArrayList<AnnotationSemantics>();

        for (String operator: getOperators()) {
            for (AnnotationSemantics semantics: getAnnotationSemantics(operator)) {
                result.add(semantics);
            }
        }

        return result;
    }

    public List<AnnotationSemantics> getFilteredAnnotations(SemanticsFilter filter)
    {
        List<AnnotationSemantics> result = new ArrayList<AnnotationSemantics>();

        for (String operator: getOperators()) {
            for (AnnotationSemantics semantics: getAnnotationSemantics(operator)) {
                if  (filter.matches(semantics)) {
                    result.add(semantics);
                }
            }
        }

        return result;
    }

    public List<AnnotationSemantics> getSingletonAnnotations()
    {
        return getFilteredAnnotations(
            new SemanticsFilter() {
                
                public boolean matches(AnnotationSemantics semantics) {
                    return semantics.canBeSingleton();
                }
            }
        );
    }

    public static abstract class SemanticsFilter
    {
        abstract boolean matches(AnnotationSemantics semantics);
    }
}
