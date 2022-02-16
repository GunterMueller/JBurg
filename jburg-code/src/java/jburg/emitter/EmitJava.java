package jburg.emitter;

import java.lang.reflect.Modifier;

@SuppressWarnings({"nls"})
public class EmitJava extends TemplateBasedEmitter implements EmitLang
{
    /**
     * Construct an emitter and load its templates.
     */
	public EmitJava()
    {
        super("Java");
	}

    @Override
	public boolean accept(String langName)
	{
        return "java".equalsIgnoreCase(langName);
	}

    @Override
    public Object getAnnotationType()
    {
        return configuration.options.annotationInterfaceName;
    }

    @Override
    public void setAllocator(Object allocator)
    {
        if (allocator != null) {
            throw new IllegalArgumentException("allocator not supported.");
        }
    }

    @Override
    protected void decodeModifiers(int modifiers, StringBuffer result)
    {
        if ( Modifier.isPublic( modifiers ) )
            result.append("public " );
        else if ( Modifier.isProtected( modifiers ) )
            result.append("protected " );
        else if ( Modifier.isPrivate( modifiers ) )
            result.append("private " );

        if ( Modifier.isStatic( modifiers ) )
            result.append("static " );

        if ( Modifier.isFinal( modifiers ) )
            result.append("final " );
    }

    @Override
    public String getStringClassName()
    {
        return "String";
    }
}
