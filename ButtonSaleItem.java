public class ButtonSaleItem extends SaleItem {
    private int qty;

    public ButtonSaleItem( int qty ) {
        this.qty = qty;
    }

    public String getItemName() {
        return "Fringe button";
    }

    public int getAmount() {
        return 500 * qty;
    }

    public int getQty() {
        return qty;
    }

    public Transaction makeTxn( BoxOffice bo ) {
        bo.wd.updateButtons( qty );

        return Transactions.makeTxnButtonSale( bo.wd.getID(), qty );
    }
}
