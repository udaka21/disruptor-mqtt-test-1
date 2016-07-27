import com.lmax.disruptor.RingBuffer;


/**
 * Created by udaka on 7/25/16.
 **/
public class EventProducer {
    //initialize the ring buffer
    private final RingBuffer<Event> ringBuffer;


    public EventProducer(RingBuffer<Event> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void onData(String message) {

        long sequence = ringBuffer.next();// Grab the next sequence


        try {
            Event event = ringBuffer.get(sequence);// Get the entry in the Disruptor
            // for the sequence

            event.setValue(message);// Fill with data
            //System.out.println(event.getValue());

        } finally {
            ringBuffer.publish(sequence);
        }

    }

}
