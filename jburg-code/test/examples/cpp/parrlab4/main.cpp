#pragma warning(disable : 4231)

#include "calcburg.h"
#include "CalcParser.hpp"
#include "CalcLexer.hpp"
#include <sstream>

using namespace std;

int main(int argc, char *argv[])
{
	try {
		string strExpr("int i;\nint x;\ni=1;\nx=0;\nx=x+i;\n");
	
		std::stringstream	stream;
		stream << strExpr;
		stream.seekp(0, std::ios_base::beg);

		cout << "Input:" << endl << strExpr << endl;

		CalcLexer lexer( stream );
		lexer.setFilename("stdin");
		CalcParser parser(lexer);
		parser.setFilename("stdin");

        // setup the AST...
		antlr::ASTFactory ast_factory;
		parser.initializeASTFactory(ast_factory);
		parser.setASTFactory(&ast_factory);

		// parse the text
		parser.prog();

		// run the generator
		calcburg tg;
		tg.regnum = 1;
		antlr::RefAST t = parser.getAST();

		// Print the resulting tree out in LISP notation
		while(t) {
			cout << ";" << t->toStringTree() << endl;
			tg.burm(t);
			t = t->getNextSibling();
		}
	} catch(exception &e) {
		cout << "Exception:\n" << e.what() << endl;
	}

	return 0;
}
