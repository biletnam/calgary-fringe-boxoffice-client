import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CaptionedComponent extends JPanel {
    JLabel caption;
    JComponent cmp;

    CaptionedComponent( String caption, JComponent cmp ) {
        this( BoxOffice.fontLabel( caption ), cmp, false );
    }

    CaptionedComponent( String caption, JComponent cmp, boolean fillWidth ) {
        this( BoxOffice.fontLabel( caption ), cmp, fillWidth );
    }

    CaptionedComponent( JLabel caption, JComponent cmp ) {
        this( caption, cmp, false );
    }

    CaptionedComponent( JLabel caption, JComponent cmp, boolean fillWidth ) {
        super();

        this.caption = caption;
        this.cmp = cmp;

        if (fillWidth) {
            setLayout( new BorderLayout() );

            add( caption, BorderLayout.WEST );
            add( cmp, BorderLayout.CENTER );
        } else {
            setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );

            add( caption );
            add( cmp );
        }
    }

    public void setEnabled( boolean enabled ) {
        super.setEnabled( enabled );
        
        caption.setEnabled( enabled );
        cmp.setEnabled( enabled );
    }    
}
