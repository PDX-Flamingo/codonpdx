package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.*;
import java.util.*;

/**
 * Created by Robert on 7/7/2014.
 */

/**
 * This is the main servlet for codonpdx, people will navigate to urls on the server
 * and this is the part that takes care of them.
 * the two main sections are doGet and doPost which take get / post requests,
 * these filter what the url are and go to the correct function based on that.
 *
 */

public class CodonPDX extends HttpServlet {

    PrintWriter out = null;

    Process p;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            //The "request" is the incoming url, the "response" is the output going back to the user
            throws ServletException, IOException {
        //This is the output back to the web browser, the type has not be specified
        out = response.getWriter();
        //This splits the get request url into a string array, each piece can be accessed by position in the request
        //(So the first one is 0, the 2nd one is 1, the 3rd one is 2.
        //Example: /codonpdx/app/somewhere would be split into
        //[0]
        //[1] codonpdx
        //[2] app
        //[3] somewhere
        String[] URI = request.getRequestURI().split("/");
        try {
            //This statement makes it switch to the case where the area following codonpdx/
            //if it's longer than codonpdx
            //example /codonpdx/app goes to app
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "app":
                    //This sends back the based web page without any funny stuff
                    request.getRequestDispatcher("/index.html").forward(request, response);
                    break;
                case "results":
                    //This checks the url length to see if there is only one result selected or more
                    if (URI.length == 4) {
                        out.print(getResultsOneToMany(URI[3]));
                        response.setContentType("application/json");
                    } else if (URI.length == 5) {
                        //While this is being passed in, it's splitting the url at position 5
                        //(which is 4 not counting the empty string) into multiple sub strings
                        out.print(getResultsOneToOne(URI[3], URI[4].split("&&&")));
                        response.setContentType("application/json");
                    } else
                        out.println("Error with request, check the URL again");
                    break;
                case "resultsaggr":
                    if (URI.length == 4) {
                        out.println(getResultsOneToManyAggregated(URI[3]));
                        response.setContentType("application/json");
                    } else
                        out.println("Error with request, check the URL again");
                    break;
                case "dlCSV":
                    //This is for downloading a csv of the results
                    if (URI.length == 4) {
                        response.setContentType("text/csv");
                        response.setHeader("filename", URI[3] + ".csv");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + URI[3] + ".csv" + "\"");
                        out.println(getCSVString(URI[3]));
                    }
                    else {
                        out.println("Error with request, check the URL again");
                    }
                    break;
                case "list":
                    //This goes to the area of the app that displays a list in the drop down menu
                    if (URI.length == 4) {
                        response.setContentType("application/json");
                        out.println(getOrganismList(URI[3].replace("%20", " ")));
                    }
                    else {
                        out.println("Error with request, check the URL again");
                    }
                    break;

                default:
                    //if nothing is supplied, we get the main page as default
                    request.getRequestDispatcher("/index.html").forward(request, response);
            }
        } catch (IOException e) {
            out.println(e.getMessage());
        }

    }


    //This is like the doGet area above
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            //This is like the same thing in the doGet, it is the output to the user
            out = response.getWriter();
            //This generates a random UUID in the format for our application
            //(It's taking out - in the string)
            String uuid = UUID.randomUUID().toString().replace("-", "");
            //As above in the doGet, this takes the URL and splits it into a string array
            //(See above in the doGet)
            String[] URI = request.getRequestURI().split("/");
            //Same sort of thing as in the doGet except that it only has a single case that it responds to
            //it takes in the properties from the properties file, takes in input from the request,
            //and it creates a file on the disk with the input from the request contained in it
            //(The file name is the UUID generated before)
            //After all this it uses a function that sends a ratio comparison request to celery
            switch (request.getRequestURI()) {
                case "/codonpdx/submitRequest":
                    Configuration config = new PropertiesConfiguration("folder.properties");
                    ParseResponse prbody = new ParseResponse(request.getReader());
                    prbody.parseInput();
                    File f = new File(config.getString("folder.share"), uuid);
                    Writer fileWriter = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fileWriter);
                    bw.write(prbody.fileContents);
                    bw.close();
                    fileWriter.close();
                    JSONObject json = new JSONObject();
                    json.put("UUID", uuid);
                    response.setContentType("application/json");
                    scheduleRatioCompare(uuid, "refseq", prbody.fileType, config.getString("folder.share"), prbody.comparisonIds);
                    out.println(json);

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private void scheduleRatioCompare(String uuid, String database, String format, String path, String[] organismList) throws InterruptedException, IOException {
        try {
            Process p = Runtime.getRuntime().exec("/vol/www/pdxcodon/codonpdx-python/celery.sh start");
            p.waitFor();
            //This grabs the properites for rabbitMQ from the MQ file
            Configuration config = new PropertiesConfiguration("mq.properties");
            //Makes a TaskScheduler object that connects to the queue
            TaskScheduler ts = TaskScheduler.getInstance(config.getString("queue.name"), config.getString("queue.host"), config.getString("queue.user"), config.getString("queue.password"), config.getString("queue.vhost"));
            //This is the function that places it on the queue
            ts.scheduleTask(uuid, "codonpdx.tasks.create_result_from_input_file", uuid, database, format, path, organismList);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            out.println(e.getMessage());
        }
    }

    //This gets the object that is being passed to the front end for displaying the one to many chart
    private JSONObject getResultsOneToMany(String uuid) {
        try {
            //brings in database properties from the properties file
            Configuration config = new PropertiesConfiguration("database.properties");
            //tells it that it's not using secure sockets layer (only for default.)
            boolean ssl = false;
            //This if makes it so that if the database properties (from the file) are true it'll use ssl
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            //This makes a instance of codondb and passes in the properties from the properties file
            //(In other words, this makes the db object which creates a connection to the db)
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            //This makes it get the object for the single to many comparison, this is like the
            //one to one except it has several organisms being compared in the chart
            //(and the object contains those)
            JSONObject result = db.getResultOneToManysAsJSON(uuid);
            return result;
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }

    }

    private JSONObject getResultsOneToManyAggregated(String uuid) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            boolean ssl = false;
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            JSONObject result = db.getResultOneToManysAsJSONAggregated(uuid);
            return result;
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }
    }

    private JSONObject getOrganismList(String organism) {
        try {
            //brings in database properties from the properties file
            Configuration config = new PropertiesConfiguration("database.properties");
            //tells it that it's not using secure sockets layer (only for default.)
            boolean ssl = false;
            //This if makes it so that if the database properties (from the file) are true it'll use ssl
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            //This makes a instance of codondb and passes in the properties from the properties file
            //(In other words, this makes the db object which creates a connection to the db)
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            //This is error checking, it makes sure that there was a database connection
            if(!db.connection) {
                JSONObject obj = new JSONObject();
                obj.put("connection error", "could not connect");
                return obj;
            }
            //This runs the db function to get the organism list in a json format,
            //This is for the drop down menu displaying a list of organisms
            JSONObject result = db.getOrganismListAsJSON(organism);
            return result;
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }
    }

    //This is for the getting the one to one results, one of the web pages displays organisms
    //in a chart, this is the version that only displays 2 of them
    private JSONObject getResultsOneToOne(String uuid, String[] compareOrganisms) {
        try {
            //brings in database properties from the properties file
            Configuration config = new PropertiesConfiguration("database.properties");
            //tells it that it's not using secure sockets layer (only for default.)
            boolean ssl = false;
            //This if makes it so that if the database properties (from the file) are true it'll use ssl
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            //This makes a instance of codondb and passes in the properties from the properties file
            //(In other words, this makes the db object which creates a connection to the db)
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            //This gets the results to pass back to the user in a JSON
            //(This is a object, "Javascript object notation")
            JSONObject result = db.getResultOneToOnesAsJSON(uuid, compareOrganisms);
            return result;
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }
    }

    private String getCSVString(String jobUUID)
    {
        try {
            //brings in database properties from the properties file
            Configuration config = new PropertiesConfiguration("database.properties");
            //tells it that it's not using secure sockets layer (only for default.)
            boolean ssl = false;
            //This if makes it so that if the database properties (from the file) are true it'll use ssl
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            //This makes a instance of codondb and passes in the properties from the properties file
            //(In other words, this makes the db object which creates a connection to the db)
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            //creates a list and uses the db function to get the csv data in object form
            List<CodonDB.ResultObject> obj = db.getResultAsResultObjectList("refseq", jobUUID);
            //This passes it into the toCSV function to convert it to a string before returning it
            return toCSV(obj);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace(out);
            return null;
        }
    }

    //This function takes the csv object and turns it into a string

    public static String toCSV(List<CodonDB.ResultObject> obj) {

        StringBuilder sb = new StringBuilder();

        // Header Information
        sb.append("Accession,Description,Taxonomy,Score,Shuffle Score\n");

        for (CodonDB.ResultObject r : obj)
        {
            r.desc = r.desc.replace("\"", "\'");
            r.taxonomy = r.taxonomy.replace("\"", "\'");

            String s = r.id + "," + "\"" + r.desc + "\"" + "," + "\"" + r.taxonomy + "\"" + "," + r.score + "," + r.shuffle_score + "\n";
            sb.append(s);
        }

        return sb.toString();
    }
}
