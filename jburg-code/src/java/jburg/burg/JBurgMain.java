package jburg.burg;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import jburg.burg.options.*;
import jburg.parser.*;

import antlr.CommonAST;
import antlr.collections.AST;

/**
 * JBurgMain drives compilation of a JBurg specification into a BURM.
 */
public class JBurgMain
{
    public static void main(String[] args) 
    {

        Options options = new Options();

        OptionParser optionProcessor = new OptionParser();

        optionProcessor.setOptions(args, options);
        optionProcessor.validate(options);

        if (!optionProcessor.hasErrors()) {
            options.validate(optionProcessor);
        }

        if (optionProcessor.hasErrors()) {
            for (String s: optionProcessor.getDiagnostics()) {
                System.err.println(s);
            }

            System.out.printf("Usage: java -jar jburg.jar %s\n", optionProcessor.describe(options));
            System.exit(1);
        } else if (options.help) {
            System.out.printf("Usage: java -jar jburg.jar %s\n", optionProcessor.describe(options));
            System.exit(0);
        }

        Logger log = new Logger(options.logInfo, options.logWarning, options.logError);

        try {
            
            File sourceFile = new File(options.inputDir, options.inputFile);

            MacroProcessingStream token_stream = new MacroProcessingStream((sourceFile));
            JBurgParser parser = new JBurgParser(token_stream);

            parser.specification();

            CommonAST t = (CommonAST)parser.getAST();

            if ( !parser.parseSuccessful() || null == t  ) {
                log.error("unable to parse specification %s.", sourceFile.getPath());
                System.exit(2);
            }
            

            if ( options.dumpParseTree ) {
                debugOut = new PrintStream( new FileOutputStream("dumpspec.xml") );

                debugOut.println("<?xml version=\"1.0\"?>");
                debugOut.println("<Specification>");
                dumpTree(t);
                debugOut.println("</Specification>");
            }

            // Compile the BURM.
            if ( ! options.syntaxCheckOnly ) {

                try {
                    options.className = options.outputFile.substring(0, options.outputFile.lastIndexOf("."));

                    // Try to create the output directory if necessary.
                    File outputPath = new File(options.outputDir, options.outputFile);
                    if(!(outputPath.getParentFile().exists() || outputPath.getParentFile().mkdirs())) {
                      log.error("Error creating output directory:" + outputPath.getAbsolutePath());
                    }

                    JBurgGenerator generator = new JBurgGenerator(t, log, options);
                    
                    generator.semanticAnalysis();
                    generator.emit(new PrintStream(new FileOutputStream(outputPath)));

                } catch ( Exception e ) {
                    log.exception("generating code", e);
                    System.exit(3);
                }
            }
        } catch(Exception e) {
            log.exception("parsing", e);
            System.exit(4);
        }
    }

	static PrintStream debugOut = null;

	/**
	 *  Dump an ANTLR AST into an XML file.
	 */
	static private void dumpTree( AST root )
	{
		while ( root != null ) {

			String id = translateASTType(root);
			String text = root.getText();

			debugOut.print("<");
			debugOut.print(id);

			if ( root.getType() != JBurgTokenTypes.BLOCK ) {

				if ( text != null && text.length() > 0 ) {
					debugOut.print ( " text=\"" + text + "\"" );
				}

				AST firstChild = root.getFirstChild();

				if ( firstChild != null ) {
					debugOut.println(">");

					dumpTree( firstChild );

					debugOut.print("</");
					debugOut.print(id);
					debugOut.println(">");
				} else {
					debugOut.println( "/>" );
				}
			} else {
				debugOut.println( ">" );
				debugOut.println( "<![CDATA[" + root.getText() + "]]>" );
				debugOut.println( "</" + id + ">" );
			}

			root = root.getNextSibling();
		}
	}

	static private Map<Integer,String> s_JBurgTokenTypes = null;

	static public String translateASTType( AST p )
	{
		//  Populate the static list of token types.
		if ( s_JBurgTokenTypes == null ) {

			s_JBurgTokenTypes = new HashMap<Integer,String>();

			Field[] fieldList = JBurgTokenTypes.class.getDeclaredFields();

			for ( int i = 0; i < fieldList.length; i++ ) {

				if ( fieldList[i].getType().equals(Integer.TYPE ) ) {

					int mods = fieldList[i].getModifiers();

					if ( Modifier.isPublic(mods) && Modifier.isStatic(mods) && Modifier.isFinal(mods) ) {

						//  public final static int is a manifest constant.
						try {
							Integer key = (Integer) fieldList[i].get(null);
							s_JBurgTokenTypes.put( key, fieldList[i].getName() );
						} catch ( Exception ex ) {
							//  Ignore this one.
						}
					}
				}
			}
		}

		String result = s_JBurgTokenTypes.get(new Integer(p.getType())).toString();

		if ( result == null )
			result = Integer.toString( p.getType() );

		return result;
	}
}
