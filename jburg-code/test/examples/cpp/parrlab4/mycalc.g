options {
	language="Cpp";
}

class CalcParser extends Parser;
options {
	genHashLines = true;		// include line number information
	buildAST = true;			// uses CommonAST by default
}

tokens {
	ADDR;
	IND;
	DECL="int";
	ZERO="0";
}

prog:   (stmt)+
    ;

stmt:   assignment
    |   decl
    ;

decl:   "int"^ ID SEMI!
    ;

assignment!
    :   id:ID eq:EQUALS e:expr SEMI!   
        {#assignment = #(#eq, #(#[ADDR,"&"],#id), #e);}
    ;

expr:   atom ((PLUS^|MINUS^) atom)* ;

atom:   INT
    |   ZERO
    |!  id:ID {#atom = #(#[IND,"*"],#(#[ADDR,"&"], #id));}
    ;

class CalcLexer extends Lexer;

WS_	:	(' '
	|	'\t'
	|	'\n'
	|	'\r')
		{ _ttype = ANTLR_USE_NAMESPACE(antlr)Token::SKIP; }
	;

LPAREN:	'('
	;

RPAREN:	')'
	;
	
EQUALS	: '='
	;

MINUS	: '-'
	;

PLUS	: '+'
	;

SEMI	: ';'
	;

protected
DIGIT
	:	'0'..'9'
	;

INT	:	(DIGIT)+
	;

ID	: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_') *
	;