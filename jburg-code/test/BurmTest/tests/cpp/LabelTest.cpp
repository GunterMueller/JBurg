#include <vector>
#include <string>

struct ArithmeticTokenHolder
{
    enum ArithmeticTokenTypes
    {
        ADD = 3,
        INT = 5,
    };
};

class TestINode
{
    public:
    ArithmeticTokenHolder::ArithmeticTokenTypes opcode;
    std::vector<TestINode*> children;
    std::string text;

    public:
    int getArity()
    {
       return children.size();
    }

    TestINode* getNthChild(int idx)
    {
        return children[idx];
    }

    int getOperator()
    {
        return (int)this->opcode;
    }

    std::string getText()
    {
        return this->text;
    }

    std::string toString()
    {
        return "TestINode";
    }
};

#include "JBurgCppLabelTest.h"

int main()
{
    TestINode root;
    root.opcode = ArithmeticTokenHolder::ADD;

    TestINode firstChild;
    firstChild.opcode = ArithmeticTokenHolder::INT;
    firstChild.text = "1";

    TestINode secondChild;
    secondChild.opcode = ArithmeticTokenHolder::INT;
    secondChild.text = "2";

    root.children.push_back(&firstChild);
    root.children.push_back(&secondChild);

    JBurgCppLabelTest burm;
    JBurgAnnotation<TestINode*>* annotatedTree = burm.label(&root);

    int costOfExpression    = annotatedTree->getCost(JBurgCppLabelTest::__expression_NT);
    int costOfFoo           = annotatedTree->getCost(JBurgCppLabelTest::__foo_NT);

    if ( costOfExpression == 3 && costOfFoo == MAX_INT_VALUE) {
        std::cerr << "Succeeded: C++ label() analysis.\n";
    } else {
        std::cerr << "C++ label() analysis FAILED: expected cost 3/max, actual " << costOfExpression << " and " << costOfFoo << std::endl;
        return 1;
    }
    return 0;

}
