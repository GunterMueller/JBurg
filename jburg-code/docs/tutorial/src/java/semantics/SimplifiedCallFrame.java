package jburg.tutorial.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.runtime.tree.Tree;

public class SimplifiedCallFrame implements Frame
{
    Map<String,DataDescriptor>  formals     = new HashMap<String,DataDescriptor>();
    Map<String,DataDescriptor>  locals      = new HashMap<String,DataDescriptor>();

    Stack<Integer>              saveAreas   = new Stack<Integer>();

    @Override
    public void addFormal(Tree formalDescriptor)
    {
        String name = formalDescriptor.getChild(1).getText();
        if ( findDescriptorUnchecked(name) != null )
            throw new IllegalStateException(String.format("Cannot redeclare %s."));
        DataDescriptor desc = new DataDescriptor(formalDescriptor, formals.size());
        formals.put(name,desc);
    }

    @Override
    public void addLocal(Tree localDescriptor)
    {
        String name = localDescriptor.getChild(1).getText();
        if ( findDescriptorUnchecked(name) != null )
            throw new IllegalStateException(String.format("Cannot redeclare %s."));
        DataDescriptor desc = new DataDescriptor(localDescriptor, locals.size());
        locals.put(name, desc);
    }

    @Override
    public boolean hasRegister(String name)
    {
        return false;
    }

    @Override
    public String getRegister(String name)
    {
        throw new IllegalStateException(String.format("Variable %s has no backing register."));
    }

    @Override
    public Integer getFrameSize()
    {
        // Caller is responsible for allocating actuals for this procedure.
        int result = locals.size() * 4 + 4;

        for (Integer saveSize: saveAreas) {
            result += saveSize * 4;
        }

        return result;
    }

    @Override
    public Integer getRaOffset()
    {
        return getSaveAreaSize();
    }

    @Override
    public boolean getNeedsRaSaved()
    {
        return true;
    }

    @Override
    public void setNeedsRaSaved(boolean setting)
    {
        // Ignored by this type of call frame.
    }

    @Override
    public Integer getOffset(String name)
    {
        int offsetInArea = findDescriptor(name).entryPosition * 4;

        if ( formals.containsKey(name) )
            return offsetInArea + getFrameSize();
        else if ( locals.containsKey(name) )
            return offsetInArea + getSaveAreaSize() + 4;
        else
            throw new IllegalStateException(String.format("Unknown DataDescriptor : %s",name));
    }

    @Override
    public void pushSaveArea(int count)
    {
        saveAreas.push(Integer.valueOf(count));
    }

    @Override
    public void popSaveArea(int count)
    {
        saveAreas.pop();
    }

    @Override
    public Integer getActualOffset(int actualIndex)
    {
        if (saveAreas.empty() || saveAreas.peek() < actualIndex) {
            throw new IllegalStateException(
                String.format(
                    "InternalError: invalid save area index %d, save area capacity %s",
                    actualIndex,
                    saveAreas.empty()? "-unavailable-": Integer.valueOf(actualIndex)
                )
            );
        }

        return actualIndex * 4;
    }

    @Override
    public boolean actualHasRegister(int actualIndex)
    {
        return false;
    }

    private int getSaveAreaSize()
    {
        int wordsInSaveAreas = 0;

        for (Integer saveAreaSize: saveAreas) {
            wordsInSaveAreas += saveAreaSize;
        }

        return wordsInSaveAreas * 4;
    }

    private DataDescriptor findDescriptor(String name)
    {
        DataDescriptor desc = findDescriptorUnchecked(name);
        if ( desc != null )
            return desc;
        else
            throw new IllegalStateException(String.format("Reference to undeclared variable %s.", name));
    }

    private DataDescriptor findDescriptorUnchecked(String name)
    {
        DataDescriptor result = this.formals.get(name);
        if ( result == null )
            result = this.locals.get(name);
        return result;
    }

    private static class DataDescriptor
    {
        DataDescriptor(Tree declaration, int entryPosition)
        {
            this.declaration   = declaration;
            this.entryPosition = entryPosition;
        }

        final Tree declaration;
        final int entryPosition;
        String register = null;
    }

}
