package jburg.tutorial.common;

import java.util.List;
import java.util.LinkedList;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

import jburg.tutorial.second.secondLexer;

/**
 * AbstractParser defines the interface
 * between the driver and the front end.
 */
public interface AbstractParser
{
    /**
     * Parse the input file and return its AST.
     */
    CommonTree parse() throws Exception;

    /**
     * @return parser diagnostics issued during the parse.
     */
    public List<String> getErrors();
}
