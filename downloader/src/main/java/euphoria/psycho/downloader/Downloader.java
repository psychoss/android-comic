package euphoria.psycho.downloader; /**
 * Created by Administrator on 2015/1/10.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

public class Downloader {

    private final PriorityBlockingQueue<DownloaderRequest> downloaderRequests_ = new PriorityBlockingQueue<DownloaderRequest>();
    private final Set<DownloaderRequest> downloader_ = new HashSet<DownloaderRequest>();
    private final DownloaderThread[] downloaderThreads_ = new DownloaderThread[5];
    private final Set<DownloaderRequest> failureDownloaderRequests_ = new HashSet<DownloaderRequest>();
    private static Downloader d_;

    private Downloader() {
    }

    public synchronized void removeRequest(DownloaderRequest d) {
        downloader_.remove(d);
    }

    public void addFailureDownloaderRequest(DownloaderRequest d) {
        synchronized (failureDownloaderRequests_) {

            if (!failureDownloaderRequests_.contains(d)) {
                failureDownloaderRequests_.add(d);
            }
        }
    }

    public int getCount() {
        return downloader_.size();
    }

    public static Downloader getInstance()

    {
        if (d_ == null) {
            d_ = new Downloader();
        }
        return d_;
    }

    public ArrayList<DownloaderRequest> getCurrentDownloadRequests() {
        if (downloader_.size() > 0) {
            final ArrayList<DownloaderRequest> downloaderRequests = new ArrayList<>();

            for (DownloaderRequest d : downloader_) {
                if (!d.mIsFinished)
                    downloaderRequests.add(d);
            }
            return downloaderRequests;
        }

        return null;
    }

    public void start() {
        stop();
        for (int i = 0; i < 5; i++) {
            downloaderThreads_[i] = new DownloaderThread();
            downloaderThreads_[i].start();
        }
    }

    public void stop() {
        for (DownloaderThread d : downloaderThreads_) {
            if (d != null)
                d.quit();
        }
    }

    public void add(DownloaderRequest d) {
        synchronized (downloader_) {
            if (!downloader_.contains(d)) {
                downloader_.add(d);
                downloaderRequests_.add(d);
            }
        }
    }


    private class DownloaderThread extends Thread {
        private boolean quit__ = false;

        public void quit() {
            quit__ = true;
            interrupt();
        }

        @Override
        public void run() {
            while (true) {
                DownloaderRequest downloaderRequest;
                try {
                    downloaderRequest = downloaderRequests_.take();

                } catch (InterruptedException i) {
                    if (quit__) {
                        return;
                    }
                    continue;
                }
                if (downloaderRequest.mIsCanceled_) {
                    continue;
                }
                downloaderRequest.doParse();
            }
        }
    }

  /*  public interface OnResponse {

        public void updateError(String msg);

        public void updateProgress(String url, int percent);

        public void notifyRequestFinished(DownloaderRequest downloaderRequest);

        public void updateSpeed(String url,long speed,long totalBytes);
    }*/
}