import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DialogPanel extends JPanel {
    DialogPanel( JButton btnDlgOk, JButton btnDlgCancel, JPanel pnlDlgMain, ActionListener cb ) {
        this( btnDlgOk, null, btnDlgCancel, true, pnlDlgMain, cb );
    }
    
    DialogPanel( JButton btnDlgOk, JButton[] btnDlgOther, JButton btnDlgCancel,
                 boolean okenabled, JPanel pnlDlgMain, ActionListener cb ) {
        this( null, btnDlgOk, btnDlgOther, btnDlgCancel, okenabled, pnlDlgMain, cb );
    }

    DialogPanel( String caption, JButton btnDlgOk, JButton[] btnDlgOther, JButton btnDlgCancel,
                 boolean okenabled, JPanel pnlDlgMain, ActionListener cb ) {
        super( new BorderLayout() );

        JPanel pnlDlgBottom = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        add( pnlDlgBottom, BorderLayout.SOUTH );

        if (caption != null) {
            pnlDlgBottom.add( BoxOffice.fontLabel( caption ) );
        }

        btnDlgOk.setFont( BoxOffice.defaultFont() );
        if (! okenabled) {
            btnDlgOk.setEnabled( false );
        }
        if (cb != null) {
            btnDlgOk.addActionListener( cb );
        }
        pnlDlgBottom.add( btnDlgOk );

        if ((btnDlgOther != null) && (btnDlgOther.length > 0)) {
            for (JButton b : btnDlgOther) {
                b.setFont( BoxOffice.defaultFont() );
                if (cb != null) {
                    b.addActionListener( cb );
                }
                pnlDlgBottom.add( b );
            }
        }

        btnDlgCancel.setFont( BoxOffice.defaultFont() );
        if (cb != null) {
            btnDlgCancel.addActionListener( cb );
        }
        pnlDlgBottom.add( btnDlgCancel );

        add( pnlDlgMain, BorderLayout.CENTER );
    }
}
