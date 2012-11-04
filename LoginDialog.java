import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LoginDialog extends JDialog implements ActionListener, KeyListener {
    private JButton btnDlgOk;
    private JButton btnDlgCancel;
    private JTextField txfName;
    private JPasswordField txfPassword;

    private BoxOffice bo;

    public LoginDialog( BoxOffice bo ) {
        super( bo.getFrame(), "Calgary Fringe Festival Box Office Login", true );

        this.bo = bo;
        Point p = getLocation();
        setBounds( p.x + 200, p.y + 200, 300, 175 );
        setResizable( false );

        GBC gbc = new GBC( 0.0, GBC.HORIZONTAL );

        JPanel pnlDlgMain = new JPanel( new GridBagLayout() );

        JLabel lblName = new JLabel( "User name:" );
        lblName.setFont( BoxOffice.defaultFont() );
        gbc.addComponent( pnlDlgMain, lblName, 0, 0 );

        txfName = new JTextField( 32 );
        txfName.setFont( BoxOffice.defaultFont() );
        txfName.addKeyListener( this );
        gbc.weightx = 1.0;
        gbc.addComponent( pnlDlgMain, txfName, 1, 0 );

        JLabel lblPassword = new JLabel( "Password:" );
        lblPassword.setFont( BoxOffice.defaultFont() );
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.addComponent( pnlDlgMain, lblPassword, 0, 1 );

        txfPassword = new JPasswordField( 32 );
        txfPassword.setFont( BoxOffice.defaultFont() );
        txfPassword.addKeyListener( this );
        gbc.weightx = 1.0;
        gbc.addComponent( pnlDlgMain, txfPassword, 1, 1 );

        btnDlgOk = new JButton( "Login" );
        btnDlgCancel = new JButton( "Cancel" );
        setContentPane( new DialogPanel( btnDlgOk, null, btnDlgCancel, false, pnlDlgMain, this ) );
        
        getRootPane().setDefaultButton( btnDlgOk );
    }

    private String buildPassword( char[] pwd ) {
        StringBuffer sb = new StringBuffer( pwd.length );

        for (char c : pwd) {
            if (Character.isLetterOrDigit( c ) || c == '_') {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    public void actionPerformed( ActionEvent e ) {
        String auth;
        String user;
        
        boolean loginOk = false;

        if (e.getSource() == btnDlgCancel) {
            dispose();
        } else if (e.getSource() == btnDlgOk) {
            user = txfName.getText();

            auth = bo.getDBLogin( user, buildPassword( txfPassword.getPassword() ) );
            
            loginOk = (auth != null);
            if (loginOk) {
                bo.doLogin( user, auth.trim() );
            }
        }

        dispose();
        bo.loadMainScreen( loginOk );
    }

    public void keyPressed( KeyEvent e ) {
        // NOP
    }

    public void keyReleased( KeyEvent e ) {
        if ((txfName.getText().length() > 0) && (txfPassword.getPassword().length > 0)) {
            btnDlgOk.setEnabled( true );
        } else {
            btnDlgOk.setEnabled( false );
        }
    }

    public void keyTyped( KeyEvent e ) {
        // NOP
    }
}
