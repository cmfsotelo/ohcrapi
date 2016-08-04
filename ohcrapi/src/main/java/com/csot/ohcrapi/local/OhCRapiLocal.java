package com.csot.ohcrapi.local;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.csot.ohcrapi.Utils;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

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
    static String tessDirectoryPath;
    static String trainedDataLanguage;
    static TessBaseAPI.ProgressNotifier progressNotifier;
    static ThinDownloadManager downloadManager;
    static NotificationManager mNotifyManager;
    static NotificationCompat.Builder mBuilder;
    static InitStatus mInitStatus = InitStatus.NON_INITIALIZED;
    // declare the dialog as a member field of your activity
    static ProgressDialog mProgressDialog;
    static boolean showNotification = false;

    static {
        downloadManager = new ThinDownloadManager();
    }

    /**
     * Calls method init with the allowDownload flag as true -> init(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier, true);
     *
     * @param ctx                 - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    public static InitStatus init(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable OhCRapiLocalInitializedCallback initializedCallback, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        return init(ctx, tessDirectoryPath, trainedDataLanguage, initializedCallback, progressNotifier, true);
    }

    /**
     * Initializes the Ocr engine (TessBaseAPI). If the engine already exists, nothing is done.
     *
     * @param ctx                 - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     * @param allowDownload       - if false, tessdata must be present in the assets folder, otherwise, the tessdata will be fetch from the internet
     */
    public static InitStatus init(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable OhCRapiLocalInitializedCallback initializedCallback, @Nullable TessBaseAPI.ProgressNotifier progressNotifier, boolean allowDownload) {
        if (mLocalOcrEngine != null) {
            return InitStatus.INITIALIZED;
        } else {
            return touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, initializedCallback, progressNotifier, allowDownload);
        }
    }

    /**
     * Calls method restart with the allowDownload flag as true -> restart(ctx, tessDirectoryPath, trainedDataLanguage, progressNotifier, true);
     *
     * @param ctx                 - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    public static InitStatus forceInit(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable OhCRapiLocalInitializedCallback initializedCallback, @Nullable TessBaseAPI.ProgressNotifier progressNotifier) {
        return restart(ctx, tessDirectoryPath, trainedDataLanguage, initializedCallback, progressNotifier, true);
    }

    /**
     * Restarts the Ocr engine (TessBaseAPI). If the engine already exists, that engine is stopped and a new one is created.
     *
     * @param ctx                 - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     * @param allowDownload       - if false, tessdata must be present in the assets folder, otherwise, the tessdata will be fetch from the internet
     */
    public static InitStatus restart(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable OhCRapiLocalInitializedCallback initializedCallback, @Nullable TessBaseAPI.ProgressNotifier progressNotifier, boolean allowDownload) {
        if (mLocalOcrEngine != null) {
            mLocalOcrEngine.clear();
            mLocalOcrEngine.end();
        }
        return touchTesseract(ctx, tessDirectoryPath, trainedDataLanguage, initializedCallback, progressNotifier, allowDownload);
    }

    public static boolean checkTessData() {
        File tessDir = new File(tessDirectoryPath + File.separator + "tessdata");
        File tessData = new File(tessDir.getAbsolutePath() + File.separator + trainedDataLanguage + ".traineddata");
        return tessData.exists();
    }

    /**
     * @param ctx                 - Context
     * @param tessDirectoryPath   - Directory where the tessdata and traineddata file will be created
     * @param trainedDataLanguage - Language of the traineddata file
     * @param progressNotifier    - Optional progressNotifier interface
     */
    static InitStatus touchTesseract(@NonNull Context ctx, @NonNull String tessDirectoryPath, @NonNull String trainedDataLanguage, @Nullable OhCRapiLocalInitializedCallback initializedCallback, @Nullable TessBaseAPI.ProgressNotifier progressNotifier, boolean downloadTessData) {
        if (mInitStatus == InitStatus.PENDING) {
            return mInitStatus;
        }
        OhCRapiLocal.trainedDataLanguage = trainedDataLanguage;
        OhCRapiLocal.tessDirectoryPath = tessDirectoryPath;
        OhCRapiLocal.progressNotifier = progressNotifier;
        mBuilder = new NotificationCompat.Builder(ctx);
        mNotifyManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mProgressDialog = new ProgressDialog(ctx);
        File tessDir = new File(tessDirectoryPath + File.separator + "tessdata");
        tessDir.mkdir();
        File tessData = new File(tessDir.getAbsolutePath() + File.separator + trainedDataLanguage + ".traineddata");
        if (!tessData.exists()) {
            if (downloadTessData) {
                return getFromWeb(trainedDataLanguage, tessData, initializedCallback);
            } else {
                return copyFromAssets(ctx, trainedDataLanguage, tessData, initializedCallback);
            }
        } else {
            Log.v(TAG, "TessData already present!");
            return initEngine(initializedCallback);
        }
    }

    private static InitStatus copyFromAssets(Context ctx, String trainedDataLanguage, File tessData, @Nullable OhCRapiLocalInitializedCallback initializedCallback) {
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
            Log.d(TAG, "Copied " + tessData.getAbsolutePath());
            return initEngine(initializedCallback);
        } catch (IOException e) {
            Log.e(TAG, "Unable to create " + tessData.getAbsolutePath() + " : " + e.toString());
            return InitStatus.FAILED;
        }
    }

    private static InitStatus getFromWeb(final String trainedDataLanguage, final File tessData, @Nullable final OhCRapiLocalInitializedCallback initializedCallback) {
        String title = "TessData Download - Language " + trainedDataLanguage.toUpperCase();
        mBuilder.setContentTitle(title)
                .setSmallIcon(android.R.drawable.stat_sys_download);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
// instantiate it within the onCreate method
        mProgressDialog.setMessage(title);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                showNotification = true;
            }
        });
        Uri downloadUri = Uri.parse("https://github.com/tesseract-ocr/tessdata/blob/master/" + trainedDataLanguage + ".traineddata?raw=true");
        Uri destinationUri = Uri.parse(tessData.getAbsolutePath() + ".tmp");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
//                .addCustomHeader("Auth-Token", "YourTokenApiKey")
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    long elapsedTime = System.currentTimeMillis();

                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        String message = "Download complete!";
                        Log.d(TAG, message);
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.setMessage(message);
                        }
                        if (showNotification) {
                            mBuilder.setContentText(message)
                                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                    .setProgress(0, 0, false);
                            mNotifyManager.notify(0, mBuilder.build());
                        }
                        try {
                            Utils.renameFile(tessData.getAbsolutePath() + ".tmp", tessData.getAbsolutePath());
                            initEngine(initializedCallback);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        String message = "Download Failed";
                        downloadManager.release();
                        Log.d(TAG, message);
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.setMessage(message);
                        }
                        if (showNotification) {
                            mBuilder.setContentText(message)
                                    .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                                    .setProgress(0, 0, false);
                            mNotifyManager.notify(0, mBuilder.build());
                        }
                        mInitStatus = InitStatus.FAILED;
                    }

                    @Override
                    public void onProgress(final DownloadRequest downloadRequest, final long totalBytes, final long downloadedBytes, final int progress) {
                        long time = System.currentTimeMillis();
                        if (time - elapsedTime > 500) {
                            String message = "Downloading: " + Utils.readableFileSize(downloadedBytes) + " / " + Utils.readableFileSize(totalBytes);
                            Log.v(TAG, "Downloading: " + Utils.readableFileSize(downloadedBytes) + " / " + Utils.readableFileSize(totalBytes) + " -> " + progress + "%");
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.setProgress(progress);
                                mProgressDialog.setMessage(message);
                            }
                            if (showNotification) {
                                mBuilder.setProgress((int) totalBytes, (int) downloadedBytes, false);
                                mBuilder.setContentText(message);
                                mNotifyManager.notify(0, mBuilder.build());
                            }
                            elapsedTime = time;
                        }

                    }
                });
        downloadManager.add(downloadRequest);
        mProgressDialog.show();
        mInitStatus = InitStatus.PENDING;
        return mInitStatus;
    }

    private static InitStatus initEngine(OhCRapiLocalInitializedCallback initializedCallback) {
        try {
            Log.d(TAG, "Initializing Tesseract engine.");
            if (progressNotifier != null) {
                mLocalOcrEngine = new TessBaseAPI(progressNotifier);
            } else {
                mLocalOcrEngine = new TessBaseAPI();
            }
            mLocalOcrEngine.init(tessDirectoryPath, trainedDataLanguage);
            if (initializedCallback != null) {
                initializedCallback.onOhCRapiInitialized();
            }
            mInitStatus = InitStatus.INITIALIZED;
        } catch (Exception e) {
            Log.e(TAG, "Failed to init Tesseract engine! " + e.getMessage());
            mInitStatus = InitStatus.FAILED;
        }
        return mInitStatus;
    }

    public enum InitStatus {
        INITIALIZED,
        NON_INITIALIZED,
        FAILED,
        PENDING
    }
}
