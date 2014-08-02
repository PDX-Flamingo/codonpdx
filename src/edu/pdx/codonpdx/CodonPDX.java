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
                        out.print(getResultsOneToOne(URI[3], URI[4]));
                        response.setContentType("application/json");
                        response.setHeader("filename", URI[3] + ".csv");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + URI[3] + ".csv" + "\"");
                    } else
                        out.println("Error with request, check the URL again");
                    break;
                case "dlCSV":
                    if (URI.length == 4)
                        response.setContentType("text/csv");
                        out.println(getCSVString(URI[3]));
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
            switch (request.getRequestURI()) {
                case "/codonpdx/submitRequest":
                    ParseResponse prbody = new ParseResponse(request.getReader());
                    prbody.parseInput();
                    File f = new File("/opt/share/", uuid);
                    Writer fileWriter = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(fileWriter);
                    bw.write(prbody.fileContents);
                    bw.close();
                    fileWriter.close();
                    JSONObject json = new JSONObject();
                    json.put("UUID", uuid);
                    response.setContentType("application/json");
                    scheduleRatioCompare(uuid, prbody.comparisonHost, prbody.fileType);
                    out.println(json);

                    break;
            }
        } catch (IOException e) {
            e.printStackTrace(out);
        } catch (InterruptedException e) {
            e.printStackTrace(out);
        }
    }

    private void scheduleRatioCompare(String uuid, String database, String format) throws InterruptedException, IOException {
        try {
            Configuration config = new PropertiesConfiguration("mq.properties");
            TaskScheduler ts = new TaskScheduler(config.getString("queue.name"), config.getString("queue.host"));
            ts.scheduleTask(uuid, "codonpdx.tasks.trigger_demo_behavior", uuid, database, format);
            ts.closeConnect();
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            out.println(e.getMessage());
        }
    }

    private JSONObject getResultsOneToMany(String uuid) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"));
            JSONObject result = db.getResultOneToManysAsJSON(uuid);
            return result;
        } catch (Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("path", System.getProperty("user.dir"));
            obj.put("error", e.getMessage());
            return obj;
        }

    }

    private JSONObject getResultsOneToOne(String uuid, String compareOrganism) {
        try {
            Configuration config = new PropertiesConfiguration("database.properties");
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"));
            JSONObject result = db.getResultOneToOnesAsJSON(uuid, compareOrganism);
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
            CodonDB db = new CodonDB(config.getString("database.url"), config.getString("database.user"), config.getString("database.password"));
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
        sb.append("Accession, Description, Taxonomy, Score, Shuffle Score\n");

        for (CodonDB.CSVResultObject r : obj)
        {
            String s = r.id + "," + r.desc + "," + r.taxonomy + "," + r.score + "," + r.shuffle_score + "\n";
            sb.append(s);
        }

        return sb.toString();
    }
}
