import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventMain {

    private static final Log log = LogFactory.getLog(EventMain.class);

    public static void main(String[] args) throws Exception {


        //Set File path
        String fileName = "temp.txt";
        String line = null;
        int lines = 0;
        // variable lines Calculate number of lines of Selected file.
        BufferedReader reader = new BufferedReader(new FileReader(fileName));


        // Executor that will be used to construct new threads for consumers
        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        LocalEventFactory factory = new LocalEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 8;

        // Construct the Disruptor
        Disruptor<Event> disruptor = new Disruptor(factory, bufferSize, executor, ProducerType.SINGLE, new BlockingWaitStrategy());

        int numberOfConsumer = 2;


        LocalMqttClient[] localMqttClient = new LocalMqttClient[numberOfConsumer];
        MessagePublishEventHandler[] messagePublishEventHandler = new MessagePublishEventHandler[numberOfConsumer];

        for (int r = 0; r < numberOfConsumer; r++) {

            int mods = r % 2; // Because we use only 2 MB's
            int port = 1883 + mods; // making MB URL port

            // Connect the handler
            localMqttClient[r] = new LocalMqttClient("tcp://localhost:" + port, "Topic " + r, "publisher " + r);
            messagePublishEventHandler[r] = new MessagePublishEventHandler(localMqttClient[r], r, numberOfConsumer);
        }


        disruptor.handleEventsWith(messagePublishEventHandler);

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();

        EventProducer producer = new EventProducer(ringBuffer);


        try {
            //count number of lines on  the  temp.txt file.
            while (reader.readLine() != null) {
                lines++;
            }
            reader.close();
            // FileReader reads text files in the default encoding..
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            for (int i = 0; i < lines; i++) {
                //read line by line.
                line = bufferedReader.readLine();
                // submit messages to write concurrently using disruptor
                producer.onData(line);
            }
            bufferedReader.close();
            fileReader.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (disruptor != null) {
            disruptor.halt();
            disruptor.shutdown();
            log.info("Disruptor shutdown");

            for (int shtdwn = 0; shtdwn < numberOfConsumer; shtdwn++) {
                localMqttClient[shtdwn].clientShutdown(); // client shutdown
            }

        }


    }

}
