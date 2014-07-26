package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.*;

/**
 * Created by Robert on 7/7/2014.
 */

public class ResultsViewServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {

        try {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
            String[] URI = request.getRequestURI().split("/");
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "resultsView":
                    if(URI.length == 4) {
                        out.println("one");
                        request.getRequestDispatcher("/resultsView.html").forward(request, response);
                    }
                    else if (URI.length == 5) {
                        out.println("two");
                        request.getRequestDispatcher("/compareTwo.html").forward(request, response);
                    }
                    break;

                default:
            }
        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        }

    }
}
