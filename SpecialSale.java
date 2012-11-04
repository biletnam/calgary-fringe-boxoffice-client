import java.io.Serializable;

public class SpecialSale implements Serializable {
    int qty;
    int ticketPrice;
    int artistPrice;

    public SpecialSale( int qty, int ticketPrice, int artistPrice ) {
        this.qty = qty;
        this.ticketPrice = ticketPrice;
        this.artistPrice = artistPrice;
    }

    public int getQty() {
        return qty;
    }

    public int getSaleAmount() {
        return (qty * ticketPrice);
    }

    public int getArtistAmount() {
        return (qty * artistPrice);
    }
}
