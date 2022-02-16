" Vim syntax file
" Language:	JBurg, a code generator generator
" Maintainer:	Tom Harwood <tharwood@users.sourceforge.net>
" URL:		http://jburg.cvs.sourceforge.net/jburg/jburg/extras/jbg.vim
" Last Change:	2010 Oct 17

" Uses java.vim, and adds a few special things for JBurg specification files.
" Those files usually have the extension  *.jbg
" Based on the JavaCC syntax file by Claudio Fleiner.

" For version 5.x: Clear all syntax items
" For version 6.x: Quit when a syntax file was already loaded
if version < 600
  syntax clear
elseif exists("b:current_syntax")
  finish
endif

" source the java.vim file
if version < 600
  source <sfile>:p:h/java.vim
else
  runtime! syntax/java.vim
endif
unlet b:current_syntax

"remove catching errors caused by wrong parenthesis (does not work in javacc
"files) (first define them in case they have not been defined in java)
syn match	javaParen "--"
syn match	javaParenError "--"
syn match	javaInParen "--"
syn match	javaError2 "--"
syn clear	javaParen
syn clear	javaParenError
syn clear	javaInParen
syn clear	javaError2

syn match JBurgPattern "[$_a-zA-Z][$_a-zA-Z0-9_. \[\]]*([^-+*/()]*)[ \t]*:" contains=javaType
syn match JBurgPattern "Pattern\s*[$_a-zA-Z]\+\s*:"  contains=javaType

syn keyword JBurgToken ReturnType INodeType BURMProperty Prologue
syn keyword JBurgToken JBurg.include

" Define the default highlighting.
" For version 5.7 and earlier: only when not done already
" For version 5.8 and later: only when an item doesn't have highlighting yet
if version >= 508 || !exists("did_css_syn_inits")
  if version < 508
    let did_css_syn_inits = 1
    command -nargs=+ HiLink hi link <args>
  else
    command -nargs=+ HiLink hi def link <args>
  endif
  HiLink JBurgToken javaScopeDecl
  HiLink JBurgPattern javaFuncDef
  delcommand HiLink
endif

let b:current_syntax = "jburg"

" vim: ts=8
