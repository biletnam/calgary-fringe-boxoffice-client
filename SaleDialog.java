import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;

public class SaleDialog extends JDialog implements ActionListener, KeyListener {
    private JButton btnDlgSale;
    private JButton btnDlgContinue;
    private JButton btnDlgCancel;

    private JButton[] btnRemoveSale;

    private BoxOffice bo;
    private SaleItem newItem;

    private JFormattedTextField ftfTendered;
    private JLabel lblChange;
    private int owing;

    public SaleDialog( BoxOffice bo ) {
        this( null, bo );
    }

    public SaleDialog( SaleItem newItem, BoxOffice bo ) {
        super( bo.getFrame(), "Make Sale", true );

        this.bo = bo;
        this.newItem = newItem;

        Point pt = getLocation();
        setBounds( pt.x + 200, pt.y + 200, 550, 400 );

        btnDlgSale = new JButton( "<HTML><B>PURCHASE NOW</B></HTML>" );
        btnDlgContinue = new JButton( "Add more to sale..." );
        btnDlgCancel = new JButton( "Cancel" );

        setDialogPanel();

        getRootPane().setDefaultButton( btnDlgSale );
    }

    private void setDialogPanel() {
        JButton[] btnsOther = { btnDlgContinue };

        JPanel pnlSale = new JPanel( new GridBagLayout() );
        GBC gbc = new GBC( 0.0, GBC.HORIZONTAL );
        gbc.anchor = GBC.NORTHWEST;
        gbc.weightx = 1.0;

        JLabel lblTitle = new JLabel( "Current Sale:" );
        lblTitle.setFont( BoxOffice.defaultFont( Font.BOLD, 14 ) );
        gbc.addComponent( pnlSale, lblTitle, 0, 0 );

        gbc.weighty = 1.0;
        gbc.fill = GBC.BOTH;

        Vector<String> descrs = bo.getCurrentSaleDescriptions();
        if (newItem != null) {
            descrs.add( newItem.getDescription() );
        }
        
        Box boxSaleContents = mkSalesList( descrs );

        JScrollPane scrSaleContents = new JScrollPane( boxSaleContents );
        scrSaleContents.setBorder( new LineBorder( Color.black ) );
        gbc.addComponent( pnlSale, scrSaleContents, 0, 1 );

        gbc.weighty = 0.0;
        gbc.fill = GBC.NONE;

        owing = bo.getCurrentSaleTotal();
        if (newItem != null) {
            owing += newItem.getAmount();
        }

        JLabel lblTotal = new JLabel( "Total owing: " + BoxOffice.formatCurr( owing ) );
        lblTotal.setFont( BoxOffice.defaultFont() );
        gbc.addComponent( pnlSale, lblTotal, 0, 2 );

        gbc.fill = GBC.HORIZONTAL;

        JPanel pnlTendered = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        JLabel lblTendered = new JLabel( "Amount Tendered: $" );
        lblTendered.setFont( BoxOffice.defaultFont() );
        pnlTendered.add( lblTendered );
        NumberFormat nftTendered = NumberFormat.getNumberInstance();
        nftTendered.setMaximumFractionDigits( 2 );
        nftTendered.setMinimumFractionDigits( 2 );
        ftfTendered = new JFormattedTextField( nftTendered );
        ftfTendered.setValue( new Double( ((double) owing) / 100 ) );
        ftfTendered.setColumns( 10 );
        ftfTendered.setFont( BoxOffice.defaultFont() );
        ftfTendered.addKeyListener( this );
        pnlTendered.add( ftfTendered );
        gbc.addComponent( pnlSale, pnlTendered, 0, 3 );

        lblChange = new JLabel( "Change: $0.00" );
        lblChange.setFont( BoxOffice.defaultFont() );
        gbc.addComponent( pnlSale, lblChange, 0, 4 );

        setContentPane( new DialogPanel( btnDlgSale, btnsOther, btnDlgCancel, true, pnlSale, this ) );
    }

    private Box mkSalesList( Vector<String> captions ) {
        Box boxSalesList = Box.createVerticalBox();

        EmptyBorder b = new EmptyBorder( 2, 2, 2, 2 );

        if (captions.size() == 0) {
            btnRemoveSale = null;
        } else {
            btnRemoveSale = new JButton[ captions.size() ];

            int i = 0;
            for (String desc : captions) {
                Box boxSale = Box.createHorizontalBox();

                JLabel lblSale = new JLabel( desc );
                lblSale.setFont( BoxOffice.defaultFont( 12 ) );
                lblSale.setHorizontalAlignment( JLabel.RIGHT );
                lblSale.setVerticalAlignment( JLabel.TOP );
                boxSale.add( lblSale );

                boxSale.add( Box.createHorizontalGlue() );
                btnRemoveSale[ i ] = new JButton( "X" );
                btnRemoveSale[ i ].setForeground( Color.red );
                btnRemoveSale[ i ].addActionListener( this );
                boxSale.add( btnRemoveSale[ i ] );

                boxSale.setBorder( b );
                boxSalesList.add( boxSale );

                i++;
            }
        }
        boxSalesList.add( Box.createVerticalGlue() );
        boxSalesList.setBorder( b );

        return boxSalesList;
    }

    public void actionPerformed( ActionEvent e ) {
        if (e.getSource() == btnDlgSale) {
            bo.addToSale( newItem );
            bo.processSale();
            dispose();
        } else if (e.getSource() == btnDlgContinue) {
            bo.addToSale( newItem );
            dispose();
        } else if (e.getSource() == btnDlgCancel) {
            dispose();
        } else if (btnRemoveSale.length > 0) {
            int salenum = -1;

            for (int i = 0; i < btnRemoveSale.length; i++) {
                if (e.getSource() == btnRemoveSale[ i ]) {
                    salenum = i;
                    break;
                }
            }
            
            if (salenum > -1) {
                if ((newItem != null) &&
                    (salenum == btnRemoveSale.length - 1)) {

                    newItem = null;
                } else {
                    bo.removeFromSale( salenum );
                }
                
                setDialogPanel();
            }
        }
    }

    public void keyTyped( KeyEvent e ) {
        // NOP
    }

    public void keyPressed( KeyEvent e ) {
        // NOP
    }

    public void keyReleased( KeyEvent e ) {
        if (e.getSource() == ftfTendered) {
            try {
                ftfTendered.commitEdit();
                int tendered = (int) (((Number) ftfTendered.getValue()).doubleValue() * 100);

                lblChange.setText( "Change: " + BoxOffice.formatCurr( tendered - owing ) );
                lblChange.setForeground( (owing > tendered) ? Color.red : Color.black );
            } catch (ParseException x) {
                // Don't worry about it for now... just see what else comes up down the line
            }
        }
    }
}
