import java.io.Serializable;

public abstract class SaleItem implements Serializable {
    public abstract String getItemName();

    public abstract int getAmount();

    public abstract int getQty();

    public abstract Transaction makeTxn( BoxOffice bo );

    public String getDescription() {
        String amt = BoxOffice.formatCurr( getAmount() );

        return getQty() + " x " + getItemName() + " = " + amt;
    }
}
