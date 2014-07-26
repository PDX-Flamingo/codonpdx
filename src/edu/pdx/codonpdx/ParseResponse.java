package edu.pdx.codonpdx;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 7/17/2014.
 *
 * This class was created to parse the response payload from our POSTs.
 * Due to issues with trying to extract the data through a standard way,
 * this class was created as a hackish solution in order to move forward.
 * Ideally this class will be replaced by using the http servlets standard
 * methods of extracting payloads.
 */
public class ParseResponse {

    private BufferedReader inputReader = null;
    private List<String> body = new ArrayList<String>();
    public String comparisonHost = null;
    public String fileContents = "";
    public String fileType = null;
    public String customSequenceName = null;
    public String customSequence = null;


    public ParseResponse(BufferedReader inputReader) {
        this.inputReader = inputReader;
    }

    public void parseInput() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            if (inputReader != null) {
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = inputReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
        }
        String temp = stringBuilder.toString();
        temp.replaceAll("\\r\\n", "\n");
        temp.replaceAll("\\r", "\n");
        temp.replaceAll("\r", "");
        String[] postElements = stringBuilder.toString().split("\n");

        for(int i = 0; i < postElements.length; i++) {
            if(postElements[i].contains("name=\"comparisonHost\"")) {
                comparisonHost = postElements[i=i+2].replaceAll("\\r", "");
                continue;
            }
            if(postElements[i].contains("name=\"file\"")) {
                i=i+3;
                for(;!postElements[i].startsWith("------");i++) {
                    fileContents += postElements[i] + "\n";
                }
                continue;
            }
            if(postElements[i].contains("name=\"fileType\"")) {
                fileType = postElements[i=i+2].replaceAll("\\r", "");
                continue;
            }
            if(postElements[i].contains("name=\"sequenceName\"")) {
                customSequenceName = postElements[i=i+2].replaceAll("\\r", "");
            }

            if(postElements[i].contains("name=\"file\"")) {
                i=i+2;
                for(;!postElements[i].contains("------");i++) {
                    customSequence += postElements[i];
                }
                continue;
            }


        }
    }

}
