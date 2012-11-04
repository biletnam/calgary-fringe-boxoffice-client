import java.io.Serializable;
import java.util.HashMap;

public class Transactions implements Serializable {
    public static Transaction makeTxnButtonSale( int windowID, int numButtons ) {
        HashMap<String, String> params = new HashMap<String, String>( 3 );
        params.put( "windowid", Integer.toString( windowID ) );
        params.put( "numbuttons", Integer.toString( numButtons ) );

        return new Transaction( "buttonsale", params );
    }

    public static Transaction makeTxnCashAdjustment( int windowID, int amount, String reason, String note ) {
        int n = (note == null ? 4 : 5);

        HashMap<String, String> params = new HashMap<String, String>( n );
        params.put( "windowid", Integer.toString( windowID ) );
        params.put( "amount", Integer.toString( amount ) );
        params.put( "reason", reason );
        if (note != null) {
            params.put( "note", note.replace( "&", "%26" ) );
        }

        return new Transaction( "cashadjustment", params );
    }
    
    public static Transaction makeTxnButtonAdjustment( int windowID, int amount, String note ) {
        int n = (note == null ? 3 : 4);

        HashMap<String, String> params = new HashMap<String, String>( n );
        params.put( "windowid", Integer.toString( windowID ) );
        params.put( "amount", Integer.toString( amount ) );
        if (note != null) {
            params.put( "note", note.replace( "&", "%26" ) );
        }

        return new Transaction( "buttonadjustment", params );
    }

    public static Transaction makeTxnCashSale( int pwindowID, int numTickets ) {
        return makeTxnCashSale( pwindowID, numTickets, 0, 0, null );
    }

    public static Transaction makeTxnCashSale( int pwindowID, int numTickets, int ticketPrice, int artistPrice, String note ) {
        int n = (ticketPrice > 0 ? 6 : 3);

        HashMap<String, String> params = new HashMap<String, String>( n );
        params.put( "pwindowid", Integer.toString( pwindowID ) );
        params.put( "numtickets", Integer.toString( numTickets ) );
        if (ticketPrice > 0) {
            params.put( "ticketprice", Integer.toString( ticketPrice ) );
            params.put( "artistprice", Integer.toString( artistPrice ) );
            params.put( "note", note.replace( "&", "%26" ) );
        }

        return new Transaction( "cashsale", params );
    }

    public static Transaction makeTxnConcessionSale( int windowID, int numItems, int itemID ) {
        HashMap<String, String> params = new HashMap<String, String>( 4 );
        params.put( "windowid", Integer.toString( windowID ) );
        params.put( "numitems", Integer.toString( numItems ) );
        params.put( "itemid", Integer.toString( itemID ) );

        return new Transaction( "concessionsale", params );
    }

    public static Transaction makeTxnIssueComp( int pwindowID, int numTickets, int reason ) {
        return makeTxnIssueComp( pwindowID, numTickets, reason, null );
    }

    public static Transaction makeTxnIssueComp( int pwindowID, int numTickets, int reason, String note ) {
        int n = (note == null ? 4 : 5);

        HashMap<String, String> params = new HashMap<String, String>( n );
        params.put( "pwindowid", Integer.toString( pwindowID ) );
        params.put( "numtickets", Integer.toString( numTickets ) );
        params.put( "reason", Integer.toString( reason ) );
        if (note != null) {
            params.put( "note", note.replace( "&", "%26" ) );
        }

        return new Transaction( "issuecomp", params );
    }

    public static Transaction makeTxnRedeemPresale( int pwindowID, String orderNum ) {
        HashMap<String, String> params = new HashMap<String, String>( 3 );
        params.put( "pwindowid", Integer.toString( pwindowID ) );
        params.put( "ordernum", orderNum );

        return new Transaction( "redeempresale", params );
    }

    public static Transaction makeTxnDonation( int windowID, int amount ) {
        return makeTxnDonation( windowID, amount, null );
    }

    public static Transaction makeTxnDonation( int windowID, int amount, String note ) {
        int n = (note == null ? 3 : 4);

        HashMap<String, String> params = new HashMap<String, String>( n );
        params.put( "windowid", Integer.toString( windowID ) );
        params.put( "amount", Integer.toString( amount ) );
        if (note != null) {
            params.put( "note", note.replace( "&", "%26" ) );
        }

        return new Transaction( "donation", params );
    }
}