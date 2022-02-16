/*
 * A parser for a very simple language: declarations, assignment statements, simple arithmetic expressions.
 */
grammar first;

options {
    output=AST;
    superClass=Parser;
}

tokens {
    COMPILATION_UNIT;
}

@header {
package jburg.tutorial.first;
    import jburg.tutorial.common.Parser;
}

@members {
    public CommonTree parse()
    throws Exception
    {
        return (CommonTree)compilationUnit().getTree();
    }
}

@lexer::header {
package jburg.tutorial.first;
}

compilationUnit:
    (decl|stmt)* EOF -> ^(COMPILATION_UNIT decl* stmt*)
    ;

/*
 * Declarations.
 */
decl: type ID ';' -> ^(type ID)
    ;

type: INT_TYPE
    ;

/*
 * Statements.
 */
stmt: assignment
    | pseudo
    ;

assignment: ID EQUALS expression ';' -> ^(EQUALS ID expression)
    ;

// Pseudo-instruction print; in most
// languages, print is a library routine
// and has no special-case logic in the compiler.
pseudo:
    PRINT expression ';' -> ^(PRINT expression)
    ;

/*
 * Expression-level syntax.
 * This simple language has only one operator,
 * but the skeleton of a more complete expression
 * syntax remains for reference.
 */
expression:
    t1=term
    (
        (PLUS t2=term -> ^(PLUS $t1 $t2))+
    |   -> $t1
    )
    ;

term:
    factor
    ;

factor:
    primary
    ;

primary:
    ID
    |
    INT_LITERAL
    ;

/*
 * Lexical analyzer.
 */
EQUALS:'=';
INT_TYPE:'int';
PLUS:'+';
PRINT:'print';

ID
    :   LETTER ('_'|LETTER|DIGIT)*
    ;

INT_LITERAL : (DIGIT+);

fragment
LETTER: ('a'..'z' | 'A'..'Z');
    
fragment
DIGIT
	:	'0'..'9';

WS  :  (' '|'\r'|'\t'|'\u000C'|'\n')+ {$channel=HIDDEN;}
    ;

COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
