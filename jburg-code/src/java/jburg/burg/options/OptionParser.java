package jburg.burg.options;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The OptionParser's major functions are:
 * <li> Parse a String array into an options holder instance
 * <li> Validate the options
 * <li> Assemble a help string.
 */
public class OptionParser
{
    private List<String> errors = new ArrayList<String>();

    /**
     * Parse options out of a command-line like array of Strings
     * and set them into an options holder instance.
     * @param optionList the array of String options.
     * @param options an instance of the options holder.
     */
    public void setOptions(String[] optionList, Object options)
    {
        boolean defaultOptionSet = false;

        for (int i = 0; i < optionList.length; i++) {

            if (optionList[i].startsWith("-")) {
                String optionName = optionList[i].substring(1);

                Field field = findField(options, optionName);

                if (field == null) {
                    error("unknown option %s", optionName);
                } else if (i+1 == optionList.length || optionList[i+1].startsWith("-")) {

                    // If this is a boolean option, set it;
                    // otherwise report a missing value.
                    if (isBoolean(field)) {
                        setOption(options, field, "true");
                    } else {
                        error("missing value for %s", optionName);
                    }
                } else {
                    setOption(options, field, optionList[++i]);
                }
            } else {
                Field f = findDefaultOption(options);
                if (f != null && !defaultOptionSet) {
                    setOption(options, f, optionList[i]);
                    defaultOptionSet = true;
                } else {
                    error("unexpected %s", optionList[i]);
                }
            }
        }
    }

    /**
     * Validate the fields of an options holder.
     * @param options the instance holding the options.
     * @param validator the object's validator; there is
     * one builtin validation (the Required annotation)
     * for convenience.
     */
    public void validate(Object options)
    {
        for (Field f: options.getClass().getDeclaredFields()) {
            
            Object fieldValue;

            try {
                fieldValue = f.get(options);
            } catch (IllegalAccessException iax) {
                // Not a public field, continue.
                continue;
            }

            if (fieldValue == null && f.getAnnotation(Required.class) != null) {

                String diagnostic = f.getAnnotation(Required.class).diagnostic();
                if (diagnostic == null) {
                    diagnostic = String.format("missing value for -%s", f.getName());
                }
                error(diagnostic);

            }
        }
    }

    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public List<String> getDiagnostics()
    {
        return errors;
    }

    /**
     * Build a description of the allowed options.
     * @param options an instance of the options holder.
     */
    public String describe(Object options)
    {
        StringBuilder buffer = new StringBuilder();
        for (Field f: options.getClass().getDeclaredFields()) {

            // Ignore internal options.
            if (f.getAnnotation(Internal.class) != null)
                continue;

            boolean optional = f.getAnnotation(Optional.class) != null;
            
            if (optional) {
                buffer.append( String.format(" [ -%s", f.getName()));
            } else {
                buffer.append( String.format(" -%s", f.getName()));
            }


            if (f.getAnnotation(Paraphrase.class) != null) {
                buffer.append(String.format(" %s", f.getAnnotation(Paraphrase.class).value()));
            } else if (!isBoolean(f)) {
                if (isInt(f)) {
                    buffer.append(" <int>");
                } else {
                    buffer.append(" <value>");
                }
            }

            if (optional) {
                buffer.append(" ]");
            }
        }

        return buffer.toString();
    }

    public void error(String format, Object ... args)
    {
        errors.add(String.format(format, args));
    }

    private void setOption(Object options, Field field, String rawValue)
    {
        try {
            if (isBoolean(field)) {
                field.setBoolean(options, Boolean.parseBoolean(rawValue));
            } else if (isInt(field)) {
                field.setInt(options, Integer.parseInt(rawValue));
            } else if (isString(field)) {
                field.set(options, rawValue);
            } else {
                error ("Unknown field type %s", field.getType());
            }
        } catch (IllegalAccessException iax) {
            error("unable to set %s due to: %s", field.getName(), iax);
        }
    }

    private Field findField(Object options, String optionName)
    {
        for (Field f: options.getClass().getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase(optionName)) {
                return f;
            }
        }

        return null;
    }

    private Field findDefaultOption(Object options)
    {
        for (Field f: options.getClass().getDeclaredFields()) {
            if (f.getAnnotation(DefaultOption.class) != null) {
                return f;
            }
        }

        return null;
    }

    private boolean isOfType(Field field, Type t)
    {
        return field.getType().equals(t);
    }

    private boolean isBoolean(Field field)
    {
        return isOfType(field, Boolean.TYPE);
    }

    private boolean isInt(Field field)
    {
        return isOfType(field, Integer.TYPE);
    }

    private boolean isString(Field field)
    {
        return isOfType(field, String.class);
    }
}
