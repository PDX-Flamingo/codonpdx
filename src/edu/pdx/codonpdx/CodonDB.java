package edu.pdx.codonpdx;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Robert on 7/12/2014.
 */
public class CodonDB {
    Connection con = null;
    Statement st = null;
    ResultSet rs = null;
    String url = null;
    String user = null;
    String password = null;


    public CodonDB(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        openConnection();
    }

    private boolean openConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(url, user, password);
        }
        catch(SQLException sqle) {
            System.out.println(sqle.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<String> getResults(String organism) throws SQLException {
        st = con.createStatement();
        rs = st.executeQuery("select * from results");
        List<String> sl = new LinkedList<String>();
        while(rs.next()) {
            sl.add(rs.getString(1) + rs.getString(2) + rs.getString(3) + rs.getString(4));
        }
        rs.close();
        st.close();
        return sl;
    }

    public JSONObject getResultOneToManysAsJSON(String UUID) throws SQLException {
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

        if(!rs.next()) {
            result.put("Error", "This result does not exist");
        }
        else {
            result.put("target", rs.getString(1));
        }

        rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrgsMatchingUUID, UUID, 1000));

        while(rs.next()) {
            JSONArray array = new JSONArray();
            array.add(rs.getString(2));
            array.add(rs.getDouble(3));
            array.add(rs.getDouble(4));
            result.put(rs.getString(1), array);
        }
        rs.close();
        st.close();
        return result;
    }

    public JSONObject getResultOneToOnesAsJSON(String UUID, String comparisonOrganism) {
        if (con == null) {
            JSONObject error = new JSONObject();
            error.put("error", "connection not made");
            return error;
        }
        JSONObject result = new JSONObject();
        try {


            st = con.createStatement();

            // Query the results table to see if this result exists.
            rs = st.executeQuery(String.format(CodonDBQueryStrings.getTargetArgforUUID, UUID));

            if (!rs.next()) {
                result.put("Error", "This result does not exist");
                return result;
            }
            String target = rs.getString(1);
            rs.close();

            rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrganismForOneToOne, "input", target));
            result.put(target, getCodonRatios("standard", getCodonCounts(rs)));
            rs.close();

            rs = st.executeQuery(String.format(CodonDBQueryStrings.getOrganismForOneToOne, "refseq", comparisonOrganism));
            result.put(comparisonOrganism, getCodonRatios("standard", getCodonCounts(rs)));
            rs.close();
            st.close();
        } catch (SQLException e) {
            JSONObject obj = new JSONObject();
            obj.put("error", e.getMessage());
            return obj;
        }
        return result;
    }

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

    private JSONObject getCodonRatios(String table, JSONObject counts) {
        JSONObject ratios = new JSONObject();
        try {
            rs = st.executeQuery(CodonDBQueryStrings.getListOfAminoAcids);
            ratios.put("description", counts.get("description"));
            Set<String> aminoacidSet = new HashSet<>();
            while (rs.next()) {
                aminoacidSet.add(rs.getString(1));
            }
            rs.close();
            for (String s : aminoacidSet) {
                JSONObject aminoacidJSON = new JSONObject();
                rs = st.executeQuery(String.format(CodonDBQueryStrings.getCodonsForAcid, s, "standard"));
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
                st.close();
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

    // class encompassing query strings
    private static class CodonDBQueryStrings {
        public static String getOrgsMatchingUUID = "(select organism2, description, score, shuffle_score from results as r inner join refseq as rs on r.organism2=rs.id where job_uuid='%1$s' order by score asc limit %2$d)"
                                                 + " UNION "
                                                 + "(select organism2, description, score, shuffle_score from results as r inner join refseq as rs on r.organism2=rs.id where job_uuid='%1$s' order by score desc limit %2$d)"
                                                 + "order by score asc";
        public static String getTargetArgforUUID = "select job_uuid from results where job_uuid='%1$s' limit 1";
        public static String getOrganismForOneToOne = "select * from %1$s where id='%2$s'";
        public static String getListOfAminoAcids = "select DISTINCT acid from codon_table";
        public static String getCodonsForAcid = "select codon from codon_table where acid='%1$s' and name='%2$s'";
    }
}
