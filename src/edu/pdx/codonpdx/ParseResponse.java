package edu.pdx.codonpdx;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Robert on 7/17/2014.
 */
public class ParseResponse {

    private BufferedReader inputReader = null;
    public String comparisonHost = null;
    public String fileContents = "";
    public String fileType = null;
    public String customSequenceName = null;
    public String customSequence = null;
    public String[] comparisonIds = new String[]{};

    /*
        We had issues getting the payload from ajax post.  The java servlet methods
        for obtaining the data weren't working.  This parser was created in response
        to that.
     */
    public ParseResponse(BufferedReader inputReader) {
        this.inputReader = inputReader;
    }

    public void parseInput() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            //reads in the string from data passed in from the javascript
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
        //gets rid of newlines and some spaces
        temp.replaceAll("\\r\\n", "\n");
        temp.replaceAll("\\r", "\n");
        temp.replaceAll("\r", "");
        String[] postElements = stringBuilder.toString().split("\n");

        //This area skips to areas in the string that we want and pulls them out
        //it knows which type of input (whether input sequence or input file)
        //based on which elements are within it in which order
        //This is effected by the order the javascript put them in
        //they also uses a count to get out of the loop

        for(int i = 0; i < postElements.length; i++) {
            //Pulls out the host name for comparison
            if(postElements[i].contains("name=\"comparisonHost\"")) {
                comparisonHost = postElements[i=i+2].replaceAll("\\r", "");
                continue;
            }

            //This grabs the file body for either file type
            if(postElements[i].contains("name=\"file\"")) {
                i=i+3;
                for(;!postElements[i].contains("------");i++) {
                    fileContents += postElements[i] + "\n";
                }
                continue;
            }
            //grabs which type of file it is
            if(postElements[i].contains("name=\"fileType\"")) {
                fileType = postElements[i=i+2].replaceAll("\\r", "");
                continue;
            }
            //grabs the name of the sequence that's being compared
            //since this also only appears in the sequence input
            //it also prepares it for being a default of FASTA format
            //before taking in the text from the sequence box
            if(postElements[i].contains("name=\"sequenceName\"")) {
                customSequenceName = postElements[i=i+2].replaceAll("\\r", "");
                fileContents += "> ";
                fileContents += customSequenceName;
                fileContents += "\n";
                fileType = "FASTA";
                continue;
            }
            //grabs the sequence, this is from the websites text input
            if(postElements[i].contains("name=\"sequenceText\""))
            {
                fileContents += postElements[i=i+2].replaceAll("\\r", "");
                continue;
            }
            //this is for a custom list of comparison
            if(postElements[i].contains("name=\"customList\""))
            {
                i=i+2;
                comparisonIds = postElements[i].replaceAll("\\r", "").split(",");
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
