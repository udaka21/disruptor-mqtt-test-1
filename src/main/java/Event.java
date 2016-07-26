/**
 * Created by udaka on 7/25/16.
 * The Event that will carry the data.
 */
public class Event {

    private String message;

    public void setValue (String message) {
        this.message = message;
    }

    public String getValue () {
       // System.out.println(message);
        return this.message;

    }


}
