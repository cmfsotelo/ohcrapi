package com.csot.ohcrapi.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.csot.ohcrapi.Line;
import com.csot.ohcrapi.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapiRemote {
    public final static String publicUrl1 = "https://api.ocr.space/parse/image"; // OCR API Endpoints
    public final static String publicUrl2 = "https://apifree2.ocr.space/parse/image"; // OCR API Endpoints
    final static String TAG = OhCRapiRemote.class.getName();
    final static Random random = new Random();
    static List<String> urlCollection = new ArrayList<>();
    static String apiKey;
    static String language;

    static {
        urlCollection.add(publicUrl1);
        urlCollection.add(publicUrl2);
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static void setApiKey(String apiKey) {
        OhCRapiRemote.apiKey = apiKey;
    }

    public static void setUrlCollection(@NonNull Collection<String> urlCollection) {
        OhCRapiRemote.urlCollection = new ArrayList<>(urlCollection);
    }

    public static void addUrl(@NonNull String url) {
        OhCRapiRemote.urlCollection.add(url);
    }

    public static void addAllUrl(@NonNull Collection<String> urlCollection) {
        OhCRapiRemote.urlCollection.addAll(urlCollection);
    }

    public static void setup(String apiKey, String language) {
        OhCRapiRemote.apiKey = apiKey;
        OhCRapiRemote.language = language;
    }

    @NonNull
    public static String getOcrUrl() {
        if (urlCollection.isEmpty()) {
            return "EMPTYURL";
        }
        int num = random.nextInt(urlCollection.size());
        return urlCollection.get(num);
    }

    public static void performRequest(@NonNull Context ctx, @NonNull String filePath, boolean isOverlayRequired, @NonNull OhCRapiRemoteListener listener) throws IOException {
        byte[] imageData = Utils.getByteArrayFromFile(new File(filePath));
        performRequest(ctx, apiKey, isOverlayRequired, imageData, language, listener);
    }

    public static void performRequest(@NonNull Context ctx, @NonNull Bitmap image, boolean isOverlayRequired, @NonNull OhCRapiRemoteListener listener) {
        byte[] imageData = Utils.bitmapToByteArray(image);
        performRequest(ctx, apiKey, isOverlayRequired, imageData, language, listener);
    }

    public static void performRequest(@NonNull Context ctx, @NonNull byte[] imageData, boolean isOverlayRequired, @NonNull OhCRapiRemoteListener listener) {
        performRequest(ctx, apiKey, isOverlayRequired, imageData, language, listener);
    }

    public static void performRequest(@NonNull Context ctx, @NonNull File imageFile, boolean isOverlayRequired, @NonNull OhCRapiRemoteListener listener) throws IOException {
        byte[] imageData = Utils.getByteArrayFromFile(imageFile);
        performRequest(ctx, apiKey, isOverlayRequired, imageData, language, listener);
    }

    public static void performRequest(@NonNull Context ctx, @NonNull final String apiKey, boolean isOverlayRequired, @NonNull byte[] data, @NonNull final String language, @NonNull OhCRapiRemoteListener listener) {
        Request<NetworkResponse> networkRequest = getRequest(apiKey, isOverlayRequired, data, language, listener);
        VolleySingleton.getInstance(ctx).addToRequestQueue(networkRequest);
    }

    public static Request<NetworkResponse> getRequest(@NonNull final String apiKey, @NonNull Bitmap image, @NonNull final String language, @NonNull OhCRapiRemoteListener listener) {
        return getRequest(apiKey, false, image, language, listener);
    }

    public static Request<NetworkResponse> getRequest(@NonNull final String apiKey, final boolean isOverlayRequired, @NonNull final Bitmap image, final String language, final @NonNull OhCRapiRemoteListener listener) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(image.getByteCount());
        image.copyPixelsToBuffer(byteBuffer);
        return getRequest(apiKey, isOverlayRequired, byteBuffer.array(), language, listener);
    }

    public static Request<NetworkResponse> getRequest(@NonNull final String apiKey, final boolean isOverlayRequired, @NonNull final byte[] data, @NonNull final String language, @NonNull final OhCRapiRemoteListener listener) {
        listener.onOhCRapiStarted();
        return new VolleyMultipartRequest(Request.Method.POST, OhCRapiRemote.getOcrUrl(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);
                            Log.d(TAG, "getRequest > RESPONSE JSON: " + resultResponse);
                            if (isOverlayRequired) {
                                Collection<Line> lines = new ArrayList<>();
                                JSONArray parsedResults = result.getJSONArray("ParsedResults");
                                JSONObject parsedResult = parsedResults.getJSONObject(0).getJSONObject("TextOverlay");
                                JSONArray linesJson = parsedResult.getJSONArray("Lines");
                                for (int i = 0; i < linesJson.length(); i++) {
                                    lines.add(new Line((JSONObject) linesJson.get(i)));
                                }
                                listener.onOhCRapiFinished(lines);
                            } else {
                                JSONArray parsedResults = result.getJSONArray("ParsedResults");
                                JSONObject parsedResult = parsedResults.getJSONObject(0);
                                String parsedText = parsedResult.getString("ParsedText");
                                listener.onOhCRapiFinished(parsedText);
                            }
                        } catch (JSONException e) {
//                            Log.e(TAG, e.getMessage(), e);
                            listener.onOhCRapiFinished(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        NetworkResponse networkResponse = error.networkResponse;
//                        String errorMessage = "Unknown error";
//                        if (networkResponse == null) {
//                            if (error.getClass().equals(TimeoutError.class)) {
//                                errorMessage = "Request timeout";
//                            } else if (error.getClass().equals(NoConnectionError.class)) {
//                                errorMessage = "Failed to connect server";
//                            }
//                        } else {
//                            String result = new String(networkResponse.data);
//                            try {
//                                JSONObject response = new JSONObject(result);
//                                String status = response.getString("status");
//                                String message = response.getString("message");
//                                Log.e("Error Status", status);
//                                Log.e("Error Message", message);
//                                if (networkResponse.statusCode == 404) {
//                                    errorMessage = "Resource not found";
//                                } else if (networkResponse.statusCode == 401) {
//                                    errorMessage = message + " Please login again";
//                                } else if (networkResponse.statusCode == 400) {
//                                    errorMessage = message + " Check your inputs";
//                                } else if (networkResponse.statusCode == 500) {
//                                    errorMessage = message + " Something is getting wrong";
//                                }
//                            } catch (JSONException e) {
////                                Log.e(TAG, errorMessage, e);
//                                listener.onOhCRapiFinished(e);
//                            }
//                        }
////                        Log.e("Error", errorMessage);
////                        error.printStackTrace();
                        listener.onOhCRapiFinished(error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("apikey", apiKey);
                params.put("language", language);
                params.put("isOverlayRequired", String.valueOf(isOverlayRequired));
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart("image.jpg", data));
                return params;
            }
        };
    }

}
