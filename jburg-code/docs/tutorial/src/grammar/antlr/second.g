/*
 * A parser for a very simple language: declarations, assignment statements, simple arithmetic expressions.
 */
grammar second;

options {
    output=AST;
    superClass=Parser;
}

tokens {
    COMPILATION_UNIT;
}

@header {
package jburg.tutorial.second;
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
package jburg.tutorial.second;
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
    | block
    | ifStmt
    | whileStmt
    ;

assignment: ID EQUALS expression ';' -> ^(EQUALS ID expression)
    ;

// Pseudo-instruction print; in most
// languages, print is a library routine
// and has no special-case logic in the compiler.
pseudo:
    PRINT expression (COMMA expression)* ';' -> ^(PRINT expression+)
    ;

block: BRACE_START stmt* BRACE_END -> ^(BRACE_START stmt*);

ifStmt: IF PAREN_START condition PAREN_END stmt ((ELSE)=>elseClause)? -> ^(IF condition stmt elseClause?);

elseClause: ELSE stmt -> ^(ELSE stmt);

whileStmt: WHILE PAREN_START condition PAREN_END stmt -> ^(WHILE condition stmt);

/*
 * Expression-level syntax.
 * This simple language has only a few operators,
 * but the skeleton of a more complete expression
 * syntax remains for reference.
 */
expression: condition;

condition:
    arithmetic_expr
    |
    arithmetic_expr logical arithmetic_expr -> ^(logical arithmetic_expr+);

arithmetic_expr:
    term
    |
    term (PLUS term)+ -> ^(PLUS term+)
    |
    term (MINUS term)+ -> ^(MINUS term+)
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
    literal
    ;

literal:
    INT_LITERAL
    |
    STRING_LITERAL
    ;

logical: EQUAL_EQUAL | LT;

/*
 * Lexical analyzer.
 */
BRACE_START:'{';
BRACE_END:'}';
COMMA:',';
ELSE:'else';
EQUAL_EQUAL:'==';
EQUALS:'=';
IF:'if';
INT_TYPE:'int';
LT:'<';
MINUS:'-';
PAREN_START:'(';
PAREN_END:')';
PLUS:'+';
PRINT:'print';
STRING:'String';
WHILE:'while';

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
