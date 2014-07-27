package edu.pdx.codonpdx;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

/**
 * Created by Robert on 7/7/2014.
 *
 * This servlet handles mapping URLs for viewing results to html files.  The html files
 * use javascript that parse the URL.
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
                        request.getRequestDispatcher("/resultsView.html").forward(request, response);
                    }
                    else if (URI.length == 5) {
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
