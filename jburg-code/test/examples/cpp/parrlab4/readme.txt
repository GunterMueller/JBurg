This is a simple example to illustrate the use JBurg in C++ mode,

This is based on Terence Parr's "Lab 4: Code Generation Using Bottom Up Rewrite System"
(URL:  http://www.cs.usfca.edu/~parrt/course/652/labs/jburg.html ),  although rewritten
in C++.

Things to note:
 - The variables from the stack in the actions are pointers (actually, auto_ptr to ensure
their destruction as the function exits
 - The action methods are expected to return a pointer to an object

