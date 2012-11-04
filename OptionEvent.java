import java.io.Serializable;

public class OptionEvent implements Serializable {
    Object src;
    int option;

    public OptionEvent( Object src, int option ) {
        this.src = src;
        this.option = option;
    }

    public Object getSource() {
        return src;
    }

    public int getOption() {
        return option;
    }
}
