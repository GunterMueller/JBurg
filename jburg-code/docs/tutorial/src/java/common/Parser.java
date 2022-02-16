package jburg.tutorial.common;

import java.util.List;
import java.util.LinkedList;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import jburg.tutorial.second.secondLexer;

/**
 * Parser is the AbstractParser implementation
 * used for non-debug front ends.
 */
public abstract class Parser extends org.antlr.runtime.Parser implements AbstractParser
{
    protected Parser(TokenStream input, RecognizerSharedState state)
    {
        super(input, state);
    }

    /** Errors found during parsing. */
    private List<String> errors = new LinkedList<String>();

    /**
     * Parse the input file and return its AST.
     */
    public abstract CommonTree parse() throws Exception;

    /**
     * Override the default error handler and cache the diagnostics.
     */
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
        //e.printStackTrace();
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errors.add(hdr + " " + msg);
    }

    /**
     * @return parser diagnostics issued during the parse.
     */
    public List<String> getErrors()
    {
        return errors;
    }
}
