/*
 * Main routine for the javascript MAT test
 */
print("starting test");

var ADD = 1;
var INT = ADD+1;

/* Define the Node class.  */
function Node(opcode) {
    this.opcode = opcode;
    this.children = [];
}

Node.prototype.getArity = function()       { return this.children.length; }
Node.prototype.getNthChild  = function(i)  { return this.children[i]; }
Node.prototype.addChild  = function(child) { this.children.push(child); }
Node.prototype.getOperator = function()    { return this.opcode; }
Node.prototype.getUserObject = function()  { return this.userObject; }

/* Hook up a simple tree and evaluate. */
var ONE = new Node(INT);
ONE.userObject = 1;
var TWO = new Node(INT);
TWO.userObject = 2;

var root = new Node(ADD);
root.addChild(ONE);
root.addChild(TWO);

print(burm(root));
