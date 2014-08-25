package edu.pdx.codonpdx;

import com.rabbitmq.client.AMQP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

// This class is used to write messages to a RabbitMQ server, where a Python service is running celery to schedule jobs
public class TaskScheduler extends QueueObject {

    private static TaskScheduler tsInstance = null;

    // Queue related variables
    private AMQP.BasicProperties properties;

    // Constrctor
    //Sets default values based on input, those input start from the config file (MQ)
    private TaskScheduler (String queue, String host, String user, String password, String vhost) throws IOException{
        QUEUE_NAME = queue;
        HOST = host;
        USER = user;
        PASSWORD = password;
        VHOST = vhost;
        properties = new AMQP.BasicProperties("application/json",null,null,null,null,null,null,null,null,null,null,user,null,null);
        openConnect();
    }

    public static TaskScheduler getInstance(String name, String host, String user, String password, String vhost) throws IOException {
        if (tsInstance == null)
            tsInstance = new TaskScheduler(name, host, user, password, vhost);
        return TaskScheduler.tsInstance;
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
        argumentListArray.put(organismListArray);
        //Make the json object
        json.put("utc", true);
        json.put("args", argumentListArray);
        json.put("taskset", taskList);
        json.put("id", id);
        json.put("task", task);
        System.out.println("Sending: " + json.toString());
        channel.basicPublish(QUEUE_NAME, QUEUE_NAME, properties, json.toString().getBytes("ASCII"));
        System.out.println(" Sent : " + json.toString());
        return id;
    }

}