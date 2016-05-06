package com.miriasystems.jkauffman.active_pay_android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.preference.PreferenceManager;

import org.apache.commons.io.IOUtils;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

import net.sf.andpdf.nio.ByteBuffer;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.beyka.tiffbitmapfactory.TiffBitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jkauffman on 3/25/2016.
 */
public class AppDelegate {

    public static void logout(Context context) {
        Intent intent = new Intent(context, ActivePayLoginActivity.class);
        context.startActivity(intent);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Use this to load a pdf file from your assets and render it to a Bitmap.
     *
     * @param context
     *            current context.
     * @param filePath
     *            of the pdf file in the assets.
     * @return a bitmap.
     */
    public static Bitmap renderToBitmap(Context context, String filePath) {
        Bitmap bi = null;
        InputStream inStream = null;
        try {

            inStream = new FileInputStream(filePath);
            bi = renderToBitmap(context, inStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                // do nothing because the stream has already been closed
            }
        }
        return bi;
    }

    public static Bitmap renderToBitmap(Context context, InputStream inStream) {
        Bitmap bi = null;
        try {

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1)
            {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] decode = baos.toByteArray();

            File fname = new File(context.getFilesDir().getAbsolutePath(), "givefilename.pdf");
            if (!fname.exists()) {
                fname.createNewFile();
            }

            OutputStream outputStream = new FileOutputStream(fname);
            baos.writeTo(outputStream);


            ByteBuffer buf = ByteBuffer.wrap(decode);
            PDFPage mPdfPage = new PDFFile(buf).getPage(0, true);
            float width = mPdfPage.getWidth();
            float height = mPdfPage.getHeight();
            RectF clip = null;
            bi = mPdfPage.getImage((int) (width), (int) (height), clip, true,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                // do nothing because the stream has already been closed
            }
        }
        return bi;
    }


    public static int calculateInTiffSampleSize(
            TiffBitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledTiffBitmapFromFile(File file,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final TiffBitmapFactory.Options options = new TiffBitmapFactory.Options();
        options.inJustDecodeBounds = true;
        TiffBitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInTiffSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return TiffBitmapFactory.decodeFile(file, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(File file,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.toString(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.toString(), options);
    }


    public static String[] loadSavedStringPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean checkBoxValue = sharedPreferences.getBoolean("CheckBox_Value", false);
        String strAddress = sharedPreferences.getString("storedServerAddress", "ServerAddress");
        String strPort = sharedPreferences.getString("storedServerPort", "ServerPort");
        String strPath = sharedPreferences.getString("storedServicePath", "ServicePath");
        System.out.println("StrAddress is : " + strAddress);
        System.out.println("StrPort is : " + strPort);
        System.out.println("StrPath is : " + strPath);

        return new String[]{strAddress, strPort, strPath};
    }

    public static boolean[] loadSavedBooleanPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean boolUseSSL = sharedPreferences.getBoolean("storedUseSSL", false);
        boolean boolUseDefaults = sharedPreferences.getBoolean("storedUseDefaults", true);
        return new boolean[]{boolUseSSL, boolUseDefaults};
    }

    public static String[] loadSavedUsernamesAsArray(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Set<String> usernameSet = sharedPreferences.getStringSet("storedUsernames", new HashSet<String>());

        String[] usernameArray = usernameSet.toArray(new String[usernameSet.size()]);
        return usernameArray;
    }

    public static Set<String> loadSavedUsernamesAsSet(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Set<String> usernameSet = sharedPreferences.getStringSet("storedUsernames", new HashSet<String>());
        return usernameSet;
    }


    public static String createRestUrl(Context context, String path) {
        String[] strArrGlobalParams = AppDelegate.loadSavedStringPreferences(context);
        boolean[] boolArrGlobalParams = AppDelegate.loadSavedBooleanPreferences(context);
        String restUrl = "";
        try {
            if (boolArrGlobalParams[0]) {
                restUrl = "https://" + strArrGlobalParams[0] + ":" + strArrGlobalParams[1] + strArrGlobalParams[2] + path;
            } else {
                restUrl = "http://" + strArrGlobalParams[0] + ":" + strArrGlobalParams[1] + strArrGlobalParams[2] + path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return restUrl;
    }





}
