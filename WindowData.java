import java.io.Serializable;
import java.util.*;

public class WindowData implements Serializable {
    private int windowID;

    private int startingFloat;
    private int startingButtons;
    private int buttonSales;
    private boolean concession;
    private int donations;
    private int cashAdjustments;
    private int buttonAdjustments;
    private int concessionSales;
    private int venueID;

    private Vector<Performance> performances;
    private Vector<ConcessionItem> citems;

    public WindowData( int windowID ) {
        this.windowID = windowID;

        performances = new Vector<Performance>();
        citems = new Vector<ConcessionItem>();

        startingFloat = 0;
        startingButtons = 0;
        buttonSales = 0;
        concession = false;
        donations = 0;
        cashAdjustments = 0;
        buttonAdjustments = 0;
        concessionSales = 0;
        venueID = 0;
    }

    public WindowData( int startingFloat, int startingButtons, int buttonSales, boolean concession,
                       int windowID, int donations, int cashAdjustments, int buttonAdjustments,
                       int concessionSales, int venueID,
                       Vector<Performance> performances, Vector<ConcessionItem> citems ) {

        this( windowID );

        this.startingFloat = startingFloat;
        this.startingButtons = startingButtons;
        this.buttonSales = buttonSales;
        this.concession = concession;
        this.donations = donations;
        this.cashAdjustments = cashAdjustments;
        this.buttonAdjustments = buttonAdjustments;
        this.concessionSales = concessionSales;
        this.venueID = venueID;

        this.performances = performances;
        this.citems = citems;
    }

    public WindowData( String dataStr, int venueID, BoxOffice bo ) {
        // The idea being that dataStr is a window definition string read from the DB

        this( WindowData.getIDFromDataString( dataStr ) );
        
        boolean isMainWindow = true;

        for (String s : dataStr.trim().split( "\n" )) {
            if (isMainWindow) {
                String[] wdefn = s.split( "\\|" );
                bo.getMessageLogger().println( "<<" + s + ">>" );

                startingFloat = Integer.parseInt( wdefn[ 2 ] );
                startingButtons = Integer.parseInt( wdefn[ 5 ] );
                buttonSales = Integer.parseInt( wdefn[ 6 ] );
                concession = (wdefn[ 1 ].equals( "Y" ) ? true : false);
                donations = Integer.parseInt( wdefn[ 3 ] );
                cashAdjustments = Integer.parseInt( wdefn[ 7 ] );
                buttonAdjustments = Integer.parseInt( wdefn[ 8 ].trim() );
                concessionSales = Integer.parseInt( wdefn[ 4 ] );
                this.venueID = venueID;
                
                readPendingTxns( bo );

                isMainWindow = false;
            } else if (s.trim().length() > 0) {
                performances.add( new Performance( s, venueID, bo ) );
            }
        }
        
        Collections.sort( performances );
    }

    public static int getIDFromDataString( String s ) {
        return Integer.parseInt( s.substring( 0, s.indexOf( '|' ) ).trim() );
    }

    public void updateButtons( int adj ) {
        buttonSales += adj;
    }

    public void updateDonations( int adj ) {
        donations += adj;
    }

    public void updateConcessionSales( int adj ) {
        concessionSales += adj;
    }

    public void adjustCash( int adj ) {
        cashAdjustments += adj;
    }

    public void adjustButtons( int adj ) {
        buttonAdjustments += adj;
    }

    public boolean hasConcession() {
        return concession;
    }

    public int getButtonsOnHand() {
        return startingButtons - buttonSales - buttonAdjustments;
    }

    public int getCashOnHand() {
        int cash = startingFloat;
        if (performances.size() > 0) {
            for (Performance p : performances) {
                cash += p.getSalesAmount();
            }
        }
        cash += (buttonSales * 500) + donations + concessionSales - cashAdjustments;

        return cash;
    }
    
    public int getID() {
        return windowID;
    }
    
    public int getVenueID() {
        return venueID;
    }
    
    public boolean isInfoTent() {
        // TODO: remove this hack and do things properly
        //  See also: Performance.Performance( String, int, BoxOffice )

        return (venueID == 1);
    }

    public Vector<Performance> getPerformanceList() {
        return performances;
    }
    
    private void readPendingTxns( BoxOffice bo ) {
        if (bo == null) {
            return;
        }
        
        Vector<Transaction> txns = bo.getWindowTxnQueue( windowID );
        
        for (Transaction txn : txns) {
            String action = txn.getParam( "action" );
            
            if (action.equals( "buttonsale" )) {
                updateButtons( Integer.parseInt( txn.getParam( "numbuttons" ) ) );
            } else if (action.equals( "cashadjustment" )) {
                adjustCash( Integer.parseInt( txn.getParam( "amount" ) ) );
            } else if (action.equals( "buttonadjustment" )) {
                adjustButtons( Integer.parseInt( txn.getParam( "amount" ) ) );
            } else if (action.equals( "concessionsale" )) {
                // TODO: complete this - I don't have time right now.
                //       We have to figure out the price based on the itemid, which: feh
            } else if (action.equals( "donation" )) {
                updateDonations( Integer.parseInt( txn.getParam( "amount" ) ) );
            } else {
                bo.getMessageLogger().println( "Reading Txns - unknown txn action: " + action );
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer( 100 );

        int ticketSales = 0;
        if (performances.size() > 0) {
            for (Performance p : performances) {
                ticketSales += p.getSalesAmount();
            }
        }

        int totalSales = ticketSales + (buttonSales * 500) + donations + concessionSales - cashAdjustments;

        sb.append( "<FONT COLOR=\"blue\">Initial <B>buttons</B>: " );
        sb.append( startingButtons );
        sb.append( ",    sold: " );
        sb.append( buttonSales );
        if (buttonAdjustments != 0) {
            sb.append( ",     adjustments: " );
            sb.append( buttonAdjustments );
        }
        sb.append( ",    REMAINING: <B>" );
        sb.append( getButtonsOnHand() );
        sb.append( "</B></FONT><BR />" );
        sb.append( "<FONT COLOR=\"green\">Initial <B>float</B>: " );
        sb.append( BoxOffice.formatCurr( startingFloat ) );
        sb.append( ",    ticket sales: " );
        sb.append( BoxOffice.formatCurr( ticketSales ) );
        sb.append( ",    button sales: " );
        sb.append( BoxOffice.formatCurr( (buttonSales * 500) ) );
        if (concessionSales > 0) {
            if (isInfoTent()) {
                sb.append( ",    superpass sales: " );
            } else {
                sb.append( ",    concession sales: " );
            }
            sb.append( BoxOffice.formatCurr( concessionSales ) );
        }
        if ((donations > 0) || (cashAdjustments != 0)) {
            sb.append( ",    payouts/other: " );
            sb.append( BoxOffice.formatCurr( donations - cashAdjustments ) );
        }
        sb.append( ",    TOTAL: <B>" );
        sb.append( BoxOffice.formatCurr( startingFloat + totalSales ) );
        sb.append( "</FONT></B>" );

        return sb.toString();
    }
}
