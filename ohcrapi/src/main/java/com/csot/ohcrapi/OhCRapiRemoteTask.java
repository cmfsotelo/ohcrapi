package com.csot.ohcrapi;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapiRemoteTask extends AsyncTask<Void, Void, String> {
    final static String TAG = OhCRapiRemoteTask.class.getName();
    private String mApiKey;
    private boolean isOverlayRequired = false;
    private Object image;
    private String mLanguage;
    private OhCRapiListener OhCRapiListener;

    public OhCRapiRemoteTask(Object image, boolean isOverlayRequired, OhCRapiListener listener) {
        this.mApiKey = OhCRapi.getApiKey();
        this.mLanguage = OhCRapi.getTrainedDataLanguage();
        this.isOverlayRequired = isOverlayRequired;
        this.image = image;
        this.OhCRapiListener = listener;
    }

    public OhCRapiRemoteTask(Object image, OhCRapiListener listener) {
        this.mApiKey = OhCRapi.getApiKey();
        this.mLanguage = OhCRapi.getTrainedDataLanguage();
        this.image = image;
        this.OhCRapiListener = listener;
    }

    public static byte[] getByteArrayFromFile(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                throw new IOException(
                        "EOF reached while trying to read the whole file");
            }
        } finally {
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {
            }
        }
        return buffer;
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            return sendPost(mApiKey, isOverlayRequired, image, mLanguage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to call service", e);
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        OhCRapiListener.onOhCRapiStarted();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        OhCRapiListener.onOhCRapiFinished(result);
    }

    private String sendPost(String apiKey, boolean isOverlayRequired, Object image, String language) throws Exception {
        if (image instanceof Bitmap) {
            return sendPost(apiKey, isOverlayRequired, (Bitmap) image, language);
        } else if (image instanceof String) {
            return sendPost(apiKey, isOverlayRequired, (String) image, language);
        } else if (image instanceof File) {
            return sendPost(apiKey, isOverlayRequired, (File) image, language);
        } else {
            return sendPost(apiKey, isOverlayRequired, (byte[]) image, language);
        }
    }

    private String sendPost(String apiKey, boolean isOverlayRequired, String imageUrl, String language) throws Exception {

        URL obj = new URL(OhCRapi.getOcrUrl()); // OCR API Endpoints
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");


        JSONObject postDataParams = new JSONObject();

        postDataParams.put("apikey", apiKey);
        postDataParams.put("isOverlayRequired", isOverlayRequired);
        postDataParams.put("url", imageUrl);
        postDataParams.put("language", language);


        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(getPostDataString(postDataParams));
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //return result
        return String.valueOf(response);
    }

    private String sendPost(String apiKey, boolean isOverlayRequired, Bitmap imageFile, String language) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(imageFile.getByteCount());
        imageFile.copyPixelsToBuffer(byteBuffer);
        return sendPost(apiKey, isOverlayRequired, byteBuffer.array(), language);
    }

    private String sendPost(String apiKey, boolean isOverlayRequired, File imageFile, String language) throws Exception {
        return sendPost(apiKey, isOverlayRequired, getByteArrayFromFile(imageFile), language);
    }

    private String sendPost(String apiKey, boolean isOverlayRequired, byte[] data, String language) throws Exception {

        URL obj = new URL(OhCRapi.getOcrUrl()); // OCR API Endpoints
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");


        JSONObject postDataParams = new JSONObject();

        postDataParams.put("apikey", apiKey);
        postDataParams.put("isOverlayRequired", isOverlayRequired);
//        byte[] base64Encoded = Base64.encode(data, Base64.DEFAULT);
        String dataString = new String(data);
        postDataParams.put("file", dataString);
        postDataParams.put("language", language);

        String logMsg = String.format("URL: " + OhCRapi.getOcrUrl() + " > posting the following JSONObject - %s", postDataParams.toString());
        Log.v(TAG, logMsg);
//        logMsg = logMsg.replaceAll("\"file\":\"[^\"]+\"", "\"file\":\"FILE CONTENT NOT INCLUDED IN LOGS\"");
//        Log.d(TAG, logMsg);
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(getPostDataString(postDataParams));
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //return result
        return String.valueOf(response);
    }
}
