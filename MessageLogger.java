import java.io.*;

public class MessageLogger {
    public static final String DEFAULT_LOGFILE = "boxoffice.log";
    public static final int    DEFAULT_LEVEL   = 0;
    
    public static final int DEBUG_NONE    = 0;
    public static final int DEBUG_LOGFILE = 1;
    public static final int DEBUG_CONSOLE = 2;
    public static final int DEBUG_EMAIL   = 4;  // Not yet implemented
    
    private int debuglevel;
    private String filename;
    private PrintWriter log;
    
    public MessageLogger() {
        this( MessageLogger.DEFAULT_LEVEL );
    }
    
    public MessageLogger( int debuglevel ) {
        this( debuglevel, MessageLogger.DEFAULT_LOGFILE );
    }
    
    public MessageLogger( int debuglevel, String filename ) {
        this.debuglevel = debuglevel;
        
        if ((this.debuglevel & MessageLogger.DEBUG_LOGFILE) != 0) {
            this.filename = filename;
            try {
                log = new PrintWriter( this.filename );
            } catch (IOException x) {
                System.out.println( "Cannot create logfile '" + this.filename + "':\n" +
                                    "Exception: " + x.getMessage() );
                this.debuglevel ^= MessageLogger.DEBUG_LOGFILE;
            }
        }
    }
    
    public void println( String message ) {
        print( message + "\n" );
        
        if ((debuglevel & MessageLogger.DEBUG_LOGFILE) != 0) {
            log.flush();
        }
    }
    
    public void print( String message ) {
        if ((debuglevel & MessageLogger.DEBUG_LOGFILE) != 0) {
            log.write( message );
        }
        
        if ((debuglevel & MessageLogger.DEBUG_CONSOLE) != 0) {
            System.out.print( message );
        }
    }
    
    public void printStackTrace( Throwable t ) {
        if ((debuglevel & MessageLogger.DEBUG_LOGFILE) != 0) {
            t.printStackTrace( log );
            log.flush();
        }
        
        // This should really go SOMEwhere; so in the absence of a destination, send to console anyway
        if (((debuglevel & MessageLogger.DEBUG_CONSOLE) != 0) ||
             (debuglevel == MessageLogger.DEBUG_NONE)) {

            t.printStackTrace();
        }
    }
    
    public void close() {
        if ((debuglevel & MessageLogger.DEBUG_LOGFILE) != 0) {
            log.close();
        }
    }
}
