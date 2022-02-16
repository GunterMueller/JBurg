package jburg.emitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.*;

/**
 * TemplateGroup is a facade for a StringTemplateGroup that
 * hides details of the .stg file management, supports name/value
 * attribute pairs for convenience, and manages a map of default
 * attributes to be supplied to all templates.
 */
class TemplateGroup
{
    TemplateGroup(String directoryName, String templateGroupName)
    {
        final List<String> loadErrors = new ArrayList<String>();
        final List<String> loadWarnings = new ArrayList<String>();

        CommonGroupLoader loader = new CommonGroupLoader(
            directoryName,
            new StringTemplateErrorListener()
            {
                public void error(String msg, Throwable t)
                {
                    System.err.println(msg);
                    loadErrors.add(msg);
                }

                public void warning(String msg)
                {
                    loadWarnings.add(msg);
                }
            }
        );

        this.templates = loader.loadGroup(templateGroupName);

        if ( loadErrors.size() > 0 )
            throw new IllegalStateException(String.format("Unable to load %s", templateGroupName));
    }

    /**
     * The backing StringTemplateGroup.
     */
    private StringTemplateGroup templates;

    /**
     * Default attributes to provide to all templates.
     * Specific values are up to the caller.
     */
    private Map<String, Object> defaultAttrs = new HashMap<String, Object>();

    private Object configuration;

    void setConfiguration(Object configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Select a template by name, with optional attributes.
     * The template is also given a copy of the template
     * group's current set of default attributes.
     * @param name the template's name.
     * @param attrValue pairs of attrName, attrValue.
     */
    public StringTemplate getTemplate(String name, Object ... attrValue)
    {
        Map<String,Object> templateAttrs = new HashMap<String, Object>();
        templateAttrs.put("defaults", this.defaultAttrs);
        templateAttrs.put("config", this.configuration);
        StringTemplate result = this.templates.getInstanceOf(name, templateAttrs);

        if (! (attrValue.length == 0 || attrValue.length % 2 == 0) )
            throw new IllegalStateException("Expected an even number of attr/value pairs");

        for ( int i = 0; i < attrValue.length; i += 2 )
        {
            result.setAttribute(attrValue[i].toString(), attrValue[i+1]);
        }

        return result;
    }

    /**
     * Register a renderer; delegates to the StringTemplateGroup's registerRenderer method.
     */
    void registerRenderer(Class<? extends Object> clazz, AttributeRenderer renderer)
    {
        this.templates.registerRenderer(clazz, renderer);
    }

    /**
     * Set a default attriburte.
     * @param key the attribute's name.
     * @param value the attribute's value.
     */
    public void setDefaultAttribute(String key, Object value)
    {
        this.defaultAttrs.put(key,value);
    }
}

