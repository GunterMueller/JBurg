/*
 * A parser for a simple language: declarations, assignment statements, simple arithmetic expressions, procedures.
 */
grammar third;

options {
    output=AST;
    superClass=Parser;
    //backtrack=true;
    //memoize=true;
}

tokens {
    ARGUMENTS;
    ASSIGNMENT;
    COMPILATION_UNIT;
    BLOCK;
    BODY;
    CALL;
    DECLS;
    ELSE;
    FORMAL;
    FUNCTION;
    FUNCTION_BODY;
    IF;
    LOCAL;
    PRINT;
    READ;
    RETURN;
    STRING;
    WHILE;
}

@header {
package jburg.tutorial.third;
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
package jburg.tutorial.third;
}

compilationUnit:
    functionDef* EOF -> ^(COMPILATION_UNIT functionDef*);

functionDef:
    type ID formalArguments functionBody -> ^(FUNCTION ID formalArguments functionBody)
    ;

functionBody: '{' (decl | stmt)* '}' -> ^(FUNCTION_BODY ^(DECLS decl*) ^(BODY stmt*));

/*
 * Declarations.
 */
decl: type ID ';' -> ^(LOCAL type ID)
    ;

type: INT_TYPE
    ;

formalArguments:
    '(' (formal (',' formal)*)? ')' -> ^(ARGUMENTS formal*);

formal: type ID -> ^(FORMAL type ID);

/*
 * Statements.
 */
stmt: assignment
    | pseudo
    | block
    | ifStmt
    | returnStmt
    | whileStmt
    ;

assignment: ID '=' expression ';' -> ^(ASSIGNMENT ID expression)
    ;

// Pseudo-statements; in most languages,
// I/O is done by library routines and
// has no special-case logic in the compiler.
pseudo:
    'print' '(' expression (',' expression)* ')' ';' -> ^(PRINT expression+)
    |
    'read' '(' identifier ')' ';' -> ^(READ identifier)
    ;

block: '{' stmt* '}' -> ^(BLOCK stmt*);

ifStmt: 'if' '(' condition ')' stmt (('else')=>elseClause)? -> ^(IF condition stmt elseClause?);

elseClause: 'else' stmt -> ^(ELSE stmt);

returnStmt: 'return' expression ';' -> ^(RETURN expression);

whileStmt: 'while' '(' condition ')' stmt -> ^(WHILE condition stmt);

/*
 * Expression-level syntax.
 */
expression: condition;

condition: comparison;

comparison:
    e1=arithmetic_expr
    (
        (logical e2=arithmetic_expr -> ^(logical $e1 $e2))+
    |    -> $e1
    )
    ;
logical: EQUAL_EQUAL | LT;

arithmetic_expr:
    t1=term
    (
        (addop t2=term -> ^(addop $t1 $t2))+
    |    -> $t1
    )
    ;
addop: PLUS | MINUS;

term:
    f1=factor
    (
        (mulop factor -> ^(mulop factor*))+
    |   -> $f1
    )
    ;
mulop: ASTERISK;

factor:
    primary
    ;

primary:
    call
    |
    identifier
    |
    literal
    ;

identifier: ID;

call: ID '(' (expression (',' expression)*)? ')' -> ^(CALL ID expression*);

literal:
    INT_LITERAL
    |
    STRING_LITERAL
    ;

/*
 * Lexical analyzer.
 */
ASTERISK: '*';
EQUAL_EQUAL:'==';
INT_TYPE:'int';
LT:'<';
MINUS:'-';
PLUS:'+';

ID
    :   LETTER ('_'|LETTER|DIGIT)*
    ;

INT_LITERAL : (DIGIT+);

fragment
LETTER: ('a'..'z' | 'A'..'Z');
    
fragment
DIGIT
	:	'0'..'9';

STRING_LITERAL
	:	'"' (ESC | ~('\\'|'"'))* '"'
	;
// Re-sync syntax hilight "

// The SPIM simulator's assembler handles
// most escape sequences; here we only need
// to deal with escapes that affect the structure
// of a string.
fragment
ESC	:	'\\'
		(	
		|	'"' // Re-sync syntax hilight "
		|	'\\'
		|	. // unknown, leave as it is
		)
	;
WS  :  (' '|'\r'|'\t'|'\u000C'|'\n')+ {$channel=HIDDEN;}
    ;

COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    ;
