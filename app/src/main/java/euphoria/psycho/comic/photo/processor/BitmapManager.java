package euphoria.psycho.comic.photo.processor;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.util.WeakHashMap;

/**
 * Created by Administrator on 2014/12/7.
 */
public class BitmapManager {
    /*枚举*/
    private static enum State {
        CANCEL,
        ALLOW
    }

    /*线程状态静态类*/
    private static class ThreadStatus {
        public State mState = State.ALLOW;
        public BitmapFactory.Options mOptions;
        public boolean mThumbRequesting;

        @Override
        public String toString() {
            String s;
            if (mState == State.ALLOW) {
                s = "Cancel";
            } else if (mState == State.CANCEL) {
                s = "Allow";
            } else {
                s = "?";
            }
            s = "Thread state = " + s + ", Options = " + mOptions;
            return s;
        }
    }

    private final WeakHashMap<Thread, ThreadStatus> mThreadStatus = new WeakHashMap<Thread, ThreadStatus>();
    private static BitmapManager mBitmapManager = null;

    /*隐藏的构造函数*/
    private BitmapManager() {

    }

    private synchronized ThreadStatus getOrCreateThreadStatus(Thread thread) {
        ThreadStatus threadStatus = mThreadStatus.get(thread);
        if (threadStatus == null) {
            threadStatus = new ThreadStatus();
            mThreadStatus.put(thread, threadStatus);
        }

        return threadStatus;
    }

    private synchronized void setDecodingOptions(Thread thread, BitmapFactory.Options options) {

        getOrCreateThreadStatus(thread).mOptions = options;
    }

    private synchronized void removeDecodingOptions(Thread thread) {
        ThreadStatus threadStatus = mThreadStatus.get(thread);
        threadStatus.mOptions = null;
    }

    public synchronized boolean canThreadDecoding(Thread thread) {
        ThreadStatus threadStatus = mThreadStatus.get(thread);
        if (threadStatus == null) {
            return true;
        }

        return threadStatus.mState != State.CANCEL;
    }

    public synchronized void allowThreadDecoding(Thread thread) {
        getOrCreateThreadStatus(thread).mState = State.ALLOW;
    }

    /*获取实例的静态方法*/
    public static synchronized BitmapManager getInstance() {
        if (mBitmapManager == null)
            mBitmapManager = new BitmapManager();
        return mBitmapManager;
    }

    public synchronized void cancelThreadDecoding(Thread thread, ContentResolver contentResolver) {
        ThreadStatus threadStatus = getOrCreateThreadStatus(thread);
        threadStatus.mState = State.CANCEL;
        if (threadStatus.mOptions != null) {
            threadStatus.mOptions.requestCancelDecode();
        }
        notifyAll();
        try {
            synchronized (threadStatus) {
                while (threadStatus.mThumbRequesting) {
                    MediaStore.Images.Thumbnails.cancelThumbnailRequest(contentResolver, -1, thread.getId());
                    MediaStore.Video.Thumbnails.cancelThumbnailRequest(contentResolver, -1, thread.getId());
                    threadStatus.wait(200);
                }
            }
        } catch (Exception exception) {
        }
    }

    /*获取缩略图*/
    public Bitmap getThumbnail(ContentResolver contentResolver, long originalId, int kind, BitmapFactory.Options options, boolean isVideo) {
        Thread thread = Thread.currentThread();
        ThreadStatus threadStatus = getOrCreateThreadStatus(thread);
        if (!canThreadDecoding(thread)) {
            return null;
        }
        try {
            synchronized (threadStatus) {
                threadStatus.mThumbRequesting = true;
            }
            if (isVideo) {
                return MediaStore.Video.Thumbnails.getThumbnail(contentResolver, originalId, thread.getId(), kind, options);
            } else {
                return MediaStore.Images.Thumbnails.getThumbnail(contentResolver, originalId, thread.getId(), kind, options);
            }
        } finally {
            synchronized (threadStatus) {
                threadStatus.mThumbRequesting = false;
                threadStatus.notifyAll();
            }
        }
    }

    /*解码图片*/
    public Bitmap decodeFileDescriptor(FileDescriptor fileDescriptor, BitmapFactory.Options options) {
        if (options.mCancel) {
            return null;
        }
        Thread thread = Thread.currentThread();
        if (!canThreadDecoding(thread)) {
            return null;
        }
        setDecodingOptions(thread, options);
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        removeDecodingOptions(thread);
        return bitmap;
    }
}























