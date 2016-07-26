package com.csot.ohcrapi.local;

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

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapiLocal {
    final static String TAG = OhCRapiLocal.class.getName();
    static TessBaseAPI mLocalOcrEngine;

    /** Initializes the Ocr engine (TessBaseAPI). If the engine already exists, nothing is done.
     * @param ctx - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    public static void init(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        if (mLocalOcrEngine == null) {
            mLocalOcrEngine = touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier);
        }
    }

    /** Restarts the Ocr engine (TessBaseAPI). If the engine already exists, that engine is stopped and a new one is created.
     * @param ctx - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    public static void restart(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        if (mLocalOcrEngine != null) {
            mLocalOcrEngine.clear();
            mLocalOcrEngine.end();
        }
        mLocalOcrEngine = touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier);
    }

    /**
     *
     * @param ctx - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     * @return returns the created TessBaseAPI (OCR Engine) instance
     */
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
                Log.v(TAG, "Copied " + tessData.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + tessData.getAbsolutePath() + " : " + e.toString());
            }
        } else {
            Log.d(TAG, "TessData already present");
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
