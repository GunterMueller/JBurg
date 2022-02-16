import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import java.io.FileInputStream;

public class ExpressionEvalautorDriver
{
    public static void main(String[] args) throws Exception 
	{
        if ( args.length == 0 )
		{
			usage();
			System.exit(1);
		}

		FileInputStream file_input = new FileInputStream(args[0]);

        ANTLRInputStream input = new ANTLRInputStream(file_input);
        ExprLexer lexer = new ExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);
        ExprParser.expr_return r = parser.expr();

        // walk resulting tree
        CommonTree t = (CommonTree)r.getTree();
        ExprEmitter emitter = new ExprEmitter();
		emitter.burm(t);
		System.out.println( "result = " + emitter.getResult().toString() );
    }

	static void usage()
	{
		System.err.println("Usage: java ExpressionEvalautorDriver <input file>");
	}
}

