package com.sdm.BkParser.SupportClasses.Rest;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Component
@Scope("singleton")
public class JSONParser {
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    static String json1 = "";
    static JSONObject Objec = null;


    public JSONParser() {

    }

    public JSONObject makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {

        // Making HTTP request
        try {
            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params);
                httpPost.setEntity(urlEncodedFormEntity);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                httpGet.addHeader("Content-Type", "application/json; charset=utf-8");
                httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0");
                httpGet.addHeader("Connection", "keep-alive");
                HttpResponse httpResponse = httpClient.execute(httpGet);

                System.out.println(httpResponse.getEntity().getContentEncoding());
                //JSONObject jsonObject = new JSONObject(httpResponse.getEntity().getContent());
               // System.out.println(jsonObject.toString());

                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, StandardCharsets.UTF_8), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
            is.close();
            json = sb.toString();

        } catch (Exception e) {

        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            System.out.println("can't create json");
        }

        // return JSON String
        return jObj;

    }





    public JSONObject httpsPOST(String url, String par)  {

        URL u = null;
        try {
            u = new URL(url);
            URLConnection conn = u.openConnection();
            HttpURLConnection http = (HttpURLConnection)conn;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

             /*
        StringJoiner sj = new StringJoiner("&","","");
                for(Map.Entry<String,String> entry : arguments.entrySet())
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + ":"
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
         */


            byte[] out = par.getBytes(StandardCharsets.UTF_8);
            //byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            /*

             */
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty( "Content-Length", String.valueOf(length));
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }


            InputStreamReader isr = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String inputLine;
            StringBuilder sb1 = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                sb1.append(inputLine + "\n");
            }
            json1 = sb1.toString();
            br.close();
            isr.close();

            http.disconnect();



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





        try {
            Objec = new JSONObject(json1);
        } catch (JSONException e) { }

        return Objec;
    }


    public JSONObject httpsGET(String url)  {


        try {
            URL u;
            u = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setConnectTimeout(3_000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setDoOutput(true);
            conn.setDoInput(true);
            //DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            //output.writeBytes(query);
            //output.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,StandardCharsets.UTF_8 );
            BufferedReader br = new BufferedReader(isr);

            String inputLine;
            StringBuilder sb1 = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                sb1.append(inputLine + "\n");
            }
            json1 = sb1.toString();
            br.close();
            isr.close();
            is.close();
            conn.disconnect();

            try {
                Objec = new JSONObject(json1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Objec;
    }



    public JSONObject httpGetWithGZIP(String url){

        DefaultHttpClient httpClient = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-Type", "application/json; charset=utf-8");
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0");
        httpGet.addHeader("Connection", "keep-alive");
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            is = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), StandardCharsets.UTF_8), 1000000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String line = null;
        while (true) {
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(line + "/n");
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        json = sb.toString();

        // try parse the string to a JSON object
        try {
        jObj = new JSONObject(json);
        } catch (JSONException e) {
        System.out.println("can't create json");
        }
        // return JSON String
        return jObj;
        }


    public JSONObject httpsGetWithAuth(String url, String auth)  {


        try {
            URL u;
            u = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");

            conn.setRequestProperty("Authorization", "Basic "+ auth);

            conn.setConnectTimeout(3_000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setDoOutput(true);
            conn.setDoInput(true);
            //DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            //output.writeBytes(query);
            //output.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;
            StringBuilder sb1 = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                sb1.append(inputLine + "\n");
            }
            json1 = sb1.toString();
            br.close();
            isr.close();
            is.close();
            conn.disconnect();

            try {
                Objec = new JSONObject(json1);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Objec;
    }



    public StringBuilder httpsGETHTML(String url)  {

        StringBuilder resp = new StringBuilder();
        try {
            URL u;
            u = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setConnectTimeout(3_000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setDoOutput(true);
            conn.setDoInput(true);
            //DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            //output.writeBytes(query);
            //output.close();
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is,StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);

            String inputLine;
            StringBuilder sb1 = new StringBuilder();

            while ((inputLine = br.readLine()) != null) {
                sb1.append(inputLine + "\n");
            }
            //json1 = sb1.toString();
            br.close();
            isr.close();
            is.close();
            conn.disconnect();
            resp = sb1;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return resp;
    }

    public JSONObject httpsPOSTForOlimp(String url, String par, String key)  {

        URL u = null;
        try {
            u = new URL(url);
            URLConnection conn = u.openConnection();
            HttpURLConnection http = (HttpURLConnection)conn;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

             /*
        StringJoiner sj = new StringJoiner("&","","");
                for(Map.Entry<String,String> entry : arguments.entrySet())
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + ":"
                            + URLEncoder.encode(entry.getValue(), "UTF-8"));
         */


            byte[] out = par.getBytes(StandardCharsets.UTF_8);
            //byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            /*

             */
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty( "Content-Length", String.valueOf(length));
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("x-bf", "7b3874512c00963561d144a0feceec12");
            http.setRequestProperty("x-token", key);
            http.connect();
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }


            InputStreamReader isr = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String inputLine;
            StringBuilder sb1 = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                sb1.append(inputLine + "\n");
            }
            json1 = sb1.toString();
            br.close();
            isr.close();

            http.disconnect();



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }





        try {
            Objec = new JSONObject(json1);
        } catch (JSONException e) { }

        return Objec;
    }
}



