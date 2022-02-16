package jburg.burg;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import jburg.burg.options.*;

public class Options
{
    @Optional
    public boolean debug       = false;

    @Optional
    public boolean syntaxCheckOnly = false;

    @Optional
    public boolean logInfo = true;

    @Optional
    public boolean logWarning = true;

    @Optional
    public boolean logError = true;

    @DefaultOption
    @Required(diagnostic="Specify a grammar file.")
    @Paraphrase("<grammar file>")
    public String inputFile  = null;

    @Paraphrase("<input directory>")
    public String inputDir   = null;

    @Required(diagnostic="Specify an output file.")
    @Paraphrase("<output file>")
    public String outputFile = null;

    @Paraphrase("<output directory>")
    public String outputDir  = null;

    @Internal
    public boolean dumpParseTree   = false;

    @Optional
    public boolean help = false;

    @Optional
    @Paraphrase("name of the generated class")
    public String className = null;

    @Paraphrase("interface file name")
    public String interfaceFile = null;

    @Paraphrase("Annotation interface name")
    public String annotationInterfaceName = "JBurgAnnotation";

    /** Generate code to abort the BURM on error
     *  instead of throwing an exception.
     */
    @Optional
    public boolean neverThrow = false;

    public void validate(OptionParser optionParser)
    {
        validateGrammarFile(optionParser);
    }

    public void validateGrammarFile(OptionParser optionParser)
    {
        if (!asPath(inputDir, inputFile).canRead()) {
            optionParser.error("Unable to read grammar file %s%s%s",
                inputDir != null? inputDir : "",
                inputDir != null? File.separator: "",
                inputFile
            );
        }
    }

    /**
     * Construct a path from a file name and optional directory name.
     * @param dirName the directory name.  May be null
     * @param fileName the file name.
     * @return a path made from the directory and file name.
     */
    private File asPath(String dirName, String fileName)
    {
        return new File(dirName, fileName);
    }
}
