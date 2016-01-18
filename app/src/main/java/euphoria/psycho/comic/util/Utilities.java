package euphoria.psycho.comic.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * // * Created by Administrator on 2015/1f/8.
 */


public class Utilities {
//
//    static Utilities util;
//    public native String request(String host, String path);
//
//    static {
//        util=new Utilities();
//        System.loadLibrary("app");
//    }
//
//    public static String getRequest(String url) throws MalformedURLException {
//        final URL u = new URL(url);
//        return util.request(u.getHost(), u.getPath());
//    }

    public static final Pattern dNumber = Pattern.compile("[0-9]+");
    private static final Pattern dPatternCommonImageFile = Pattern.compile("(jpg)|(png)|(jpeg)", Pattern.CASE_INSENSITIVE);
    private static final String GOOD_GTLD_CHAR =
            "a-zA-Z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
    public static final String GOOD_IRI_CHAR =
            "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
    private static final String IRI
            = "[" + GOOD_IRI_CHAR + "]([" + GOOD_IRI_CHAR + "\\-]{0,61}[" + GOOD_IRI_CHAR + "]){0,1}";
    private static final String GTLD = "[" + GOOD_GTLD_CHAR + "]{2,63}";

    private static final String HOST_NAME = "(" + IRI + "\\.)+" + GTLD;
    public static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

    private static Context context_;
    public static final Pattern DOMAIN_NAME
            = Pattern.compile("(" + HOST_NAME + "|" + IP_ADDRESS + ")");

    private static final String dReservedChars = "|\\?*<\":>+[]/'";
    private static String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
    private static final String normalVideoTypePattern = "(3g2)|(3gp)|(3gp2)|(3gpp)|" +
            "(amv)|(asf)|(avi)|(divx)" +
            "|(drc)|(dv)|(f4v)|(flv)|(gvi)|(gxf)|(ismv)|(iso)|" +
            "(m1v)|(m2v)|(m2t)|(m2ts)|(m4v)|(mkv)|(mov)|(mp2)|" +
            "(mp2v)|(mp4)|(mp4v)|(mpe)|(mpeg)|(mpeg1)|(mpeg2)|" +
            "(mpeg4)|(mpg)|(mpv2)|(mts)|(mtv)|(mxf)|(mxg)|(nsv)|" +
            "(nut)|(nuv)|(ogm)|(ogv)|(ogx)|(ps)|(rec)|(rm)|(rmvb)|" +
            "(tod)|(ts)|(tts)|(vob)|(vro)|(webm)|(wm)|(wmv)|(wtv)|" +
            "(xesc)|(3ga)|(a52)|(aac)|(ac3)|(adt)|(adts)|(aif)|(aifc)|" +
            "(aiff)|(amr)|(aob)|(ape)|(awb)|(caf)|(dts)|(flac)|(it)|" +
            "(m4a)|(m4b)|(m4p)|(mid)|(mka)|(mlp)|(mod)|(mpa)|(mp1)|(mp2)|" +
            "(mp3)|(mpc)|(mpga)|(oga)|(ogg)|(oma)|(opus)|(ra)|(ram)|(rmi)|" +
            "(s3m)|(spx)|(tta)|(voc)|(vqf)|(w64)|(wav)|(wma)|(wv)|(xa)|(xm)";
    public static final Pattern WEB_URL = Pattern.compile(
            "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                    + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                    + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                    + "(?:" + DOMAIN_NAME + ")"
                    + "(?:\\:\\d{1,5})?)" // plus option port number
                    + "(\\/(?:(?:[" + GOOD_IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~"  // plus option query params
                    + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?"
                    + "(?:\\b|$)"); // and finally, a word boundary or end of

    // input.  This is to stop foo.sure from
    // matching as foo.su
    public static Context getContext() {
        return context_;
    }

    public static void setContext(Context context) {
        context_ = context;
    }

    public static Context getApplicationContext() throws Exception {

        if (context_ != null)
            return context_.getApplicationContext();
        else
            throw new Exception("Context don't initialized");
    }

    /*网络*/
    public static String checkUri(String uri, String otherUri) {


        if (uri == null || uri.length() <= 1) return null;
        if (!uri.toLowerCase().startsWith("http:") && !uri.toLowerCase().startsWith("https:")) {

            if (uri.startsWith("/")) {
                return addHost(uri, otherUri);
            } else {

                if (otherUri.endsWith("/"))
                    return otherUri + uri;
                else
                    return otherUri.substring(0, otherUri.lastIndexOf("/") + 1) + uri;
            }
        } else
            return uri;

    }

    public static String addHost(String uri, String otherUri) {
        if (uri == null)
            return null;
        final Uri u = Uri.parse(uri);
        if (u.isRelative()) {
            if (otherUri == null)
                return null;
            else {
                String string = "";
                final Uri u1 = Uri.parse(otherUri);
                int pos = otherUri.lastIndexOf(":");
                if (pos != -1) {
                    string += otherUri.substring(0, pos + 3);
                } else {
                    return null;
                }

                if (uri.startsWith("/"))
                    return string + Uri.parse(otherUri).getHost() + uri;
                else {
                    return string + Uri.parse(otherUri).getHost() + "/" + uri;
                }
            }
        } else {
            return uri;
        }
    }

    public static boolean checkConnectivity(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public static boolean checkConnectivity(String host) {
        try {
            final InetAddress inetAddress = InetAddress.getByName(host);
            if (inetAddress.equals("")) {
                return false;
            } else {
                return true;
            }
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static String getUserAgent() {
        return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
        //final String s = System.getProperty("http.agent");
        //return s != null ? s : USER_AGENT;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getUserAgent(Context context) {
        return WebSettings.getDefaultUserAgent(Utilities.getContext());
    }

    public static boolean validateUri(String uri) {

        return WEB_URL.matcher(uri).matches();
    }

    /*数组*/
    public static ArrayList<String> getNewArrayList(String... strings) {

        final ArrayList<String> list = new ArrayList<>();
        for (String s : strings) {
            if (!list.contains(s))
                list.add(s);

        }
        return list;

    }

    public static boolean isListEmpty(List l) {
        if (l == null)
            return true;
        if (l.size() < 1)
            return true;
        return false;
    }
    /*时间*/

    private static final Formatter formatter_;
    private static final StringBuilder formatterStringBuilder_;

    static {
        formatterStringBuilder_ = new StringBuilder();
        formatter_ = new Formatter(formatterStringBuilder_, Locale.getDefault());

    }

    public static String millisToHumanReadable(long millis) {
        int totalSeconds = (int) millis / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        formatterStringBuilder_.setLength(0);
        if (hours > 0) {
            return formatter_.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter_.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /*文件*/
    public static boolean checkDirectory(String directory, boolean isCreate) {
        final File file = new File(directory);
        final boolean b = !file.exists() || !file.isDirectory();
        if (isCreate) {
            file.mkdirs();
        }
        return b;
    }

    public static boolean checkExtensionCandidate(String uri) {
        final Pattern p = Pattern.compile("[\\.a-z]", Pattern.CASE_INSENSITIVE);
        try {
            return p.matcher(parseUriForFileName(uri)).find();
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean checkIfHasImageFile(String directory) {
        if (isEmpty(directory)) return false;
        final File file = new File(directory);
        if (file.exists()) {
            final File[] files = file.listFiles();
            for (File f : files) {
                if (dPatternCommonImageFile.matcher(getExtension(f.getAbsolutePath())).find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
// ignore
        }
    }

    public static String combinePath(String directory, String filename) throws IOException {
        if (isEmpty(directory) || isEmpty(filename))
            throw new IOException("both of them cannot be null or empty.");
        if (!directory.endsWith("/"))
            directory += "/";
        if (filename.startsWith("/"))
            filename = filename.substring(1);
        return directory + filename;
    }

    public static String combinePath(String... strings) {
        if (strings.length >= 2) {
            try {
                String s = combinePath(strings[0], strings[1]);
                if (strings.length > 2)
                    for (int i = 2; i < strings.length; i++) {
                        if (strings[i].startsWith("/")) {
                            s += strings[i];
                        } else {
                            s += "/" + strings[i];
                        }
                    }
                return s;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public static void deleteFiles(String directory) {
        final File file = new File(directory);
        if (file.isDirectory()) {
            final List<File> files = getImageFilesNormal(directory);
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static void deleteRecursive(String path) {
        File dir = new File(path);
        File[] currList;
        Stack<File> stack = new Stack<File>();
        stack.push(dir);
        while (!stack.isEmpty()) {
            if (stack.lastElement().isDirectory()) {
                currList = stack.lastElement().listFiles();
                if (currList.length > 0) {
                    for (File curr : currList) {
                        stack.push(curr);
                    }
                } else {
                    stack.pop().delete();
                }
            } else {
                stack.pop().delete();
            }
        }
    }

    public static ArrayList<String> getDirectories(String directory) {
        if (directory == null) return null;
        final File file = new File(directory);
        if (!file.exists()) {
            return null;
        } else {
            final ArrayList<String> list = new ArrayList<>();
            final File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    list.add(f.getName());
                }
            }
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String s, String s2) {
                    final Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.getInstance(Locale.CHINA).compare(s, s2);
                }
            });
            return list;
        }
    }

    public static void getDirectoryNames(String directory, OnFileOperatorListener l) {
        if (directory == null) return;
        final File file = new File(directory);
        if (!file.exists()) {
            return;
        } else {
            final File[] files = file.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File s, File s2) {
                    final Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.getInstance(Locale.CHINA).compare(s.getName(), s2.getName());
                }
            });
            for (File f : files) {
                if (f.isDirectory()) {
                    if (l != null) {
                        l.onFile(f.getName());
                    }
                }
            }

        }
    }

    public static String getExtension(String string) {
        final int pos = string.lastIndexOf(".");
        if (pos > -1) {
            return string.substring(pos);
        }
        return null;
    }

    public static File getExternalStorageFileInDirectory(String filename) {
        return new File(Environment.getExternalStorageDirectory(), filename);
    }

    public static String getFileNameFromNow(String prefix, String suffix) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        return prefix + "-" + simpleDateFormat.format(System.currentTimeMillis()) + "-" + suffix;
    }

    public static List<String> getFileNamesFromURI(Uri uri) {
        if (uri == null) return null;
        final List<String> list = new ArrayList<>();
        final File file = new File(new File(uri.getPath()).getParent());
        if (file != null && file.exists()) {
            final Pattern pattern = Pattern.compile(normalVideoTypePattern, Pattern.CASE_INSENSITIVE);
            final File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (pattern.matcher(getExtension(f.getAbsolutePath())).find()) {
                        list.add(f.getAbsolutePath());
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String s, String s2) {
                return Collator.getInstance(Locale.CHINA).compare(s, s2);
            }
        });
        return list;
    }

    public static List<File> getFilesFromURI(Uri uri) {
        if (uri == null) return null;
        final List<File> list = new ArrayList<>();
        final File file = new File(new File(uri.getPath()).getParent());
        if (file != null && file.exists()) {
            final Pattern pattern = Pattern.compile(normalVideoTypePattern, Pattern.CASE_INSENSITIVE);
            final File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (pattern.matcher(getExtension(f.getAbsolutePath())).find()) {
                        list.add(f);
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File file, File file2) {
                return Collator.getInstance(Locale.CHINA).compare(file.getName(), file2.getName());
            }
        });
        return list;
    }

    public static ArrayList<String> getImageFiles(String directory) {
        if (directory == null) return null;
        final File file = new File(directory);
        if (!file.exists()) {
            return null;
        } else {
            final ArrayList<String> list = new ArrayList<>();
            final File[] files = file.listFiles();
            for (File f : files) {
                if (dPatternCommonImageFile.matcher(getExtension(f.getAbsolutePath())).find()) {
                    list.add(f.getAbsolutePath());
                }
            }
            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String s, String s2) {
                    final Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.getInstance(Locale.CHINA).compare(s, s2);
                }
            });
            return list;
        }
    }

    public static ArrayList<File> getImageFilesNormal(String directory) {
        if (directory == null) return null;
        final File file = new File(directory);
        if (!file.exists()) {
            return null;
        } else {
            final ArrayList<File> list = new ArrayList<>();
            final File[] files = file.listFiles();
            for (File f : files) {
                if (dPatternCommonImageFile.matcher(getExtension(f.getAbsolutePath())).find()) {
                    list.add(f);
                }
            }
            return list;
        }
    }

    public static String getLegalFileName(String filename, char substitutionCharacter) {
        if (filename == null)
            return null;
        final char[] cs = dReservedChars.toCharArray();
        for (char c : cs) {
            filename = filename.replace(c, substitutionCharacter);
        }
        return filename;
    }

    public static List<Uri> getURIsFromURI(Uri uri) {
        if (uri == null) return null;
        final List<Uri> list = new ArrayList<>();
        final File file = new File(new File(uri.getPath()).getParent());
        if (file != null && file.exists()) {
            final Pattern pattern = Pattern.compile(normalVideoTypePattern, Pattern.CASE_INSENSITIVE);
            final File[] files = file.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (pattern.matcher(getExtension(f.getAbsolutePath())).find()) {
                        list.add(Uri.fromFile(f));
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<Uri>() {
            @Override
            public int compare(Uri uri, Uri uri2) {
                return uri.toString().compareToIgnoreCase(uri2.toString());
            }
        });
        return list;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() < 1;
    }

    public static String parseUriForFileName(String uri) throws IOException {
        if (uri == null)
            throw new IOException("uri cannot be null.");
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

    public static void recursivelyDeleteEmptyDirectories(String directory) {
        if (directory == null) return;
        final File dir = new File(directory);
        if (!dir.exists()) return;
        final LinkedList<File> container = new LinkedList<>();
        container.add(dir);
        while (true) {
            File d = container.remove();
            File[] fs = d.listFiles();
            if (fs.length < 1) {
                d.delete();
            } else {
                for (File f : fs) {
                    if (f.isDirectory()) {
                        container.add(f);
                    }
                }
            }
            if (container.size() < 1)
                break;
        }
    }

    public interface OnFileOperatorListener {
        void onFile(String fileName);
    }

    /*显示*/
    public static int getScreenPxWidth() {

        return context_.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenPxHeight() {

        return context_.getResources().getDisplayMetrics().heightPixels;
    }

    /*亮度*/
    public static void setBrightness(Context context, int brightness) {
        ContentResolver cResolver = context.getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public static int getBrightness(Context context) {
        ContentResolver cResolver = context.getContentResolver();
        try {
            return Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    /*声音*/
    public static int getVolume(Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public static int getMaxVolume(Context context) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static void setVolume(Context context, int volume) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    /*颜色*/
    public static String convertColorToHex(int colorInteger) {

        return String.format("#%06X", (0xFFFFFF & colorInteger));
    }

    /*Log*/
   /* public static void pushLogToError(String tag, String name, Object o) {
        if (tag == null) {
            tag = getContext().getPackageName();
        }
        Log.e(tag, String.format("%s=%s\n", name, o));
    }

    public static void pushLogToError(String name, Object o) {
        pushLogToError(null, name, o);
    }

    public static void pushLogToError(Object... objects) {
        if (objects == null) return;
        final String tag = getContext().getPackageName();
        String msg = null;
        for (Object o : objects) {
            msg += String.format("%s=%s\n", o.toString(), o);
        }
        Log.e(tag, msg);
    }

    public static void pushLogToError(Object o) {

        Log.e(buildClassTag(o), String.format("%s\n", o));
    }

    public static String buildClassTag(Object o) {
        if (o == null)
            return null;
        String cls = o.getClass().getSimpleName();
        if (cls != null && !isEmpty(cls)) {
            final int end = cls.lastIndexOf('.');
            if (end > 0) {
                cls = cls.substring(end + 1);
            }

        }
        return cls + "_" + Integer.toHexString(System.identityHashCode(cls));
    }*/
    public static String getHTMLSource(String url) throws IOException {
        URL yahoo = new URL(url);
        URLConnection yc = yahoo.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();

        return a.toString();


    }

    /*Math*/
    public static int round(float value) {
        long lx = (long) (value * (65536 * 256f));
        return (int) ((lx + 0x800000) >> 24);
    }

    /*项目*/
    public static String getPictureDirectory(String title) {
        if (title == null)
            return null;

        return getExternalStorageFileInDirectory(Constants.PICTURE_DIRECTORY + getLegalFileName(title, '_')).getAbsolutePath();

    }


}
