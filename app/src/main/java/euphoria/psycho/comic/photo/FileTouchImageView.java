package euphoria.psycho.comic.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import java.io.File;
import java.io.FileInputStream;

import euphoria.psycho.comic.util.Utilities;


public class FileTouchImageView extends UrlTouchImageView {
    private final static String TAG = "FileTouchImageView";
    private final static int pxHeight_ = Utilities.getScreenPxHeight();
    private final static int pxWidth_ = (int) (Utilities.getScreenPxWidth() * 1.5);

    public FileTouchImageView(Context ctx) {
        super(ctx);

    }

    public FileTouchImageView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public void setUrl(String imagePath) {

        new ImageLoadTask().execute(imagePath);
    }

    public static int findSampleSize(int actualWidth,

                                     int desiredWidth) {
        double w = (double) actualWidth / desiredWidth;

        float n = 1.0F;
        while ((n * 2) <= w) {
            n *= 2;
        }
        return (int) n;

    }

    //No caching load
    public class ImageLoadTask extends UrlTouchImageView.ImageLoadTask {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String path = strings[0];
            Bitmap bm = null;
            try {
                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                //BufferedInputStream bis = new BufferedInputStream(fis)
          /*     InputStreamWrapper bis = new InputStreamWrapper(fis, 8192, file.length());
                bis.setProgressListener(new InputStreamWrapper.InputStreamProgressListener()
                {
                    @Override
                    public void onProgress(float progressValue, long bytesLoaded,
                                           long bytesTotal)
                    {
                        publishProgress((int)(progressValue * 100));
                    }
*/

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int w = options.outWidth;
               /* int h = options.outHeight;*/
                if (w > pxWidth_)
                    options.inSampleSize = 2;

                options.inJustDecodeBounds = false;

                bm = BitmapFactory.decodeStream(fis, null, options);

                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bm;
        }
    }
}