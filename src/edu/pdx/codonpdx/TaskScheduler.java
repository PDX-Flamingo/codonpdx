package edu.pdx.codonpdx;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import java.util.UUID;
import org.json.*;

// This class is used to write messages to a RabbitMQ server, where a Python service is running celery to schedule jobs
public class TaskScheduler extends QueueObject {

    // Queue related variables
    private AMQP.BasicProperties properties;

    //Message string
    private final String startJob = "{\"utc\": true, \"args\": [%1$s, %2$s, \"%3$s\", \"%4$s\", \"%5$s\"], \"taskset\": \"%6$s\", \"id\": \"%7$s\", \"task\": \"%8$s\", \"kwargs\": {}}";

    // Constrctor
    public TaskScheduler (String queue, String host, String user, String password, String vhost) throws IOException{
        QUEUE_NAME = queue;
        HOST = host;
        USER = user;
        PASSWORD = password;
        VHOST = vhost;
        properties = new AMQP.BasicProperties("application/json",null,null,null,null,null,null,null,null,null,null,user,null,null);
        openConnect();
    }

    // Schedule a task with a random UUID for the id.  id is returned
    // so that the results of the message can be retrieved if needed


    public String scheduleTask(String id, String task, String file, String database, String format, String path, String[] organismList) throws IOException {
        if(!this.connectToQueue()) {
            System.out.println("Could not connect to queue to write");
            return null;
        }
        String taskList = UUID.randomUUID().toString();  // find out if taskList is even needed
        StringBuilder sb = new StringBuilder();
        JSONObject json = new JSONObject();
        // Create the organism list
        JSONArray organismListArray = new JSONArray();
        for(String s : organismList) {
            organismListArray.put(s);
        }
        // Create the ArumentList
        JSONArray argumentListArray = new JSONArray();
        argumentListArray.put(id);
        argumentListArray.put(path+file);
        argumentListArray.put(database.toLowerCase());
        argumentListArray.put(format.toLowerCase());
        //argumentListArray.put(organismListArray);
        //Make the json object
        json.put("utc", true);
        json.put("args", argumentListArray);
        json.put("taskset", taskList);
        json.put("id", id);
        json.put("task", task);
        channel.basicPublish(QUEUE_NAME, QUEUE_NAME, properties, json.toString().getBytes("ASCII"));
        System.out.println(" Sent : " + json.toString());
        return id;
    }

}