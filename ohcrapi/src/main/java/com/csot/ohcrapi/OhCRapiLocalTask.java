package com.csot.ohcrapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;
import java.util.Collection;

/**
 * Description Created on 19-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class OhCRapiLocalTask extends AsyncTask<Void, Void, Void> {
    protected final static String TAG = OhCRapiLocalTask.class.getName();
    private OhCRapiListener mOhCRapiListener;
    private String filePath;
    private Bitmap mBitmap;
    private String scannedText;
    private TessBaseAPI baseApi = OhCRapi.mLocalOcrEngine;
    private Collection<Rect> rectangles;

    public OhCRapiLocalTask(OhCRapiListener OhCRapiListener, String filePath) {
        this.mOhCRapiListener = OhCRapiListener;
        this.filePath = filePath;
    }

    public OhCRapiLocalTask(OhCRapiListener OhCRapiListener, Bitmap bitmap) {
        this.mOhCRapiListener = OhCRapiListener;
        this.mBitmap = bitmap;
    }

    public OhCRapiLocalTask(OhCRapiListener OhCRapiListener, Bitmap bitmap, Collection<Rect> rectangles) {
        this.mOhCRapiListener = OhCRapiListener;
        this.mBitmap = bitmap;
        this.rectangles = rectangles;
    }

    @Override
    protected Void doInBackground(Void... params) {
        processImage();
        scannedText = scanImage();
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mOhCRapiListener.onOhCRapiStarted();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mOhCRapiListener.onOhCRapiFinished(scannedText);
    }

    private void processImage() {
        if (mBitmap == null) {
            int imageOrientationCode = getImageOrientation();
            Bitmap rawBitmap = getBitmapFromPath();
            // Getting the bitmap in right orientation.
            this.mBitmap = rotateBitmap(rawBitmap, imageOrientationCode);
        }
    }

    private Bitmap getBitmapFromPath() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 4;
        return BitmapFactory.decodeFile(this.filePath, bmOptions);
    }

    private int getImageOrientation() {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(this.filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert exif != null;
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private String scanImage() {
        long startTime = System.nanoTime();
        String recognizedText;
        baseApi.setImage(this.mBitmap);
        if (rectangles == null || rectangles.isEmpty()) {
            recognizedText = baseApi.getUTF8Text();
            baseApi.clear();
        } else {
            StringBuilder sb = new StringBuilder();
            for (Rect r : rectangles) {
                baseApi.setRectangle(r);
                String s = baseApi.getUTF8Text();
                Log.d(TAG, s);
                sb.append(s);
                sb.append("\n");
            }
            recognizedText = sb.toString();
        }
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        Log.v(TAG, "Took: " + duration + "ms");
        return recognizedText;
    }

}






