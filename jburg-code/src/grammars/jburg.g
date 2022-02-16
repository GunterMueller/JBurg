header 
{
package jburg.parser;
}

{
import java.io.*;

import antlr.CommonAST;
import antlr.collections.AST;
}

class JBurgParser extends Parser; 
options {
   buildAST = true;         // uses CommonAST by default
   k=7;                     
   exportVocab=JBurg;
}


tokens 
{ 
    //  Synthetic token types used to synthesize nodes.
    ANNOTATION_DECLARATION;
    COST_FUNCTION;
    COST_FUNCTION_EXPLICIT_PARAMETERS; 
    EXPLICIT_REDUCTION;
    EXTENDS_CLASS_SPECIFICATION;
    HEADER_DECLARATION;
    IMPLEMENTS_INTERFACE_SPECIFICATION;
    INODE_ADAPTER_DECLARATION;
    INODE_TYPE_DECLARATION;
    LANGUAGE_DECLARATION;
    LITERAL_COST_SPEC; 
    MANIFEST_CONSTANT;
    MEMBER_ACCESS;
    NODE_TYPE;
    NON_TERMINAL_ENUM;
    NON_TERMINAL_PARAMETER;
    OPERAND_ARBITRARY_ARITY;
    OPERAND_AT_LEAST_ONE_ARITY;
    OPERAND_LIST;
    OPERATOR_SPECIFICATION;
    OPTION_SETTING;
    PACKAGE_SPECIFICATION; 
    PATTERN_DECLARATION;
    PATTERN_RULE; 
    PATTERN_SPECIFICATION;
    PROCEDURE_CALL; 
    PROPERTY_SPECIFICATION; 
    REDUCER_NODE_NAME;
    REDUCTION_ACTION;
    REDUCTION_DECLARATION;
    RETURN_DECLARATION;
    SIMPLE_TRANSFORMATION_RULE; 
    SYMBOLIC_COST_SPEC;
    TERMINAL_PATTERN; 
    TRANSFORMATION_RULE; 
    TYPED_RETURN_DECLARATION;
    VOLATILE_COST;
    WILDCARD_STATE;
}

/*
 *  Support routines to be included in the parser.
 */
{
    /*
     *  Error-handling code.
     */
    boolean parseOk = true;

	public void reportError(antlr.RecognitionException e)
	{
        super.reportError(e);
        parseOk = false;
	}

    public boolean parseSuccessful()
    {
        return this.parseOk;
    }
}

/*
 *  **********************************************************
 *  **  Parser productions (mostly in alphabetical order).  **
 *  **********************************************************
 */

//  "start" production is specification.

allocator_declaration:
    ALLOCATOR alloc:multipart_identifier SEMI
    {
        #allocator_declaration = #([ALLOCATOR], alloc);
    }
    ;

annotation_declaration!:
    getter1:get_annotation_declaration setter1:set_annotation_declaration
    {
        #annotation_declaration = #([ANNOTATION_DECLARATION], getter1, setter1);
    }
    | setter2:set_annotation_declaration getter2:get_annotation_declaration
    {
        #annotation_declaration = #([ANNOTATION_DECLARATION], getter2, setter2);
    }
    ;

annotation_extends_declaration!:
    ANNOTATION_EXTENDS annotation_extends_name:multipart_identifier
    {
        #annotation_extends_declaration = #([ANNOTATION_EXTENDS], #annotation_extends_name);
    }
    ;

cost_function!:
    funcId:IDENTIFIER LPAREN RPAREN functionBody:BLOCK 
    {
        #cost_function = #([COST_FUNCTION], funcId, functionBody);
    }
    ;

cost_specification:
   COLON! 
   (
       (procedure_call) =>procedure_call
       |
       simple_cost_spec
   )
   ;

declaration:
       allocator_declaration
   |   annotation_declaration
   |   annotation_extends_declaration
   |   error_handler_declaration
   |   extends_declaration
   |   header_declaration
   |   implements_declaration
   |   inclass_declaration
   |   init_static_annotation_declaration
   |   inode_type_declaration
   |   inode_adapter_declaration
   |   inode_access_declaration
   |   language_declaration
   |   manifest_constant
   |   nonterminal_enum
   |   opcode_declaration
   |   opcode_nodetype_declaration
   |   option_declaration
   |   package_declaration
   |   property_specification
   |   reducer_node_name_declaration
   |   return_declaration
   |   strict_type_declaration
   |   volatile_cost_functions_declaration
   |   wildcard_reduction_declaration
   ;

error_handler_declaration:
    DEFAULT_ERROR_HANDLER action:BLOCK
    {
        #error_handler_declaration  = #([DEFAULT_ERROR_HANDLER], action);
    }
    ;

get_annotation_declaration!:
    GET_ANNOTATION LPAREN get_param_name:multipart_identifier RPAREN get_action: BLOCK
    {
        #get_annotation_declaration = #([GET_ANNOTATION], get_param_name, get_action);
    }
    ;

init_static_annotation_declaration!:
    INIT_STATIC_ANNOTATION init_action: BLOCK
    {
        #init_static_annotation_declaration = #([INIT_STATIC_ANNOTATION], init_action);
    }
    ;

header_declaration:
    HEADER header_block:BLOCK
    {
        #header_declaration = #([HEADER_DECLARATION], header_block);
    }
    ;
   
extends_declaration!:
    EXTENDS base_class_name:multipart_identifier SEMI
    {
        #extends_declaration = #([EXTENDS_CLASS_SPECIFICATION], base_class_name);
    }
    ;

implements_declaration!:
    IMPLEMENTS implements_interface_name:multipart_identifier SEMI
    {
        #implements_declaration = #([IMPLEMENTS_INTERFACE_SPECIFICATION], implements_interface_name);
    }
    ;

inclass_declaration!:
    inclass_block:BLOCK
	{
        #inclass_declaration = #([INCLASS_DECLARATION], inclass_block);
	}
    | INCLASS_DECLARATION inclass_block2:BLOCK
    {
        #inclass_declaration = #([INCLASS_DECLARATION], inclass_block2);
    }
	;

inode_adapter_declaration!:
    INODE_ADAPTER inode_adapter_class:multipart_identifier SEMI
    {
        #inode_adapter_declaration = #([INODE_ADAPTER_DECLARATION], inode_adapter_class);
    } 
    ;

inode_access_declaration!:
        GET_OPERATOR LPAREN op_inode:multipart_identifier RPAREN op_action: BLOCK
    {
        #inode_access_declaration = #([GET_OPERATOR], op_inode, op_action);
    }
    |   GET_COUNT LPAREN count_inode:multipart_identifier RPAREN count_action: BLOCK
    {
        #inode_access_declaration = #([GET_COUNT], count_inode, count_action);
    }
    |   GET_CHILD LPAREN child_inode:multipart_identifier COMMA child_index:multipart_identifier RPAREN child_action: BLOCK
    {
        #inode_access_declaration = #([GET_CHILD], child_inode, child_index, child_action);
    }
    ;
   
inode_type_declaration!:
    INODE_TYPE inode_type:multipart_identifier SEMI
    {
        #inode_type_declaration = #([INODE_TYPE_DECLARATION], inode_type);
    } 
    ;
   
language_declaration!:
    LANGUAGE lang_name:IDENTIFIER SEMI
    {
        #language_declaration = #([LANGUAGE_DECLARATION], lang_name);
    }
    ;

strict_type_declaration!:
    STRICT_TYPE SEMI
    {
        #strict_type_declaration = #([STRICT_TYPE]);
    }
    ;

reducer_node_name_declaration!:
    REDUCER_NODE_NAME name:IDENTIFIER SEMI
    {
        #reducer_node_name_declaration = #([REDUCER_NODE_NAME], name);
    }
    ;

wildcard_reduction_declaration!:
    WILDCARD_STATE name:IDENTIFIER SEMI
    {
        #wildcard_reduction_declaration = #([WILDCARD_STATE], name);
    }
    ;

manifest_constant!:
    MANIFEST_CONSTANT constName:IDENTIFIER EQUALS constValue:INT SEMI
    {
        #manifest_constant = #([MANIFEST_CONSTANT], constName, constValue);
    }
    ;

multipart_identifier:
    firstPart:IDENTIFIER ( options{greedy=true;}: ( PERIOD | (COLON COLON) ) IDENTIFIER )* (STAR)?
    {
        //  Coalesce all the child nodes' text.
        StringBuffer allText = new StringBuffer();

        AST current = #firstPart;

        while (current != null) 
		{
            allText.append(current.getText());
            current = current.getNextSibling();
		}

        //  Synthesize an IDENTIFIER node, and
        //  give it the re-assembled multipart ID.
        #multipart_identifier = #([IDENTIFIER]);
	    #multipart_identifier.setText( allText.toString() );
    }
    ;

arrow_id:
    firstPart:IDENTIFIER ARROW secondPart:IDENTIFIER
    {
        //  Coalesce all the child nodes' text.
        StringBuffer allText = new StringBuffer();

        allText.append(#firstPart.getText());
        allText.append("->");
        allText.append(#secondPart.getText());

        //  Synthesize an IDENTIFIER node, and
        //  give it the re-assembled multipart ID.
        #arrow_id = #([IDENTIFIER]);
	    #arrow_id.setText( allText.toString() );
    }
    ;

multipart_identifier_java_only:
    firstPart:IDENTIFIER ( PERIOD IDENTIFIER )?
    {
        //  Coalesce all the child nodes' text.
        StringBuffer allText = new StringBuffer();

        AST current = #firstPart;

        while (current != null) 
		{
            allText.append(current.getText());
            current = current.getNextSibling();
		}

	    //  Synthesize a node with an explict type.
        #multipart_identifier_java_only = #([IDENTIFIER]);
	    #multipart_identifier_java_only.setText( allText.toString() );
    }
    ;

n_ary_operand!:
    at_least_one_operand:parameter_decl PLUS!  //  Number of operands may vary, but at least one
    {
        #n_ary_operand = #([OPERAND_AT_LEAST_ONE_ARITY], at_least_one_operand );
    }
    | arbitrary_operands:parameter_decl STAR!  //  Number of operands may vary
    {
        #n_ary_operand = #([OPERAND_ARBITRARY_ARITY], arbitrary_operands );
    }
    ;

nonterminal_enum!:
    NON_TERMINAL_ENUM ntClass: multipart_identifier SEMI
    {
        #nonterminal_enum = #([NON_TERMINAL_ENUM], ntClass);
    }
    ;

opcode_nodetype_declaration!:
    NODE_TYPE opcode:multipart_identifier_java_only EQUALS opcode_node_type:type_name SEMI
    {
        #opcode_nodetype_declaration = #([NODE_TYPE], opcode, opcode_node_type);
    }
    ;

opcode_declaration!:
    OPCODE_TYPE opcodeType:multipart_identifier_java_only SEMI
    {
        #opcode_declaration = #([OPCODE_TYPE], #opcodeType);
    }
    ;

operand_list:
    operand ( options { greedy=true;}: COMMA! operand)* (COMMA! n_ary_operand)?
    {
        #operand_list = #([OPERAND_LIST], #operand_list);
    }
    |    manyop:n_ary_operand
    {
        #operand_list = #([OPERAND_LIST], manyop );
    }
    ;

operand:
    operator_specification          //  Embedded pattern match
    |    parameter_decl             //  Simple list of subgoals
    ;

operator_specification!:
    ( multipart_identifier_java_only LPAREN VOID RPAREN ) =>
    terminalId:multipart_identifier_java_only LPAREN VOID RPAREN
    {
        #operator_specification = #([TERMINAL_PATTERN], terminalId);
    }
    |   
    ( multipart_identifier_java_only LPAREN VOID RPAREN IDENTIFIER) =>
    namedTerminalId:multipart_identifier_java_only LPAREN VOID RPAREN terminalName:IDENTIFIER
    {
        #operator_specification = #([TERMINAL_PATTERN], namedTerminalId, terminalName);
    }
    |    
    operatorId:multipart_identifier_java_only LPAREN operands:operand_list RPAREN
    {
        #operator_specification = #([OPERATOR_SPECIFICATION], operatorId, operands);
    }
    ;

option_declaration!:
    OPTION_TOK option_name:IDENTIFIER EQUALS option_value:IDENTIFIER SEMI
    {
        #option_declaration = #([OPTION_SETTING], option_name, option_value);
    }
    ;

package_declaration!:
    PACKAGE package_name:multipart_identifier SEMI
    {
        #package_declaration = #([PACKAGE_SPECIFICATION], package_name);
    }
    ;

parameter_decl!:
    id1:IDENTIFIER id2:IDENTIFIER
   	{
        #parameter_decl = #([NON_TERMINAL_PARAMETER], id1, id2);
	}
    ;

pattern_declaration!:
    PATTERN name:IDENTIFIER pattern:pattern_specification SEMI
    {
        #pattern_declaration = #([PATTERN_DECLARATION], name, pattern);
    }
    ;

pattern_specification!:
    op:operator_specification
	{
		#pattern_specification = #([PATTERN_SPECIFICATION], op);
	}
	;
    
procedure_call!
    {
        AST result = #([PROCEDURE_CALL]);
    }
    :
        (
        id:multipart_identifier {result.addChild(#id);}
        | arrowId:arrow_id  {result.addChild(#arrowId);}
        )
        LPAREN 
        (
            first_param:expression { result.addChild(#first_param); }
            (COMMA next_param:expression { result.addChild(#next_param); })*
        )?
        RPAREN
    {
        #procedure_call = result;
    }
    ;
 
prologue_section!:
    b:BLOCK { #prologue_section = #b; }
    |
    p:procedure_call { #prologue_section = #p; }
    ;

property_specification!:
    BURM_PROPERTY property_type:type_name property_name:IDENTIFIER SEMI
    {
        #property_specification = #([PROPERTY_SPECIFICATION], property_type, property_name);
    }
    ;

return_declaration!:
    RETURN_TYPE return_type:type_name SEMI
    {
        #return_declaration = #([RETURN_DECLARATION], return_type);
    } 
    |    RETURN_TYPE state_name:IDENTIFIER EQUALS state_return_type:type_name SEMI
    {
        #return_declaration = #([TYPED_RETURN_DECLARATION], state_name, state_return_type);
    }
    ;

/*
 *  Reduce a rule.
 *  Rule types are:<ul>
 *  <li>  Pattern rules, which combine subgoals with other nodes further up the tree.
 *  <li>  Simple transformation rules.  Like iBurg's chain rules, simple transformation
 *        rules allow one goal to satisfy additional goals.
 *  <li>  Complex transformation rules, which allow the reducer to actively transform
 *        a subgoal to satisfy additional subgoals.
 *  </ul>
 */
rule!:
        //  A pattern-matching rule. 
        nonTerminalRuleId:IDENTIFIER EQUALS 
        pattern:pattern_specification 
        cost:cost_specification 
        action:reduction_action    
    {
        #rule = #([PATTERN_RULE], nonTerminalRuleId, pattern, cost, action);
    }
    |    //  A simple transformation rule.
        simpleTransformationTarget:IDENTIFIER EQUALS! 
        simpleTransformationSource:IDENTIFIER SEMI!
    {
        #rule = #([SIMPLE_TRANSFORMATION_RULE], simpleTransformationTarget, simpleTransformationSource );
    }
    |    //  A complex transformation from one nonterminal state to another.
        transformationTarget:IDENTIFIER EQUALS!  
        transformationSource:IDENTIFIER 
        transformationCost:cost_specification 
        transformationAction:reduction_action
    {
        #rule = #([TRANSFORMATION_RULE], transformationTarget, transformationSource, transformationCost, transformationAction );
    }
    ;

reduction_action!:
    (PROLOGUE prologue:prologue_section)? 
    (
        EXPLICIT_REDUCTION  reducer_call: procedure_call SEMI
        {
            #reduction_action = #([EXPLICIT_REDUCTION], reducer_call);
        }
        |
        action:BLOCK
        {
            #reduction_action = #([REDUCTION_ACTION], action);
        }
    )
    {
        if ( #prologue != null )
            #reduction_action.addChild(#([PROLOGUE],prologue));
    }
    ;

reduction_declaration!:
    nonTerminalRuleId:IDENTIFIER EQUALS  PATTERN name:IDENTIFIER cost:cost_specification r:reduction_action
    {
        #reduction_declaration = #([REDUCTION_DECLARATION], nonTerminalRuleId, name, cost, r);
    }
    ;

set_annotation_declaration!:
    SET_ANNOTATION LPAREN set_inode_name:multipart_identifier COMMA set_anno_type:type_name set_anno_name:multipart_identifier RPAREN set_action: BLOCK
    {
        #set_annotation_declaration = #([SET_ANNOTATION], set_inode_name, set_anno_type, set_anno_name, set_action);
    }
    ;

simple_cost_spec!:
    iCost:INT
    {
        #simple_cost_spec = #([LITERAL_COST_SPEC], iCost);
    }
    | sCost: multipart_identifier
    {
        #simple_cost_spec = #([SYMBOLIC_COST_SPEC], sCost);
    }
    ;

specification:
    (declaration | rule | cost_function | pattern_declaration | reduction_declaration )* EOF!
    ;

type_name!:
    stem:multipart_identifier (LANGLE ptype:type_name RANGLE)?
    {
        if ( null == #ptype ) {
            #type_name = #stem;
        } else {
            #type_name = #([IDENTIFIER]);
            #type_name.setText(String.format("%s<%s>", #stem.getText(), #ptype.getText()));
        }
    }
    | VOID STAR
    {
        #type_name = #([IDENTIFIER]);
        #type_name.setText("void*");
    }
    ;

volatile_cost_functions_declaration:
    VOLATILE_COST (multipart_identifier (COMMA!)?)* SEMI!
    {
        #volatile_cost_functions_declaration = #([VOLATILE_COST], #volatile_cost_functions_declaration);
    }
    ;

/*
 *
 *  **************************
 *  **  Expression grammar  **
 *  **************************
 *
 */
expression!:
    px:postfix_expression
    {
        #expression = #px;
    }
    ;

postfix_expression!:
     primary:primary_expression
     {
         #postfix_expression = #primary;
     }
     (
         options { greedy=true; }:
         (
             PERIOD member:expression
             {
                 #postfix_expression = #([MEMBER_ACCESS], #postfix_expression, member);
             }
         )
     )*
     ;

primary_expression!:
    (multipart_identifier LPAREN) => procall:procedure_call
    {
        #primary_expression = #procall;
    }
    | (sharp:SHARP)? id:multipart_identifier
    {
        if ( sharp == null )
            #primary_expression = #id;
        else
        {
            String composite_param = "#" + #id.getText();
            #primary_expression = #([IDENTIFIER]);
            #primary_expression.setText(composite_param);
        }
    }
    | numeric_literal:INT
    {
        #primary_expression = #numeric_literal;
    }
    ;


/*
 *  ***************************************
 *  **  Lexical analyzer specification.  **
 *  ***************************************
 */
class JBurgANTLRLexer extends Lexer;
options
{
   //  Accept ASCII characters as input; UNICODE would be great but too many alternatives breaks java switch stmt
   charVocabulary = '\3'..'\377';
   // charVocabulary = '\3'..'\377' | '\u1000'..'\u1fff';

   k=12;
}

tokens
{
    VOID = "void";
}

{
    /**  
     *  Path to the main source file so that includes can be
     *  relative to it.
     */
    public java.io.File mainSourceFile = null;

    /*
     *  Multiple token stream support for include files.
     */
    public static antlr.TokenStreamSelector selector; // must be assigned externally

    public void uponEOF() throws TokenStreamException, CharStreamException
    {
        popNestedLexer();
        try
        {
            selector.pop(); // return to old lexer/stream
            selector.retry();
        }
        catch (java.util.NoSuchElementException e)
        {
            // No including stream, return EOF
        }
    }

    /**
     *  Substitutions to be made to the JBurg syntax proper.
     */
    public java.util.Map<String,String> jburgSubstitutionText = java.util.Collections.emptyMap();
    /**
     *  Substitutions to be made to the reduction text within a BLOCK token.
     */
    public java.util.Map<String,String> blockSubstitutionText = java.util.Collections.emptyMap();

    /**
     *  Next lexer in the nesting; may be processing an include 
     *  file, or re-lexing a macro substitution.
     */
    private JBurgANTLRLexer nestedLexer = null;

    /**
     *  Previous lexer in the nesting.  Used to signal
     *  end-of-file events for the current lexer.
     */
    private JBurgANTLRLexer parent = null;

    /**
     *  Get the current, i.e., most deeply nested, lexer.
     */
    public JBurgANTLRLexer getCurrent()
    {
        if ( nestedLexer != null )
            return nestedLexer.getCurrent();
        else
            return this;
    }

    /**
     *  Push a macro-processing nestedLexer onto the linked list of nestedLexers.
     */
    public void pushMacroLexer(JBurgANTLRLexer macro_lexer)
    throws antlr.TokenStreamRetryException
    {
        macro_lexer.mainSourceFile = getCurrent().mainSourceFile;
        pushNestedLexer(macro_lexer);
        this.selector.push(macro_lexer);
        this.selector.retry();
    }

    /**
     *  Push a nested lexer onto the linked list of nestedLexers.
     */
    private void pushNestedLexer(JBurgANTLRLexer sub_lexer)
    {
        JBurgANTLRLexer current = getCurrent();
        current.nestedLexer = sub_lexer;
        sub_lexer.parent = current;
    }

    /**
     *  Pop the current nested lexer off the linked list of nestedLexers.
     *  Noop if the current lexer has no parent and is therefore the
     *  main lexer.
     */
    private void popNestedLexer()
    {
        if ( this.parent != null )
        {
            assert(this.parent.nestedLexer == this);
            this.parent.nestedLexer = null;
        }
    }

}  //  end inclass definitions

WS :    (' '
   |    '\t'
   |    '\n'    {newline();}
   |    '\r')
    { _ttype = Token.SKIP; }
   ;

//  Whitespace limited to a single line.  Used to process include directives.
protected
DIRECTIVE_WS :    (' ' | '\t' | "\\\n" | "\\\r" | "\\\r\n" )* { _ttype = Token.SKIP; } ;

ARROW:  "->";
COLON:  ':';
COMMA:  ',';
EQUALS: '=';
LANGLE: '<';
LPAREN: '(';
PERIOD: '.';
PLUS:   '+';
RANGLE: '>';
RPAREN: ')';
SEMI:   ';';
SHARP:  '#';
STAR:   '*';

protected LBRACE: '{';
protected RBRACE: '}';

// Single-line comments
COMMENT
  : "//" (~('\n'|'\r'))* { $setType(Token.SKIP);}
  ;   


// multiple-line comments
ML_COMMENT
  : "/*"
    ( { LA(2)!='/' }? '*'
    | ( ("\r\n") => "\r\n" |'\n') { newline(); }
    | ~('*'|'\n'|'\r')
    )*
    "*/"
    { $setType(Token.SKIP); }
  ;   
  
protected
DIGIT
   :    '0'..'9'
   ;

INT :   ('-')? (DIGIT)+
   ;

protected DIRECTIVE_STRING
    : '"'! ( '\\' . | ~('\\'|'"') )* '"'!
    //  Hokey: re-sync syntax coloring "
    ;

protected KEYWORD: "@";

// Brute-force case insensitive keywords.
protected A:    ("A"|"a");
protected B:    ("B"|"b");
protected C:    ("C"|"c");
protected D:    ("D"|"d");
protected E:    ("E"|"e");
protected F:    ("F"|"f");
protected G:    ("G"|"g");
protected H:    ("H"|"h");
protected I:    ("I"|"i");
protected J:    ("J"|"j");
protected K:    ("K"|"k");
protected L:    ("L"|"l");
protected M:    ("M"|"m");
protected N:    ("N"|"n");
protected O:    ("O"|"o");
protected P:    ("P"|"p");
protected Q:    ("Q"|"q");
protected R:    ("R"|"r");
protected S:    ("S"|"s");
protected T:    ("T"|"t");
protected U:    ("U"|"u");
protected V:    ("V"|"v");
protected W:    ("W"|"w");
protected X:    ("X"|"x");
protected Y:    ("Y"|"y");
protected Z:    ("Z"|"z");

ALLOCATOR:              KEYWORD A L L O C A T O R;
ANNOTATION_EXTENDS:     KEYWORD A N N O T A T I O N E X T E N D S;
BURM_PROPERTY:          KEYWORD P R O P E R T Y;
DEFAULT_ERROR_HANDLER:  KEYWORD E R R O R H A N D L E R;
EXPLICIT_REDUCTION:     KEYWORD R E D U C T I O N;
EXTENDS:                KEYWORD E X T E N D S ;
GENERATE_INTERFACE:     KEYWORD G E N E R A T E I N T E R F A C E;
GET_ANNOTATION:         KEYWORD G E T A N N O T A T I O N;
GET_CHILD:              KEYWORD G E T I N O D E C H I L D;
GET_COUNT:              KEYWORD G E T I N O D E C O U N T;
GET_OPERATOR:           KEYWORD G E T I N O D E O P E R A T O R;
HEADER:                 KEYWORD H E A D E R;
IMPLEMENTS:             KEYWORD I M P L E M E N T S;
INCLASS_DECLARATION:    KEYWORD M E M B E R S;
INCLUDE :               KEYWORD I N C L U D E;
INIT_STATIC_ANNOTATION: KEYWORD I N I T A L I Z E S T A T I C A N N O T A T I O N;
INODE_ADAPTER :         KEYWORD I N O D E A D A P T E R;
INODE_TYPE:             KEYWORD I N O D E T Y P E;
LANGUAGE:               KEYWORD L A N G U A G E;
MANIFEST_CONSTANT :     KEYWORD C O N S T A N T;
NODE_TYPE :             KEYWORD N O D E T Y P E;
NON_TERMINAL_ENUM:      KEYWORD N O N T E R M I N A L T Y P E;
OPCODE_TYPE:            KEYWORD O P C O D E T Y P E;
OPTION_TOK:             KEYWORD O P T I O N;
PACKAGE:                KEYWORD P A C K A G E;
PATTERN:                KEYWORD P A T T E R N;
PROLOGUE:               KEYWORD P R O L O G U E;
REDUCER_NODE_NAME:      KEYWORD R E D U C E R N O D E N A M E;
RETURN_TYPE:            KEYWORD R E T U R N T Y P E;
SET_ANNOTATION:         KEYWORD S E T A N N O T A T I O N;
STRICT_TYPE:            KEYWORD S T R I C T R E T U R N T Y P E;
VOLATILE_COST:          KEYWORD V O L A T I L E C O S T F U N C T I O N S;
WILDCARD_STATE:         KEYWORD W I L D C A R D S T A T E;

protected INCLUDE_PARAMETER[java.util.Map<String,String> params] :  lhs:IDENTIFIER  EQUALS rhs:DIRECTIVE_STRING
{
    params.put(lhs.getText(),rhs.getText());
}
;
protected INCLUDE_BLOCK_PARAM[java.util.Map<String,String> params]: LBRACE DIRECTIVE_WS lhs:DIRECTIVE_STRING EQUALS rhs:DIRECTIVE_STRING DIRECTIVE_WS RBRACE
{
    params.put(lhs.getText(),rhs.getText());
};

JBURG_DIRECTIVE
{
    java.util.Map<String,String> jburg_subs = new java.util.HashMap<String,String>();
    java.util.Map<String,String> block_subs = new java.util.HashMap<String,String>();
}
    :
    INCLUDE DIRECTIVE_WS fileName:DIRECTIVE_STRING 
    (
        DIRECTIVE_WS 
        (
            jburg_param:INCLUDE_PARAMETER[jburg_subs]
            |
            block_param:INCLUDE_BLOCK_PARAM[block_subs]
        )
    )*
    {
        String includeFileName = fileName.getText();
        java.io.File includeFile = null;

        try
        {
            //  Try to open the include file relative to the main source file.
            includeFile = new java.io.File(mainSourceFile.getParentFile(), includeFileName);

            if ( !includeFile.exists() )
            {
                //  Use the include file name as it was given; if it's really
                //  not found, ANTLR processing will throw a FileNotFoundException.
                includeFile = new java.io.File(includeFileName);
            }

            JBurgANTLRLexer sublexer = new JBurgANTLRLexer(new java.io.DataInputStream(new java.io.FileInputStream(includeFile)));
            pushNestedLexer(sublexer);
            sublexer.mainSourceFile = includeFile;
            sublexer.setFilename(includeFile.getName());
            sublexer.jburgSubstitutionText = jburg_subs;
            sublexer.blockSubstitutionText = block_subs;

            selector.push(sublexer);
            selector.retry();
        }
        catch ( java.io.FileNotFoundException no_file )
        {
            System.err.println("Unable to include " + includeFile + " : " + no_file.getMessage());
            //  Without a retry() call this will cascade into a parse error.
        }
    }
    ;

IDENTIFIER
options {
   testLiterals = true;
   paraphrase = "an identifier";
}
   :    ('a'..'z' | 'A'..'Z' | "_") ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
   ;


BLOCK
options
{
   paraphrase = "a block of code";
}
   :   '{'  
           ( BLOCK 
           | ( ("\r\n") => "\r\n" |'\n') { newline(); }
           | ~( '{' | '}'|'\n'|'\r')
           )* 
       '}'
   ;   

