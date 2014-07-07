package edu.pdx.codonpdx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * This is an abstract class which encompasses some of the shared
 * behavior between any of the classes which interact with the message
 * queues.
 */
public abstract class QueueObject {
    // Basic connection information
    protected String QUEUE_NAME;
    protected String HOST;
    protected Connection connection;
    protected ConnectionFactory factory;
    protected Channel channel;


    // Opens a connection based on set queue and host
    public void openConnect() throws IOException {
        factory = new ConnectionFactory();
        factory.setHost(HOST);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    // Attempts to connect to the queue requests, if fails, returns false
    public boolean connectToQueue() {
        try {
            channel.queueDeclarePassive(QUEUE_NAME);
            return true;
        }
        catch (IOException e)
        {
            System.out.println("Queue does not exist");
            return false;
        }
    }

    // method provided to clean up when user finished
    public void closeConnect() throws IOException {
        channel.close();
        connection.close();
    }
}
