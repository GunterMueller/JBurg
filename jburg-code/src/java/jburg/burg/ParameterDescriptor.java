package jburg.burg;

/**
 *  A ParameterDescriptor is a pair (name, subgoalState) which parallels
 *  the FOO(name, subgoalState) found in the specification's syntax.
 */
class ParameterDescriptor
{
    String paramName;
    String paramState;
    ArityType arityType;
    
    enum ArityType {Fixed, Variable}

    ParameterDescriptor(String paramName, String paramState, ArityType arityType)
    {
        this.paramName = paramName;
        this.paramState = paramState;
        this.arityType = arityType;
    }
}
