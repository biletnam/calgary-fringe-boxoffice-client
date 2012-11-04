public class ConcessionSaleItem extends SaleItem {
    private ConcessionItem citem;
    private int qty;

    public ConcessionSaleItem( ConcessionItem citem, int qty ) {
        this.citem = citem;
        this.qty = qty;
    }

    public String getItemName() {
        return citem.getName();
    }

    public int getAmount() {
        return (citem.getPrice() * qty);
    }

    public int getQty() {
        return qty;
    }

    public Transaction makeTxn( BoxOffice bo ) {
        bo.wd.updateConcessionSales( getAmount() );

        return Transactions.makeTxnConcessionSale( bo.wd.getID(), qty, citem.getID() );
    }
}
