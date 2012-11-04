import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;

public class BoxOfficeApp extends JFrame implements WindowListener {
    public final int VERSION_MAJOR = 0;
    public final int VERSION_MINOR = 7;
    public final int VERSION_REVISION = 40;

    private String today;

    private SessionData sd;
    private boolean online;

    private ImageIcon artistReportImage;
    
    private MessageLogger msglog;
    private TransactionLogger txnlog;
    
    private BoxOffice bo;


    // ---------------------------------------------------------------------------------------------
    // Required by WindowListener
    
    public void windowOpened( WindowEvent e ) {
        // NOP
    }

    public void windowClosing( WindowEvent e ) {
        // NOP
    }

    public void windowClosed( WindowEvent e ) {
        if (bo.numPendingTxns() > 0) {
            if (online) {
                bo.flushTxnQueue();
            }
        }

        txnlog.close();
        
        // Check again before resetting the log, because we may have
        //  gone offline somewhere in the middle of that and not been able
        //  to flush them all.
        if (bo.numPendingTxns() == 0) {
            txnlog.reset();
        }
        
        saveState();

        msglog.close();
    }

    public void windowIconified( WindowEvent e ) {
        // NOP
    }

    public void windowDeiconified( WindowEvent e ) {
        // NOP
    }

    public void windowActivated( WindowEvent e ) {
        // NOP
    }

    public void windowDeactivated( WindowEvent e ) {
        // NOP
    }


    // ---------------------------------------------------------------------------------------------
    // BoxOfficeApp User Space
    
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( getPreferredLookAndFeel() );
        } catch (Exception e) {
            // NOP - this isn't worth chasing down; just use whatever we get and live with it
        }
        
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    BoxOfficeApp b = new BoxOfficeApp( "Calgary Fringe Festival - Box Office" );
                }
            }
        );
    }

    public static String getPreferredLookAndFeel() {
        Vector<String> prefs = new Vector<String>();
        
        // In order of preference, from most to least preferred:
        prefs.add( "Windows" );
        prefs.add( "GTK+" );
        prefs.add( "Macintosh" );
        prefs.add( "Nimbus" );
        prefs.add( "Metal" );
        
        String bestFound = "";

        for (UIManager.LookAndFeelInfo lfi : UIManager.getInstalledLookAndFeels()) {
            int i = prefs.indexOf( lfi.getName() );
            if (i >= 0) {
                bestFound = lfi.getClassName();
                if (i == 0) {
                    break;
                }
                prefs.setSize( i );
            }
        }
        
        if (bestFound.equals( "" )) {
            bestFound = UIManager.getSystemLookAndFeelClassName();            
        }
        
        return bestFound;
    }
    
    private boolean saveState() {
        String filename = "save_" + today + ".ser";
        ObjectOutputStream outstr;
        boolean written = false;
        
        if (bo == null) {
            return false;
        }
        
        bo.stopAllTimers();
        bo.putSessionData( sd );
 
        try {
            outstr = new ObjectOutputStream( new FileOutputStream( filename ) );
            outstr.writeObject( bo );
            outstr.close();

            written = true;
        } catch (IOException x) {
            msglog.printStackTrace( x );
        }

        return written;
    }
    
    private BoxOffice loadState() {
        String filename = "save_" + today + ".ser";
        ObjectInputStream instr;
        BoxOffice bo = null;

        if ((new File( filename )).isFile()) {
            try {
                instr = new ObjectInputStream( new FileInputStream( filename ) );
                bo = (BoxOffice) (instr.readObject());
                instr.close();
            } catch (IOException x) {
                msglog.printStackTrace( x );
            } catch (ClassNotFoundException x) {
                msglog.printStackTrace( x );
            }
        }
        
        this.sd = bo.getSessionData();
        
        return bo;
    }

    public BoxOfficeApp( String caption ) {
        super( caption );
        
        msglog = new MessageLogger( MessageLogger.DEBUG_LOGFILE );

        sd = new SessionData();

        String curvsn = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_REVISION;
        msglog.println( "Box Office app version: " + curvsn );

        // Note the following sets the online status as a side effect!
        String reqvsn = checkRequestedVersion();

        if (reqvsn != null) {
            String msg = "<HTML>" +
                         "Obsolete Box Office app version detected: " + curvsn + "<BR />" +
                         "Please upgrade to latest version: " + reqvsn + "<BR /><BR />" +
                         "(Latest can be obtained from <A HREF=\"http://boxoffice.calgaryfringe.ca/download\">http://boxoffice.calgaryfringe.ca/download</A>.)" +
                         "</HTML>";
            JOptionPane.showMessageDialog( null, msg, "Box Office: Version Check Failed", JOptionPane.WARNING_MESSAGE );
            return;
        }
        
        today = (new SimpleDateFormat( "yyyy-MM-dd" )).format( new Date() );

        setContentPane( new JPanel( new BorderLayout() ) );
        Collection<Transaction> txnqueue = setupTxnLogger();
        
        if (online) {
            bo = new BoxOffice( this );
            
            initFrame();

            for (Transaction txn : txnqueue) {
                // TODO: some housekeeping - maybe just empty out both logs here, then enqueue these without the ignore flag?
                bo.enqueueTxn( txn, true );
            }

            setContentPane( bo );

            LoginDialog dlg = new LoginDialog( bo );
            dlg.setVisible( true );
        } else {
            // Load up from the serialized & saved version

            bo = loadState();
            if (bo == null) {
                String msg = "<HTML>" +
                             "Cannot connect to internet to download show data, and there is no saved program state from today to restore.<BR /><BR />" +
                             "Please re-establish internet connection and try again." +
                             "</HTML>";
                JOptionPane.showMessageDialog( null, msg, "Box Office: Cannot Load Data", JOptionPane.ERROR_MESSAGE );
            } else {
                initFrame();

                bo.setApp( this );
                bo.showConnectionStatus();
                setContentPane( bo );
            }
        }
    }

    private Collection<Transaction> setupTxnLogger() {
        // First, load any local unsent transactions, so as to try them again later
        Collection<Transaction> txnqueue = TransactionLogger.loadIncomplete( msglog );       
        
        try {
            txnlog = new TransactionLogger();
        } catch (IOException x) {
            // Oh God. I think we just... die? Until I have a better idea.
            x.printStackTrace();
            System.exit( 1 );
        }
        
        return txnqueue;
    }

    private String checkRequestedVersion() {
        int major = 0;
        int minor = 0;
        int revision = 0;
                
        Transaction txn = new Transaction( "checkversion" );
        String resp = makeGatewayRequest( txn, "Could Not Contact Gateway" );

        try {
            String[] required = resp.trim().split( "\\." );

            major = Integer.parseInt( required[ 0 ] );
            minor = Integer.parseInt( required[ 1 ] );
            revision = Integer.parseInt( required[ 2 ] );
        } catch (Exception x) {
            // NOP - just use defaults of 0
        }

        return checkRequestedVersion( major, minor, revision );
    }
    
    private String checkRequestedVersion( int major, int minor, int revision ) {
        boolean versionok = true;
        
        msglog.println( "Requested Version: " + major + "." + minor + "." + revision );

        if (VERSION_MAJOR < major) {
            versionok = false;
        } else if (VERSION_MAJOR == major) {
            if (VERSION_MINOR < minor) {
                versionok = false;
            } else if (VERSION_MINOR == minor) {
                if (VERSION_REVISION < revision) {
                    versionok = false;
                }
            }
        }
        
        if (versionok) {
            return null;
        }
        
        return major + "." + minor + "." + revision;
    }

    public String makeGatewayRequest( Transaction txn, String errorTitleMsg ) {
        String resp = null;

        HttpURLConnection cxn = null;
        try {
            String paramStr = txn.getParamStr( sd );

            msglog.println( "{" + paramStr + "}" );

            cxn = (HttpURLConnection) ((new URL( sd.gateway )).openConnection());
            cxn.setConnectTimeout( 15000 );
            cxn.setRequestMethod( "POST" );
            cxn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            cxn.setRequestProperty( "Content-Length", Integer.toString( paramStr.getBytes().length ) );
            cxn.setRequestProperty( "Content-Language", "en-US" );
            cxn.setUseCaches( false );
            cxn.setDoInput( true );
            cxn.setDoOutput( true );

            DataOutputStream wr = new DataOutputStream( cxn.getOutputStream() );
            wr.writeBytes( paramStr );
            wr.flush();
            wr.close();

            BufferedReader rd = new BufferedReader( new InputStreamReader( cxn.getInputStream() ) );
            String instr;
            StringBuffer sb = new StringBuffer();
            while ((instr = rd.readLine()) != null) {
                sb.append( instr );
                sb.append( "\r\n" );
            }

            rd.close();
            resp = sb.toString();

            msglog.println( resp );

            if (resp.startsWith( "fail" )) {
                String msg = (resp.length() > 7 ? resp.substring( 7 ).trim() :
                              "An unknown error occurred trying to save purchase information.") + "\n\n" +
                              "If you continue to receive this error, please call Sean or Blair.";
                JOptionPane.showMessageDialog( null, msg, errorTitleMsg, JOptionPane.WARNING_MESSAGE );

                resp = null;
            } else if (resp.equals( "success" )) {
                // NOP - just don't truncate if there's nothing else
            } else if (resp.startsWith( "success" )) {
                resp = resp.substring( 8 );
            }
        } catch (ConnectException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (NoRouteToHostException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (HttpRetryException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (ProtocolException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (SocketException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (SocketTimeoutException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (UnknownHostException x) {
            msglog.printStackTrace( x );
            resp = "network error";
        } catch (Exception x) {
            msglog.println( "general error:" );
            msglog.printStackTrace( x );
            resp = null;
        }

        if (cxn != null) {
            cxn.disconnect();
        }
        
        setOnlineStatus( ! resp.equals( "network error" ) );

        return resp;
    }
    
    public void setSessionData( String username, String sessionID, boolean online ) {
        sd.username = username;
        sd.sessionID = sessionID;
        this.online = online;
    }
    
    private void setOnlineStatus( boolean online ) {
        if (online != this.online) {
            this.online = online;

            if (this.online) {
                if (bo != null) {
                    bo.flushTxnQueue();
                }
            }
        }
    }
    
    public boolean isOnline() {
        return online;
    }

    private void initFrame() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Toolkit tk = Toolkit.getDefaultToolkit();

        Insets i = tk.getScreenInsets( gc );
        Dimension d = tk.getScreenSize();

        int w = Math.min( 1600, d.width - (i.left + i.right) );
        int h = Math.min( 900, d.height - (i.top + i.bottom) );
        setSize( w, h );

        artistReportImage = new ImageIcon( "images/boxofficeform.png", "Artist Reconciliation Report" );

        addWindowListener( this );
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        setIconImage( new ImageIcon( "images/fringe_logo_64x64.png" ).getImage() );

        setMaximizedBounds( ge.getMaximumWindowBounds() );
        setVisible( true );
        setExtendedState( getExtendedState() | JFrame.MAXIMIZED_BOTH );
    }

    public ImageIcon getLocalImage( String imageName ) {
        if (imageName.equals( "Artist Report" )) {
            return artistReportImage;
        }
        
        return null;
    }

    public MessageLogger msgLog() {
        return msglog;
    }
    
    public TransactionLogger txnLog() {
        return txnlog;
    }
}
