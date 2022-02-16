package jburg.tutorial.semantics;

import java.util.*;

import org.antlr.runtime.tree.Tree;

/**
 * The Frame is an abstract
 * description of a stack frame.
 */
public interface Frame
{
    /**
     * Add a formal parameter to a function's frame.
     * @param formalDescriptor the formal's descriptor,
     * which holds its type and name.
     */
    void addFormal(Tree formalDescriptor);

    /**
     * Add a local variable to a frame.
     * @param localDescriptor the local's descriptor, 
     * which holds its type and name.
     */
    void addLocal(Tree localDescriptor);

    /**
     * @return the stack frame's current size.
     * This may vary as register save areas and
     * actual parameters are allocated and released.
     */
    Integer getFrameSize();

    /**
     * @return the offset of $ra in the frame.
     */
    Integer getRaOffset();

    /**
     * @return true if the function needs to save its $ra.
     */
    boolean getNeedsRaSaved();

    /**
     * Set the function's $ra-save status.
     * @param setting the state of the $ra register;
     * may be set from false to true, and from true
     * to true, but may not be set from true to false.
     */
    void setNeedsRaSaved(boolean setting);

    /**
     * Does the given lvalue have a backing register?
     * @param name the name of the lvalue of interest.
     * @return true if the lvalue is register-based.
     */
    boolean hasRegister(String name);

    /**
     * Get an lvalue's backing register; hasRegister must be true.
     * @param name the name of the lvalue of interest.
     * @return the register backing the lvalue.
     */
    String getRegister(String name);

    /**
     * Get an lvalue's backing stack offset; hasRegister must be false.
     * @param name the name of the lvalue of interest.
     * @return the stack offset backing the lvalue.
     */
    Integer getOffset(String name);

    /**
     * Push a save area for actual parameters
     * or for spilled registers.
     * @param count the number of words required.
     */
    void pushSaveArea(int count);

    void popSaveArea(int count);

    /**
     * Get the offset of an actual parameter; actualHasRegister must be false.
     * @param actualIndex the index of the actual parameter desired.
     */
    Integer getActualOffset(int actualIndex);

    /**
     * Is the given actual transmitted in a register?
     * @param actualIndex the index of the actual parameter.
     */
    boolean actualHasRegister(int actualIndex);
}
