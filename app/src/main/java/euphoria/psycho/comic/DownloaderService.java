package euphoria.psycho.comic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import euphoria.psycho.comic.util.Constants;
import euphoria.psycho.comic.util.Utilities;
import euphoria.psycho.downloader.Downloader;
import euphoria.psycho.downloader.DownloaderRequest;

/**
 * Created by Administrator on 2015/1/16.
 */
public class DownloaderService extends Service {

    private final Downloader mDownloader = Downloader.getInstance();
    /* private final IBinder mBinder = new LocalBinder();*/
    private boolean isStarted;

    private void add(String uri, String directory) {
        DownloaderRequest downloaderRequest = new DownloaderRequest(mDownloader, uri, getFile(directory, uri));
        mDownloader.add(downloaderRequest);
        if (!isStarted) {
            mDownloader.start();

            isStarted = true;
        }
    }

    private File getFile(String directory, String uri) {


        try {
            return new File(Utilities.combinePath(directory, Utilities.parseUriForFileName(uri)));
        } catch (IOException e) {
            return null;
        }

    }

    private ArrayList<DownloaderRequest> getList() {

        return new ArrayList<DownloaderRequest>(mDownloader.getCurrentDownloadRequests());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            final ArrayList<String> list = intent.getStringArrayListExtra(Constants.DOWNLOADER_LIST);
            final String d = intent.getStringExtra(Constants.DOWNLOADER_DIRECTORY);
            if (!Utilities.isListEmpty(list)) {
                for (String s : list) {
                    add(s, d);
                }
            }
        }
        startDownloadActivity();
        return START_STICKY;
    }

    private void startDownloadActivity() {

        final Intent intent = new Intent(this, DownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        /*return mBinder;*/
        return null;
    }

    @Override
    public void onCreate() {
        isStarted = false;
    }

   /* @Override
    public void onStatusChanged() {

        sendBroadcast(new Intent(Constants.UPDATE_DOWNLOAD_LIST));
    }

    @Override
    public void onErrorOccurred(StopRequestException e) {

    }*/

   /* public class LocalBinder extends Binder {
        public DownloaderService getService() {
            return DownloaderService.this;
        }
    }*/
}
