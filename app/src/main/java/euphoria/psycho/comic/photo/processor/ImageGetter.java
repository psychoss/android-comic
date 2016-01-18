package euphoria.psycho.comic.photo.processor;


import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Administrator on 2014/12/7.
 */

interface ImageGetterCallback {

}

public class ImageGetter {

    private ContentResolver mContentResolver;

    private volatile boolean mCancel = false;
    private Thread mThread;

    public ImageGetter(ContentResolver contentResolver) {


        mContentResolver = contentResolver;

        mThread = new Thread(new ImageGetterRunnable());
        mThread.setName("ImageGetter");
        mThread.start();
    }

    public synchronized void cancelCurrent() {
        mCancel = true;

    }

    private class ImageGetterRunnable implements Runnable {

        @Override
        public void run() {

        }
    }
}


class GetterHandler extends Handler {
    private static final int IMAGE_GETTER_CALLBACK = 1;

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case IMAGE_GETTER_CALLBACK:
                ((Runnable) message.obj).run();
                break;
        }
    }

    public void postGetterCallback(Runnable runnable) {

        postGetterCallback(runnable, 0);
    }

    public void postGetterCallback(Runnable runnable, long delay) {
        if (runnable == null) {
            throw new NullPointerException("Callback cannot be null.");
        }
        Message message = Message.obtain();
        message.what = IMAGE_GETTER_CALLBACK;
        message.obj = runnable;
        sendMessageDelayed(message, delay);
    }

    public void removeAllGetterCallbacks() {
        removeMessages(IMAGE_GETTER_CALLBACK);
    }
}