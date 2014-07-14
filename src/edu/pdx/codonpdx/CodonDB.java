package edu.pdx.codonpdx;

import org.json.simple.JSONObject;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

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
        return sl;
    }

    public JSONObject getResultsAsJSON(String UUID) throws SQLException {
        if(con == null)  {
            JSONObject error = new JSONObject();
            error.put("error", "connection not made");
            return error;
        }

        st = con.createStatement();
        rs = st.executeQuery("select organism1, organism2, score from results where job_uuid='" + UUID + "'");
        JSONObject result = new JSONObject();
        while(rs.next()) {
            result.put(rs.getString(2), rs.getDouble(3));
        }
        rs.close();
        return result;
    }

    public static void main(String [] args) {
        try {
            CodonDB db = new CodonDB("jdbc:postgresql://localhost/pdxcodon", "pdxcodon", "secret");
            if(!db.openConnection()) {
                System.out.println("can't connect oops lawl");
                return;
            }
            List<String> sl = db.getResults("none");
            for (String s : sl) {
                System.out.println(s);
            }
        }
        catch (SQLException sqle) {
            System.out.println("oops");
        }

    }
}
