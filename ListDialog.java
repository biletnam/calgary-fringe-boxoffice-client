import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

public class ListDialog extends JDialog implements ActionListener, EnabledListener {
    private JButton btnDlgOk;
    private JButton btnDlgCancel;
    private Performance p;
    private ListPanel pnlList;

    public ListDialog( int remaining, Vector<Selectable> options, Performance p, BoxOffice bo ) {
        super( bo.getFrame(), "Select Name", true );

        this.p = p;
        Point pt = getLocation();
        setBounds( pt.x + 200, pt.y + 200, 400, 400 );

        btnDlgOk = new JButton( "Pick Up" );
        btnDlgCancel = new JButton( "Cancel" );
        pnlList = new ListPanel( options, "Select person picking up presale tickets:", this );
        String caption = remaining + " ticket(s) remaining...     ";

        setContentPane( new DialogPanel( caption, btnDlgOk, null, btnDlgCancel, false, pnlList, this ) );
    }

    public void actionPerformed( ActionEvent e ) {
        if (e.getSource() == btnDlgOk) {
            int option = pnlList.getSelectedOption();
            if (option != -1) {
                BoxOffice bo = p.app();

                for (Presale ps : p.presales) {
                    if (ps.getID() == option) {
                        p.useUpCapacity( ps.redeem() );
                        bo.processTxn( Transactions.makeTxnRedeemPresale( p.getID(), Integer.toString( option ) ) );
                        dispose();
                    }
                }
            }
        } else if (e.getSource() == btnDlgCancel) {
            dispose();
        }
    }

    public void setEnabled( boolean enabled ) {
        btnDlgOk.setEnabled( enabled );
    }
}
