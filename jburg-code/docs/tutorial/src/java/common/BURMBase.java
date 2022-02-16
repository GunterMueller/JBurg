package jburg.tutorial.common;

import java.io.PrintWriter;
import org.antlr.runtime.tree.CommonTree;

/**
 * BURMBase is the common superclass of the various example
 * compilers' code generators; it exposes a common API for
 * the driver program.
 */
public abstract class BURMBase
{
    /**
     * Run the BURM.
     * @param root the root of the subtree to be converted.
     */
    public abstract void burm(CommonTree root) throws Exception;
    /**
     * Get the result of a burm() run.
     * @return the final value from the BURM's result stack.
     */
    public abstract Object getResult();

    /**
     * Dump a problematic tree for the debugger.
     * @param root the root of the tree to be dumped.
     * @param writer the dump destination.
     */
    public abstract void dump(CommonTree root, PrintWriter writer) throws Exception;
}
