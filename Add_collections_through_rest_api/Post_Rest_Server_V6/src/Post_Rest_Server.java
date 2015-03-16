/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.print.attribute.standard.DateTimeAtCompleted;
import org.apache.http.Header;
import org.apache.http.HttpRequestInterceptor;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author pjha
 */
public class Post_Rest_Server {

    /**
     * @param args the command line arguments
     */
    public static String session_name = "";
    public static String session_id = "";
    public static String csrf_token = "";

    public static void main(String[] args) {
        //********* variable declaration ******************
        String path = "data\\";
        String dictionary = path + "ds_info_dictionary.csv";
        String dictionary_granule = path + "dictionaryGranule.csv";
        String datafile = path + "ds_info.csv";
        String[] linkedfile = new String[2];
        linkedfile[0] = path + "ds_urls.csv";
        linkedfile[1] = path + "ds_ssp.csv";
        String mapping = path + "mapping.csv";
        String[] granulefiles = new String[]{path + "lis_inv.csv", path + "gpm_inv.csv", path + "ssmi_inv.csv"};
        int no_of_columns_in_csv = 50;
        //********* variable declaration End ******************
        FileInputStream config = null;
        String rest_server = "", username = "", password = "";

        try {
            config = new FileInputStream("config.txt");
            Scanner scanner = new Scanner(config);

            while (scanner.hasNextLine()) {
                String[] elements = scanner.nextLine().split("=");
                if (elements[0].equals("rest_server")) {
                    rest_server = elements[1];
                } else if (elements[0].equals("username")) {
                    username = elements[1];
                } else if (elements[0].equals("password")) {
                    password = elements[1];
                }
            }
            scanner.close();
            //********* Main Program Code ******************                

            String[][] dsinfo = readCSV(datafile, no_of_columns_in_csv);
            String[][] dsurl = readCSV(linkedfile[0], no_of_columns_in_csv);
            String[][] dsssp = readCSV(linkedfile[1], no_of_columns_in_csv);

            Post_Rest rest = new Post_Rest();
            String connectioninfo = rest.getConnection(rest_server, username, password);
            appendToFile(connectioninfo);
            String keyfield = getKeyField(mapping);
            String parameter = "";

            for (int i = 1; i < dsinfo.length; i++) {
            //for (int i = 2; i < 4; i++) {
                String value_for_key = getValueForKey(dsinfo, i, keyfield);
                parameter = getParameter(dsinfo, i, dictionary, "");
                //**********************************************************
                
                //System.out.println(value_for_key);
                
                
                String parameter1 = getParameters(dsurl, keyfield, mapping, linkedfile[0], value_for_key, path + "ds_url_dictionary.csv");
                String parameter2 = getParameters(dsssp, keyfield, mapping, linkedfile[1], value_for_key, path + "ds_ssp_dictionary.csv");
                parameter = parameter +parameter1+ parameter2+ "type=collection&model=1&sell_price=0.0";
                System.out.println(parameter);
                String post_response = rest.postdata(parameter);                
                System.out.println(post_response);
                if (post_response != null) {
                    appendToFile(post_response);
                }
                System.out.println("******************************************");
            }

            //  **************************************************************************************************
            //Inserting granules          

//            //for (int i = 0; i < granulefiles.length; i++) {
//                for (int i = 2; i < 3; i++) {
//                String inventory[][] = readCSV(granulefiles[i], no_of_columns_in_csv);
//                for (int j = 1; j < inventory.length; j++) {
//                   // for (int j = 1; j < 2; j++) {
//                    if (inventory[j][0] != null) {
//                        String granules_parameter = getParameter(inventory, j, dictionary_granule, "");
//                       // System.out.println(granules_parameter);
//                        granules_parameter = granules_parameter + "type=inventory&model=1&sell_price=0.0";
//                        String post_response = rest.postdata(granules_parameter);
//                        if (post_response != null) {
//                            //System.out.println(post_response);
//                            appendToFile(post_response);
//                        }
//                    }
//                }
//            }

            //********* Main Program Code End ******************                  
            //  System.out.println(rest_server);

        } catch (Exception ex) {
            appendToFile(ex);
        }
    }

    public static String getKeyField(String mapping) throws Exception {
        String keyfield = "";
        String[][] mapping_data = readCSV(mapping, 10);
        for (int i = 0; i < mapping_data.length; i++) {
            if (mapping_data[0][i].trim().equalsIgnoreCase("linkfield")) {
                keyfield = mapping_data[1][i];
                break;
            }
        }
        return keyfield;
    }

    public static String[][] readCSV(String filename, int no_of_columns_in_csv) throws Exception {
 
        String[] dataRow = new String[no_of_columns_in_csv];
        List<String[]> al = new ArrayList<String[]>();
        CSVReader reader = new CSVReader(new FileReader(filename));
        while ((dataRow = reader.readNext()) != null) {
            al.add(dataRow);
        }

        String[][] data = new String[al.size()][no_of_columns_in_csv];
        data = al.toArray(data);
        return data;
    }

    private static String getValueForKey(String[][] csvfile, int row, String keyfield) {
        String output = "";
        for (int i = 0; i < csvfile[0].length; i++) {
            if (csvfile[0][i].trim().equalsIgnoreCase(keyfield)) {
                output = csvfile[row][i];
                break;
            }
        }
        return output;
    }

    private static String getParameter(String[][] dataArray, int row, String dictionaryfile, String field_group) throws Exception {
        String output = "", key, datatype = "";
        Map<String, String> dictionary = new HashMap<>();
        Map<String, String> datatype_dictionary = new HashMap<>();
        dictionary = buildDictionaryFromCSV(dictionaryfile, 0, 1);
        datatype_dictionary = buildDictionaryFromCSV(dictionaryfile, 0, 2);
 
        for (int j = 0; j < dataArray[0].length; j++) {
            key = checkForKey(dataArray[0][j].trim(), dictionary);
            
 
            datatype = checkForKey(dataArray[0][j].trim(), datatype_dictionary);
 
            
            if (key != null && dataArray[row][j].length() != 0) {
                  
                if (datatype.equalsIgnoreCase("date")) {
                    dataArray[row][j] = convertToDate(dataArray[row][j]);
 
                }

                

                if (field_group.length() == 0) {
                    output = output + key + "=" + dataArray[row][j] + "&";
                } else {
                    output = output + field_group + key + "=" + dataArray[row][j] + "&";
                }
                  
            }
        }
 
        return output;
    }

    public static Map buildDictionaryFromCSV(String csvfile, int first_column, int second_column) throws Exception {
        Map<String, String> dictionary = new HashMap<>();
        String[][] csvArray = readCSV(csvfile, 100);
         
        for (int i = 0; i < csvArray.length; i++) {
            dictionary.put(csvArray[i][first_column].trim(), csvArray[i][second_column].trim());
     
        }
        return dictionary;
    }

    public static String checkForKey(String Key, Map<String, String> dictionary) {
        String value = null;
        //System.out.println(Key);
        if (dictionary.containsKey(Key)) {
            value = dictionary.get(Key);
        } else {
            value = "";
        }

        if (value == null) {
            return null;
        } else if (value.equals("")) {
            return null;
        } else {
           
            return value;
        }
    }

    private static int getKeyIndex(String[] header, String keyfield) {
        int key_index = -1;
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(keyfield)) {
                key_index = i;
                break;
            }
        }
        return key_index;
    }

    private static String getFieldGroup(String mapping, String filepath) throws Exception {
        String groupname = "";
        String[][] mapping_data = readCSV(mapping, 100);
        String filename = filepath.substring(filepath.lastIndexOf("\\") + 1);
        for (int i = 0; i < mapping_data.length; i++) {
            if (mapping_data[i][1].trim().equalsIgnoreCase(filename)) {
                groupname = mapping_data[i][3];
                break;
            }
        }
        return groupname;
    }

    private static String getParameters(String[][] dsurl, String keyfield, String mapping, String linkedfile, String value_for_key, String dictionary) throws Exception {
        
        String output_parameter = "";
        int key_column_index = getKeyIndex(dsurl[0], keyfield);        
        String field_group = getFieldGroup(mapping, linkedfile);
        int field_group_number = 0;
        for (int j = 1; j < dsurl.length; j++) {
            if (dsurl[j][0] != null) {
                if (dsurl[j][key_column_index].trim().equalsIgnoreCase(value_for_key)) {
                    String tmp = getParameter(dsurl, j, dictionary, field_group + "[und][" + field_group_number + "]");
                    
                    if (tmp.length() != 0) {
                        field_group_number++;
                        output_parameter += tmp;
                    }
                }
            }
        }
        return output_parameter;
    }

    private static String convertToDate(String datestring) {
        String[] parseddate = datestring.split("-");
        if (parseddate.length != 3) {
            System.out.println("Date format not matched.. Expecting dd-MON-yy");
        }
        int month, year;
        switch (parseddate[1].toLowerCase()) {
            case "jan":
                month = 1;
                break;
            case "feb":
                month = 2;
                break;
            case "mar":
                month = 3;
                break;
            case "apr":
                month = 4;
                break;
            case "may":
                month = 5;
                break;
            case "jun":
                month = 6;
                break;
            case "jul":
                month = 7;
                break;
            case "aug":
                month = 8;
                break;
            case "sep":
                month = 9;
                break;
            case "oct":
                month = 10;
                break;
            case "nov":
                month = 11;
                break;
            case "dec":
                month = 12;
                break;
            default:
                month = 0;
                break;
        }
        year = Integer.parseInt(parseddate[2]);
        if (year > 50) {
            year = 1900 + year;
        } else {
            year = 2000 + year;
        }
 
        return month + "/" + parseddate[0] + "/" + year;
    }

    public static void appendToFile(Exception e) {
        try {
            FileWriter fstream = new FileWriter("post.log", true);
            BufferedWriter out = new BufferedWriter(fstream);
            PrintWriter pWriter = new PrintWriter(out, true);
            Date now = new Date();
            pWriter.printf("\n%s\t", now.toString());
            e.printStackTrace(pWriter);
        } catch (Exception ie) {
            throw new RuntimeException("Could not write Exception to file", ie);
        }
    }

    public static void appendToFile(String text) {
        try {
            FileWriter fstream = new FileWriter("post.log", true);
            BufferedWriter out = new BufferedWriter(fstream);
            PrintWriter pWriter = new PrintWriter(out, true);
            // simple arithmetics. Be careful of int overflows!
            Date now = new Date();
            pWriter.printf("\n%s\t", now.toString());
            pWriter.printf("%s\n", text);
        } catch (Exception ie) {
            throw new RuntimeException("Could not write Exception to file", ie);
        }
    }
}