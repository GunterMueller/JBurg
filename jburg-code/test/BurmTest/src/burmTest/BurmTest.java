package burmTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *  BurmTest reads a simple tree description language,
 *  builds a test tree, and invokes the specified BURM
 *  to rewrite the tree.
 */
public class BurmTest extends org.xml.sax.helpers.DefaultHandler
{
    Stack<TestINode>  nodeStack = new Stack<TestINode>();

    Map<String,Integer> opcodes = new HashMap<String,Integer>();

    TestINode rootNode;

    Object burm;

    String expectedResult = null;
    String expectedError  = null;
    String timeTest = null;

    static final int INVALID_COMMANDLINE = 100;
    static final int FAILED_NO_ERROR = INVALID_COMMANDLINE + 1;
    static final int FAILED_WRONG_OUTPUT = FAILED_NO_ERROR + 1;

    static final int FAILED_UNEXPECTED_EXCEPTION = 200;
    static final int FAILED_WRONG_ERROR = FAILED_UNEXPECTED_EXCEPTION + 1;

    public static void main(String[] args)
    throws Exception
    {
        if ( args.length < 1 )
        {
            usage();
            System.exit(INVALID_COMMANDLINE);
        }

        String test_file = args[0];

        BurmTest testobj = new BurmTest();
        testobj.parse(test_file);

        try
        {
            String burm_result = testobj.burm().toString();

            if ( null == testobj.expectedResult )
            {
                System.out.println(burm_result);
                if ( testobj.expectedError != null )
                {
                    System.err.println("FAILED: " + test_file + " Expected error " + testobj.expectedError );
                    System.exit(FAILED_NO_ERROR);
                }
            }
            else if ( testobj.expectedResult.equals(burm_result ) )
            {
                //  Printing to System.err so that the message 
                //  shows up when ant is in -quiet mode.
                System.err.println("Succeeded: " + test_file);
            }
            else
            {
                System.err.println(String.format("FAILED: " + test_file + " expected %s, actual %s", testobj.expectedResult, burm_result));
                System.exit(FAILED_WRONG_OUTPUT);
            }
        }
        catch ( Exception burm_failure )
        {
            if ( null == testobj.expectedError )
            {
                System.err.println("FAILED: " + test_file + " unexpected exception:");
                burm_failure.printStackTrace();
                System.exit(FAILED_UNEXPECTED_EXCEPTION);
            }
            else if ( burm_failure.getMessage().equals(testobj.expectedError) )
                System.out.println("Succeeded: " + test_file + " caught expected exception " + burm_failure.getMessage());
            else
            {
                System.out.println("FAILED: " + test_file + " expected error " + testobj.expectedError + ", actual " + burm_failure.getMessage());
                System.exit(FAILED_WRONG_ERROR);
            }
        }

    }

    static void usage()
    {
        System.out.println("usage: BurmTest <test spec file>");
    }

    /**
     *  Parse the test specification.
     */
    void parse(String spec_file)
    throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
    
        SAXParser parser = factory.newSAXParser();
        java.io.InputStream input = new java.io.FileInputStream(spec_file);
        
        parser.parse(input, this);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        if ( "BurmTest".equalsIgnoreCase(qName) )
        {
            this.expectedResult = attributes.getValue("expectedResult");
            this.timeTest = attributes.getValue("timeTest");
            String burm_class_name = attributes.getValue("burm");
            try
            {
                Class burm_class = Class.forName(burm_class_name);
                this.burm = burm_class.newInstance();
            }
            catch ( Exception no_burm )
            {
                System.err.println("Unable to instantiate " + burm_class_name + " due to " + no_burm);
            }
        }
        else if ( "Opcodes".equalsIgnoreCase(qName) )
        {
            loadOpcodes(attributes.getValue("class"));
        }
        else if ( "Node".equalsIgnoreCase(qName) )
        {
            int opcode = opcodes.get(attributes.getValue("opcode"));
            String userObject = attributes.getValue("userObject");

            TestINode node;

            if ( attributes.getIndex("nodeClass") != -1 )
            {
                try
                {
                    node = (TestINode)
                        Class.forName(attributes.getValue("nodeClass"))
                                .getDeclaredConstructor(Integer.class, String.class)
                                    .newInstance(new Integer(opcode), userObject);
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    System.exit(1);
                    node = null; // compiler appeasement
                }
            }
            else
            {
                node = new TestINode(opcode, userObject);
            }

            if ( !nodeStack.isEmpty() )
                nodeStack.peek().addChild(node);

            nodeStack.push(node);
        }
        else if ( 
            "grammar".equalsIgnoreCase(qName) ||
            "javaSupportFiles".equalsIgnoreCase(qName)
            )
        {
            //  build-time element, ignore.
        }
        else
            throw new IllegalArgumentException("Unknown node type " + qName);
    }

    @Override
    public void endElement(String uri,  String localName,  String qName)
    {
        if ( "Node".equalsIgnoreCase(qName) )
            this.rootNode = nodeStack.pop();
    }

    /**
     *  Rewrite the test tree and get the result.
     *  @return the result of the tree rewrite.
     */
    @SuppressWarnings("unchecked")
    private Object burm()
    throws Exception
    {
        Class burm_class = this.burm.getClass();
        try
        {
            //  The burm doesn't implement a known class,
            //  but it has known methods to rewrite the
            //  input tree and return a result.
            Method burm_method = burm_class.getDeclaredMethod( "burm", TestINode.class);

            long startTime = System.nanoTime();
                
            burm_method.invoke(burm, this.rootNode);

            if ( "true".equals(this.timeTest) )
                System.out.printf("%1.2f ms.\n", (System.nanoTime() - startTime) / 1000000.0);

            try
            {
                Method result_method = burm_class.getDeclaredMethod( "getResult");
                return result_method.invoke(burm);
            }
            catch ( Exception ex )
            {
                System.err.println("Error getting burm result:");
                ex.printStackTrace();
                System.exit(2);
            }
         }
         catch ( Exception ex )
         {
            java.io.PrintWriter dumper;

            dumper = new java.io.PrintWriter(new java.io.FileWriter("failedBurm.xml"));
			dumper.println ( "<?xml version=\"1.0\"?>");
			dumper.println("<BurmDump date=\"" + new java.util.Date().toString() + "\">");

            try
            {
                Method dump_method = burm_class.getDeclaredMethod("dump", dumper.getClass());
                dump_method.invoke(burm, dumper);
            }
            catch ( NoSuchMethodException doesntWantDump )
            {
                //  This will be handled by the throw.
            }
            catch ( Throwable no_dump )
            {
                System.err.println("Unable to dump due to:");
                no_dump.printStackTrace();
            }

			dumper.println("<AST>");
			dumper.println(this.rootNode.toString());
			dumper.println("</AST>");

			dumper.println("</BurmDump>");
			dumper.flush();
			dumper.close();
            
            throw ex;
         }
         return null;
    }

    /**
     *  Load the symbolic name to opcode encodings.
     */
    private void loadOpcodes(String class_name)
    {
        try
        {
            Class<? extends Object> tokenTypes = Class.forName(class_name);
            //  Traverse the names of the OP_foo constants
            //  in AbcConstants and load their values.
            for ( Field f: tokenTypes.getFields())
            {
                String field_name = f.getName();
               try
               {
                   int field_value = f.getInt(null);
                   opcodes.put(field_name, field_value);
               }
               catch ( Exception noFieldValue)
               {
                   //  Ignore, continue...
               }
            }
        }
        catch ( Throwable no_class)
        {
            System.err.println("Unable to load " + class_name + ": " + no_class.getLocalizedMessage());
            no_class.printStackTrace();
        }
        
    }
}
