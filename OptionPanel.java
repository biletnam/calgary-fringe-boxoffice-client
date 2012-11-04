import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;

public class OptionPanel extends JPanel implements ActionListener {
    private int option;
    private GBC gbc;
    private OptionListener cb;
    transient private MessageLogger msglog;

    private Vector<Selectable> optionlist;

    public OptionPanel( Vector<Selectable> olist, String listcaption, MessageLogger msglog ) {
        this( olist, null, listcaption, null, msglog );
    }

    public OptionPanel( Vector<Selectable> olist, JButton btnSelect, String listcaption, MessageLogger msglog ) {
        this( olist, btnSelect, listcaption, null, msglog );
    }

    public OptionPanel( Vector<Selectable> olist, JButton btnSelect, String listcaption, OptionListener cb, MessageLogger msglog ) {
        super( new GridBagLayout() );

        ButtonGroup grp = new ButtonGroup();
        JRadioButton[] optItems = new JRadioButton[ olist.size() ];

        this.cb = cb;
        this.msglog = msglog;

        int i;
        int n;
        String caption;
        boolean selected = false;

        optionlist = olist;

        gbc = new GBC( 0.2, GBC.NONE );
        gbc.insets = new Insets( 2, 40, 2, 5 );
        gbc.anchor = GBC.SOUTHWEST;

        JLabel lblSelectionTitle = new JLabel( listcaption );
        lblSelectionTitle.setFont( BoxOffice.defaultFont( 16 ) );
        gbc.addComponent( this, lblSelectionTitle, 0, 0 );

        gbc.weighty = 0.0;

        i = 0;
        n = olist.size();
        for (Selectable opn : olist) {
            if ((btnSelect == null) && (i == n - 1)) {
                gbc.weighty = 1.0;
                gbc.anchor = GBC.NORTHWEST;
            }

            optItems[ i ] = new JRadioButton( opn.toString() );
            optItems[ i ].setFont( BoxOffice.defaultFont() );
            grp.add( optItems[ i ] );
            if (opn.canSelect()) {
                if (!selected) {
                    optItems[ i ].setSelected( true );
                    option = opn.getID();
                    selected = true;
                }
            } else {
                optItems[ i ].setEnabled( false );
            }
            optItems[ i ].addActionListener( this );

            gbc.addComponent( this, optItems[ i ], 0, i + 1 );
            i++;
        }

        gbc.weighty = 1.0;
        gbc.anchor = GBC.NORTHWEST;

        if (btnSelect != null) {
            btnSelect.setEnabled( selected );
            gbc.addComponent( this, btnSelect, 0, i + 1 );
        }
    }

    public void actionPerformed( ActionEvent e ) {
        String caption = ((JRadioButton) e.getSource()).getText();

        for (Selectable opt : optionlist) {
            msglog.print( "+" );
            if (opt.toString().equals( caption )) {
                option = opt.getID();
                break;
            }
        }
        if (cb != null) {
            cb.optionSelected( new OptionEvent( this, option + 1 ) );
        }
        msglog.println( "\n" + Integer.toString( option ) );
    }
    
    public Selectable getSelected() {
        for (Selectable s : optionlist) {
            if (s.getID() == option) {
                return s;
            }
        }

        return null;
    }

    public int getSelectedOption() {
        return option;
    }

    public void setMessageLogger( MessageLogger msglog ) {
        this.msglog = msglog;
    }
}
