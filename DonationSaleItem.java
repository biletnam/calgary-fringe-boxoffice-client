public class DonationSaleItem extends SaleItem {
    private int amount;

    public DonationSaleItem( int amount ) {
        this.amount = amount;
    }

    public String getItemName() {
        return BoxOffice.formatCurr( amount ) + " Donation";
    }

    public int getAmount() {
        return amount;
    }

    public int getQty() {
        return 1;
    }

    public Transaction makeTxn( BoxOffice bo ) {
        bo.wd.updateDonations( getAmount() );

        return Transactions.makeTxnDonation( bo.wd.getID(), getAmount() );
    }
}
