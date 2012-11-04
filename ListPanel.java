import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

public class ListPanel extends JPanel implements ListSelectionListener {
    private JList lstOptions;
    private Vector<Selectable> vslItems;
    private int option = -1;
    private EnabledListener cb;

    public ListPanel( Vector<Selectable> olist, String listcaption, EnabledListener cb ) {
        this( olist, null, listcaption, cb );
    }

    public ListPanel( Vector<Selectable> olist, JButton btnSelect, String listcaption, EnabledListener cb ) {
        super( new BorderLayout() );

        this.cb = cb;

        vslItems = olist;

        lstOptions = new JList( olist.toArray() );
        lstOptions.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
        lstOptions.setLayoutOrientation( JList.VERTICAL );
        lstOptions.setVisibleRowCount( -1 );
        lstOptions.addListSelectionListener( this );
        lstOptions.setFont( BoxOffice.defaultFont() );

        JScrollPane scrList = new JScrollPane( lstOptions );

        add( scrList, BorderLayout.CENTER );
    }

    public void valueChanged( ListSelectionEvent e ) {
        if (! e.getValueIsAdjusting()) {
            if (lstOptions.getSelectedIndex() == -1) {
                option = -1;
            } else {
                Selectable s = vslItems.get( lstOptions.getSelectedIndex() );
                if (s.canSelect()) {
                    option = s.getID();
                } else {
                    option = -1;
                }
            }

            cb.setEnabled( (option == -1 ? false : true) );
        }
    }

    public int getSelectedOption() {
        return option;
    }
}
