package com.channelsoft.sop.shareObject;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 29/7/2016.
 */
public class JSONParser {

    private static String jobj = null;
    private static final String TAG = "JSONParser";
    private Context context;

    JSONParser() {
    }

    String getJSONFromUrl(Context context, String method, String url, String data) {
        try {
            this.context = context;
            URL link = new URL(url);

            BufferedReader rd = setupBufferReader(method, link, data);
            StringBuilder sb = new StringBuilder();
            String line;

            if (rd != null) {
                while ((line = rd.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } else {
                Log.d(TAG, "Network request: Nothing is sending back!");
            }
            rd.close();

            try {
                Object json = new JSONTokener(sb.toString()).nextValue();
                jobj = (json instanceof JSONObject ? new JSONObject(sb.toString()).toString() : new JSONArray(sb.toString()).toString());
            } catch (JSONException e) {
                Log.d(TAG, "Return Data Error: " + e);
//                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }

    private BufferedReader setupBufferReader(String method, URL url, String data) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            if (!SharedPreferenceManager.getUser(context).equals("default")) {
//                String access = SharedPreferenceManager.getConsumerKey(context) + ":" + SharedPreferenceManager.getConsumerSecret(context);
//                String basicAuth = "Basic " + new String(android.util.Base64.encode(access.getBytes(), android.util.Base64.NO_WRAP));
//                conn.addRequestProperty("Authorization", basicAuth);
//            }
            conn.setRequestMethod(method);

            OutputStreamWriter wr;
            switch (method) {
                case "POST":
                case "DELETE":
                    conn.setDoOutput(true);
                    wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();
                    wr.close();
                    Log.d("haha", "post data: " + data);
                    return new BufferedReader(new InputStreamReader(conn.getInputStream()));
                case "GET":
                    InputStream inputStream = conn.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    return new BufferedReader(inputStreamReader);
                default:
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

