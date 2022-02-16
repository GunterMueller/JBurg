package jburg.patternmatcher;

import java.util.*;

/**
 * Match a pattern with some known and some unknown symbols against a text.
 * The symbol type, T, must have a hashCode() implementation that gives
 * instances that are equal the same hash code.
 */
public class ParameterizedPatternMatcher<T extends Object>
{
    /**
     * @param text a list of symbols that forms the text to match against.
     * Position 0 is a placeholder that is not matched against.
     * @param pattern a list of symbols that forms the pattern to find
     * in text.  Position 0 is a placeholder.
     * @param initialNamingFunction a map of symbols in the pattern
     * to their matching symbols in the text; not all symbols in the
     * pattern need be mapped to symbols in the text.
     */
    public ParameterizedPatternMatcher(List<T> text, List<T> pattern, Map<T,T> initialNamingFunction)
    {
        this.text = text;
        this.pattern = pattern;
        this.initialNamingFunction = initialNamingFunction;

        this.predText = pred(text);
        this.predPattern = pred(pattern);
    }

    /**
     * The text to match against.
     */
    private List<T> text;

    /**
     * predText[j] = position of last previous occurence of text[j] in the text.
     */
    private int[] predText;

    /**
     * The pattern to search for in text.
     */
    private List<T> pattern;

    /**
     * predPattern[j] = position of last previous occurence of pattern[j] in the pattern.
     */
    private int[] predPattern;

    /**
     * Symbols in the pattern mapped to corresponding
     * symbols in the text.
     */
    private Map<T,T> initialNamingFunction;

    /**
     * Scan the text and find matches to the pattern.
     * This is a modified Morris-Pratt pattern
     * matcher using a function that determines if
     * the pattern match can be extended, instead of
     * Morris-Pratt's symbol-to-symbol comparison.
     * @return a list of matching positions.
     * @see #getUnknownSymbolMapping(List<Integer>),
     * which creates a map of unknown symbols to text 
     * using this return value.
     */
    public List<Integer> getMatches()
    {
        List<Integer> result = new ArrayList<Integer>();
        int[] failureTable = computeFailureTable();

        int m = getPatternSize();
        int n = getTextSize();

        // Start of the next candidate match in the text.
        int i = 0;
        // Current symbol in the pattern.
        int j = 0;

        while ( i <= n - m )
        {
            while ( j < m && matchExtends(i, j) )
                j++;

            if ( j == m )
                result.add(i+1);

            // Move on to the next candidate text and pattern symbol,
            // as determined by the failure table; these may not be
            // the next text symbol and first pattern symbol, if the
            // failure table computation can find periodicites in the
            // pattern that allow it to skip unmatchable text.
            i = i + Math.max(1, j - failureTable[j]);
            j = Math.max(0, failureTable[j]);
        }

        return result;
    }

    /**
     * Does the current match extend one position further?
     *
     * @param i the cursor in the text, positioned before
     * the start of the pattern-match attempt in progress
     * (the "window").
     *
     * @param j the cursor in the pattern, positioned
     * before the pattern symbol of interest.
     *
     * @return the truth value of these conditions:
     *
     * <li>(Sufficient) The pattern symbol is mapped to the
     * text symbol.  This criterion has precedence over other
     * criteria, so the pattern matcher misbehaves if multiple
     * pattern symbols are mapped to the same text symbol.
     *
     * <li>The text symbol cannot be mapped to a different pattern symbol.
     *
     * <li>If the pattern symbol has previously appeared, it matched the same text symbol.
     *
     * <li>If none of the other conditions apply, then this must be the
     * first appearance of the text symbol in the current pattern match.
     *
     */
    private boolean matchExtends(int i, int j)
    {
        T u = pattern.get(j+1);
        T v = text.get(i+j+1);

        // Pattern symbol explicitly mapped to text symbol?
        if ( initialNamingFunction.containsKey(u)  )
        {
            return initialNamingFunction.get(u).equals(v);
        }
        // Text symbol mapped to a different pattern symbol?
        else if ( initialNamingFunction.containsValue(v) )
        {
            return false;
        }
        // Previous occurence of this pattern symbol matched the same text symbol?
        else if ( predPattern[j+1] > 0 )
        {
            return v.equals(text.get(i+predPattern[j+1]));
        }
        // First appearance of this text symbol in the pattern-match window?
        else
        {
            return predText[i+j+1] <= i;
        }
    }

    /**
     * Compute a table of shift positions to expedite
     * mismatches.  This is basically the same algorithm
     * used to compute the Morris-Pratt failure table,
     * but using the parameterized pattern match strategy
     * instead of simple symbol comparisions.
     * @return the failure table as an array of int scalars.
     */
    private int[] computeFailureTable()
    {
        int m = getPatternSize();
        int[] result = new int[m+1];

        result[0] = -1;
        int t = -1;

        for (int j = 1; j <= m; j++ )
        {
            while ( t >= 0 && ! matchExtendsPrime(t, j-t) )
                t = result[t];
            t = t + 1;
            result[j] = t;
        }

        return result;
    }

    /**
     * Use the same pattern-matching strategy outlined
     * above to find the (parameterized) borders of the
     * pattern; this is done by matching the pattern
     * against itself.
     */
    private boolean matchExtendsPrime(int t, int k)
    {
        if ( t+k >= getPatternSize() )
            return false;

        T u = pattern.get(t+1);
        T v = pattern.get(t+k+1);

        if ( initialNamingFunction.containsKey(u)  )
            return initialNamingFunction.get(u).equals(v);
        else if ( initialNamingFunction.containsValue(v) )
            return false;
        else if ( predPattern[k+1] > 0 )
            return v.equals(pattern.get(t+predPattern[k+1]));
        else
            return predPattern[t+k+1] <= t;
    }

    /**
     * Compute the pred table of a string of symbols;
     * pred[j] is the largest previously seen (1-based)
     * index of the j'th symbol in the string.
     */
    private int[] pred(List<T> x)
    {
        int length = x.size() - 1;

        int[] result = new int[length+1];
        result[0] = -1;

        Map<T,Integer> last = new HashMap<T,Integer>();

        for ( int i = 1; i <= length; i++ )
        {
            T s = x.get(i);
            if ( last.containsKey(s) )
                result[i] = last.get(s);
            last.put(s, i);
        }

        return result;
    }

    /**
     * Compute the mapping of unknown pattern symbols
     * to text symbols for a set of pattern matches.
     * @param the set of matches, represented as 1-based
     * text positions of the start of the pattern match.
     */
    public List<Map<T,T>> getUnknownSymbolMapping(List<Integer> matches)
    {
        List<Map<T,T>> result = new ArrayList<Map<T,T>>();
        int m = getPatternSize();

        for ( Integer matchStart: matches )
        {
            assert matchStart + m <= this.text.size(): String.format("text size %d, required %d (%d + %d)", this.text.size(), matchStart + m, matchStart, m);

            Map<T,T> patternToText = new HashMap<T,T>();
            result.add(patternToText);

            for ( int i = 1; i <= m; i++ )
            {
                T patternSym = pattern.get(i);
                if  ( ! initialNamingFunction.containsKey(patternSym) )
                {
                    // The offset in the text is zero-based
                    // from the start of the pattern match.
                    T textSym = text.get(matchStart + i - 1);

                    if ( !patternToText.containsKey(patternSym) )
                    {
                        patternToText.put(patternSym, textSym);
                    }
                    else
                    {
                        assert patternToText.get(patternSym).equals(textSym): String.format("pattern %s expected %s, actual %s", patternSym, patternToText.get(patternSym), textSym);
                    }
                }
            }
        }

        return result;
    }

    /**
     * @return the 1-based number of symbols in the pattern.
     */
    private int getPatternSize()
    {
        return this.pattern.size() - 1;
    }

    /**
     * @return the 1-based number of symbols in the text.
     */
    private int getTextSize()
    {
        return this.text.size() - 1;
    }
}
