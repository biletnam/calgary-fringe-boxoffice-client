import java.io.Serializable;

public class SessionData implements Serializable {
    public String sessionID;
    public String username;
    public String gateway;
    
    public SessionData() {
        sessionID = null;
        username = null;
        gateway = "http://boxoffice.calgaryfringe.ca/gateway.php";
    }
}
