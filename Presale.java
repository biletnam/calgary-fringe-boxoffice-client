import java.io.Serializable;

public class Presale implements Selectable, Serializable {
    private String name;
    private String orderNum;
    private int numTickets;
    private boolean redeemed;

    public Presale( String name, String orderNum, int numTickets, boolean redeemed ) {
        this.name = name;
        this.orderNum = orderNum;
        this.numTickets = numTickets;
        this.redeemed = redeemed;
    }

    public String toString() {
        return (name + " (" + orderNum + ") -- " + numTickets +
                " ticket(s)   " + (redeemed ? "PICKED UP" : ""));
    }

    public int getID() {
        try {
            return Integer.parseInt( orderNum );
        } catch (NumberFormatException x) {
            return 0;
        }
    }

    public boolean canSelect() {
        return (!redeemed);
    }

    public int getNumTickets() {
        return numTickets;
    }

    public int redeem() {
        return redeem( true );
    }

    public int redeem( boolean redeemed ) {
        this.redeemed = redeemed;
        return numTickets;
    }

    public boolean isRedeemed() {
        return redeemed;
    }
}
