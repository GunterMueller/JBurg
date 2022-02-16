#include <vector>
#include <string>
#include <sstream>

#define SSTR( x ) dynamic_cast< std::ostringstream & >( \
            (  << std::dec << x ) ).str()

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
        if (text.size()) {
            return text;
        } else {
            std::ostringstream result;
            result << getOperator();
            return result.str();
        }
    }
};

#include "WildcardCppEmitter.h"

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

    WildcardCppEmitter burm;
    burm.burm(&root);
    long result = burm.getlongResult();
    if (result == 47) {
        printf("succeeded: C++ fixed-arity wildcard\n");
        return 0;
    } else {
        printf("C++ fixed-arity wildcard FAILED: expected 47, actual %ld\n", result);
        return 2;
    }
}
