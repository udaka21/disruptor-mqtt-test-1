import com.lmax.disruptor.EventFactory;

/**
 * Created by udaka on 7/25/16.
 * In order to allow the Disruptor to preallocate these events for us,
 * We need to an EventFactory that will perform the construction
 */
public class LocalEventFactory implements EventFactory<Event> {

    //Creating new instance for fill the ring buffer
    public Event newInstance() {
       // System.out.println(new Event());
        return new Event();
    }

}
