import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Iterator;
import java.util.List;

public class Chapter1Driver
{
    public static void main(String[] args) throws Exception 
	{
        if ( args.length < 2 )
		{
			usage();
			System.exit(1);
		}

		FileInputStream file_input = new FileInputStream(args[0]);
		PrintWriter asm_output = new PrintWriter(new FileWriter(args[1]));


        ANTLRInputStream input = new ANTLRInputStream(file_input);
        ExprLexer lexer = new ExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExprParser parser = new ExprParser(tokens);

        //  Walk statements
		CommonTree root = (CommonTree) parser.prog().getTree();

		MipsAsmWriter emitter = new MipsAsmWriter(asm_output);

        ExprEmitter insn_selector = new ExprEmitter();
		insn_selector.setRegalloc(new MipsRegisterAllocator());
		insn_selector.setMipsemitter(emitter);

		emitter.enterProcedure("main");


		for ( int i = 0; i < root.getChildCount(); i++ )
		{
			CommonTree next_stmt = (CommonTree) root.getChild(i);

			if ( next_stmt.getToken().getType() == ExprParser.DECL )
			{
				emitter.printDecl(next_stmt.getChild(0), next_stmt.getChild(1));
			}
			else
			{
				insn_selector.burm(next_stmt);
			}
		}

		emitter.exitProcedure();

		asm_output.close();
    }

	static void usage()
	{
		System.err.println("Usage: java Chapter1Driver <source file> <assembly output file>");
	}
}

