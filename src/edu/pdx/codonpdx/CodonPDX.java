package edu.pdx.codonpdx;

import javax.naming.ConfigurationException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Robert on 7/7/2014.
 */

public class CodonPDX extends HttpServlet {

    PrintWriter out = null;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        out = response.getWriter();
        String[] URI = request.getRequestURI().split("/");
        try {
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "app":
                    request.getRequestDispatcher("/index.html").forward(request, response);
                    break;
                case "results":
                    if (URI.length == 4) {
                        out.print(getResultsOneToMany(URI[3]));
                        response.setContentType("application/json");
                    } else if (URI.length == 5) {
                        out.print(getResultsOneToOne(URI[3], URI[4].split("&&&")));
                        response.setContentType("application/json");
                    } else
                        out.println("Error with request, check the URL again");
                    break;
                case "dlCSV":
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
                    if (URI.length == 4) {
                        response.setContentType("application/json");
                        out.println(getOrganismList(URI[3].replace("%20", " ")));
                    }
                    else {
                        out.println("Error with request, check the URL again");
                    }
                    break;

                default:
                    request.getRequestDispatcher("/index.html").forward(request, response);
            }
        } catch (IOException e) {
            out.println(e.getMessage());
        }

    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            out = response.getWriter();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String[] URI = request.getRequestURI().split("/");
            switch (request.getRequestURI()) {
                case "/codonpdx/submitRequest":
                    Configuration config = new PropertiesConfiguration("tomcat.properties");
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
                    scheduleRatioCompare(uuid, "refseq", prbody.fileType, config.getString("folder.share"), new String[] {});
                    out.println(json);

                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    private void scheduleRatioCompare(String uuid, String database, String format, String path, String[] organismList) throws InterruptedException, IOException {
        try {
            Configuration config = new PropertiesConfiguration("mq.properties");
            TaskScheduler ts = TaskScheduler.getInstance(config.getString("queue.name"), config.getString("queue.host"), config.getString("queue.user"), config.getString("queue.password"), config.getString("queue.vhost"));
            ts.scheduleTask(uuid, ".create_result_from_input_file", uuid, database, format, path, organismList);
            ts.closeConnect();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            out.println(e.getMessage());
        }
    }

    private JSONObject getResultsOneToMany(String uuid) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            boolean ssl = false;
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            JSONObject result = db.getResultOneToManysAsJSON(uuid);
            return result;
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("path", System.getProperty("user.dir"));
            obj.put("error", e.getMessage());
            return obj;
        }

    }

    private JSONObject getOrganismList(String organism) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            boolean ssl = false;
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            if(!db.connection) {
                JSONObject obj = new JSONObject();
                obj.put("connection error", "could not connect");
                return obj;
            }

            JSONObject result = db.getOrganismListAsJSON(organism);
            return result;
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }
    }

    private JSONObject getResultsOneToOne(String uuid, String[] compareOrganisms) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            boolean ssl = false;
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
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
            Configuration config = new PropertiesConfiguration("database.properties");
            boolean ssl = false;
            if(config.getString("database.ssl").equals("true"))
                ssl = true;
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"), ssl);
            List<CodonDB.CSVResultObject> obj = db.getResultAsResultObjectList("refseq", jobUUID);
            return toCSV(obj);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace(out);
            return null;
        }
    }

    public static String toCSV(List<CodonDB.CSVResultObject> obj) {

        StringBuilder sb = new StringBuilder();

        // Header Information
        sb.append("Accession,Description,Taxonomy,Score,Shuffle Score\n");

        for (CodonDB.CSVResultObject r : obj)
        {
            r.desc = r.desc.replace("\"", "\'");
            r.taxonomy = r.taxonomy.replace("\"", "\'");

            String s = r.id + "," + "\"" + r.desc + "\"" + "," + "\"" + r.taxonomy + "\"" + "," + r.score + "," + r.shuffle_score + "\n";
            sb.append(s);
        }

        return sb.toString();
    }
}
