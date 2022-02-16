grammar Expr;

options {
    output=AST;
    ASTLabelType=CommonTree; 
}

tokens
{
	DECL;
	EVAL_EXPR;
    INDIR;
}

prog: 
	(decl)* (stmt)+
	;

stmt:   
    evalExpr
    | assignmentStmt
    ;

decl: 
	type ID SEMI -> ^(DECL type ID)
    ;

type: 
	INT_TYPE
	;

assignmentStmt:
    ID EQUALS expr SEMI -> ^(EQUALS ID expr)
    ;

evalExpr: 
	expr SEMI -> ^(EVAL_EXPR expr)
	;
  
expr:   multExpr ((PLUS^|MINUS^) multExpr)?
    ; 

multExpr: 
	unaryExpr (STAR^ unaryExpr)?
    ; 

unaryExpr:
	atom -> atom
    | MINUS atom -> ^(MINUS atom)
	;

atom:   literal 
    |   ID -> ^(INDIR ID)
    |   '('! expr ')'!
    ;

literal:
    INTEGER_LITERAL
	;

/*
 *   Tokens 
 */

//  Whitespace
NEWLINE:'\r'? '\n' {skip();} ;
WS  :   (' '|'\t')+ {skip();} ;

//  Keywords
//  Keywords must come before
//  identifiers, since both 
//  consist of a string of characters.
INT_TYPE : 'int';

//  Identifiers and literals
ID  :   ('a'..'z'|'A'..'Z')+ ;
INTEGER_LITERAL :   '0'..'9'+ ;

//  Operators and other punctuation
SEMI: ';';
EQUALS : '=' ;
PLUS : '+' ;
MINUS : '-' ;
STAR : '*' ;

