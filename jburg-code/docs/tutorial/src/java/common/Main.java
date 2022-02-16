package jburg.tutorial.common;

import java.lang.reflect.Constructor;
import java.io.*;
import java.util.Properties;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import jburg.tutorial.common.*;

/**
 * Parse a source file and generate MIPS assembly.
 */
public class Main
{
    public static void main(String[] argv)
    throws Exception
    {
        // Load the configuration-specific properties.
        Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("configuration.properties"));

        String lexerClass = properties.get("lexerClass").toString();
        String parserClass = properties.get("parserClass").toString();
        String burmClass = properties.get("burmClass").toString();

        CommonTree tree = parse(argv[0], lexerClass, parserClass);

        if ( tree != null )
        {
            PrintWriter writer = new PrintWriter(argv[1]);
            // Write the source tree for documentation.
            writer.printf( "# %s\n", tree.toStringTree());
            writer.flush();
            writer.println(generateCode(tree, burmClass));
            writer.close();
        }
    }

    /**
     * Parse the source file.
     * @return the parsed AST.
     */
    protected static CommonTree parse(String srcFile, String lexerName, String parserName)
    throws Exception
    {
        ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(srcFile));

        Class lexerClass = Class.forName(lexerName);
        Constructor lexerCtor = lexerClass.getConstructor(CharStream.class);
        Lexer lexer = (Lexer)lexerCtor.newInstance(new Object[] {input});

        TokenRewriteStream tokens = new TokenRewriteStream(lexer);

        Class parserClass = Class.forName(parserName);
        Constructor parserCtor = parserClass.getConstructor(TokenStream.class);
        AbstractParser parser = (AbstractParser)parserCtor.newInstance(new Object[] {tokens});

        CommonTree result = (CommonTree)parser.parse();

        if ( parser.getErrors().size() > 0 )
        {
            result = null;
            for ( String msg: parser.getErrors() )
                System.err.println(msg);
        }

        return result;
    }

    /**
     * Generate code.
     * @param tree the root of the subtree which
     * is to be converted into code.
     */
    protected static Object generateCode(CommonTree tree, String burmClass)
    throws Exception
    {
        BURMBase burm = (BURMBase)Class.forName(burmClass).newInstance();
        try
        {
            burm.burm(tree);
            return burm.getResult();
        }
        catch ( IllegalStateException ex )
        {
            burm.dump(tree, new PrintWriter("/tmp/failedBurm.xml"));
            throw ex;
        }
    }
}

