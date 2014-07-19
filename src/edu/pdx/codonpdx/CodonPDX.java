package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Robert on 7/7/2014.
 */

@MultipartConfig
public class CodonPDX extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {

        try {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
            String[] URI = request.getRequestURI().split("/");
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "app":
                    request.getRequestDispatcher("homePage.jsp").forward(request, response);
                    break;
                case "testconnection":
                    testConnection(response);
                    break;

                case "results":
                    out.print(getResults(URI[3]));
                    break;
                case "codonpdx/testing_queue":
                    break;
                default:
                    response.sendRedirect("/codonpdx/app");
            }
        } catch (InterruptedException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        }

    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {
        try {
            PrintWriter out = response.getWriter();
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
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getValue(Part part) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[1024];
        for (int length = 0; (length = reader.read(buffer)) > 0;) {
            value.append(buffer, 0, length);
        }
        return value.toString();
    }

    private void testConnection(HttpServletResponse response) throws InterruptedException, IOException {
        PrintWriter out = response.getWriter();
        TaskScheduler ts = new TaskScheduler("celery", "localhost");
        String id = ts.scheduleTask("codonpdx.tasks.random_int").replace("-", "");
        Thread.sleep(4000);
        ResponseConsumer qc = new ResponseConsumer(id, "localhost");
        String message = qc.getResponseFromQueue();
        int i = 0;
        while(message == null) {
            if(i == 5) break;
            Thread.sleep(2000);
            message = qc.getResponseFromQueue();
            i++;
        }
        ts.closeConnect();
        qc.closeConnect();

        out.println(message);
    }

    private void scheduleRatioCompare(String uuid, String database, String format) throws InterruptedException, IOException {
        TaskScheduler ts = new TaskScheduler("celery", "localhost");
        String id = ts.scheduleTask(uuid, "codonpdx.tasks.trigger_demo_behavior", uuid, database, format);
        ts.closeConnect();
    }

    private JSONObject getResults(String uuid) {
        try {
            CodonDB db = new CodonDB("jdbc:postgresql://localhost/pdxcodon", "pdxcodon", "secret");
            JSONObject result = db.getResultsAsJSON(uuid);
            return result;
        } catch(Exception e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }

    }

}
