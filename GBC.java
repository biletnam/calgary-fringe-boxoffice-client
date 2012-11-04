import java.awt.Container;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class GBC extends GridBagConstraints {
    public GBC() {
        this( 1.0, BOTH );
    }

    public GBC( double weights ) {
        this( weights, BOTH );
    }

    public GBC( int fill ) {
        this( 1.0, fill );
    }

    public GBC( double weights, int fill ) {
        super( 0, 0, 1, 1, weights, weights, NORTHWEST,
               fill, new Insets( 5, 5, 5, 5 ), 0, 0 );
    }

    public void addComponent( Container ctr, Component cmp, int x, int y ) {
        addComponent( ctr, cmp, x, y, 1, 1 );
    }

    public void addComponent( Container ctr, Component cmp, int x, int y, int wd, int ht ) {
        gridx = x;
        gridy = y;
        gridwidth = wd;
        gridheight = ht;

        ((GridBagLayout) ctr.getLayout()).setConstraints( cmp, this );
        ctr.add( cmp );
    }
}
