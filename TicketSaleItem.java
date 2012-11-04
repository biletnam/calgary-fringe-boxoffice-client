public class TicketSaleItem extends SaleItem {
    private Performance p;
    private int qty;
    private int price = 0;
    private int toArtist = 0;
    private String note = null;

    public TicketSaleItem( Performance p, int qty ) {
        this( p, qty, 0, 0, null );
    }

    public TicketSaleItem( Performance p, int qty, int price, int toArtist, String note ) {
        this.p = p;
        this.qty = qty;
        this.price = price;
        this.toArtist = toArtist;
        this.note = note;
    }

    public String getItemName() {
        return p.getTitle();
    }

    public int getAmount() {
        int amt = qty * (price > 0 ? price : p.getTicketPrice());

        return amt;
    }

    public int getQty() {
        return qty;
    }

    public Transaction makeTxn( BoxOffice bo ) {
        if (price == 0) {
            p.updateSales( qty );
        } else {
            p.addSpecialSale( new SpecialSale( qty, price, toArtist ) );
        }

        return Transactions.makeTxnCashSale( p.getID(), qty, price, toArtist, note );
    }
}
