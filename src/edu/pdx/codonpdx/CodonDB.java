package edu.pdx.codonpdx;

import org.json.*;

import java.sql.*;
import java.util.*;

/**
 * Created by Robert on 7/12/2014.
 */

//This is the database class, it creates a connection between tomcat and the database (then closes it)
//It holds all the information retrieval functions and SQL queries
//Please note that in all of these the variables st that are created are for statement variables
//and rs are the queries that are run against the database


public class CodonDB {
    //variables
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    String url = null;
    String user = null;
    String password = null;
    Boolean connection = false;
    Boolean useSSL = false;
    int MAX_TRIES = 20;

    //Constructor, this takes in the database properties being passed in
    //These values come from the DB properties file
    public CodonDB(String url, String user, String password, Boolean SSL) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.useSSL = SSL;
        connection = openConnection();
    }

    //This opens a connection to the database
    //All the properties come from the DB file
    private boolean openConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", user);
            properties.setProperty("password", password);
            if(useSSL)
                properties.setProperty("ssl", "true");
            properties.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
            con = DriverManager.getConnection(url, properties);
        }
        catch(SQLException sqle) {
            System.out.println(sqle.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //This just grabs everything from the results table, never used
    public List<String> getResults(String organism) throws SQLException {
        st = con.createStatement();
        rs = st.executeQuery("select * from results");
        List<String> sl = new LinkedList<String>();
        while(rs.next()) {
            sl.add(rs.getString(1) + rs.getString(2) + rs.getString(3) + rs.getString(4));
        }
        rs.close();
        st.close();
        con.close();
        return sl;
    }

    //almost the same thing as below (one to one) except it does it for many instead of two
    public JSONObject getResultOneToManysAsJSON(String UUID) throws SQLException, InterruptedException {
        //checks if database is open
        if(con == null)  {
            JSONObject error = new JSONObject();
            error.put("things", url + user + password);
            error.put("error", "connection not made");
            return error;
        }
        JSONObject result = new JSONObject();

        // Check if this result exists
        st = con.createStatement();
        rs = st.executeQuery(String.format(CodonDBQueryStrings.getTargetArgforUUID, UUID));

        //This makes the code wait for the comparison to finish,
        // if it times out then it sends and error back
        int tryCount = 0;
        while(!rs.next()) {
            if(tryCount > MAX_TRIES) {
                result.put("Error", "Result not found.  Please try again, or re-run your query.");
                rs.close();
                st.close();
                con.close();
                return result;
            }
            tryCount++;
            Thread.currentThread().sleep(3000);
            rs = st.executeQuery(String.format(CodonDBQueryStrings.getTargetArgforUUID, UUID));
        }
        result.put("target", rs.getString(1));
        rs.close();
        //finds all the organisms that match the request
        rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrgsMatchingUUID, UUID, 1000));

        //grabs all the data and puts it into an array, then drops that into results
        //(This is doing it one at a time because of the transaction with the database)
        while(rs.next()) {
            JSONArray array = new JSONArray();
            array.put(rs.getString(2));
            array.put(rs.getDouble(3));
            array.put(rs.getDouble(4));
            array.put(rs.getString(5));
            result.put(rs.getString(1), array);
        }
        rs.close();
        st.close();
        con.close();
        return result;
    }

    //This is the function that runs the data retrieval for the one on on comparison
    //and returns a JSON object back to the main CodonPDX servlet
    public JSONObject getResultOneToOnesAsJSON(String UUID, String[] comparisonOrganisms) {
        //Checks if the database connection was successful
        if (con == null) {
            JSONObject error = new JSONObject();
            error.put("error", "connection not made");
            return error;
        }
        //This is what will be returned
        JSONObject result = new JSONObject();
        try {


            st = con.createStatement();

            // Query the results table to see if this result exists.

            rs = st.executeQuery(String.format(CodonDBQueryStrings.getTargetArgforUUID, UUID));

            int tryCount = 0;
            //This makes the code wait for the comparison to finish,
            // if it times out then it sends and error back
            while(!rs.next()) {
                if(tryCount > MAX_TRIES) {
                    result.put("Error", "Result not found.  Please try again, or re-run your query.");
                    rs.close();
                    st.close();
                    con.close();
                    return result;
                }
                tryCount++;
                Thread.currentThread().sleep(3000);
                rs = st.executeQuery(String.format(CodonDBQueryStrings.getTargetArgforUUID, UUID));
            }
            //target holds the target of the queries (But you can see that)
            String target = rs.getString(1);
            rs.close();

            //takes in the input and target values and uses the format function to run the SQL dynamically
            rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrganismForOneToOne, "input", target));
            result.put(target, getCodonRatios("standard", getCodonCounts(rs)));
            rs.close();
            //This does the same as above, but it does it for any extra organisms
            for(String s: comparisonOrganisms) {
                rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrganismForOneToOne, "refseq", s));
                result.put(s, getCodonRatios("standard", getCodonCounts(rs)));
                rs.close();
            }
            st.close();
            con.close();
        } catch (SQLException e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    //This just finds all the codon values and puts them in the object that was passed to it
    private JSONObject getCodonCounts(ResultSet rs) throws SQLException {
        JSONObject entry = null;
        if (rs.next()) {
            entry = new JSONObject();
            entry.put("description", rs.getString(2));
            entry.put("aaa", rs.getInt(4));
            entry.put("aac", rs.getInt(5));
            entry.put("aag", rs.getInt(6));
            entry.put("aat", rs.getInt(7));
            entry.put("aca", rs.getInt(8));
            entry.put("acc", rs.getInt(9));
            entry.put("acg", rs.getInt(10));
            entry.put("act", rs.getInt(11));
            entry.put("aga", rs.getInt(12));
            entry.put("agc", rs.getInt(13));
            entry.put("agg", rs.getInt(14));
            entry.put("agt", rs.getInt(15));
            entry.put("ata", rs.getInt(16));
            entry.put("atc", rs.getInt(17));
            entry.put("atg", rs.getInt(18));
            entry.put("att", rs.getInt(19));
            entry.put("caa", rs.getInt(20));
            entry.put("cac", rs.getInt(21));
            entry.put("cag", rs.getInt(22));
            entry.put("cat", rs.getInt(23));
            entry.put("cca", rs.getInt(24));
            entry.put("ccc", rs.getInt(25));
            entry.put("ccg", rs.getInt(26));
            entry.put("cct", rs.getInt(27));
            entry.put("cga", rs.getInt(28));
            entry.put("cgc", rs.getInt(29));
            entry.put("cgg", rs.getInt(30));
            entry.put("cgt", rs.getInt(31));
            entry.put("cta", rs.getInt(32));
            entry.put("ctc", rs.getInt(33));
            entry.put("ctg", rs.getInt(34));
            entry.put("ctt", rs.getInt(35));
            entry.put("gaa", rs.getInt(36));
            entry.put("gac", rs.getInt(37));
            entry.put("gag", rs.getInt(38));
            entry.put("gat", rs.getInt(39));
            entry.put("gca", rs.getInt(40));
            entry.put("gcc", rs.getInt(41));
            entry.put("gcg", rs.getInt(42));
            entry.put("gct", rs.getInt(43));
            entry.put("gga", rs.getInt(44));
            entry.put("ggc", rs.getInt(45));
            entry.put("ggg", rs.getInt(46));
            entry.put("ggt", rs.getInt(47));
            entry.put("gta", rs.getInt(48));
            entry.put("gtc", rs.getInt(49));
            entry.put("gtg", rs.getInt(50));
            entry.put("gtt", rs.getInt(51));
            entry.put("taa", rs.getInt(52));
            entry.put("tac", rs.getInt(53));
            entry.put("tag", rs.getInt(54));
            entry.put("tat", rs.getInt(55));
            entry.put("tca", rs.getInt(56));
            entry.put("tcc", rs.getInt(57));
            entry.put("tcg", rs.getInt(58));
            entry.put("tct", rs.getInt(59));
            entry.put("tga", rs.getInt(60));
            entry.put("tgc", rs.getInt(61));
            entry.put("tgg", rs.getInt(62));
            entry.put("tgt", rs.getInt(63));
            entry.put("tta", rs.getInt(64));
            entry.put("ttc", rs.getInt(65));
            entry.put("ttg", rs.getInt(66));
            entry.put("ttt", rs.getInt(67));
        }

        return entry;
    }

    //This is for the drop down list on the web page for users finding
    public JSONObject getOrganismListAsJSON(String organism) {
        JSONObject list = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            st = con.createStatement();
            //runs the query with format to run the SQL dynamically
            //it grabs first few values that match the starting letters inputted
            rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrganismIdListQuery, organism.toLowerCase()));
            while(rs.next()) {
                JSONArray organismArray = new JSONArray();
                if(rs.getString(1).toLowerCase().startsWith(organism.toLowerCase()))
                {
                    organismArray.put(rs.getString(1));
                    organismArray.put(rs.getString(2));
                    array.put(organismArray);                    
                }
                else if(rs.getString(2).toLowerCase().startsWith(organism.toLowerCase())) {
                    organismArray.put(rs.getString(1));
                    organismArray.put(rs.getString(2));   
                    array.put(organismArray);                    
                }
            }
            list.put("list", array);
            rs.close();
            st.close();
            con.close();
        } catch (SQLException e) {
            JSONObject error = new JSONObject();
            error.put("error", "Error trying to get list of organisms");
            return error;
        }

        return list;
    }


    //This gets the codon ratio for the organism being compared
    private JSONObject getCodonRatios(String table, JSONObject counts) {
        JSONObject ratios = new JSONObject();
        try {
            rs = st.executeQuery(CodonDBQueryStrings.getListOfAminoAcids);
            //puts in the description (name) and takes the table
            //that was passed in and puts it into the object as well
            ratios.put("description", counts.get("description"));
            Set<String> aminoacidSet = new HashSet<>();
            //takes the list from the database and drops it into the variable one at a time
            while (rs.next()) {
                aminoacidSet.add(rs.getString(1));
            }
            rs.close();
            //for each element of the list that was taken out of the database above ^
            //grabs the codon (This should only be for the standard table right now)
            for (String s : aminoacidSet) {
                JSONObject aminoacidJSON = new JSONObject();
                rs = st.executeQuery(String.format(CodonDBQueryStrings.getCodonsForAcid, s, table));
                int count = 0;
                Set<String> codonSet = new HashSet<>();
                while (rs.next()) {
                    String codon = rs.getString(1).toLowerCase();
                    count += Integer.parseInt(counts.get(codon).toString());
                    codonSet.add(codon);
                }

                for (String t : codonSet) {
                    aminoacidJSON.put(t, count > 0 ? Double.parseDouble(counts.get(t).toString()) / count : 0);
                }


                ratios.put(s, aminoacidJSON);
                rs.close();
            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            String t = "";
            for(StackTraceElement s : e.getStackTrace()) {
                t += s.toString();
            }
            error.put("error", t);
            return error;
        }
        return ratios;
    }

    //This is for getting the data for a job out of the database and returning it as the CSV object
    //(This makes the CSVs being downloaded actually have stuff in them)
    public List<CSVResultObject> getResultAsResultObjectList(String seqDatabase, String jobUUID)
    {
        List<CSVResultObject> obj = new ArrayList<CSVResultObject>();

        try {
            st = con.createStatement();
            rs = st.executeQuery(String.format(CodonDBQueryStrings.getInformationForCSVLine, seqDatabase, jobUUID));

            //Grabs data out of the database one row at a time, inputs them into variables
            //and drops them into the object
            while (rs.next())
            {
                String id = rs.getString(1);
                String taxonomy = rs.getString(2);
                String description = rs.getString(3);
                double score = rs.getDouble(4);
                double shuffle_score = rs.getDouble(5);

                obj.add(new CSVResultObject(id, taxonomy, description, score, shuffle_score));
            }
            rs.close();
            st.close();
            con.close();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }


        return obj;
    }

    //Just an object for the CSV results, used by that ^
    public class CSVResultObject {
        public String id;
        public String desc;
        public String taxonomy;
        public double score;
        public double shuffle_score;

        public CSVResultObject(String id, String desc, String taxonomy, double score, double shuffle_score)
        {
            this.id = id;
            this.desc = desc;
            this.taxonomy = taxonomy;
            this.score = score;
            this.shuffle_score = shuffle_score;
        }
    }

    // class encompassing query strings
    private static class CodonDBQueryStrings {
        public static String getInformationForCSVLine = "select id, description, taxonomy, score, shuffle_score from %1$s as rs inner join results as r on r.organism2 = rs.id where job_uuid='%2$s'";
        public static String getOrgsMatchingUUID = "(select organism2, description, score, shuffle_score, taxonomy from results as r inner join refseq as rs on r.organism2=rs.id where job_uuid='%1$s' order by score asc limit %2$d)"
                                                 + " UNION "
                                                 + "(select organism2, description, score, shuffle_score, taxonomy from results as r inner join refseq as rs on r.organism2=rs.id where job_uuid='%1$s' order by score desc limit %2$d)"
                                                 + "order by score asc";
        public static String getTargetArgforUUID = "select job_uuid from results where job_uuid='%1$s' limit 1";
        public static String getOrganismForOneToOne = "select * from %1$s where id='%2$s'";
        public static String getListOfAminoAcids = "select DISTINCT acid from codon_table";
        public static String getCodonsForAcid = "select codon from codon_table where acid='%1$s' and name='%2$s'";
        public static String getOrganismIdListQuery = "select id, description from refseq where lower(id) like '%1$s%%' or lower(description) like '%1$s%%' limit 10";
    }
}
