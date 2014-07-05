package edu.pdx.codonpdx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;

// This class is used to write messages to a RabbitMQ server, where a Python service is running celery to schedule jobs
public class TaskScheduler {

    // Queue related variables
    private String QUEUE_NAME;
    private String HOST;
    private Connection connection;
    private ConnectionFactory factory;
    private Channel channel;
    private AMQP.BasicProperties properties;

    //Message strings
    private String testString = "{\"expires\": null, \"utc\": true, \"args\": [%1$d], \"chord\": null, \"callbacks\": null, \"errbacks\": null, \"taskset\": \"%2$s\", \"id\": \"%3$s\", \"retries\": 0, \"task\": \"%4$s\", \"timelimit\": [null, null], \"eta\": null, \"kwargs\": {}}";

    public TaskScheduler (String queue, String host) throws IOException{
        QUEUE_NAME = queue;
        HOST = host;
        properties = new AMQP.BasicProperties("application/json",null,null,null,null,null,null,null,null,null,null,"guest",null,null);
        openConnect();
    }

    public void openConnect() throws IOException {
        factory = new ConnectionFactory();
        factory.setHost(HOST);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public String scheduleTask(String task) throws IOException {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        String taskList = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        String message = formatter.format(testString, 5, taskList, id, task).toString();
        channel.basicPublish(QUEUE_NAME, QUEUE_NAME, properties, message.getBytes("ASCII"));
        System.out.println(" Sent : " + message);
        return id;
    }

    public void closeConnect() throws IOException {
        channel.close();
        connection.close();
    }

    public static void main(String[] argv)
            throws java.io.IOException {
        TaskScheduler ts = new TaskScheduler("celery", "localhost");
        ts.scheduleTask("proj.tasks.random_list");
        ts.closeConnect();
    }
}