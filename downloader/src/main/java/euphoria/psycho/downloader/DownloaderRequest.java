package euphoria.psycho.downloader;

import android.net.Uri;


import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * @author Administrator
 */
public class DownloaderRequest implements Comparable<DownloaderRequest> {
    protected static final String mALLOWEDURI_CHARS = "@#&=*+-_.,:!?()/~'%";
    private static final String TAG = "DownloaderRequest";
    private static final int mRETRYCOUNT = 3;
    public boolean mIsCanceled_ = false;
    public long mCurrentBytes;
    public final Downloader mDownloader;
    public DownloadListener mDownloadListener;
    public final File mFile_;
    public final boolean mIsFileExistNotDownload;
    public boolean mIsFinished = false;
    private long mLastSpeedBytes;
    private long mLastSpeedTime;
    private int mPercent;
    private long mSpeed;
    private long mSpeedBytes;
    private long mSpeedStartTime;
    public final int mTimeout_;
    private long mTotalBytes;
    public String mUrl_;
    private static final Object mLock = new Object();
    private static final String mUSERAGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";


    public DownloaderRequest(Downloader downloader, String mUrl_, DownloadListener downloadListener, int mTimeout_, File mFile_, boolean isFileExistNotDownload) {
        this.mUrl_ = mUrl_;
        this.mDownloadListener = downloadListener;
        this.mTimeout_ = mTimeout_;
        this.mFile_ = mFile_;
        mIsFileExistNotDownload = isFileExistNotDownload;
        mDownloader = downloader;
        final boolean isFileExists = mFile_.exists();
        if (isFileExists && mIsFileExistNotDownload) {
            mIsCanceled_ = true;
            final long size = mFile_.length();
            mCurrentBytes = size;
            mTotalBytes = size;
            mPercent = 100;
        }
    }

    public DownloaderRequest(Downloader downloader, String url, DownloadListener downloadListener, File file) {
        this(downloader, url, downloadListener, 5000, file, false);
    }

    public DownloaderRequest(Downloader downloader, String url, File file) {
        this(downloader, url, null, 5000, file, true);
    }

    public DownloaderRequest(Downloader downloader, String url, DownloadListener downloadListener, File file, boolean isFileExistNotDownload) {
        this(downloader, url, downloadListener, 5000, file, isFileExistNotDownload);
    }

    private void addHeader(HttpURLConnection httpURLConnection) {
//if (mHeaders != null) {
//  for (Pair<String, String> pair : mHeaders) {
// httpURLConnection.addRequestProperty(pair.first, pair.second);
// }
//  }
        if (httpURLConnection.getRequestProperty("User-Agent") == null) {
            httpURLConnection.addRequestProperty("User-Agent", mUSERAGENT);
        }
        httpURLConnection.setRequestProperty("Accept-Encoding", "identity");
    }

    private void checkConnectivity() throws StopRequestException {
        try {
            final InetAddress inetAddress = InetAddress.getByName("baidu.com");
            if (inetAddress.equals("")) {
                throw new StopRequestException("NO NETWORK");
            }
        } catch (UnknownHostException e) {
            throw new StopRequestException("NO NETWORK");
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException r) {
                throw r;
            } catch (Exception e) {
            }
        }
    }

    @Override
    public int compareTo(DownloaderRequest downloaderRequest) {
        return 0;
    }


    public void doParse() {
        int retryCount = 0;
        if (!mIsFileExistNotDownload && mFile_.exists()) {
            mFile_.delete();
        }
       /* Log.e(TAG, String.format("mIsCanceled_=%s\n", mIsCanceled_));*/
        URL url = null;
        while (retryCount++ < mRETRYCOUNT) {
            try {
                checkConnectivity();
                try {
                    url = new URL(Uri.encode(mUrl_, mALLOWEDURI_CHARS));
                } catch (MalformedURLException e) {
                    updateException("url=new URL(mUrl_)");
                }
                HttpURLConnection httpURLConnection = null;
                try {
                    if (url == null) {
                        notifyFinished();
                        return;
                    }
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setInstanceFollowRedirects(false);
                    httpURLConnection.setConnectTimeout(mTimeout_);
                    httpURLConnection.setReadTimeout(mTimeout_);
                    addHeader(httpURLConnection);
                    final int responseCode = httpURLConnection.getResponseCode();
                    switch (responseCode) {
                        case HTTP_OK: {
                            parseConnectionHeader(httpURLConnection);
                            transferData(httpURLConnection);
                            return;
                        }
                        case HTTP_SEE_OTHER:
                        case HTTP_MOVED_PERM:
                        case HTTP_MOVED_TEMP: {
                            final String location = httpURLConnection.getHeaderField("Location");
                            url = new URL(url, location);
                            if (responseCode == HTTP_MOVED_PERM) {
                                mUrl_ = url.toString();
                            }
                            continue;
                        }
                        default: {
                            throw new StopRequestException(String.format("Un handle NetworkState=%s", responseCode));
                        }
                    }
                } catch (IOException e) {
                    if (e instanceof ProtocolException) {
                        updateException("ProtocolException");
                    } else {
                        updateException("httpURLConnection=url.openConnection()");
                    }
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            } catch (StopRequestException e) {
                updateException(e.getMessage());
            }
        }
    }

    private static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void notifyFinished() {
        mIsFinished = true;
        if (mDownloadListener != null)
            mDownloadListener.onStatusChanged();
    }

    private void parseConnectionHeader(HttpURLConnection httpURLConnection) throws StopRequestException {
        final String transferEncoding = httpURLConnection.getHeaderField("Transfer-Encoding");
        if (transferEncoding == null) {
            mTotalBytes = getHeaderFieldLong(httpURLConnection, "Content-Length", -1);
        } else {
            mTotalBytes = -1;
        }
        checkConnectivity();
    }

    private void transferData(HttpURLConnection httpURLConnection) throws StopRequestException {
        final boolean hasLength = mTotalBytes != -1;
        final String transferEncoding = httpURLConnection.getHeaderField("Transfer-Encoding");
        final boolean isChunked = "chunked".equalsIgnoreCase(transferEncoding);
        if (!hasLength && !isChunked) {
            throw new StopRequestException("can't know size of download, giving up");
        }
//ParcelFileDescriptor parcelFileDescriptor = null;
        FileDescriptor fileDescriptor = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            try {
                inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                throw new StopRequestException("inputStream = httpURLConnection.getInputStream()");
            }
            try {

                outputStream = new FileOutputStream(mFile_);
                fileDescriptor = ((FileOutputStream) outputStream).getFD();
                transferData(inputStream, outputStream, fileDescriptor);
            } catch (IOException e) {
                throw new StopRequestException("parcelFileDescriptor=ParcelFileDescriptor.open(mFile_,ParcelFileDescriptor.MODE_READ_WRITE)");
            }
        } finally {
            closeQuietly(inputStream);
            if (fileDescriptor != null) {
                try {
                    fileDescriptor.sync();
                } catch (SyncFailedException e) {
                    throw new StopRequestException(" fileDescriptor.sync();");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    throw new StopRequestException(" outputStream.flush()");
                } finally {
                    closeQuietly(outputStream);
                }
            }
        }
    }

    private void transferData(InputStream inputStream, OutputStream outputStream, FileDescriptor fileDescriptor) throws StopRequestException {
        final byte[] buffer = new byte[8192];
        while (true) {
            int length = -1;
            try {
                length = inputStream.read(buffer);
            } catch (IOException e) {
                throw new StopRequestException("transferData:inputStream.read(buffer)");
            }
            if (length == -1) {
                mIsFinished = true;
                mPercent = 100;
                if (mDownloadListener != null)
                    mDownloadListener.onStatusChanged();
                break;
            }
            try {
                outputStream.write(buffer, 0, length);
                mCurrentBytes += length;
                updateProgress(fileDescriptor);
            } catch (IOException e) {
                throw new StopRequestException("transferData:outputStream.write(buffer,0,length)");
            }
        }
    }

    /*更新错误*/
    private void updateException(String message) {
        mIsFinished = true;
        mDownloader.addFailureDownloaderRequest(this);
        if (mDownloadListener != null) {
            mDownloadListener.onStatusChanged();
            mDownloadListener.onErrorOccurred(new StopRequestException(message));
        }
    }

    /*更新进度*/
    private void updateProgress(FileDescriptor fileDescriptor) throws StopRequestException {
//final long now = SystemClock.elapsedRealtime();
        final long now = System.currentTimeMillis();
        final long timeDelta = now - mSpeedStartTime;
        if (timeDelta > 500) {
            final long speed = ((mCurrentBytes - mSpeedBytes) * 1000) / timeDelta;
            if (mSpeed == 0) {
                mSpeed = speed;
            } else {
                mSpeed = (mSpeed * 3 + speed) / 4;
            }
            if (mSpeedStartTime != 0) {
                if (mDownloadListener != null)
                    mDownloadListener.onStatusChanged();
            }
            mSpeedStartTime = now;
            mSpeedBytes = mCurrentBytes;
        }
        final long bytesDelta = mCurrentBytes - mLastSpeedBytes;
        final long timeDeltaForLast = now - mLastSpeedTime;
/*65536*/
        if (bytesDelta > 10240 && timeDeltaForLast > 1000) {
            try {
                /*Log.e(TAG, String.format("mCurrentBytes=%s; mTotalBytes=%s\n", mCurrentBytes, mTotalBytes));*/
                fileDescriptor.sync();
                mLastSpeedTime = now;
                mLastSpeedBytes = mCurrentBytes;
                mPercent = (int) (mCurrentBytes * 100 / mTotalBytes);
                if (mDownloadListener != null)
                    mDownloadListener.onStatusChanged();
              /*  Log.e(TAG, String.format("percent=%s\n", String.format("%s %%", mPercent)));*/
            } catch (SyncFailedException e) {
                throw new StopRequestException("fileDescriptor.sync()");
            }
        }
        if (mTotalBytes == 0)
            mTotalBytes = mCurrentBytes;
    }

    public void setDownloadListener(DownloadListener d) {
        mDownloadListener = d;
    }

    public long getSpeed() {

        return mSpeed;

    }


    public String getUrl_() {
        return mUrl_;
    }

    public long getCurrentByte() {

        return mCurrentBytes;

    }


    public int getPercent() {

        return mPercent;

    }

    public long getTotalBytes() {

        return mTotalBytes;

    }

}