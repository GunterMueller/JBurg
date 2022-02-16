package jburg.burg;

/**
 *  A simple message logging facility.
 */
public class Logger
{

    protected boolean logInfo;
    protected boolean logWarning;
    protected boolean logError;
    protected boolean logDebug;

    protected int errorCount = 0;

    protected Logger(boolean log_info, boolean log_warning, boolean log_error)
    {
        this.logInfo = log_info;
        this.logWarning = log_warning;
        this.logError = log_error;
    }

    /** Log an informational message. */
    public void info(String format, Object... args )
    {
        if ( logInfo )
            System.out.printf(format, args);
    }

    /** Log a warning. */
    public void warning(String wmsg, Object... args)
    {
        if ( this.logWarning )
            System.err.printf("warning: " + wmsg, args);
    }

    /** Log an error. */
    public void error(String emsg, Object... args)
    {
        if ( this.logError )
            System.err.printf("error: " + emsg, args);
        this.errorCount++;
    }

    /** Log an exception. */
    public void exception(String context, Exception ex)
    {
        if ( this.logError )
        {
            System.err.printf("error: unexpected exception while %s:", context);
            ex.printStackTrace();
        }
        this.errorCount++;
    }

    /** Log an event of debugging interest only. */
    public void debug(String context, Object... args)
    {
        if (this.logDebug) {
            System.out.printf(context, args);
        }
    }
    /**
     * @return the cumulative error count.
     */
    public int getErrorCount()
    {
        return this.errorCount;
    }
}
