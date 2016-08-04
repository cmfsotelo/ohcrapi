package com.csot.ohcrapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * Description Created on 29-07-2016.
 *
 * @author <a href="mailto:carlos.sotelo7@gmail.com">csotelo</a>
 * @version $Revision : 1 $
 */
public class Utils {

    public static byte[] getByteArrayFromFile(File file) throws IOException {
           /* if (file.length() > MAX_FILE_SIZE) {
                throw new FileTooBigException(file);
            }*/
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

    public static byte[] bitmapToByteArray(Bitmap bm, Bitmap.CompressFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(format, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static byte[] decodeFromFile(InputStream fileStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[1024];
        while ((read = fileStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        fileStream.close();
        return out.toByteArray();
    }

    public static Bitmap decodeImageFromFile(InputStream fileStream) throws Exception {
        return byteArrayToBitmap(decodeFromFile(fileStream));
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean renameFile(String sourceName, String targetName) throws IOException {
        File from = new File(sourceName);
        File to = new File(targetName);
        return from.renameTo(to);
    }
}
