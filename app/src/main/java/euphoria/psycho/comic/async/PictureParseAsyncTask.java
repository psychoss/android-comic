package euphoria.psycho.comic.async;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import euphoria.psycho.comic.db.DatabaseHelper;
import euphoria.psycho.comic.listener.OnListener;
import euphoria.psycho.comic.util.Constants;
import euphoria.psycho.comic.util.Utilities;

/**
 * Created by Administrator on 2015/1/11.
 */
public class PictureParseAsyncTask extends AsyncTask<String, Void, List<Pair<String, String>>> {


    private final OnListener<List<Pair<String, String>>> onListener_;
    private final boolean isRefresh_;

    public PictureParseAsyncTask(OnListener<List<Pair<String, String>>> l, boolean isRefresh) {
        onListener_ = l;
        isRefresh_ = isRefresh;

    }

    @Override
    protected void onPostExecute(List<Pair<String, String>> l) {


        if (onListener_ != null)
            onListener_.onFetchDataFinished(l);
    }

    @Override
    protected List<Pair<String, String>> doInBackground(String... strings) {

        if (strings.length < 1) {

            return new ArrayList<>();


        }
        try {
            return doParse(strings[0]);
        } catch (IOException e) {

            return new ArrayList<>();
        }
    }

    private void reportError(String message) {
        if (onListener_ != null)
            onListener_.onErrorOccurred(message);
    }

    private boolean checkWhetherSpecifyPage(String uri, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(uri).find();
    }

    private List<Pair<String, String>> doParse(String uri) throws IOException {

        List<Pair<String, String>> list = null;
        final DatabaseHelper databaseHelper = new DatabaseHelper(Utilities.getContext(), Utilities.getExternalStorageFileInDirectory(Constants.PICTURE_DIRECTORY_DATABASE).getAbsolutePath(), uri);


        if (!isRefresh_) {

            list = databaseHelper.getRecords(uri);
            if (list.size() > 0)
                return list;
        }

        if (!Utilities.checkConnectivity("baidu.com")) {

            return null;
        }


        if (list == null) {
            list = new ArrayList<>();
        }
        // Check the uri whether is for the one piece.
        if (checkWhetherSpecifyPage(uri, "ishuhui")) {

            list = doParseSpecify(uri, "div.thumbnail a");


        } else if (Pattern.compile("bearhk", Pattern.CASE_INSENSITIVE).matcher(uri).find()) {
            list = doParseSpecify(uri, "td h3 a");
        } else {
            list = doParseSpecify(uri, "div a");
        }

        if (list == null || list.size() > 0) {


            databaseHelper.insert(list);

        }
        return list;
    }

    private List<Pair<String, String>> doParseSpecify(String uri, String selector) throws IOException {

        Document document = new Document("");

        Elements elements = null;
        try {
            final String source = Utilities.getHTMLSource(uri);
            elements = document.html(source).select(selector);
            //Jsoup.connect(uri).get();
        } catch (Exception e) {
            if (e != null)
                Log.e("Net error:", e.toString());
        }

        if (elements == null) {
            return null;
        } else {
            if (elements.size() < 1) {
                return null;
            }
            final List<Pair<String, String>> pairs = new ArrayList<>();
            for (Element element : elements) {
               if (element.className().contains("tv-link")){
                   continue;
               }
                String title = Utilities.getLegalFileName(element.attr("title"), '_');
                if (Utilities.isEmpty(title)) {
                    title = Utilities.getLegalFileName(element.text(), '_');
                }
                final String href = Utilities.checkUri(element.attr("href"), uri);
                // length more than 1
                // excluding '/' or '#'
                if (href!=null) {
                    final Pair<String, String> pair = Pair.create(title, href);
                    if (!pairs.contains(pair))
                        pairs.add(pair);
                }
            }
            Collections.sort(pairs, new Comparator<Pair<String, String>>() {
                @Override
                public int compare(Pair<String, String> pair, Pair<String, String> pair2) {
                    final Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.compare(pair.first, pair2.first);
                }
            });
            return pairs;
        }

    }

//    private List<Pair<String, String>> doParseNormal(String uri) throws IOException {
//        final Document document = Jsoup.connect(uri).timeout(TIMEOUT).userAgent(USER_AGENT).get();
//        final Elements elements = document.select("div a");
//
//        if (elements == null) {
//            return null;
//        } else {
//            if (elements.size() < 1) {
//                return null;
//            }
//            final List<Pair<String, String>> pairs = new ArrayList<>();
//            for (Element element : elements) {
//                String title = Utilities.getLegalFileName(element.attr("title"), '_');
//
//                if (Utilities.isEmpty(title)) {
//                    title = Utilities.getLegalFileName(element.html().trim(), '_');
//                }
//                final String href = Utilities.checkUri(element.attr("href"), uri);
//                if (!Utilities.isEmpty(title) && !Utilities.isEmpty(href)) {
//                    final Pair<String, String> pair = Pair.create(title, href);
//
//                    pairs.add(pair);
//                }
//            }
//            return pairs;
//        }
//
//    }
}
