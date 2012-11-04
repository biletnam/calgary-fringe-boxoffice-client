import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;

public class OptionDialog extends JDialog implements ActionListener, OptionListener {
    private JButton btnDlgOk;
    private JButton btnDlgCancel;
    private JPanel pnlNote;
    private JTextField txfNote;
    private JLabel lblNoteTitle;
    private Performance p;
    private OptionPanel pnlOptions;

    public OptionDialog( Vector<Selectable> options, Performance p, BoxOffice bo ) {
        super( bo.getFrame(), "Select Reason", true );

        this.p = p;
        Point pt = getLocation();
        setBounds( pt.x + 200, pt.y + 200, 375, 500 );

        btnDlgOk = new JButton( "Issue tickets" );
        btnDlgCancel = new JButton( "Cancel" );
        pnlOptions = new OptionPanel( options, null, "Indicate reason for issuing tickets:", this, bo.getMessageLogger() );

        pnlNote = new JPanel( new GridLayout( 2, 1 ) );
        pnlNote.setBorder( new EmptyBorder( 0, 10, 10, 10 ) );
        lblNoteTitle = new JLabel( "Enter reason:", JLabel.LEFT );
        lblNoteTitle.setFont( BoxOffice.defaultFont() );
        pnlNote.add( lblNoteTitle );
        txfNote = new JTextField( "", 30 );
        txfNote.setFont( BoxOffice.defaultFont() );
        pnlNote.add( txfNote );
        pnlNote.setVisible( false );

        JPanel pnlContent = new JPanel( new BorderLayout() );
        pnlContent.add( pnlOptions, BorderLayout.CENTER );
        pnlContent.add( pnlNote, BorderLayout.SOUTH );

        setContentPane( new DialogPanel( btnDlgOk, btnDlgCancel, pnlContent, this ) );
    }

    public void actionPerformed( ActionEvent e ) {
        String note = null;

        if (e.getSource() == btnDlgOk) {
            String reason = pnlOptions.getSelected().toString();
            BoxOffice bo = p.app();

            if ((txfNote.getText() != null) && (txfNote.getText().length() > 0)) {
                note = txfNote.getText();
            }

            int n = bo.askNumberOfItems( "Number of " + reason + " tickets issued?", 0 );
            if (! p.spaceLeft( n )) {
                JOptionPane.showMessageDialog( null, "Not enough remaining capacity.",
                                               "Can't Issue " + n + " Tickets", JOptionPane.WARNING_MESSAGE );
                return;
            }
            if (n != 0) {
                int rsn = pnlOptions.getSelectedOption() + 1;
                if (rsn == 2) {
                    // Reason code: Media
                    if (note == null) {
                        note = JOptionPane.showInputDialog( "Enter name of Media source:" );
                    }
                    if (note != null) {
                        p.addMediaName( note );
                    }
                } else if (rsn == 3) {
                    // Reason code: Artist Password
                    p.updateArtistComps( n );
                } else if (rsn == 5) {
                    // Reason code: Superpass
                    p.updateSuperpasses( n );
                } else if (n == 7) {
                    // Reason code: Other
                    if (note == null) {
                        note = JOptionPane.showInputDialog( "Why are you issuing these extra tickets?" );
                    }
                }
                bo.processTxn( Transactions.makeTxnIssueComp( p.getID(), n, rsn, note ) );
                p.updateComps( n );

                dispose();
            }
        } else if (e.getSource() == btnDlgCancel) {
            dispose();
        }
    }

    public void optionSelected( OptionEvent e ) {
        if (e.getSource() == pnlOptions) {
            txfNote.setText( "" );
            switch (e.getOption()) {
                case 2:
                    // Media
                    lblNoteTitle.setText( "Enter name of Media source:" );
                    pnlNote.setVisible( true );
                    break;
                case 7:
                    // Other
                    lblNoteTitle.setText( "Enter reason:" );
                    pnlNote.setVisible( true );
                    break;
                default:
                    pnlNote.setVisible( false );
            }
            getContentPane().validate();
        }
    }
}
