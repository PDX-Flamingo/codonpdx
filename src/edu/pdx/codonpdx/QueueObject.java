package edu.pdx.codonpdx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by Robert on 7/6/2014.
 */
public abstract class QueueObject {
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

    // method provided to clean up when user finished
    public void closeConnect() throws IOException {
        channel.close();
        connection.close();
    }
}
