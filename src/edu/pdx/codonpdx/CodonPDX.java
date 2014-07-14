package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;

/**
 * Created by Robert on 7/7/2014.
 */
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
            //out.println(request.getRequestURI());
            switch (request.getRequestURI()) {
                case "/codonpdx/app":
                    if(ServletFileUpload.isMultipartContent(request)) {

                    }
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    String codonstring = request.getParameter("codonstring");
                    String filename = request.getParameter("filenameforwrite");
                    out.println("Email " + username);
                    out.println("Password entered " + password);
                    out.println("codon string ");
                    out.println(codonstring);
                    String content = username + "\n" + password + "\n" + codonstring;
                    out.println(content);

                    File file = new File("/opt/share/" + filename + ".html");

                    // if file doesnt exists, then create it
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(content);
                    bw.close();

                    //PrintWriter printingfileout = new PrintWriter(new FileWriter("/opt/share/" + filenameforwrite + "/.html"));
                    //if (!printingfileout.exists()) {
                    //    file.createNewFile();
                    //}
                    //printingfileout.print(content);
                    //printingfileout.close();


                break;
            }
        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        }
    }

    private void testConnection(HttpServletResponse response) throws InterruptedException, IOException {
        PrintWriter out = response.getWriter();
        TaskScheduler ts = new TaskScheduler("celery", "localhost");
        String id = ts.scheduleTask("proj.tasks.random_int").replace("-", "");
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
