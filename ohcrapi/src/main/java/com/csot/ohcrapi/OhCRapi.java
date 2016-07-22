package com.csot.ohcrapi;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapi {
    final static String TAG = "OCR";
    final static Random random = new Random();
    final static String url = "https://api.ocr.space/parse/image"; // OCR API Endpoints
    final static String url2 = "https://apifree2.ocr.space/parse/image"; // OCR API Endpoints
    static OhCRapi mInstance;
    static TessBaseAPI mLocalOcrEngine;
    String trainedDataLanguage = "eng";
    String apiKey;

    private OhCRapi(String trainedDataLanguage, String apiKey) {
        this.trainedDataLanguage = trainedDataLanguage;
        this.apiKey = apiKey;
    }

    public static void initRemote(String trainedDataLanguage, String apiKey) {
        mInstance = new OhCRapi(trainedDataLanguage, apiKey);
    }

    //ProgressNotifier
    public static void initLocal(Context ctx, String tessDirectoryPath, String trainedDataLanguage, TessBaseAPI.ProgressNotifier progressNotifier) {
        if (mLocalOcrEngine == null) {
            mLocalOcrEngine = touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier);
        }
    }

    public static String getApiKey() {
        return mInstance.apiKey;
    }

    public static String getTrainedDataLanguage() {
        return mInstance.trainedDataLanguage;
    }

    public static String getOcrUrl() {
        int num = random.nextInt(2);
        return (num == 1) ? url : url2;
    }

    @NonNull
    static TessBaseAPI touchTesseract(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        File tessDir = new File(tessDirectoryPath + File.separator + "tessdata");
        tessDir.mkdir();
        File tessData = new File(tessDir.getAbsolutePath() + File.separator + trainedDataLanguage + ".traineddata");
        if (!tessData.exists()) {
            try {
                AssetManager assetManager = ctx.getAssets();
                InputStream in = assetManager.open("tessdata/" + trainedDataLanguage + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                // Output stream with the location where we have to write the eng.traineddata file.
                OutputStream out = new FileOutputStream(tessData);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();
//                mInstance.trainedDataLanguage = trainedDataLanguage;
                Log.v(OhCRapi.TAG, "Copied " + tessData.getAbsolutePath());
            } catch (IOException e) {
                Log.e(OhCRapi.TAG, "Was unable to copy " + tessData.getAbsolutePath() + " : " + e.toString());
            }
        } else {
            Log.d(OhCRapi.TAG, "TessData already present");
        }
        TessBaseAPI tess;
        if (progressNotifier != null) {
            tess = new TessBaseAPI(progressNotifier);
        } else {
            tess = new TessBaseAPI();
        }
        tess.init(tessDirectoryPath, trainedDataLanguage);
        return tess;
    }
}
