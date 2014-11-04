package com.evilgeniustechnologies.Wordrific.utilties;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benjamin on 5/23/14.
 */
public class DawnUtilities {
    public static final String TOKEN = "|";
    public static final String PREFS = "Preferences";

    // Check internet connection
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return wifiNetwork != null && wifiNetwork.isConnected() ||
                mobileNetwork != null && mobileNetwork.isConnected() ||
                activeNetwork != null && activeNetwork.isConnected();
    }

    public static String parseClues(List<String> clueList) {
        String clues = "";
        for (String clue : clueList) {
            if (!TextUtils.isEmpty(clue)) {
                clues += clue + TOKEN;
            }
        }
        if (TextUtils.isEmpty(clues)) {
            return clues;
        }
        return clues.substring(0, clues.length() - TOKEN.length());
    }

    public static List<String> getClues(String clues) {
        return Arrays.asList(clues.split("\\" + TOKEN));
    }

    public static Bitmap decodeUri(Context context, Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 232;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
    }

    public static byte[] convertToByte(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
        return blob.toByteArray();
    }

    public static String getFileName(Context context, Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(
                selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    public static String getCleanAnswer(String answer) {
        return answer.toLowerCase()
                .trim()
                .replaceAll("  ", " ")
                .replaceAll(",", "")
                .replaceAll("\"", "")
                .replaceAll("\'", "")
                .replaceAll("-", " ")
                .replaceAll("three", "3")
                .replaceAll("one", "1")
                .replaceAll("fifty", "50")
                .replaceAll("five hundred", "500")
                .replaceAll("seven", "7")
                .replaceAll("thirties", "30s")
                .replaceAll("one hundred", "100")
                .replaceAll("twenty", "20")
                .replaceAll("forty", "40")
                .replaceAll("per cent", "%")
                .replaceAll("percent", "%")
                .replaceAll("ninety nine", "99")
                .replaceAll("1 %", "1%")
                .replaceAll("99 %", "99%");
    }

    public static List<Integer> getConfigurations(String configs) {
        List<Integer> configurations = new ArrayList<Integer>();
        String configVariables = configs.replace("{", "")
                .replace("}", "")
                .replaceAll("\"", "")
                .replaceAll("\n", "")
                .replaceAll(" ", "");
        String[] variables = configVariables.split(",");
        for (String variable : variables) {
            String[] row = variable.split(":");
            if (TextUtils.isDigitsOnly(row[1])) {
                configurations.add(Integer.parseInt(row[1]));
            }
        }
        return configurations;
    }

    public static class Tuple {
        public Object head;
        public Object tail;

        public Tuple(Object head, Object tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    public static class Triple {
        public String start;
        public String middle;
        public String end;

        public Triple(String start, String middle, String end) {
            this.start = start;
            this.middle = middle;
            this.end = end;
        }
    }
}
