package jburg.tutorial.semantics;

/**
 * The LexicalContext class holds a stack
 * of... stack frames.
 */
public class LexicalContext
{
    public LexicalContext(LexicalContext parent)
    {
        this.frame = new SimplifiedCallFrame();
        this.parent = parent;
    }

    /** This lexical context's stack frame. */
    private final Frame frame;

    private LexicalContext parent;

    /**
     * @return this lexical context's frame.
     */
    public Frame getFrame()
    {
        return this.frame;
    }

    public LexicalContext getParent()
    {
        return this.parent;
    }
}
