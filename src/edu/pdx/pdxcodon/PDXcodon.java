package edu.pdx.pdxcodon;

import com.rabbitmq.client.QueueingConsumer;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Robert on 7/7/2014.
 */
public class PDXcodon extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {

        try {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
            switch (request.getRequestURI()) {
               case "/codonpdx/main":
                   request.getRequestDispatcher("homePage.jps").forward(request, response);
                    break;
                case "/codonpdx/testconnection":
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
                default:
                    request.getRequestDispatcher("homePage.jsp").forward(request, response);
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

    //private void testConnection()

}
