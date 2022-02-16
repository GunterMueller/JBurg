" Vim syntax file
" Language:	JBurg, a code generator generator
" Maintainer:	Tom Harwood <tharwood@users.sourceforge.net>
" URL:		http://jburg.cvs.sourceforge.net/jburg/jburg/extras/jbg.vim
" Last Change:	2015 March 26

" Uses java.vim, and adds a few special things for JBurg2 specification files.
" Those files usually have the extension  *.jbg

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

" Allow C++ constructs: this would be better done
" in the filetype entry discriminating on language.
let java_allow_cpp_keywords = 1
syn match javaOK "[\\@`]"
syn match javaOK "<<<\|\.\.\|=>\|||=\|&&=\|[^-]->\|\*\/"

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

syn match JBurgToken "@\(A\|a\)llocator"
syn match JBurgToken "@\(A\|a\)nnotation\(E\|e\)xtends"
syn match JBurgToken "@\(P\|p\)roperty"
syn match JBurgToken "@\(E\|e\)rror\(H\|h\)andler"
syn match JBurgToken "@\(R\|r\)eduction"
syn match JBurgToken "@\(E\|e\)xtends"
syn match JBurgToken "@\(G\|g\)enerate\(I\|i\)nterface"
syn match JBurgToken "@\(G\|g\)et\(A\|a\)nnotation"
syn match JBurgToken "@\(G\|g\)et\(I\|i\)nodechild"
syn match JBurgToken "@\(G\|g\)et\(I\|i\)nodecount"
syn match JBurgToken "@\(G\|g\)et\(I\|i\)node\(O\|o\)perator"
syn match JBurgToken "@\(G\|g\)uard"
syn match JBurgToken "@\(H\|h\)eader"
syn match JBurgToken "@\(I\|i\)mplements"
syn match JBurgToken "@\(M\|m\)embers"
syn match JBurgToken "@\(I\|i\)nclude"
syn match JBurgToken "@\(I\|i\)nitalize\(S\|s\)tatic\(A\|a\)nnotation"
syn match JBurgToken "@\(I\|i\)node\(A\|a\)dapter"
syn match JBurgToken "@\(I\|i\)node\(T\|t\)ype"
syn match JBurgToken "@\(L\|l\)anguage"
syn match JBurgToken "@\(C\|c\)onstant"
syn match JBurgToken "@\(N\|n\)ode\(T\|t\)ype"
syn match JBurgToken "@\(N\|n\)onterminal\(T\|t\)ype"
syn match JBurgToken "@\(O\|o\)pcode\(T\|t\)ype"
syn match JBurgToken "@\(O\|o\)ption"
syn match JBurgToken "@\(P\|p\)ackage"
syn match JBurgToken "@\(P\|p\)attern"
syn match JBurgToken "@\(P\|p\)rologue"
syn match JBurgToken "@\(R\|r\)eturn\(T\|t\)ype"
syn match JBurgToken "@\(S\|s\)etannotation"
syn match JBurgToken "@\(S\|s\)trict\(R\|r\)eturn\(T\|t\)ype"
syn match JBurgToken "@\(V\|v\)olatile\(C\|c\)ost\(F\|f\)unctions"
syn match JBurgToken "@\(W\|w\)ildcard\(S\s\)tate"

syn match JBurgPattern "@\(P\|p\)attern\s*[$_a-zA-Z][$_a-zA-Z0-9_]*\s*[a-zA-Z][a-zA-Z0-9_]*\s*\(.*\)\s*;"  contains=javaType
syn match JBurgPattern "^[$_a-zA-Z][$_a-zA-Z0-9_]*\s*=\s*@\(P\|p\)attern\s*[$_a-zA-Z][$_a-zA-Z0-9_]*\s*\(.*\)\s*:"  contains=javaType
syn match JBurgPattern "^[$_a-zA-Z][$_a-zA-Z0-9_]*\s*=\s*[a-zA-Z][a-zA-Z0-9_]*\s*\(.*\)\s*:" contains=javaType
syn match JBurgReference "#[$_a-zA-Z][$_a-zA-Z0-9_]*"

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
  HiLink JBurgToken StorageClass
  HiLink JBurgReference StorageClass
  HiLink JBurgPattern Special
  delcommand HiLink
endif

let b:current_syntax = "jbg"
