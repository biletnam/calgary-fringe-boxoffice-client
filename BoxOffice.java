import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class BoxOffice extends JPanel implements ActionListener, MouseListener {
    transient private BoxOfficeApp app;
    
    private SessionData sd;

    private JButton[] btnButtonSale;
    private JButton[] btnConcessionSale;
    private JButton btnDonation;
    private JButton btnVenueReport;
    private JButton btnSuperpassSale;

    // While historically accurate, the name "timCheckConnection" is now misleading.
    //  It does check the connection status, but ALSO handles flushing the
    //  transaction queue so long as the app is online! ¡Muy importante!
    private javax.swing.Timer timCheckConnection;
    private boolean timerRunning;

    private JTabbedPane tpnVenue;
    private JButton btnVenueSelect;
    private OptionPanel pnlVenueSelect;

    public WindowData wd;

    private Vector<Transaction> pendingTxns = new Vector<Transaction>();
    private Vector<Color> tabColors = new Vector<Color>( 36 );
    private Vector<ConcessionItem> citems;
    private Vector<SaleItem> currentSale = new Vector<SaleItem>();

    private JLabel lblOnlineStatus;
    private JLabel lblStatus;
    private SaleStatusPanel pnlCurrentSale;
    private ImageIcon imgOnline;
    private ImageIcon imgOffline;

    private Calendar seniorDay = new GregorianCalendar( 2012, Calendar.AUGUST, 7 );


    // ---------------------------------------------------------------------------------------------
    // Required by ActionListener

    public void actionPerformed( ActionEvent e ) {
        int n = 0;
        String resp = null;
        String caption;
        boolean handled = false;

        if (e.getSource() == btnVenueSelect) {
            initWindowData( pnlVenueSelect.getSelectedOption() );

            remove( getMainComponent() );
            add( makeVenuePanel(), BorderLayout.CENTER );
            updateStatus();
        } else {
            if (e.getSource() == timCheckConnection) {
                // Note the dual purpose for this timer! See also comment above at point
                //  of declaration. The flushTxnQueue() method includes a check to see whether
                //  we are online (the status of which is set in the bowels of pingServer()).
                pingServer();
                flushTxnQueue();
            } else if (pnlCurrentSale.triggeredAction( e )) {
                cancelSale();
            } else if (e.getSource() == btnVenueReport) {
                VenueReportDialog dlg = new VenueReportDialog( this );
                dlg.setVisible( true );
            } else if (e.getSource() == btnSuperpassSale) {
                String[] itemNames = new String[ 3 ];
                int i = 0;
                for (ConcessionItem c : citems) {
                    if (c.getName().startsWith( "Superpass" )) {
                        itemNames[ i ] = c.getName();
                        handled = true;
                        i++;
                    }
                }
                if (handled) {
                    sellSuperpasses( itemNames );
                } else {
                    JOptionPane.showMessageDialog( null, "Couldn't create Superpass book sale item",
                                                   "Error", JOptionPane.WARNING_MESSAGE );
                }
            } else if (e.getSource() == btnDonation) {
                n = askDollarValue( "Donation Amount?" );
                if (n != 0) {
                    SaleDialog dlg = new SaleDialog( new DonationSaleItem( n ), this );
                    dlg.setVisible( true );
                }
            } else if (e.getSource() == btnButtonSale[ 4 ]) {
                n = askNumberOfItems( "Number of buttons sold?" );
                if (n != 0) {
                    SaleDialog dlg = new SaleDialog( new ButtonSaleItem( n ), this );
                    dlg.setVisible( true );
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    if (e.getSource() == btnButtonSale[ i ]) {
                        SaleDialog dlg = new SaleDialog( new ButtonSaleItem( i + 1 ), this );
                        dlg.setVisible( true );
                        handled = true;
                        break;
                    }
                }
                
                if ((! handled) && (btnConcessionSale != null) && (btnConcessionSale.length > 0)) {
                    for (int i = 0; i < btnConcessionSale.length; i++) {
                        if (e.getSource() == btnConcessionSale[ i ]) {
                            ConcessionItem citem = citems.get( i );

                            n = askNumberOfItems( "Amount of " + citem.getName() + " sold?" );
                            if (n != 0) {
                                SaleDialog dlg = new SaleDialog( new ConcessionSaleItem( citem, n ), this );
                                dlg.setVisible( true );
                            }

                            break;
                        }
                    }
                }
            }

            updateStatus();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Required by MouseListener
    
    public void mouseClicked( MouseEvent e ) {
        SaleDialog dlg = new SaleDialog( this );
        dlg.setVisible( true );
    }

    public void mouseEntered( MouseEvent e ) {
        // NOP
    }

    public void mouseExited( MouseEvent e ) {
        // NOP
    }

    public void mousePressed( MouseEvent e ) {
        // NOP
    }

    public void mouseReleased( MouseEvent e ) {
        // NOP
    }


    // ---------------------------------------------------------------------------------------------
    // BoxOffice user space
    
    public BoxOffice( BoxOfficeApp app ) {
        super( new BorderLayout() );
        this.app = app;
    }

    public void addToSale( SaleItem itm ) {
        if (itm != null) {
            currentSale.add( itm );
            updateStatus();
        }
    }

    private boolean areOnSameDay( Calendar c1, Calendar c2 ) {
        return ((c1.get( Calendar.YEAR ) == c2.get( Calendar.YEAR )) &&
                (c1.get( Calendar.DAY_OF_YEAR ) == c2.get( Calendar.DAY_OF_YEAR )));
    }
    
    private int askDollarValue( String question ) {
        return askDollarValue( question, 0 );
    }

    private int askDollarValue( String question, int defaultAmount ) {
        Number n = askNumericValue( question, "Enter Amount", "float" );
        return (n == null ? defaultAmount : (int) (100 * n.floatValue()));
    }

    public int askNumberOfItems( String question ) {
        return askNumberOfItems( question, 0 );
    }

    public int askNumberOfItems( String question, int defaultNumber ) {
        Number n = askNumericValue( question, "Enter Number", "int" );
        return (n == null ? defaultNumber : n.intValue());
    }

    private Number askNumericValue( String question, String title, String type ) {
        String resp = null;
        String caption = question;
        Number n = null;

        while (resp == null) {
            resp = JOptionPane.showInputDialog( null, caption, title, JOptionPane.QUESTION_MESSAGE );
            if (resp == null) {
                return n;
            }

            try {
                if (type.equals( "int" )) {
                    n = new Integer( resp );
                } else if (type.equals( "float" )) {
                    n = new Float( resp );
                }
            } catch (NumberFormatException x) {
                resp = null;
                caption = "<HTML>Could not understand.<BR />" + question + "<BR /></HTML>";
            }
        }

        return n;
    }

    public void cancelSale() {
        currentSale.removeAllElements();
    }

    private void defaultColor( JComponent cmp ) {
        setComponentColor( cmp, Color.white );
    }

    private void defaultColor2( JComponent cmp ) {
        setComponentColor( cmp, new Color( 235, 235, 235 ) );
    }

    public static Font defaultFont() {
        return defaultFont( 12 );
    }

    public static Font defaultFont( int size ) {
        return defaultFont( Font.PLAIN, size );
    }

    public static Font defaultFont( int style, int size ) {
        return new Font( "Verdana", style, size );
    }

    public void doLogin( String username, String sessionID ) {
        app.setSessionData( username, sessionID, true );
    }

    private void drawUI() {
        JLabel lblTitle;

        int[] clrs = {
            0x0099FF, 0x3399FF, 0x6699FF, 0x9999FF, 0xCC99FF, 0XFF99FF,
            0x0099CC, 0x3399CC, 0x6699CC, 0x9999CC, 0xCC99CC, 0XFF99CC,
            0x009999, 0x339999, 0x669999, 0x999999, 0xCC9999, 0XFF9999,
            0x00CCFF, 0x33CCFF, 0x66CCFF, 0x99CCFF, 0xCCCCFF, 0XFFCCFF,
            0x00CCCC, 0x33CCCC, 0x66CCCC, 0x99CCCC, 0xCCCCCC, 0XFFCCCC,
            0x00CC99, 0x33CC99, 0x66CC99, 0x99CC99, 0xCCCC99, 0XFFCC99
        };
        for (int clr : clrs) {
            tabColors.add( new Color( clr ) );
        }

        imgOnline = new ImageIcon( "images/online_icon.png", "(Online)" );
        imgOffline = new ImageIcon( "images/offline_icon.png", "(Offline)" );

        defaultColor( this );

        Box boxTitle = Box.createHorizontalBox();
        lblTitle = new JLabel( "Calgary Fringe Festival - Box Office", JLabel.LEFT );
        lblTitle.setFont( defaultFont( Font.BOLD, 20 ) );
        boxTitle.add( lblTitle );
        
        boxTitle.add( Box.createHorizontalGlue() );
        lblOnlineStatus = new JLabel( imgOnline, JLabel.RIGHT );
        lblOnlineStatus.setForeground( Color.green );
        boxTitle.add( lblOnlineStatus );
        
        boxTitle.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        add( boxTitle, BorderLayout.NORTH );

        JPanel pnlStatus = new JPanel();
        pnlStatus.setLayout( new BoxLayout( pnlStatus, BoxLayout.X_AXIS ) );
        setComponentColor( pnlStatus, Color.white );

        lblStatus = new JLabel( "<HTML>Please supply login credentials</HTML>" );
        lblStatus.setFont( defaultFont( 14 ) );
        lblStatus.setBorder( new EmptyBorder( 2, 5, 2, 5 ) );
        pnlCurrentSale = new SaleStatusPanel( this, this );
        pnlStatus.add( lblStatus );
        pnlStatus.add( Box.createGlue() );
        pnlStatus.add( pnlCurrentSale );

        add( pnlStatus, BorderLayout.SOUTH );

        Vector<Selectable> venues = getDBVenueList();

        btnVenueSelect = new JButton( "Select" );
        btnVenueSelect.setFont( defaultFont() );
        btnVenueSelect.addActionListener( this );

        pnlVenueSelect = new OptionPanel( venues, btnVenueSelect, "Select the venue where you are located:", app.msgLog() );
        add( new JScrollPane( pnlVenueSelect ), BorderLayout.CENTER );
    }

    public void enqueueTxn( Transaction txn ) {
        enqueueTxn( txn, false );
    }

    public void enqueueTxn( Transaction txn, boolean ignoreLogging ) {
        if (! ignoreLogging) {
            app.txnLog().txnGenerated( txn, app.msgLog() );
        }
        pendingTxns.add( txn );
    }

    public boolean flushTxnQueue() {
        if (! app.isOnline()) {
            return false;
        }

        while (pendingTxns.size() > 0) {
            Transaction txn = pendingTxns.get( 0 );

            String resp = makeGatewayRequest( txn, "Could Not Save Sale Data to Server" );

            if ((resp != null) && (resp.equals( "network error" ))) {
                return false;
            } else {
                app.txnLog().txnCompleted( txn, app.msgLog() );
                pendingTxns.remove( 0 );
            }
        }

        return true;
    }

    public static JLabel fontLabel( String s ) {
        JLabel lbl = new JLabel( s );
        lbl.setFont( defaultFont() );

        return lbl;
    }

    public static String formatCurr( int amt ) {
        return String.format( "$%,.2f", (amt / 100.0) );
    }

    public Vector<String> getCurrentSaleDescriptions() {
        Vector<String> sales = new Vector<String>( currentSale.size() );

        if (currentSale.size() > 0) {
            for (SaleItem itm : currentSale) {
                sales.add( itm.getDescription() );
            }
        }

        return sales;
    }

    public int getCurrentSaleTotal() {
        int total = 0;
        
        if (currentSale.size() > 0) {
            for (SaleItem itm : currentSale) {
                total += itm.getAmount();
            }
        }

        return total;
    }

    private Vector<ConcessionItem> getDBConcessionList( boolean isInfoTent ) {
        Transaction txn = new Transaction( "concessionlist" );

        String resp = makeGatewayRequest( txn, "Could Not Load Venue Details" );
        if (resp == null) {
            return null;
        }

        Vector<ConcessionItem> clist = new Vector<ConcessionItem>( 5 );
        for (String item : resp.trim().split( "\n" )) {
            if (item.trim().length() > 0) {
                String[] cdefn = item.split( "\\|" );
                ConcessionItem c = new ConcessionItem( Integer.parseInt( cdefn[ 0 ] ), cdefn[ 1 ],
                                                       Integer.parseInt( cdefn[ 2 ] ),
                                                       cdefn[ 3 ].trim().equals( "Y" ) );                
                if (c.isVisible() || isInfoTent) {
                    clist.add( c );
                }    
            }
        }

        return clist;
    }
    
    public String getDBLogin( String username, String password ) {
        HashMap<String, String> params = new HashMap<String, String>( 3 );
        params.put( "username", username );
        params.put( "password", password );

        Transaction txn = new Transaction( "login", params );

        return makeGatewayRequest( txn, "Could Not Login" );
    }

    private Vector<Selectable> getDBVenueList() {
        Transaction txn = new Transaction( "venuelist" );

        String resp = makeGatewayRequest( txn, "Could Not Load List of Available Venues" );
        if (resp == null) {
            return null;
        }

        Vector<Selectable> vlist = new Vector<Selectable>();
        for (String v : resp.split( "\n" )) {
            if (v.trim().length() > 0) {
                String[] vdefn = v.split( "\\|", 3 );
                vlist.add( (Selectable) (new Venue(
                    vdefn[ 2 ], Integer.parseInt( vdefn[ 0 ] ), Integer.parseInt( vdefn[ 1 ] )
                )) );
            }
        }

        return vlist;
    }
    
    public JFrame getFrame() {
        return (JFrame) app;
    }
        
    public ImageIcon getLocalImage( String imageName ) {
        return app.getLocalImage( imageName );
    }

    private Component getMainComponent() {
        BorderLayout l = (BorderLayout) (getLayout());
        return l.getLayoutComponent( BorderLayout.CENTER );
    }
    
    public MessageLogger getMessageLogger() {
        return app.msgLog();
    }

    public Vector<Transaction> getPWindowTxnQueue( int id ) {
        return getTxnQueueMatching( "pwindowid", String.valueOf( id ) );
    }
    
    public SessionData getSessionData() {
        return sd;
    }
    
    private Vector<Transaction> getTxnQueueMatching( String param, String value ) {
        Vector<Transaction> txns = new Vector<Transaction>();
        
        if (pendingTxns != null) {
            for (Transaction txn : pendingTxns) {
                if (txn.matchesParam( param, value )) {
                    txns.add( txn );
                }
            }
        }
        
        return txns;
    }

    public Vector<Transaction> getWindowTxnQueue( int id ) {
        return getTxnQueueMatching( "windowid", String.valueOf( id ) );
    }

    private void initWindowData( int venueID ) {
        HashMap<String, String> params = new HashMap<String, String>( 2 );
        params.put( "venueid", Integer.toString( venueID ) );

        Transaction txn = new Transaction( "pwindowlist", params );
        String resp = makeGatewayRequest( txn, "Could Not Load List of Today's Performances at This Venue" );
        if (resp == null) {
            return;
        }
        
        // TODO: properly determine whether it's the info tent
        citems = getDBConcessionList( (venueID == 1) );
        wd = new WindowData( resp, venueID, this );
    }

    public boolean isSeniorDay() {
        return areOnSameDay( new GregorianCalendar(), seniorDay );
    }

    public void loadMainScreen( boolean loginOk ) {
        if (loginOk) {
            drawUI();
            updateStatus( "Select an available venue" );

            timCheckConnection = new javax.swing.Timer( 30000, this );
            if (! app.isOnline()) {
                timCheckConnection.start();
            }
        } else {
            JLabel lblNoLogin = new JLabel( "<HTML>Login Cancelled.</HTML>", JLabel.CENTER );
            lblNoLogin.setFont( defaultFont() );
            add( lblNoLogin );
            validate();
        }
    }
    
    private JButton makeButton( String caption ) {
        return makeButton( caption, this );
    }

    public static JButton makeButton( String caption, ActionListener listener ) {
        JButton btn = new JButton( caption );

        btn.setFont( defaultFont() );
        if (listener != null) {
            btn.addActionListener( listener );
        }
        return btn;
    }

    private JPanel makeConcessionPanel() {
        JPanel pnlConcession;

        int n = 0;
        if (citems.size() > 0) {
            for (ConcessionItem c : citems) {
                if (c.isVisible()) {
                    n++;
                }
            }
        }
        int cols = Math.min( (n / 4) + 1, 1 );

        pnlConcession = new JPanel( new GridBagLayout() );
        setComponentColor( pnlConcession, new Color( 255, 255, 51 ) );

        GBC gbc = new GBC( 1.0, GBC.HORIZONTAL );
        gbc.weighty = 0.0;
        gbc.anchor = GBC.NORTHWEST;

        JLabel lblConcessionTitle = new JLabel( "Concession Sales" );
        lblConcessionTitle.setFont( defaultFont( Font.BOLD, 18 ) );
        gbc.addComponent( pnlConcession, lblConcessionTitle, 0, 0, cols + 2, 1 );

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.fill = GBC.BOTH;
        gbc.anchor = GBC.SOUTHWEST;

        if (n > 0) {
            btnConcessionSale = new JButton[ n ];
            int i = 0;
            for (ConcessionItem c : citems) {
                if (c.isVisible()) {
                    btnConcessionSale[ i ] = makeButton( c.toString(), this );
                    gbc.addComponent( pnlConcession, btnConcessionSale[ i ], i / 4, (i % 4) + 1 );
                    i++;
                }
            }
        }
        btnDonation = makeButton( "Donation", this );
        gbc.addComponent( pnlConcession, btnDonation, n / 4, (n % 4) + 1 );

        return pnlConcession;
    }

    public String makeGatewayRequest( Transaction txn ) {
        return makeGatewayRequest( txn, "Server Communication Error" );
    }

    public String makeGatewayRequest( Transaction txn, String errorTitleMsg ) {
        boolean online = app.isOnline();
        String resp = app.makeGatewayRequest( txn, errorTitleMsg );
        
        if (online != app.isOnline()) {
            showConnectionStatus();
        }
        
        return resp;
    }

    private JPanel makeVenuePanel() {
        int i;
        String captionButton;

        JPanel pnlVenue = new JPanel( new BorderLayout() );
        pnlVenue.setBorder( new LineBorder( Color.black ) );

        int n = (wd.isInfoTent() ? 9 : 8);
        JPanel pnlButtons = new JPanel( new GridLayout( n, 1, 10, 10 ) );
        pnlButtons.setBorder( new EmptyBorder( 10, 10, 10, 10 ) );

        JLabel lblButtonTitle = new JLabel( "Fringe Button Sales ($5.00)" );
        lblButtonTitle.setFont( defaultFont( Font.BOLD, 14 ) );
        pnlButtons.add( lblButtonTitle );

        btnButtonSale = new JButton[ 5 ];
        for (i = 0; i < 5; i++) {
            if (i == 0) {
                captionButton = "Sell 1 button";
            } else if (i == 4) {
                captionButton = "Sell more than 4 buttons...";
            } else {
                captionButton = "Sell " + (i + 1) + " buttons";
            }

            btnButtonSale[ i ] = makeButton( captionButton, this );
            pnlButtons.add( btnButtonSale[ i ] );
        }

        JLabel lblOther = new JLabel( "Other" );
        lblOther.setFont( defaultFont( Font.BOLD, 14 ) );
        pnlButtons.add( lblOther );

        if (wd.isInfoTent()) {
            btnSuperpassSale = makeButton( "Sell Superpass", this );
            pnlButtons.add( btnSuperpassSale );
        }    

        btnVenueReport = makeButton( "Venue Super Reconciliation", this );
        pnlButtons.add( btnVenueReport );

        defaultColor2( pnlButtons );
        pnlVenue.add( pnlButtons, BorderLayout.EAST );

        tpnVenue = new JTabbedPane( JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT );
        defaultColor2( tpnVenue );
        pnlVenue.add( tpnVenue, BorderLayout.CENTER );

        i = 0;
        for (Performance p : wd.getPerformanceList()) {
            String caption = p.getShowNameTime();

            Color tabColor;
            if (tabColors.size() > 0) {
                tabColor = tabColors.remove( (int) (Math.random() * tabColors.size()) );
            } else {
                tabColor = new Color( 235, 235, 235 );
            }

            JLabel lblPerformanceName = new JLabel( caption );
            lblPerformanceName.setFont( defaultFont( 14 ) );
            setComponentColor( lblPerformanceName, tabColor );

            JScrollPane scrWindow = new JScrollPane( p.getWindowPanel( tabColor ) );
            scrWindow.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
            tpnVenue.addTab( caption, scrWindow );
            tpnVenue.setTabComponentAt( i, lblPerformanceName );
            tpnVenue.setBackgroundAt( i, tabColor );
            i++;
        }
        if (wd.hasConcession()) {
            JLabel lblConcession = new JLabel( "Concession Sales" );
            lblConcession.setFont( defaultFont( 14 ) );
            setComponentColor( lblConcession, new Color( 255, 255, 51 ) );

            tpnVenue.addTab( "Concession Sales", makeConcessionPanel() );
            tpnVenue.setTabComponentAt( i, lblConcession );
            tpnVenue.setBackgroundAt( i, new Color( 255, 255, 51 ) );
        }

        return pnlVenue;
    }

    public int numPendingTxns() {
        if (pendingTxns == null) {
            return 0;
        }
        
        return pendingTxns.size();
    }

    private void pingServer() {
        // Just for checking the connection status.
        //  Setting the status flag and handling associated UI updates
        //  all happens within makeGatewayRequest().

        makeGatewayRequest( new Transaction( "checkversion" ) );
    }

    public void processSale() {
        while (currentSale.size() > 0) {
            enqueueTxn( currentSale.remove( 0 ).makeTxn( this ) );
        }

        // Instead of flushing here, let's just wait and let it happen from
        //  the timer, which gets fired on a separate event queue. Hopefully
        //  we get a noticeable performance win out of doing this.
        //flushTxnQueue();

        updateStatus();
    }

    public void processTxn( Transaction txn ) {
        enqueueTxn( txn );

        // Instead of flushing here, let's just wait and let it happen from
        //  the timer, which gets fired on a separate event queue. Hopefully
        //  we get a noticeable performance win out of doing this.
        //return flushTxnQueue();
    }

    public void putSessionData( SessionData sd ) {
        this.sd = sd;
    }

    public void removeFromSale( int i ) {
        if (i >= 0 && i < currentSale.size()) {
            currentSale.remove( i );
            updateStatus();
        }    
    }

    public void removeFromSale( SaleItem itm ) {
        if (itm != null) {
            currentSale.remove( itm );
            updateStatus();
        }
    }
    
    private void sellSuperpasses( String[] superpassItems ) {
        String itm = (String) JOptionPane.showInputDialog(
            null, "Which type of Superpass book?", "Superpass Book Sale",
            JOptionPane.QUESTION_MESSAGE, null, superpassItems, superpassItems[ 0 ]
        );

        if (itm != null) {
            for (ConcessionItem c : citems) {
                if (itm.equals( c.getName() )) {
                    int n = askNumberOfItems( "Number of books sold?" );
                    if (n != 0) {
                        SaleDialog dlg = new SaleDialog( new ConcessionSaleItem( c, n ), this );
                        dlg.setVisible( true );
                    }
                    break;
                }
            }
        }
    }
    
    public void setApp( BoxOfficeApp app ) {
        // Basically part of the deserialization process.
        
        this.app = app;
        
        if (pnlVenueSelect != null) {
            pnlVenueSelect.setMessageLogger( app.msgLog() );
        }
        
        resumeAllTimers();
    }

    public static void setComponentColor( JComponent cmp, Color c ) {
        cmp.setOpaque( true );
        cmp.setBackground( c );
    }

    public void showConnectionStatus() {
        boolean online = app.isOnline();

        if (timCheckConnection != null) {
            if (! timCheckConnection.isRunning()) {
                timCheckConnection.start();
            }
        }

        if (lblOnlineStatus != null) {
            if (online) {
                lblOnlineStatus.setIcon( imgOnline );
            } else {
                lblOnlineStatus.setIcon( imgOffline );
            }
    
            lblOnlineStatus.revalidate();
        }
    }
    
    private void showCurrentSale() {
        if (currentSale.size() == 0) {
            pnlCurrentSale.setVisible( false );
        } else {
            int qty = 0;
            int amt = 0;
            for (SaleItem itm : currentSale) {
                qty += itm.getQty();
                amt += itm.getAmount();
            }

            pnlCurrentSale.updateStatus( "Current Sale:<BR />" + qty + " items / " + formatCurr( amt ) );
            pnlCurrentSale.setVisible( true );
        }
    }
    
    public void stopAllTimers() {
        if (timCheckConnection != null) {
            timerRunning = timCheckConnection.isRunning();
            timCheckConnection.stop();
        }
        
        if (wd != null) {
            Vector<Performance> plist = wd.getPerformanceList();
            if (plist != null) {
                for (Performance p : plist) {
                    p.stopTimers();
                }
            }
        }
    }
    
    public void resumeAllTimers() {
        if (timCheckConnection != null) {
            if (timerRunning) {
                timCheckConnection.start();
            }
        }
        
        if (wd != null) {
            Vector<Performance> plist = wd.getPerformanceList();
            if (plist != null) {
                for (Performance p : plist) {
                    p.resumeTimers();
                }
            }
        }
    }

    public void updateStatus() {
        updateStatus( wd.toString() );
    }

    private void updateStatus( String newStatus ) {
        lblStatus.setText( "<HTML>" + newStatus + "</HTML>" );
        showCurrentSale();

        if (wd != null) {
            Vector<Performance> ps = wd.getPerformanceList();

            if (ps.size() > 0) {
                for (Performance p : ps) {
                    p.updateStatus();
                }
            }
        }

        revalidate();
    }
}
