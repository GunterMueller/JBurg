#include <stdio.h>
#include <vector>
#include <string>

#include "AccessorAndAllTest.h"

#define POSTCONDITION(x) if (!(x)) { printf("failed postcondition: %s\n", #x); exit(1); }
int main()
{
    INode constant(Constant);
    constant.setIntValue(12);
    AccessorAndAllTest burm;
    JBurgAnnotation<INode*>* annotation = burm.label(&constant);
    POSTCONDITION(annotation->getCost(AccessorAndAllTest::__constant_NT) < 1000);
    burm.reduce(&constant,AccessorAndAllTest::__constant_NT);
    Value* value = burm.getValuePtrResult();
    POSTCONDITION(value->intValue == 12);
    return 0;
}
