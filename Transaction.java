import java.io.Serializable;
import java.util.HashMap;

public class Transaction implements Serializable {
    private static long last_timestamp = 0;
    private static int  last_mult = 0;
    
    long timestamp = 0;
    int  mult = 0;

    private HashMap<String, String> params;
    private String id;


    public static Transaction fromString( String s ) {
        Transaction txn = new Transaction();
        
        String[] spec = s.split( "::", 2 );
        String[] id = spec[ 0 ].split( "\\." );
        
        try {
            txn.timestamp = Long.parseLong( id[ 0 ] );
            txn.mult = Integer.parseInt( id[ 1 ] );
        } catch (NumberFormatException x) {
            x.printStackTrace();
        }
        
        for (String param : spec[ 1 ].split( "&" )) {
            if (param.trim().length() > 0) {
                String[] map = param.split( "=", 2 );
                if (! (map[ 0 ].equals( "txnid" ))) {
                    txn.addParam( map[ 0 ], map[ 1 ] );
                }
            }
        }
        
        return txn;
    }
    
    
    
    // NOTE THIS IS A "SPECIAL" PRIVATE CONSTRUCTOR, ONLY DESIGNED TO BE CALLED FROM fromString() ABOVE!
    // 
    //  See below for the public ones.
    //
    private Transaction() {
        // 5 seems as good a number as any
        params = new HashMap<String, String>( 5 );
    }


    // THESE are the constructors you're looking for... :-)
    
    public Transaction( String action ) {
        this( action, new HashMap<String, String>( 1 ) );
    }

    public Transaction( String action, HashMap<String, String> params ) {
        makeID();

        this.params = params;
        addParam( "action", action );
    }

    private void makeID() {
        timestamp = System.currentTimeMillis();

        if (timestamp == last_timestamp) {
            last_mult++;
            mult = last_mult;
        } else {
            last_timestamp = timestamp;
            last_mult = 0;
        }
    }

    public String getID() {
        return (String.valueOf( timestamp ) + "." + String.valueOf( mult ));
    }

    public boolean hasParam( String k ) {
        return params.containsKey( k );
    }
    
    public String getParam( String k ) {
        return ((String) params.get( k ));
    }

    public String addParam( String k, String v ) {
        return ((String) params.put( k, v ));
    }

    public String delParam( String k ) {
        return ((String) params.remove( k ));
    }
    
    public boolean matchesParam( String k, String v ) {
        if (params.containsKey( k )) {
            if (params.get( k ).equals( v )) {
                return true;
            }
        }
        
        return false;
    }

    public String getParamStr() {
        return getParamStr( "" );
    }
    
    public String getParamStr( SessionData sd ) {
        return getParamStr( "sessionid=" + sd.sessionID + "&username=" + sd.username );
    }
    
    public String getParamStr( String prefix ) {
        StringBuffer sb = new StringBuffer( 200 );

        sb.append( prefix );
        if (params != null) {
            for (String k : params.keySet()) {
                sb.append( "&" );
                sb.append( k );
                sb.append( "=" );
                sb.append( getParam( k ) );
            }
        }
        sb.append( "&txnid=" + getID() );

        if (sb.length() > 0) {
            return sb.toString();
        }

        return "";
    }

    public String toString() {
        return getID() + "::" + getParamStr();
    }
}
