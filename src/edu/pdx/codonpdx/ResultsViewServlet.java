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

//This is another servlet which takes in requests for the chart and the result list

public class ResultsViewServlet extends HttpServlet{
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {

        try {
            PrintWriter out = response.getWriter();
            out.println(request.getRequestURI());
            //splits the URL like in the CodonPDX.java, it makes the parts of the URL
            //become elements of a string array based on where the / symbols are
            //(note that the first one is always empty)
            String[] URI = request.getRequestURI().split("/");
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "resultsView":
                    //This checks if it has a comparison element, if it doesn't then it goes to the result list
                    //if it does, it goes to the chart
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
