import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ArtistReportPanel extends JPanel {
    private String artistName;
    private String showName;
    private String venueName;
    private String today;
    private String totalPayout;
    private int artistPrice;
    private int ticketPrice;
    private int ticketSales;
    private int superpasses;
    private int tenDollars;
    private int artistComps;
    private int otherComps;

    private Vector<String> allMedia;
    private ImageIcon bgImage;

    public ArtistReportPanel( Performance p, ImageIcon bgImage ) {
        super();
        
        this.bgImage = bgImage;

        artistName = p.getArtist();
        showName = p.getShowName();
        venueName = p.getVenueName();
        today = new SimpleDateFormat( "EEEE, MMMM dd, yyyy" ).format( new Date() );
        ticketPrice = p.getTicketPrice();
        artistPrice = p.getArtistPrice();
        artistComps = p.getArtistComps();
        superpasses = p.getSuperpasses();
        tenDollars = superpasses + p.getSpecialSalesQty( 1000 );
        otherComps = p.getTicketComps() - artistComps - superpasses;
        ticketSales = p.getBasicTicketSales();
        totalPayout = BoxOffice.formatCurr( p.getArtistPayout() );
        allMedia = p.getMediaNames( true );

        setLayout( null );
        setPreferredSize( new Dimension( 945, 730 ) );
        validate();
    }

    public void paintComponent( Graphics g ) {
        super.paintComponent( g );

        g.drawImage( bgImage.getImage(), 0, 0, null );

        g.setFont( BoxOffice.defaultFont( Font.BOLD + Font.ITALIC, 11 ) );
        g.setColor( Color.black );
        g.drawString( artistName, 155, 71 );
        g.drawString( showName, 155, 87 );
        g.drawString( venueName, 155, 103 );
        g.drawString( today, 155, 120 );
        g.drawString( BoxOffice.formatCurr( ticketPrice ), 155, 136 );
        g.drawString( BoxOffice.formatCurr( artistPrice ), 265, 259 );
        g.drawString( BoxOffice.formatCurr( 1000 ), 265, 274 );

        g.setColor( Color.red );
        g.drawString( "" + ticketSales, 185, 259 );
        g.drawString( "" + tenDollars, 185, 274 );
        g.drawString( BoxOffice.formatCurr( artistPrice * ticketSales ), 350, 259 );
        g.drawString( BoxOffice.formatCurr( 1000 * tenDollars ), 350, 274 );
        g.drawString( "" + otherComps, 770, 226 );
        g.drawString( "" + artistComps, 770, 242 );
        if (allMedia != null) {
            int i = 290;
            for (String mediaName : allMedia) {
                g.drawString( mediaName, 590, i );
                i += 15;
            }
        }

        g.setFont( BoxOffice.defaultFont( Font.BOLD, 14 ) );
        g.drawString( totalPayout, 350, 522 );
    }
}
