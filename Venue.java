import java.io.Serializable;

public class Venue implements Selectable, Serializable {
    public String name;
    public int id;
    public int curPWindows;

    public Venue( String venueName, int venueID ) {
        this( venueName, venueID, 0 );
    }

    public Venue( String venueName, int venueID, int venueCurPWindows ) {
        name = venueName;
        id = venueID;
        curPWindows = venueCurPWindows;
    }

    public boolean canSelect() {
        return (curPWindows > 0 ? true : false);
    }

    public int getID() {
        return id;
    }

    public String toString() {
        return (name + " (" + curPWindows + " current show" + (curPWindows == 1 ? "" : "s") + ")");
    }
}
