import jburg.patternmatcher.ParameterizedPatternMatcher;

import java.io.*;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PPMTest
{
    public static void main(String[] argv)
    throws Exception
    {
        for ( String filespec: argv )
            new TestLoader().parse(filespec);
        System.exit(errCount);
    }

    private static int errCount = 0;

    private void runTest()
    {
        if ( text == null ) throw new IllegalStateException("Not specified: <Text value=\"abacdbab\"/>");
        if ( pattern == null ) throw new IllegalStateException("Not specified: <Pattern value=\"abc\"/>");

        ParameterizedPatternMatcher<Character> matcher = new ParameterizedPatternMatcher<Character>(
            this.text,
            this.pattern,
            this.initialNamingFunction
        );

        List<Integer> matches = matcher.getMatches();

        if ( matches.equals(expectedResults) )
        {
            if ( expectedMatches.equals(matcher.getUnknownSymbolMapping(matches)) )
                System.out.printf("%s succeeded.\n", this.testName);
            else
                failed("expected matched mappings %s, actual %s", expectedMatches, matcher.getUnknownSymbolMapping(matches));
        }
        else if ( expectedResults != null )
        {
            failed("expected match positions %s, actual %s", expectedResults, matches);
        }
        else
            System.out.printf("%s\n\tmatches=%s\nunknown-to-text=%s\n", this, matches, matcher.getUnknownSymbolMapping(matches));
    }

    private void failed(String diagnosticFormat, Object ... args)
    {
        System.out.printf("FAILED: %s\n\t%s.\n", this, String.format(diagnosticFormat, args));
        errCount++;
    }

    public String toString()
    {
        return String.format("%s\n\ttext = %s\n\tpattern = %s\n\tinitialNaming = %s", this.testName, text, pattern, initialNamingFunction);
    }

    List<Character> text;
    List<Character> pattern;
    List<Integer> expectedResults = null;
    List<Map<Character,Character>> expectedMatches = new ArrayList<Map<Character,Character>>();
    Map<Character, Character> initialNamingFunction = new HashMap<Character,Character>();

    String testName;

    private static class TestLoader extends org.xml.sax.ext.DefaultHandler2
    {
        private PPMTest currentTest = null;
        Map<Character,Character> expectedMatches = new HashMap<Character,Character>();

        /**
         * Parse and execute tests contained in the specified file.
         * @param filename the name of an XML file containing test
         * specifications.
         */
        void parse(String filename)
        throws Exception
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
    
            SAXParser parser = factory.newSAXParser();
            InputStream input = new FileInputStream(filename);
            parser.parse(input, this);
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
        {
            if ( "PPMTest".equals(localName) )
            {
            }
            else if ( "Test".equals(localName) )
            {
                if ( currentTest != null )
                    throw new IllegalStateException("<Test> elements may not be nested.");
                currentTest = new PPMTest();
                currentTest.testName = attributes.getValue("name");
            }
            else if ( "Text".equals(localName) )
            {
                currentTest.text = toCharList(attributes.getValue("value"));
            }
            else if ( "Pattern".equals(localName) )
            {
                currentTest.pattern = toCharList(attributes.getValue("value"));
            }
            else if ( "InitialNaming".equals(localName) )
            {
            }
            else if ( "Mapping".equals(localName) )
            {
                Character patSym  = attributes.getValue("pattern").charAt(0);
                Character textSym = attributes.getValue("text").charAt(0);
                currentTest.initialNamingFunction.put(patSym, textSym);
            }
            else if ( "Expected".equals(localName) )
            {
                if (currentTest.expectedResults == null )
                    currentTest.expectedResults = new ArrayList<Integer>();
                currentTest.expectedResults.add(getIntAttribute(attributes, "position"));
            }
            else if ( "Matched".equals(localName) )
            {
                Character patSym  = attributes.getValue("pattern").charAt(0);
                Character textSym = attributes.getValue("text").charAt(0);

                if ( this.expectedMatches == null )
                    this.expectedMatches = new HashMap<Character,Character>();
                this.expectedMatches.put(patSym, textSym);
            }
            else
            {
                throw new IllegalStateException("unknown element " + localName);
            }
        }

        public void endElement(String uri,  String localName,  String qName)
        {
            if ( "Test".equals(localName) )
            {
                this.currentTest.runTest();
                this.currentTest = null;
            }
            else if ( "Expected".equals(localName) )
            {
                currentTest.expectedMatches.add(this.expectedMatches);
                this.expectedMatches = new HashMap<Character,Character>();
            }
        }
        
    }

    /**
     * Fetch an int attribute.
     * @param attributes - current XML tag's attributes.
     * @param attr_name - the desired attribute's name.
     * @return the int value of the attribute.
     * @throws UnsupportedEncodingException
     */
    static int getIntAttribute(Attributes attributes, String attr_name)
    {
        return Integer.parseInt(attributes.getValue(attr_name), 10);
    }

    /**
     * Fetch a boolean attribute.
     * @param attributes - current XML tag's attributes.
     * @param attr_name - the desired attribute's name.
     * @return the int value of the attribute.
     * @throws UnsupportedEncodingException
     */
    static boolean getBooleanAttribute(Attributes attributes, String attr_name)
    {
        return Boolean.parseBoolean(attributes.getValue(attr_name));
    }

    private static List<Character> toCharList(String s)
    {
        List<Character> result = new ArrayList<Character>();
        result.add(null);  // Placeholder to force 1-based counting
        for ( int i = 0; i < s.length(); i++ )
            result.add(s.charAt(i));
        return result;
    }

    private static Map<Character,Character> processNamingFunction(String consolidated)
    {
        Map<Character,Character> result = new HashMap<Character,Character>();

        String[] pairs = consolidated.split(";");

        for ( String s: pairs )
        {
            String[] elements = s.split("=");
            if ( elements.length != 2 )
                throw new IllegalStateException("The initial naming function must be specified as s=s' pairs");

            result.put(elements[0].charAt(0), elements[1].charAt(0));
        }

        return result;
    }
}
