
/**
 * create a consumer that will handle these events.
 * In our case all we want to do is print the value out the the console.
 */
public class MessagePublishEventHandler implements com.lmax.disruptor.EventHandler<Event> {

    LocalMqttClient mqClient;
    private final long ordinal;
    private final long numberOfConsumers;

    public MessagePublishEventHandler(LocalMqttClient mqClient, final long ordinal, final long numberOfConsumers) {


        this.mqClient = mqClient;
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;

    }


    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {

        if ((sequence % numberOfConsumers) == ordinal) {
            try {

                byte[] stringEvent = event.getValue().getBytes();
                System.out.println("Event: " + new String(stringEvent));

                // Publishing to mqtt topic "simpleTopic"
                mqClient.publishMessage(event.getValue().getBytes());

                //mqttPublisherClient.disconnect();
            } catch (Exception e) {

            } finally {
                event.clear();
            }

        }

    }


}


