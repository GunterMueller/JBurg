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

#include "JBurgCppTestEmitterDebug.h"

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

    JBurgCppTestEmitterDebug burm;
    burm.burm(&root);
    std::cerr << "Succeeded: C++ debug (check gensrc/cppDebug.xml)\n";
    return 0;
}
