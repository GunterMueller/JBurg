package jburg.emitter;

import java.io.PrintStream;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.AttributeRenderer;

import jburg.burg.JBurgPatternMatcher;
import jburg.burg.ir.GetNthChild;
import jburg.burg.ir.GetOperator;

/**
 *
 * Emit a BURM in C++.
 * 
 * TODO: Should output into a .h &amp; a .cpp file (perhaps file output should be handled in this class?)
 * 
 * @author Nick Brereton
 * @author Tom Harwood
 *
 */
@SuppressWarnings("nls")
public class EmitCpp extends TemplateBasedEmitter implements EmitLang
{
    private static class InstanceInitializer
    {
        InstanceInitializer(Object instanceName, Object instanceInitialValue)
        {
            this.instanceName = instanceName;
            this.instanceInitialValue = instanceInitialValue;
        }
        
        Object instanceName;
        Object instanceInitialValue;
    }

    private List<InstanceInitializer> instanceInitializers = new ArrayList<InstanceInitializer>();

	public EmitCpp()
    {
		super("Cpp");

        templates.registerRenderer(String.class, new TypeRenderer());
        boilerplate.registerRenderer(String.class, new TypeRenderer());
	}

	@Override
    public boolean accept(String langName)
	{
		return "cpp".equalsIgnoreCase(langName);
	}

    public void emitHeader(
        String packageName,
        String headerBlock,
        String baseClassName,
        List<String> interfaceNames,
        PrintStream output
        )
	{
		if (headerBlock != null) {
			//  Strip off the enclosing "{" and "}".
			output.println(headerBlock.substring(1, headerBlock.length() - 2));
		}

        Object nStates = configuration.ntType.equals("int")? configuration.goalStateNames.size(): null;

        output.println(boilerplate.getTemplate(
            "header",
            "baseClass",            convertAnnotationType(baseClassName),
            "interfaceNames",       interfaceNames,
            "getOperator",          new GetOperator("m_node"),
            "packageName",          packageName,
            "headerBlock",          headerBlock,
            "getNthChildIdx",       new GetNthChild("result", getTemplate("arrayAccess", "stem", "accessPath", "index", "idx")),
            "annotationAccessor",   this.annotationAccessor,
            "singletonAnnotations", singletonAnnotations,
            "nStates",              nStates
        ));
	}
	
	@Override
    public void emitSubgoalLookupTables(int max_action, Map<Integer, Vector<JBurgPatternMatcher>> rules_by_action, PrintStream output)
	{
        StringTemplate staticDecls = boilerplate.getTemplate("staticDeclarations", "maxSize", Integer.toString(max_action + 1));

        for ( Integer rule_num: rules_by_action.keySet() )
        {
            //  Emit the subgoals in reverse order so they are reduced and pushed
            //  onto the stack in the correct order.
            Vector<JBurgPatternMatcher> matchers = rules_by_action.get(rule_num); 
            
            for ( int pattern_num = matchers.size() -1; pattern_num >= 0; pattern_num--  )
            {
                JBurgPatternMatcher matcher = matchers.elementAt(pattern_num);

                StringTemplate subgoalInitializer = boilerplate.getTemplate(
                    "subgoalInitializer",
                    "ruleNumber",       rule_num,
                    "patternNumber",    pattern_num,
                    "matcher",          matcher,
                    "subgoal",          genGetGoalState(matcher.getSubgoal())
                );

                staticDecls.setAttribute("initializers", subgoalInitializer);

                for ( JBurgPatternMatcher.PathElement idx: matcher.generateAccessPath() ) {
                    subgoalInitializer.setAttribute(
                        "elements",
                        boilerplate.getTemplate(
                            "subgoalInitializerElement",
                            "ruleNumber",       rule_num,
                            "patternNumber",    pattern_num,
                            "element",          idx.index
                        )
                    );
                }
                
            }
        }

        output.println(staticDecls);
	}
	
	@Override
    public void emitTrailer(
        Set<String>         goalStateNames, 
        Map<String,Object>  burm_properties, 
        Object              default_error_handler, 
        Map<Integer,Object> prologue_blocks,
        PrintStream         output
        ) 
    {
        super.emitTrailer(goalStateNames, burm_properties, default_error_handler, prologue_blocks, output);

		//  Emit definitions for static data.
        //  TODO: If the trailer template could close the class definition, then these
        //  templates could be included in the trailer, making this override redundant.
		output.println(boilerplate.getTemplate("staticDefinitions"));
        output.println(boilerplate.getTemplate("emitStateNames", "stateNames", goalStateNames));

        if (this.annotationAccessor != null) {
            output.println(boilerplate.getTemplate("annotationAccessorStaticDefinitions"));
        }
        if (configuration.wildcardState != null) {
            output.println(boilerplate.getTemplate("placeholderCostDefinition"));
        }
    }

	@Override
    public Object genInstanceField ( int modifiers, Object type, Object name, Object initializer )
	{
        // Instance initialization is done via the constructor in C++.
		if ( initializer != null ) {
			this.instanceInitializers.add(new InstanceInitializer(name, initializer));
		}

        return super.genInstanceField(modifiers, type, name, null);
	}

    @Override
	protected void decodeModifiers( int modifiers, StringBuffer result )
	{
		if ( Modifier.isPublic(modifiers) ) {
			result.append("public:\n");
		} else if ( Modifier.isPrivate(modifiers) ) {
			result.append("private:\n");
		}

		if ( Modifier.isStatic(modifiers) ) {
			result.append("static ");
		}

		if ( Modifier.isFinal(modifiers) ) {
			result.append("const ");
		}
	}

    @Override
    public Object getAnnotationType()
    {
        return convertAnnotationType(configuration.options.annotationInterfaceName);
    }

    @Override
    public StringTemplate getConstructorBody(Object decl, Object baseClass, Object ... superParameters)
    {
        StringTemplate result = getTemplate(
            "constructorBody",
            "decl", decl,
            "baseClassName", "JBurgSpecializedAnnotation",
            "superParameters", superParameters
        );

        if (!instanceInitializers.isEmpty()) {

            ArrayList<Object> fields = new ArrayList<Object>();
            ArrayList<Object> values = new ArrayList<Object>();

            for (InstanceInitializer initializer: instanceInitializers) {
                fields.add(initializer.instanceName);
                values.add(initializer.instanceInitialValue);
            }
            result.setAttribute("instanceVariables", fields);
            result.setAttribute("instanceInitializers", values);
            instanceInitializers.clear();
        }

        return result;
    }

	private String convertAnnotationType(String inType)
    {
		if (configuration.options.annotationInterfaceName.equals(inType) || "JBurgSpecializedAnnotation".equals(inType)) {
			//  This is a parameterized type in C++ BURMs.
            return String.format("%s<%s>*",inType, getINodeType());
		} else {
			return inType;
		}
	}

    private String convertPointerTypeToBaseType(Object rawType)
    {
        String inType = rawType.toString();

        if (inType.endsWith("*")) {
            return inType.substring(0,inType.length()-1);
        } else {
            return inType;
        }
    }

    private String convertBaseTypeToPointerType(Object rawType)
    {
        String inType = rawType.toString();

        if (!inType.endsWith("*")) {
            return inType + "*";
        } else {
            return inType;
        }
    }

    private String convertIntermediateType(Object rawType)
    {
        return rawType.toString()
            .replaceAll("\\*","")
            .replaceAll("::","_")
            .replaceAll("<","_")
            .replaceAll(">","_");
    }

    /**
     * Render a C++ type reference (or other random string)
     * according to C++'s context-sensitive rules.
     */
    public class TypeRenderer implements AttributeRenderer
    {
        public String toString(Object o) {
            return toString(o, null, null);
        }

        public String toString(Object o, String formatString) {
            return toString(o, formatString, null);
        }

        public String toString(Object o, String formatString, Locale locale) {
            if ("templateArg".equalsIgnoreCase(formatString) ) {
                return convertPointerTypeToBaseType(o.toString());
            } else if ("annotationAsTemplateArg".equalsIgnoreCase(formatString)) {
                return convertPointerTypeToBaseType(convertAnnotationType(o.toString()));
            } else if ("typeReference".equalsIgnoreCase(formatString)) {
                return convertAnnotationType(o.toString());
            } else if ("returnTypeDeclaration".equalsIgnoreCase(formatString)) {
                return String.format("%s\t%sResult;", o, o);
            } else if ("intermediateTypeDeclaration".equalsIgnoreCase(formatString)) {
                return String.format("%s\t%sResult;\n", o, convertIntermediateType(o));
            } else if ("intermediateType".equalsIgnoreCase(formatString)) {
                return String.format("%sResult", convertPointerTypeToBaseType(o.toString()));
            } else if ("getReferenceType".equalsIgnoreCase(formatString)) {
                return convertBaseTypeToPointerType(o);
            } else {
                return o.toString();
            }
        }

    }

    @Override
    public Object genVariadicActionRoutineParameter(Object stackName, Object paramType, Object paramName)
	{
        return getTemplate(
            "actionRoutineParameterVariadic",
            "initialValue", configuration.reducedValuesName,
            "paramType", paramType,
            "paramName", paramName
        );
	}

    @Override
    public String getStringClassName()
    {
        return "const char*";
    }
    @Override
    public Object defineContainer(Object contentsType)
    {
        return null;
    }

    @Override
    public Object getAddChild()
    {
        // TODO: Need better analysis of what's a pointer vs. a reference.
        return "narySubtrees.push_back(child)";
    }

}
