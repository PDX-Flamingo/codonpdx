package edu.pdx.codonpdx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;

// This class is used to write messages to a RabbitMQ server, where a Python service is running celery to schedule jobs
public class TaskScheduler extends QueueObject {

    // Queue related variables
    private AMQP.BasicProperties properties;

    //Message strings, some of this could probably be removed.
    private String testString = "{\"expires\": null, \"utc\": true, \"args\": [%1$d], \"chord\": null, \"callbacks\": null, \"errbacks\": null, \"taskset\": \"%2$s\", \"id\": \"%3$s\", \"retries\": 0, \"task\": \"%4$s\", \"timelimit\": [null, null], \"eta\": null, \"kwargs\": {}}";
    private String startJob = "{\"utc\": true, \"args\": [%1$s, %2$s, \"%3$s\", \"%4$s\"], \"taskset\": \"%5$s\", \"id\": \"%6$s\", \"task\": \"%7$s\", \"kwargs\": {}}";

    // Constrctor
    public TaskScheduler (String queue, String host) throws IOException{
        QUEUE_NAME = queue;
        HOST = host;
        properties = new AMQP.BasicProperties("application/json",null,null,null,null,null,null,null,null,null,null,"guest",null,null);
        openConnect();
    }

    public String scheduleTask(String id, String task, String file, String database, String format) throws IOException {
        if(!this.connectToQueue()) {
            System.out.println("Could not connect to queue to write");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);  // Some of this should be moved to separate methods
        String taskList = UUID.randomUUID().toString();  // find out if taskList is even needed
        String message = formatter.format(startJob, "\"" + id + "\"", "\"/opt/share/" + file + "\"", database.toLowerCase(), format.toLowerCase(), taskList, id, task).toString();
        channel.basicPublish(QUEUE_NAME, QUEUE_NAME, properties, message.getBytes("ASCII"));
        System.out.println(" Sent : " + message);
        return id;
    }

}