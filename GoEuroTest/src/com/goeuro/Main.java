package com.goeuro;

import com.csvreader.CsvWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {

    public static void main(String[] args) {

        String cityName = "Berlin";

        try {
            cityName = args[0].toString();
        } catch (Exception e) {
            System.out.println("Insufficient parameter\n usage GoEuroText <City>");
        }

        URL url = null;
        try {
            url = new URL("http://api.goeuro.com/api/v2/position/suggest/en/" + cityName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String response = "";
        if (url != null) {
            try {
                response = getURL(url);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }


        String outputFile = "./mycsv.csv";


        try {

            JSONArray arr = (JSONArray) JSONValue.parse(response);

            if (arr.size() == 0) {
                System.out.println("Location is empty or do not exists");
            } else {
                File outputRealFile = new File(outputFile);
                if (outputRealFile.exists()) outputRealFile.delete();

                CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
                for (Object obj : arr) {
                    JSONObject jsonObj = (JSONObject) obj;
                    String id = jsonObj.get("_id").toString();
                    String name = jsonObj.get("name").toString();
                    String type = jsonObj.get("type").toString();
                    JSONObject geoPositon = (JSONObject) jsonObj.get("geo_position");
                    String longitude = geoPositon.get("longitude").toString();
                    String latitude = geoPositon.get("latitude").toString();
                    csvOutput.write(id);
                    csvOutput.write(name);
                    csvOutput.write(type);
                    csvOutput.write(latitude);
                    csvOutput.write(longitude);
                    csvOutput.endRecord();
                    id = null;
                    name = null;
                    type = null;
                    latitude = null;
                    longitude = null;
                    jsonObj = null;
                }
                csvOutput.close();
            }


        } catch (IOException e) {
            System.out.println("Unable to write csv file");
            e.printStackTrace();
        }

        System.out.println("done ! your file is ready");
    }


    public static String getURL(URL url) throws IOException {
        HttpURLConnection conn = null;
        StringBuilder stringBuilder = new StringBuilder();

        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(5000);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", "Mozilla | Telsso Browser");
        conn.addRequestProperty("Referer", "telsso.com");

        boolean redirect = false;

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }


        if (redirect) {
            String newUrl = conn.getHeaderField("Location");
            String cookies = conn.getHeaderField("Set-Cookie");
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestProperty("Cookie", cookies);
            conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            conn.addRequestProperty("User-Agent", "Mozilla");
            conn.addRequestProperty("Referer", "telsso.com");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }


        conn.getInputStream().close();

        return stringBuilder.toString();
    }


}
