import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class Performance implements ActionListener, Comparable<Performance>, Serializable {
    private int pwindowID;

    private int performanceID;
    private int venueID;
    private int startingTickets;
    private int ticketSales;
    private int ticketComps;
    private int ticketPrice;
    private int artistPrice;
    private int superpasses;
    private int artistcomps;
    private int remainingCap;
    private int overflowCap;
    private int infoTentSales;
    private String startTime;
    private String showName;
    private String venueName;
    private String artistName;
    private GregorianCalendar windowClosesAt;
    private GregorianCalendar checkCapacityAt;
    private boolean capacityUpdated;
    private boolean canPayArtist;

    private Vector<String> mediaNames;
    private Vector<SpecialSale> specialSales;

    public Presale[] presales;

    private JPanel pnlPWindow;
    private JButton[] btnCashSale;
    private JButton btnSeniorSale;
    private JButton btnTicketPrepurchase;
    private JButton btnTicketSpecial;
    private JButton btnArtistReport;
    private JButton btnUpdateNumbers;
    
    private javax.swing.Timer timCheckExpiry;
    private boolean timerRunning;
    private JLabel lblPWindowStatus;
    private JLabel lblCapacity;

    private BoxOffice bo;

    public Performance( int pwindowID ) {
        this.pwindowID = pwindowID;

        performanceID = 0;
        venueID = 0;
        startingTickets = 0;
        ticketSales = 0;
        ticketComps = 0;
        ticketPrice = 0;
        artistPrice = 0;
        superpasses = 0;
        artistcomps = 0;
        remainingCap = 0;
        overflowCap = 0;
        infoTentSales = 0;

        startTime = "";
        showName = "";
        venueName = "";
        artistName = "";

        windowClosesAt = new GregorianCalendar();
        windowClosesAt.setTimeInMillis( 0 );
        checkCapacityAt = (GregorianCalendar) windowClosesAt.clone();
        capacityUpdated = false;
        canPayArtist = true;

        mediaNames = new Vector<String>( 3 );
        specialSales = new Vector<SpecialSale>();

        presales = new Presale[ 0 ];

        bo = null;

        pnlPWindow = null;
    }

    public Performance( int pwindowID, int performanceID, int venueID, int startingTickets, int ticketSales,
                        int ticketComps, int specialQty, int specialAmt, int ticketPrice, int artistPrice,
                        String startTime, String showName, String venueName, String artistName,
                        GregorianCalendar windowClosesAt, GregorianCalendar checkCapacityAt,
                        boolean capacityUpdated, boolean canPayArtist,
                        Vector<String> mediaNames, Vector<SpecialSale> specialSales,
                        int remainingCap, int overflowCap, Presale[] presales, BoxOffice bo ) {

        this( pwindowID );

        this.performanceID = performanceID;
        this.venueID = venueID;
        this.startingTickets = startingTickets;
        this.ticketSales = ticketSales;
        this.ticketComps = ticketComps;
        this.ticketPrice = ticketPrice;
        this.artistPrice = artistPrice;
        this.startTime = startTime;
        this.showName = showName;
        this.venueName = venueName;
        this.artistName = artistName;
        this.windowClosesAt = windowClosesAt;
        this.checkCapacityAt = checkCapacityAt;
        this.capacityUpdated = capacityUpdated;
        this.canPayArtist = canPayArtist;
        this.mediaNames = mediaNames;
        this.specialSales = specialSales;

        artistcomps = 0;
        superpasses = 0;
        infoTentSales = 0;

        initSpecialSales( specialQty, specialAmt );
        setCaps( remainingCap, overflowCap );

        this.presales = presales;
        this.bo = bo;
        if (! isAtInfoTent()) {
            fetchInfoTentSales();
        }
        
        readPendingTxns();
    }

    public Performance( String dataStr, int venueID, BoxOffice bo ) {
        // The idea being that dataStr is a pwindow definition string read from the DB

        this( Performance.getIDFromDataString( dataStr ) );

        String[] pwdefn = dataStr.split( "\\|" );
        Presale[] ps = null;

        performanceID = Integer.parseInt( pwdefn[ 1 ].trim() );
        this.venueID = venueID;
        startingTickets = Integer.parseInt( pwdefn[ 8 ].trim() );
        ticketSales = Integer.parseInt( pwdefn[ 9 ].trim() );
        ticketComps = Integer.parseInt( pwdefn[ 12 ].trim() );
        ticketPrice = Integer.parseInt( pwdefn[ 7 ].trim() );
        artistPrice = Integer.parseInt( pwdefn[ 13 ].trim() );
        superpasses = Integer.parseInt( pwdefn[ 10 ].trim() );
        artistcomps = Integer.parseInt( pwdefn[ 11 ].trim() );
        startTime = pwdefn[ 5 ].trim();
        showName = pwdefn[ 2 ].trim();
        venueName = pwdefn[ 4 ].trim();
        artistName = pwdefn[ 3 ].trim();
        try {
            GregorianCalendar now = new GregorianCalendar();
            windowClosesAt.setTime( (new SimpleDateFormat( "HH:mm:ss" )).parse( pwdefn[ 6 ].trim() ) );
            windowClosesAt.set( now.get( Calendar.YEAR ), now.get( Calendar.MONTH ), now.get( Calendar.DAY_OF_MONTH ) );
            checkCapacityAt = (GregorianCalendar) windowClosesAt.clone();
            checkCapacityAt.add( Calendar.MINUTE, -35 );
            if (checkCapacityAt.before( now )) {
                capacityUpdated = true;
            }
        } catch (ParseException x) {
            // NOP - just use the time of epoch + 0, as already set in the default constructor
        }

        int specialQty = Integer.parseInt( pwdefn[ 14 ].trim() );
        int specialAmt = Integer.parseInt( pwdefn[ 15 ].trim() );
        initSpecialSales( specialQty, specialAmt );

        setCaps( Integer.parseInt( pwdefn[ 16 ].trim() ), Integer.parseInt( pwdefn[ 17 ].trim() ) );
        if (pwdefn[ 18 ].trim().equals( "Y" )) {
            canPayArtist = false;
        }
        parsePresales( pwdefn, 19 );

        this.bo = bo;
        if (! isAtInfoTent()) {
            fetchInfoTentSales();
        }

        readPendingTxns();
    }

    public static int getIDFromDataString( String s ) {
        return Integer.parseInt( s.substring( 0, s.indexOf( '|' ) ).trim() );
    }

    private void initSpecialSales( int specialQty, int specialAmt ) {
        if (specialQty > 0) {
            // TODO: we actually need to pull the entire list out, sigh
            specialSales.add( new SpecialSale( specialQty, (specialAmt / specialQty), (specialAmt / specialQty) ) );
        }
    }

    private void parsePresales( String[] defn, int presaleOffset ) {
        int n = defn.length;
        int i;
        
        presales = new Presale[ (n - presaleOffset) / 4 ];
        if (n > presaleOffset) {
            for (i = presaleOffset; i < n; i+=4) {
                presales[ (i - presaleOffset) / 4 ] = new Presale(
                    defn[ i ], defn[ i + 1 ], Integer.parseInt( defn[ i + 2 ] ),
                    defn[ i + 3 ].trim().equals( "Y" )
                );
            }
        }
    }

    private void setCaps( int remainingCap, int overflowCap ) {
        int remainingTickets = getRemainingTickets();
        if (remainingTickets < remainingCap) {
            remainingCap = remainingTickets;
        }

        this.remainingCap = remainingCap;
        this.overflowCap = overflowCap;

        if (! spaceLeft()) {
            stopSales();
        }

        if (lblCapacity != null) {
            updateCapacityStatus();
        }
    }

    public void useUpCapacity( int numTickets ) {
        remainingCap -= numTickets;

        if (! spaceLeft()) {
            stopSales();
        }

        if (lblCapacity != null) {
            updateCapacityStatus();
        }
    }

    public boolean spaceLeft() {
        return spaceLeft( 1 );
    }
    
    public boolean spaceLeft( int n ) {
        return ((remainingCap + overflowCap) >= n);
    }

    public JPanel getWindowPanel() {
        return getWindowPanel( new Color( 235, 235, 235 ) );
    }

    public JPanel getWindowPanel( Color bgColor ) {
        if (pnlPWindow == null) {
            initWindowPanel( bgColor );
        }

        return pnlPWindow;
    }

    private void initWindowPanel( Color bgColor ) {
        btnCashSale = new JButton[ 5 ];

        int i;
        String captionTicket;

        pnlPWindow = new JPanel( new BorderLayout() );

        lblPWindowStatus = new JLabel( "" );
        BoxOffice.setComponentColor( lblPWindowStatus, Color.white );
        lblPWindowStatus.setFont( BoxOffice.defaultFont( 14 ) );
        lblPWindowStatus.setBorder( new EmptyBorder( 2, 5, 2, 5 ) );
        pnlPWindow.add( lblPWindowStatus, BorderLayout.SOUTH );


        JPanel pnlPWindowContents = new JPanel( new GridBagLayout() );
        BoxOffice.setComponentColor( pnlPWindowContents, bgColor );

        GBC gbc = new GBC( 1.0, GBC.HORIZONTAL );
        gbc.anchor = GBC.NORTHWEST;

        gbc.weighty = 0.0;

        JLabel lblShowTitle = new JLabel( getDisplayTitle() );
        lblShowTitle.setFont( BoxOffice.defaultFont( Font.BOLD, 18 ) );
        gbc.addComponent( pnlPWindowContents, lblShowTitle, 0, 0, 3, 1 );

        gbc.weighty = 0.9;

        lblCapacity = new JLabel( getCapacityString() );
        lblCapacity.setFont( BoxOffice.defaultFont( Font.BOLD, 15 ) );
        gbc.addComponent( pnlPWindowContents, lblCapacity, 0, 1, 3, 1 );

        gbc.weighty = 0.0;
        gbc.anchor = GBC.SOUTHWEST;

        JLabel lblCashTitle = new JLabel( "Tickets sold for cash (" + showTicketPrice() + ")" );
        lblCashTitle.setFont( BoxOffice.defaultFont( Font.BOLD, 14 ) );
        gbc.addComponent( pnlPWindowContents, lblCashTitle, 0, 2 );

        JLabel lblOtherTitle = new JLabel( "Non-cash tickets" ) ;
        lblOtherTitle.setFont( BoxOffice.defaultFont( Font.BOLD, 14 ) );
        gbc.addComponent( pnlPWindowContents, lblOtherTitle, 1, 2 );

        gbc.weighty = 1.0;
        gbc.fill = GBC.BOTH;

        for (i = 0; i < 5; i++) {
            if (i == 0) {
                captionTicket = "Sell 1 ticket";
            } else if (i == 4) {
                captionTicket = "Sell more than 4 tickets...";
            } else {
                captionTicket = "Sell " + (i + 1) + " tickets";
            }

            btnCashSale[ i ] = BoxOffice.makeButton( captionTicket, this );
            gbc.addComponent( pnlPWindowContents, btnCashSale[ i ], 0, i + 3 );
        }

        btnTicketPrepurchase = BoxOffice.makeButton( "Pre-purchased ticket pickup...", this );
        gbc.addComponent( pnlPWindowContents, btnTicketPrepurchase, 1, 3 );

        btnTicketSpecial = BoxOffice.makeButton( "Issue non-cash ticket(s)...", this );
        gbc.addComponent( pnlPWindowContents, btnTicketSpecial, 1, 4 );

        if (bo.isSeniorDay()) {
            GregorianCalendar dob = new GregorianCalendar();
            dob.add( Calendar.YEAR, -55 );
            String dobCaption = "(to those born on or before " +
                                dob.get( Calendar.MONTH ) + " " +
                                dob.get( Calendar.DAY_OF_MONTH ) + ", " +
                                dob.get( Calendar.YEAR ) + ")";

            btnSeniorSale = BoxOffice.makeButton( "<HTML>Sell \"Senior Tuesday\" tickets<BR />" +
                                                  dobCaption + "...</HTML>", this );
            gbc.addComponent( pnlPWindowContents, btnSeniorSale, 1, 5 );
        }

        btnUpdateNumbers = BoxOffice.makeButton( "Get latest numbers / presales", this );
        gbc.addComponent( pnlPWindowContents, btnUpdateNumbers, 1, 6 );

        btnArtistReport = BoxOffice.makeButton( "Artist Reconciliation Report", this );
        gbc.addComponent( pnlPWindowContents, btnArtistReport, 1, 7 );

        pnlPWindow.add( pnlPWindowContents, BorderLayout.CENTER );
        updateStatus();

        timCheckExpiry = new javax.swing.Timer( 30000, this );
        if (expired() || (! spaceLeft())) {
            stopSales();
        } else {
            timCheckExpiry.start();
        }
    }

    public void actionPerformed( ActionEvent e ) {
        int n = 0;
        String resp = null;
        String caption;

        if (e.getSource() == timCheckExpiry) {
            GregorianCalendar now = new GregorianCalendar();
            checkCapacity( now );
            checkExpiry( now );
        } else if (e.getSource() == btnArtistReport) {
            ArtistReportDialog dlg = new ArtistReportDialog( this, bo );
            dlg.setVisible( true );
        } else if (e.getSource() == btnTicketSpecial) {
            String[] reasons = { "Volunteer", "Media Pass", "Artist (Password)", "Host Comp",
                                 "Superpass", "All-Access Pass", "(Other)" };
            Vector<Selectable> selReasons = new Vector<Selectable>( reasons.length );

            for (int i = 0; i < reasons.length; i++) {
                selReasons.add( (Selectable) new SelString( reasons[ i ], i ) );
            }

            OptionDialog dlg = new OptionDialog( selReasons, this, bo );
            dlg.setVisible( true );
        } else if (e.getSource() == btnUpdateNumbers) {
            updateNumbers();
        } else if (e.getSource() == btnTicketPrepurchase) {
            if ((presales == null) || (presales.length == 0)) {
                JOptionPane.showMessageDialog( null, "There are no online presales for this show.",
                                               "No presales", JOptionPane.INFORMATION_MESSAGE );
            } else {
                int remaining = 0;
                Vector<Selectable> selPresales = new Vector<Selectable>( presales.length );
                for (Presale ps : presales) {
                    selPresales.add( (Selectable) ps );
                    if (! ps.isRedeemed()) {
                        remaining += ps.getNumTickets();
                    }
                }

                ListDialog dlg = new ListDialog( remaining, selPresales, this, bo );
                dlg.setVisible( true );
            }
        } else if (e.getSource() == btnSeniorSale) {
            n = bo.askNumberOfItems( "Number of \"Senior Tuesday\" tickets sold?", 0 );
            if (! spaceLeft( n )) {
                JOptionPane.showMessageDialog( null, "Not enough remaining capacity.",
                                               "Can't Sell " + n + " Tickets", JOptionPane.WARNING_MESSAGE );
                return;
            }
            if (n != 0) {
                SaleDialog dlg = new SaleDialog( new TicketSaleItem( this, n, 1000, 1000, "Senior Tuesday" ), bo );
                dlg.setVisible( true );
                bo.updateStatus();
            }
        } else if (e.getSource() == btnCashSale[ 4 ]) {
            n = bo.askNumberOfItems( "Number of tickets sold?", 0 );
            if (! spaceLeft( n )) {
                JOptionPane.showMessageDialog( null, "Not enough remaining capacity.",
                                               "Can't Sell " + n + " Tickets", JOptionPane.WARNING_MESSAGE );
                return;
            }
            if (n != 0) {
                SaleDialog dlg = new SaleDialog( new TicketSaleItem( this, n ), bo );
                dlg.setVisible( true );
                bo.updateStatus();
            }
        } else {
            for (int i = 0; i < 4; i++) {
                if (e.getSource() == btnCashSale[ i ]) {
                    n = i + 1;
                    if (! spaceLeft( n )) {
                        JOptionPane.showMessageDialog( null, "Not enough remaining capacity.",
                                                       "Can't Sell " + n + " Tickets", JOptionPane.WARNING_MESSAGE );
                        return;
                    }
                    SaleDialog dlg = new SaleDialog( new TicketSaleItem( this, n ), bo );
                    dlg.setVisible( true );
                    bo.updateStatus();

                    break;
                }
            }
        }

        updateStatus();
    }
    
    private void stopSales() {
        if (btnSeniorSale != null) {
            btnSeniorSale.setEnabled( false );
        }
        if (btnTicketPrepurchase != null) {
            btnTicketPrepurchase.setEnabled( false );
            btnTicketSpecial.setEnabled( false );
            for (JButton b : btnCashSale) {
                b.setEnabled( false );
            }
            btnUpdateNumbers.setEnabled( false );

            timCheckExpiry.stop();
        }
    }
    
    private boolean updateNumbers() {
        // Just to be on the safe side, in case making the gateway request takes forever:
        timCheckExpiry.stop();

        HashMap<String, String> params = new HashMap<String, String>( 2 );
        params.put( "pwindowid", Integer.toString( pwindowID ) );

        Transaction txn = new Transaction( "remainingcapacity", params );
        String resp = bo.makeGatewayRequest( txn, "Could not verify remaining capacity for " + venueName );

        // Restart the timer NOW, in case setCaps() turns it back off again (we don't want to re-restart it in that case).
        timCheckExpiry.start();
        if (resp == null) {
            return false;
        }

        String[] defn = resp.split( "\\|" );
        int remaining = Integer.parseInt( defn[ 0 ].trim() );
        int overflow = Integer.parseInt( defn[ 1 ].trim() );

        parsePresales( defn, 2 );

        setCaps( remaining, overflow );
        
        if (! isAtInfoTent()) {
            fetchInfoTentSales();
        }

        return true;
    }

    private void checkCapacity( GregorianCalendar now ) {
        // This really isn't thread-safe at all. But since the timer only fires every 30
        //  seconds, I don't think I care. I sure hope not, anyway.

        if (isAtInfoTent()) {
            return;
        }
        if (capacityUpdated) {
            return;
        }

        if (checkCapacityAt.before( now )) {
            capacityUpdated = updateNumbers();
        }
    }

    private void checkExpiry( GregorianCalendar now ) {
        if (expired( now )) {
            stopSales();
        }
    }
    
    private boolean expired() {
        return expired( new GregorianCalendar() );
    }

    private boolean expired( GregorianCalendar now ) {
        return (windowClosesAt.before( now ));
    }

    public void updateStatus() {
        updateStatus( getStatusString() );
        if (lblCapacity != null) {
            updateCapacityStatus();
        }
    }

    private void updateStatus( String newStatus ) {
        lblPWindowStatus.setText( "<HTML>" + newStatus + "</HTML>" );
        pnlPWindow.validate();
    }
    
    private void updateCapacityStatus() {
        lblCapacity.setText( getCapacityString() );
    }
    
    public String getCapacityString() {
        int okToSell = remainingCap;

        String s = "** Remaining Capacity: " + remainingCap + " **";

        if (! isAtInfoTent()) {
            int n = getNumUnredeemedPresales();
            okToSell -= n;
            if (n > 0) {
                s = s + "<BR />" +
                    "&nbsp; &bull; " + n + " online " + (n == 1 ? "sale" : "sales") + " to be picked up<BR />" +
                    "&nbsp; &bull; " + okToSell + " available to be sold";
            }
        } else {
            if (remainingCap == 0) {
                s = s + "<BR /><SMALL>(Recall 20% of tickets also held at venue; there may be a few left there.)</SMALL>";
            }
        }

        if (okToSell <= 0) {
            s = "<FONT COLOR=\"red\" SIZE=\"+1\">" + s + "</FONT>";
        }

        return "<HTML>" + s + "</HTML>";
    }

    public String getTitle() {
        return showName + " (" + artistName + ")";
    }

    public String getArtist() {
        return artistName;
    }

    public String getShowName() {
        return showName;
    }
    
    public String getShowNameTime() {
        return showName + " (" + startTime + ")";
    }

    public String getVenueName() {
        return venueName;
    }

    public int getID() {
        return pwindowID;
    }

    public String showTicketPrice() {
        return BoxOffice.formatCurr( ticketPrice );
    }

    public int getTicketPrice() {
        return ticketPrice;
    }

    public int getArtistPrice() {
        return artistPrice;
    }

    public String getDisplayTitle() {
        return "<HTML>" + getTitle() + "<BR />" + venueName + " at " + startTime +
               " (tickets: " + showTicketPrice() + ")</HTML>";
    }

    public String getFullTitle() {
        return getTitle() + " - " + venueName + " at " + startTime +
               " (tickets: " + showTicketPrice() + ")";
    }

    public boolean hasPresales() {
        if (presales == null) {
            return false;
        }

        return true;
    }

    public int getNumPresales() {
        int n = 0;
        if (presales != null) {
            for (Presale ps : presales) {
                n += ps.getNumTickets();
            }
        }

        return n;
    }
    
    private int getNumUnredeemedPresales() {
        int n = 0;
        if (presales != null) {
            for (Presale ps : presales) {
                if (! ps.isRedeemed()) {
                    n += ps.getNumTickets();
                }
            }
        }
        
        return n;
    }

    public void redeemPresale( int id ) {
        if (presales != null) {
            for (Presale ps : presales) {
                if (ps.getID() == id) {
                    useUpCapacity( ps.redeem() );
                    break;
                }
            }
        }
    }

    public void updateSales( int adj ) {
        ticketSales += adj;
        useUpCapacity( adj );
    }

    public void updateComps( int adj ) {
        ticketComps += adj;
        useUpCapacity( adj );
    }

    public void updateArtistComps( int adj ) {
        artistcomps += adj;
    }

    public void updateSuperpasses( int adj ) {
        superpasses += adj;
    }
    
    public void addSpecialSale( SpecialSale sale ) {
        specialSales.add( sale );
        useUpCapacity( sale.getQty() );
    }

    public int getArtistComps() {
        return artistcomps;
    }

    public int getSuperpasses() {
        return superpasses;
    }

    public int getTicketComps() {
        return ticketComps;
    }

    public int getBasicTicketSales() {
        return ticketSales;
    }

    public int getAllTicketSales() {
        return ticketSales + getSpecialSalesQty( 0 );
    }

    public int getSalesAmount() {
        return (ticketSales * ticketPrice) + getSpecialSales( 0 );
    }

    public int getSpecialSalesQty( int forTicketPrice ) {
        int qty = 0;

        if (specialSales.size() > 0) {
            for (SpecialSale sale : specialSales) {
                // TODO: actually implement the forTicketPrice parameter
                qty += sale.qty;
            }
        }

        return qty;
    }

    public int getSpecialSales( int forTicketPrice ) {
        int sales = 0;

        if (specialSales.size() > 0) {
            for (SpecialSale sale : specialSales) {
                // TODO: actually implement the forTicketPrice parameter
                sales += sale.getSaleAmount();
            }
        }

        return sales;
    }

    public void addMediaName( String name ) {
        mediaNames.add( name );
    }

    public Vector<String> getMediaNames() {
        return getMediaNames( false );
    }

    public Vector<String> getMediaNames( boolean reDownload ) {
        if (reDownload) {
            fetchMediaNames();
        }

        return mediaNames;
    }

    private void fetchMediaNames() {
        HashMap<String, String> params = new HashMap<String, String>( 2 );
        params.put( "performanceid", Integer.toString( performanceID ) );

        Transaction txn = new Transaction( "medianames", params );
        String resp = bo.makeGatewayRequest( txn, "Could Not Retrieve Media Attendance Names" );
        if (resp == null) {
            // leave things as they are
        } else {
            mediaNames = new Vector<String>();

            for (String name : resp.trim().split( "\n" )) {
                if (name.trim().length() > 0) {
                    mediaNames.add( name.trim() );
                }
            }
        }
    }

    public int getArtistPayout() {
        int payout = (artistPrice * ticketSales) + (1000 * superpasses) + getSpecialSales( 0 );

        return payout;
    }
    
    public boolean artistPayoutOk() {
        return (canPayArtist && (! isAtInfoTent()));
    }

    public void makeArtistPayout() {
        int payout = getArtistPayout();

        bo.wd.adjustCash( payout );
        bo.processTxn( Transactions.makeTxnCashAdjustment( bo.wd.getID(), payout, "Payout", getShowName() ) );
        bo.updateStatus();

        canPayArtist = false;
    }
    
    public void stopTimers() {
        if (timCheckExpiry != null) {
            timerRunning = timCheckExpiry.isRunning();
            timCheckExpiry.stop();
        }
    }
    
    public void resumeTimers() {
        if (timCheckExpiry != null) {
            if (timerRunning) {
                timCheckExpiry.start();
            }
        }
    }
    
    public int getStartTimeMins() {
        if (startTime == null) {
            return 0;
        }
        if (startTime.equals( "" )) {
            return 0;
        }
        
        String s = startTime.trim();

        int hrs = 0;
        int min = 0;

        try {
            if (s.substring( 1, 2 ).equals( ":" )) {
                hrs = Integer.parseInt( s.substring( 0, 1 ) );
                min = Integer.parseInt( s.substring( 2, 4 ) );
            } else {
                hrs = Integer.parseInt( s.substring( 0, 2 ) );
                min = Integer.parseInt( s.substring( 3, 5 ) );
            }
        } catch (NumberFormatException x) {
            return 0;
        }

        if (hrs == 12) {
            hrs = 0;
        }
        if (s.endsWith( "PM" )) {
            hrs += 12;
        }

        return (hrs * 60) + min;
    }

    public int compareTo( Performance p ) {
        // Compare by title at info tent, by start time at venue
        //  If we don't know, then default to title

        if (bo != null) {
            if (isAtInfoTent()) {
                return compareByTitle( p );
            } else {
                return compareByTime( p );
            }
        }

        return compareByTitle( p );
    }

    private int compareByTitle( Performance p ) {
        // Start times are used if the title is the same

        int m = getStartTimeMins();
        int pm = p.getStartTimeMins();

        int comp = getTitle().compareTo( p.getTitle() );

        if (comp == 0) {
            comp = m - pm;
        }

        return comp;
    }

    private int compareByTime( Performance p ) {
        // Title is used if the start time is the same

        int m = getStartTimeMins();
        int pm = p.getStartTimeMins();

        int comp;

        if (m == pm) {
            comp = getTitle().compareTo( p.getTitle() );
        } else {
            comp = m - pm;
        }

        return comp;
    }

    public int getRemainingTickets() {
        int allComps = ticketComps;
        if ((presales != null) && (presales.length > 0)) {
            for (Presale ps : presales) {
                if (ps.isRedeemed()) {
                    allComps += ps.getNumTickets();
                }
            }
        }

        return startingTickets - (getAllTicketSales() + allComps);
    }
    
    private int fetchInfoTentSales() {
        HashMap<String, String> params = new HashMap<String, String>( 2 );
        params.put( "performanceid", Integer.toString( performanceID ) );

        Transaction txn = new Transaction( "infotentsales", params );
        String resp = bo.makeGatewayRequest( txn, "Could Not Retrieve Info Tent Sales Data" );
        if (resp == null) {
            resp = "0";
        }

        try {
            infoTentSales = Integer.parseInt( resp.trim() );
        } catch (NumberFormatException x) {
            // Leave as 0 (for lack of a better idea)
        }

        return infoTentSales;
    }

    private void readPendingTxns() {
        if (bo == null) {
            return;
        }
        
        Vector<Transaction> txns = bo.getPWindowTxnQueue( pwindowID );
        
        for (Transaction txn : txns) {
            String action = txn.getParam( "action" );
            
            if (action.equals( "cashsale" )) {
                updateSales( Integer.parseInt( txn.getParam( "numtickets" ) ) );
            } else if (action.equals( "issuecomp" )) {
                updateComps( Integer.parseInt( txn.getParam( "numtickets" ) ) );
            } else if (action.equals( "redeempresale" )) {
                redeemPresale( Integer.parseInt( txn.getParam( "ordernum" ) ) );
            } else {
                bo.getMessageLogger().println( "Reading Txns - unknown txn action: " + action );
            }
        }
    }

    public String getStatusString() {
        boolean isInfoTent = isAtInfoTent();

        int allComps = ticketComps;
        int remainingPresales = 0;
        int ticketsLeft = remainingCap; //getRemainingTickets();
        String remaining;
        String indent = "&nbsp; &nbsp; ";

        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "<HTML>" );
        sb.append( "<B>TICKET SALES:</B><BR />" );

        if ((presales != null) && (presales.length > 0)) {
            for (Presale ps : presales) {
                if (ps.isRedeemed()) {
                    allComps += ps.getNumTickets();
                } else {
                    remainingPresales += ps.getNumTickets();
                }
            }
        }

        if (! isInfoTent) {
            ticketsLeft -= remainingPresales;
        }

        remaining = "<B>Left to sell: " + ticketsLeft + "</B>";
        if (ticketsLeft <= 0) {
            remaining = "<FONT COLOR=\"red\">" + remaining + "</FONT>";
        }

        if (! isInfoTent) {
            sb.append( indent + "At info tent: " + infoTentSales + "<BR />" );
        }

        sb.append( indent + "Cash sales: " + getAllTicketSales() + "<BR />" );
        sb.append( indent + "Non-cash sales: " + allComps + "<BR />" );

        if (! isInfoTent) {
            sb.append( indent + "(Remaining pre-orders: " + remainingPresales + ")<BR />" );
        }

        sb.append( "<BR />" + indent + remaining );
        sb.append( "</HTML>" );

        return sb.toString();
    }
    
    private boolean isAtInfoTent() {
        if (bo != null) {
            if (bo.wd != null) {
                return bo.wd.isInfoTent();
            }
        }

        if (venueID == 1) {
            // This is a total hack.
            //  TODO: Fix it!
            //  See also: WindowData.isInfoTent()

            return true;
        }

        return false;
    }

    public BoxOffice app() {
        return bo;
    }
}
