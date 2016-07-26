import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventMain
{
    public static void main(String[] args) throws Exception
    {
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
        Disruptor<Event> disruptor = new Disruptor<Event>(factory, bufferSize, executor);
      /** Disruptor<Event> disruptor = new Disruptor(factory, bufferSize, executor, // Single producer
                ProducerType.SINGLE,
                new BlockingWaitStrategy());
        */

        // Connect the handler
        LocalMqttClient mq = new LocalMqttClient("tcp://localhost:1883","Topic 1");
        MessagePublishEventHandler msgHandler = new MessagePublishEventHandler(mq,0,2);
        disruptor.handleEventsWith(msgHandler);

        LocalMqttClient mq1 = new LocalMqttClient("tcp://localhost:1884","Topic 2");
        MessagePublishEventHandler msgHandler1 = new MessagePublishEventHandler(mq1,1,2);
        disruptor.handleEventsWith(msgHandler1);

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
                Thread.sleep(100);
                }
            bufferedReader.close();
            fileReader.close();


        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }



    }

}
