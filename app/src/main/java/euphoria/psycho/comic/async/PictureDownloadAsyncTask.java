package euphoria.psycho.comic.async;

import android.os.AsyncTask;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.psycho.comic.listener.OnListener;
import euphoria.psycho.comic.util.Utilities;

/**
 * Created by Administrator on 2015/1/11.
 */
public class PictureDownloadAsyncTask extends AsyncTask<Pair<String, String>, Void, Pair<String, ArrayList<String>>> {
    private static final String SPECIFY_URI_CSS_SELECTOR = "img";
    private final OnListener<Pair<String, ArrayList<String>>> onListener_;

    public PictureDownloadAsyncTask(OnListener<Pair<String, ArrayList<String>>> l) {
        onListener_ = l;
    }

    @Override
    protected void onPostExecute(Pair<String, ArrayList<String>> pair) {
        if (onListener_ != null)
            onListener_.onFetchDataFinished(pair);

         /*   final Intent intent = new Intent(context_, DownloaderActivity.class);
            intent.putStringArrayListExtra(Constants.DOWNLOADER_LIST, arrayList_);
            final String dir = Utilities.getExternalStorageFileInDirectory(Constants.PICTURE_DIRECTORY + Utilities.getLegalFileName(title_, '_')).getAbsolutePath();

            Utilities.checkDirectory(dir, true);
            intent.putExtra(Constants.DOWNLOADER_DIRECTORY, dir);
            context_.startActivity(intent);*/

    }

    @Override
    protected Pair<String, ArrayList<String>> doInBackground(Pair<String, String>... strings) {

        if (strings.length < 1)
            return null;
        try {
            return doParse(strings[0]);
        } catch (IOException e) {

        }

        return null;
    }


    private Pair<String, ArrayList<String>> doParse(Pair<String, String> pair) throws IOException {

        final ArrayList<String> list = new ArrayList<>();


        final String second = pair.second;

//        if (!Utilities.checkConnectivity("baidu.com")) {
//            return null;
//        }
       /* Utilities.pushLogToError(Utilities.getUserAgent());*/


        String content = null;

        try {
            try {
               content = Utilities.getHTMLSource(pair.second);

                //Jsoup.connect(uri).get();
            } catch (Exception e) {
            }
        } catch (Exception e) {

        }
        if (content == null) {

           return null;
        }
        final Matcher matcher = Pattern.compile("src=[\"\']{1}([^\"\']*?\\.(jpg|png|jpeg))", Pattern.CASE_INSENSITIVE).matcher(content);

        while (matcher.find()) {
            final String m = matcher.group(1);
            if (!Utilities.isEmpty(m) && !m.endsWith("favicon.png") && !m.endsWith("group.png")) {
                final String s = Utilities.addHost(m, second);
                list.add(s);
            }
        }

        if (list.size() > 0)
            return Pair.create(pair.first, list);
        else
            return null;

    }


}
