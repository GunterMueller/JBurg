public interface PerformanceOpcodes
{
    public static int ADD = 1;
    public static int INT = ADD + 1;
    public static int PAREN = INT + 1;
    public static int IDENTIFIER = PAREN + 1;
    public static int ASSIGN = IDENTIFIER + 1;
    public static int PRINT = ASSIGN + 1;
    public static int BLOCK = PRINT + 1;
    public static int MULTIPLY = BLOCK + 1;
}
