import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SaleStatusPanel extends JPanel {
    private JButton btnReset;
    private JLabel lblStatus;

    SaleStatusPanel( MouseListener cb1, ActionListener cb2 ) {
        this( cb1, cb2, "" );
    }

    SaleStatusPanel( MouseListener cb1, ActionListener cb2, String status ) {
        super( new FlowLayout( FlowLayout.LEFT ) );

        addMouseListener( cb1 );

        setBorder( new LineBorder( Color.black ) );
        BoxOffice.setComponentColor( this, Color.white );

        lblStatus = new JLabel();
        lblStatus.setFont( BoxOffice.defaultFont() );
        BoxOffice.setComponentColor( lblStatus, Color.white );
        add( lblStatus );
        
        add( new JLabel( "  " ) ); // Quick and dirty spacing :-)

        btnReset = new JButton( "X" );
        btnReset.setForeground( Color.red );
        btnReset.addActionListener( cb2 );
        add( btnReset );

        updateStatus( status );
    }

    public void updateStatus( String newStatus ) {
        lblStatus.setText( "<HTML>" + newStatus + "</HTML>" );
        validate();
    }

    public boolean triggeredAction( ActionEvent e ) {
        return ((e.getSource() == btnReset) ||
                (e.getSource() == this));
    }
}
