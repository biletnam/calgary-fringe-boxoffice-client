import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.border.*;

public class VenueReportDialog extends JDialog implements ActionListener {
    JButton btnConfirm;
    JCheckBox ckbVerified;
    JTextField txfButtons;
    JFormattedTextField ftfCash;
    JFormattedTextField ftfSurplus;
    JTextField txfNote;

    int expectedCash;
    int expectedButtons;

    BoxOffice bo;

    public VenueReportDialog( BoxOffice bo ) {
        super( bo.getFrame(), "Venue Supervisor Reconciliation Report", true );

        this.bo = bo;

        Point pt = getLocation();
        setBounds( pt.x + 200, pt.y + 200, 600, 600 );

        NumberFormat nftCurrency = NumberFormat.getNumberInstance();
        nftCurrency.setMaximumFractionDigits( 2 );
        nftCurrency.setMinimumFractionDigits( 2 );

        JPanel pnlReport = new JPanel( new GridBagLayout() );
        GBC gbc = new GBC( 1.0, GBC.HORIZONTAL );
        gbc.weighty = 0.0;
        gbc.anchor = GBC.NORTHWEST;
        gbc.insets = new Insets( 5, 5, 5, 5 );

        expectedCash = bo.wd.getCashOnHand();
        expectedButtons = bo.wd.getButtonsOnHand();

        StringBuffer sb = new StringBuffer( "<HTML>" );
        sb.append( "Expected Fringe buttons on-hand: " + expectedButtons + "<BR />" );
        sb.append( "Expected tickets on-hand<BR /><UL>" );
        for (Performance p : bo.wd.getPerformanceList()) {
            int tix = p.getRemainingTickets();
            sb.append( "<LI>" + p.getTitle() + ": " + (tix < 0 ? 0 : tix) + "</LI>" );
        }
        sb.append( "</UL>" );
        sb.append( "Expected cash on-hand: " + BoxOffice.formatCurr( expectedCash ) + "</HTML>" );
        JLabel expected = new JLabel( sb.toString() );
        expected.setFont( BoxOffice.defaultFont() );
        gbc.addComponent( pnlReport, expected, 0, 0 );

        txfButtons = new JTextField( "0", 4 );
        txfButtons.setFont( BoxOffice.defaultFont() );
        JPanel pnlButtonsField = new CaptionedComponent( "Actual Fringe buttons on-hand: ", txfButtons );
        gbc.insets = new Insets( 15, 5, 0, 5 );
        gbc.addComponent( pnlReport, pnlButtonsField, 0, 1 );
        ftfCash = new JFormattedTextField( nftCurrency );
        ftfCash.setValue( 0.0 );
        ftfCash.setColumns( 10 );
        ftfCash.setFont( BoxOffice.defaultFont() );
        gbc.insets = new Insets( 0, 5, 0, 5 );
        gbc.addComponent( pnlReport, new CaptionedComponent( "Actual cash on-hand (before any surplus run): $", ftfCash ), 0, 2 );
        txfNote = new JTextField( "", 35 );
        txfNote.setFont( BoxOffice.defaultFont() );
        gbc.addComponent( pnlReport, new CaptionedComponent( "Notes: ", txfNote, true ), 0, 3 );

        ckbVerified = new JCheckBox( "The above amounts have been verified as correct (or a note has been added if not)." );
        ckbVerified.setFont( BoxOffice.defaultFont() );
        gbc.insets = new Insets( 15, 5, 0, 5 );
        gbc.addComponent( pnlReport, ckbVerified, 0, 4 );

        ftfSurplus = new JFormattedTextField( nftCurrency );
        ftfSurplus.setValue( 0.0 );
        ftfSurplus.setColumns( 10 );
        ftfSurplus.setFont( BoxOffice.defaultFont() );
        CaptionedComponent cpcSurplus = new CaptionedComponent( "Surplus cash run? $", ftfSurplus );
        if (! bo.wd.isInfoTent()) {
            cpcSurplus.setEnabled( false );
        }
        gbc.insets = new Insets( 0, 5, 5, 5 );
        gbc.weighty = 1.0;
        gbc.addComponent( pnlReport, cpcSurplus, 0, 5 );

        JPanel pnlDlgMain = new JPanel( new BorderLayout() );
        JScrollPane scrReport = new JScrollPane( pnlReport );
        scrReport.setBorder( new LineBorder( Color.black ) );
        pnlDlgMain.add( scrReport, BorderLayout.CENTER );
        JLabel lblTitle = new JLabel( "Venue Supervisor Reconciliation Report" );
        lblTitle.setFont( BoxOffice.defaultFont( Font.BOLD, 13 ) );
        lblTitle.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
        pnlDlgMain.add( lblTitle, BorderLayout.NORTH );

        btnConfirm = new JButton( "Confirm" );

        setContentPane( new DialogPanel( btnConfirm, null, new JButton( "Cancel" ), true, pnlDlgMain, this ) );
    }

    public void actionPerformed( ActionEvent e ) {
        if (e.getSource() == btnConfirm) {
            int btns;
            int cash;
            int surplus;

            if (! ckbVerified.isSelected()) {
                JOptionPane.showMessageDialog( null, "Please verify that the amounts shown are correct.",
                                               "Reconciliation Report: Not Verified", JOptionPane.INFORMATION_MESSAGE );
                return;
            }

            try {
                btns = Integer.parseInt( txfButtons.getText().trim() );
            } catch (NumberFormatException x) {
                btns = 0;
            }
            if (btns == 0) {
                String msg = "Confirm: Do you really have zero Fringe buttons remaining?";
                String title = "Reconciliation Report: No Fringe Buttons";
                if (JOptionPane.showConfirmDialog( null, msg, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            cash = (int) ((((Number) ftfCash.getValue()).doubleValue() * 100) + 0.1);
            if (cash == 0) {
                String msg = "Confirm: Do you really have zero cash on-hand?";
                String title = "Reconciliation Report: No Cash";
                if (JOptionPane.showConfirmDialog( null, msg, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            surplus = (int) ((((Number) ftfSurplus.getValue()).doubleValue() * 100) + 0.1);

            int wid = bo.wd.getID();

//            if ((cash != expectedCash) || (btns != expectedButtons)) {
//                String msg = "    STOP!!    \n" +
//                             "==============\n\n\n" +
//                             "Do not do anything else yet.\n\n" +
//                             "PHONE BOX OFFICE CENTRAL NOW.\n\n" +
//                             "Has Box Office Central given you the go-ahead to click the \"Yes\" Button?\n";
//
//                if (JOptionPane.showConfirmDialog( null, msg, "Cash Count Mismatch", JOptionPane.YES_NO_OPTION,
//                                                   JOptionPane.ERROR_MESSAGE ) == JOptionPane.NO_OPTION) {
//                    return;
//                }
//            }

            if ((btns == expectedButtons) && (cash == expectedCash) && (surplus == 0)) {
                bo.processTxn( Transactions.makeTxnCashAdjustment( wid, 0, "Other", txfNote.getText() ) );
            }
            if (btns != expectedButtons) {
                bo.wd.adjustButtons( expectedButtons - btns );
                bo.processTxn( Transactions.makeTxnButtonAdjustment( wid, expectedButtons - btns, txfNote.getText() ) );
            }
            if (cash != expectedCash) {
                bo.wd.adjustCash( expectedCash - cash );
                bo.processTxn( Transactions.makeTxnCashAdjustment( wid, expectedCash - cash, "Other", txfNote.getText() ) );
            }
            if (surplus != 0) {
                bo.wd.adjustCash( surplus );
                bo.processTxn( Transactions.makeTxnCashAdjustment( wid, surplus, "Surplus", null ) );
            }
        }

        dispose();
    }
}
