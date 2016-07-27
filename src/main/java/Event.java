/**
 * The Event that will carry the data.
 */
public class Event {

    private String message;

    public void setValue(String message) {
        this.message = message;
    }

    public String getValue() {
        return this.message;

    }

    public void clear() {

        this.message = null;

    }


}
