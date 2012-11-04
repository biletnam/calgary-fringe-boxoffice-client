import java.io.*;
import java.util.*;

public class TransactionLogger {
    private FileWriter generated_log;
    private FileWriter completed_log;

    private static String generated_filename = "txns_all.dat";
    private static String completed_filename = "txns_done.dat";

    public static Collection<Transaction> loadIncomplete() {
        return loadIncomplete( null );
    }
    
    public static Collection<Transaction> loadIncomplete( MessageLogger ml ) {
        HashMap<String, Transaction> alltxns = new HashMap<String, Transaction>( 100 );
        String instr;
        Transaction txn;
        
        try {
            BufferedReader generated = new BufferedReader( new FileReader( generated_filename ) );
            while ((instr = generated.readLine()) != null) {
                txn = Transaction.fromString( instr.trim() );
                alltxns.put( txn.getID(), txn );
            }
            generated.close();
            
            BufferedReader completed = new BufferedReader( new FileReader( completed_filename ) );
            while ((instr = completed.readLine()) != null){
                alltxns.remove( instr );
            }
            completed.close();
        } catch (IOException x) {
            if (ml == null) {
                x.printStackTrace();
            } else {
                ml.printStackTrace( x );
            }
            
            return new Vector<Transaction>();
        }
        
        return alltxns.values();
    }

    public TransactionLogger() throws IOException {
        generated_log = new FileWriter( generated_filename, true );
        completed_log = new FileWriter( completed_filename, true );
    }
    
    public void txnGenerated( Transaction txn ) {
        txnGenerated( txn, null );
    }
    
    public void txnGenerated( Transaction txn, MessageLogger ml ) {
        try {
            generated_log.write( txn.toString() + "\n" );
            generated_log.flush();
        } catch (IOException x) {
            if (ml == null) {
                x.printStackTrace();
            } else {
                ml.printStackTrace( x );
            }
        }
    }
    
    public void txnCompleted( Transaction txn ) {
        txnCompleted( txn.getID(), null );
    }
    
    public void txnCompleted( String id ) {
        txnCompleted( id, null );
    }

    public void txnCompleted( Transaction txn, MessageLogger ml ) {
        txnCompleted( txn.getID(), ml );
    }
    
    public void txnCompleted( String id, MessageLogger ml ) {
        try {
            completed_log.write( id + "\n" );
            completed_log.flush();
        } catch (IOException x) {
            if (ml == null) {
                x.printStackTrace();
            } else {
                ml.printStackTrace( x );
            }
        }
    }
    
    public void reset() {
        try {
            File f;
            
            f = new File( generated_filename );
            f.delete();
            f.createNewFile();
            
            f = new File( completed_filename );
            f.delete();
            f.createNewFile();
        } catch (IOException x) {
            // Don't worry about it. So long as we keep the order of the above intact,
            //  it won't cause problems if we stop the process. And if we just fail to delete
            //  any of the files, well *shrug*. It's only a bit of nice housekeeping after all.
            //
            // But the order is important!!
            //  Deleting <generated> and leaving <completed> ?   A-Ok
            //  Deleting <completed> and leaving <generated> ?   Bad juju
        } catch (SecurityException x) {
            // See above comment
        }
    }
    
    public void close() {
        try {
            generated_log.close();
            completed_log.close();
        } catch (IOException x) {
            // Umm... ok?
        }
    }
}
