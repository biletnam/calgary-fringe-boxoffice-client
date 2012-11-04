import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ArtistReportDialog extends JDialog implements ActionListener {
    Performance p;
    JButton btnPayout;
    BoxOffice bo;

    public ArtistReportDialog( Performance p, BoxOffice bo ) {
        super( bo.getFrame(), "Artist Reconciliation Report", true );

        this.p = p;
        this.bo = bo;

        Point pt = getLocation();
        setBounds( pt.x + 200, pt.y + 200, 600, 500 );

        JPanel pnlDlgMain = new JPanel( new BorderLayout() );
        pnlDlgMain.add( new JScrollPane( new ArtistReportPanel( p, bo.getLocalImage( "Artist Report" ) ) ), BorderLayout.CENTER );
        btnPayout = new JButton( "Confirm and Make Payout" );

        setContentPane( new DialogPanel( btnPayout, null, new JButton( "Cancel" ), p.artistPayoutOk(), pnlDlgMain, this ) );
        pack();
    }

    public void actionPerformed( ActionEvent e ) {
        if (e.getSource() == btnPayout) {
            int payout = p.getArtistPayout();
            String showName = p.getShowName();
            String msg = "Confirm payout to artist of " + BoxOffice.formatCurr( payout ) + "?";
            int response = JOptionPane.showConfirmDialog( null, msg, "Artist Payout", JOptionPane.OK_CANCEL_OPTION );
            if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
            
            p.makeArtistPayout();
        }

        dispose();
    }
}
