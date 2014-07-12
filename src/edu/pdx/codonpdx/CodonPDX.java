package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Robert on 7/7/2014.
 */
public class CodonPDX extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {

        try {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
            switch (request.getRequestURI()) {
               case "/codonpdx-zac/app":
                   request.getRequestDispatcher("homePage.jsp").forward(request, response);
                    break;
                case "/codonpdx-zac/testconnection":
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
                    break;
                case "codonpdx-zac/testing_queue":

                    break;
                default:
                    response.sendRedirect("/codonpdx/app");
            }
        }
        catch (InterruptedException e) {
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
                case "/codonpdx-zac/app":
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    String codonstring = request.getParameter("codonstring");
                    String filenameforwrite = request.getParameter("filenameforwrite");
                    out.println("Email " + username);
                    out.println("Password entered " + password);
                    out.println("codon string ");
                    out.println(codonstring);
                    String content = username + "\n" + password + "\n" + codonstring;
                    out.println(content);

                    File file = new File("/opt/share/" + filenameforwrite + ".html");

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
    //private void testConnection()

}
