import java.io.Serializable;

public class SelString implements Selectable, Serializable {
    private boolean selectable;
    private int id;
    private String str;

    public SelString() {
        this( "", true, 0 );
    }

    public SelString( boolean b ) {
        this( "", b, 0 );
    }

    public SelString( int i ) {
        this( "", true, i );
    }

    public SelString( boolean b, int i ) {
        this( "", b, i );
    }

    public SelString( String s ) {
        this( s, true, 0 );
    }

    public SelString( String s, boolean b ) {
        this( s, b, 0 );
    }

    public SelString( String s, int i ) {
        this( s, true, i );
    }

    public SelString( String s, boolean b, int i ) {
        str = s;
        selectable = b;
        id = i;
    }

    public void setSelectable( boolean b ) {
        selectable = b;
    }

    public boolean canSelect() {
        return selectable;
    }

    public void setID( int i ) {
        id = i;
    }

    public int getID() {
        return id;
    }

    public void setString( String s ) {
        str = s;
    }

    public String toString() {
        return str;
    }
}
