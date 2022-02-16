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

    void setAnnotation(intptr_t annotation)
    {
        this->annotation = annotation;
    }

    intptr_t getAnnotation()
    {
        return this->annotation;
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

    private:
    intptr_t annotation;

};

#include "WildcardCppAccessorEmitter.h"

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

    WildcardCppAccessorEmitter burm;
    burm.burm(&root);
    long result = burm.getlongResult();
    if (result == 2) {
        printf("succeeded: C++ variable-arity wildcard with accessor\n");
        return 0;
    } else {
        printf("C++ fixed-arity wildcard FAILED: expected 2, actual %ld\n", result);
        return 2;
    }
}
