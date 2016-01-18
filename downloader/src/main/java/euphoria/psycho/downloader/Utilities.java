package euphoria.psycho.downloader;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;

/**
 * Created by Administrator on 2015/1/10.
 */
public class Utilities {

    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }


    public static int getContentLength(HttpURLConnection httpURLConnection) {
        final String s = httpURLConnection.getHeaderField("Content-Length");

        try {
            return s != null ? Integer.parseInt(s) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    public static File getFileFromURL(File destinationDirectory, String url) {
        File file = new File(destinationDirectory, getFileNameFromURL(url));
        // int i = 0;
        //while (file.exists()) {
        //  i++;

        //file = new File(getFileNameBySuffix(file.getAbsolutePath(), i));
        // }
        return file;
    }

    public static String parseUriForFileName(String uri) {

        if (uri == null)
            return null;
        String r = null;

        int slashPosition = uri.lastIndexOf("/");
        System.out.print(String.format("slashPosition=%s\n", slashPosition));
        if (slashPosition != -1) {

            r = uri.substring(++slashPosition);
            System.out.print(String.format("r=%s\n", r));
        } else {
            r = uri;
        }
        String[] rs = r.split("\\.[a-zA-Z0-9]+");
        System.out.print(String.format("rs.length=%s\n", rs.length));
        if (rs.length > 1) {
            r = r.replace(rs[rs.length - 1], "");
        }
        return r;
    }

    public static String getFileNameBySuffix(String str, int i) {
        if (str == null) {
            return null;
        }

        int pos = str.lastIndexOf(".");

        if (pos == -1) {
            return str;
        }

        return str.substring(0, pos) + String.valueOf(i) + str.substring(pos);
    }


    public static String getFileNameFromURL(String url) {
        String result = "";
        int pos = url.lastIndexOf("/");
        if (pos > -1) {
            result = url.substring(++pos);
        }
        return result;
    }


    public static String getUserAgent() {

        final String s = System.getProperty("http.agent");
        return s != null ? s : Constants.USER_AGENT;
    }


    public static void setRequestProperty(HttpURLConnection httpURLConnection) {

        httpURLConnection.setRequestProperty("User-Agent", getUserAgent());
        httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
        httpURLConnection.setRequestProperty("Connection", "keep-alive");
        httpURLConnection.setRequestProperty("Accept", "*/*");
    }

    /*Log*/
    public static void pushLogToError(Context context, String tag, String name, Object o) {
        if (tag == null) {
            tag = context.getPackageName();
        }
        Log.e(tag, String.format("%s=%s\n", name, o));
    }


    public static void pushLogToError(Object... objects) {
        if (objects == null) return;
        final String tag = "debug";
        String msg = null;
        for (Object o : objects) {
            msg += String.format("%s=%s\n", o.toString(), o);
        }
        Log.e(tag, msg);
    }
}
