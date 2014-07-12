package edu.pdx.pdxcodon;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

/**
 * Basic class for consuming the response queues from scheduling tasks.
 */
public class ResponseConsumer extends QueueObject {

    public ResponseConsumer(String queue, String host) throws IOException {
        QUEUE_NAME = queue;
        HOST = host;
        openConnect();
    }

    // Sets up the consumer for the Q, and then consumes a single message.
    // This should probably be made into two separate classes
    public String getResponseFromQueue() throws IOException, InterruptedException {
        if(!connectToQueue()) {
            return null;
        }
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(QUEUE_NAME, true, consumer);
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
        System.out.println(" [x] Received '" + message + "'");
        return message;
    }

    public static void main(String[] argv)
            throws java.io.IOException,
            java.lang.InterruptedException {

        ResponseConsumer rc = new ResponseConsumer(argv[0], "localhost");
        String message = rc.getResponseFromQueue();
        rc.closeConnect();
        System.exit(1);
    }
 }
