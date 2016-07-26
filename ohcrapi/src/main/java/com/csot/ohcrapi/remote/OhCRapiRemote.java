package com.csot.ohcrapi.remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapiRemote {
    protected final static String TAG = OhCRapiRemote.class.getName();
    final static Random random = new Random();
    final static String url = "https://api.ocr.space/parse/image"; // OCR API Endpoints
    final static String url2 = "https://apifree2.ocr.space/parse/image"; // OCR API Endpoints

    public static String getOcrUrl() {
        int num = random.nextInt(2);
        return (num == 1) ? url : url2;
    }

    public static void performRequest(@NonNull Context ctx, @NonNull final String apiKey, @NonNull Bitmap imageFile, @NonNull final String language, @NonNull OhCRapiRemoteListener listener) {
        Request<NetworkResponse> networkRequest = getRequest(apiKey, false, imageFile, language, listener);
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
//                        String parsedText = "";
//                        String resultResponse = new String(response.data);
//                        try {
//                            JSONObject result = new JSONObject(resultResponse);
//                            JSONArray parsedResults = result.getJSONArray("ParsedResults");
//                            JSONObject parsedResult = parsedResults.getJSONObject(0);
//                            parsedText = parsedResult.getString("ParsedText");
//                        } catch (JSONException e) {
//                            Log.e(TAG, e.getMessage(), e);
//                        }
//                        Log.i(TAG, "getRequest > RESPONSE JSON: " + resultResponse);
                        listener.onOhCRapiFinished(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = "Unknown error";
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = "Request timeout";
                            } else if (error.getClass().equals(NoConnectionError.class)) {
                                errorMessage = "Failed to connect server";
                            }
                        } else {
                            String result = new String(networkResponse.data);
                            try {
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");
                                Log.e("Error Status", status);
                                Log.e("Error Message", message);
                                if (networkResponse.statusCode == 404) {
                                    errorMessage = "Resource not found";
                                } else if (networkResponse.statusCode == 401) {
                                    errorMessage = message + " Please login again";
                                } else if (networkResponse.statusCode == 400) {
                                    errorMessage = message + " Check your inputs";
                                } else if (networkResponse.statusCode == 500) {
                                    errorMessage = message + " Something is getting wrong";
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, errorMessage, e);
                            }
                        }
                        Log.v("Error", errorMessage);
                        error.printStackTrace();
                        listener.onOhCRapiFinished(networkResponse);
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
