grammar Expr;
options {
    output=AST;
    ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}


stmt:   expr NEWLINE        -> expr
    |   ID EQUALS expr NEWLINE -> ^(EQUALS ID expr)
    |   NEWLINE             ->
    ;

expr:   multExpr ((PLUS^|MINUS^) multExpr)*
    ; 

multExpr
    :   atom (STAR^ atom)*
    ; 

atom:   INT 
    |   ID
    |   '('! expr ')'!
    ;

ID  :   ('a'..'z'|'A'..'Z')+ ;
INT :   '0'..'9'+ ;
NEWLINE:'\r'? '\n' ;
WS  :   (' '|'\t')+ {skip();} ;
EQUALS : '=' ;
PLUS : '+' ;
MINUS : '-' ;
STAR : '*' ;

