package edu.pdx.codonpdx;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DownloadCSV extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            PrintWriter out = response.getWriter();
            String[] URI = request.getRequestURI().split("/");
            switch (URI.length < 3 ? "none" : URI[2]) {
                case "dlCSV":
                    if (URI.length == 4)
                        out.println(getCSVString(URI[3]));
                        response.setContentType("text/csv");
                    break;
            }
        } catch (IOException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        }
    }

    private String getCSVString(String jobUUID)
    {
        CodonDB db = new CodonDB("jdbc:postgresql://localhost/pdxcodon", "pdxcodon", "secret");
        List<CodonDB.CSVResultObject> obj = db.getResultAsResultObjectList("refseq", jobUUID);
        return toCSV(obj);
    }

    public static String toCSV(List<CodonDB.CSVResultObject> obj) {

        StringBuilder sb = new StringBuilder();

        // Header Information
        sb.append("Accession, Description, Taxonomy, Score, Shuffle Score\n");

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
