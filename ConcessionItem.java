import java.io.Serializable;

public class ConcessionItem implements Serializable {
    private int id;
    private String name;
    private int price;
    private boolean visible;

    public ConcessionItem( int id, String name, int price, boolean visible ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.visible = visible;
    }

    public int getID() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
    
    public boolean isVisible() {
        return visible;
    }

    public String toString() {
        return name + " (" + BoxOffice.formatCurr( price ) + ")";
    }
}
