package euphoria.psycho.comic;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import euphoria.psycho.comic.photo.FilePagerAdapter;
import euphoria.psycho.comic.photo.GalleryViewPager;
import euphoria.psycho.comic.util.Constants;
import euphoria.psycho.comic.util.ContentView;
import euphoria.psycho.comic.util.InjectView;
import euphoria.psycho.comic.util.Injector;


/**
 * Created by Administrator on 2014/12/25.
 */
@ContentView(R.layout.activity_gallery)

public class GalleryActivity extends Activity {

    private FilePagerAdapter filePagerAdapter_;
    private ArrayList<String> files_;

    private int currentSelected_ = 0;
    private int count_;
    private boolean autoFing_ = false;
    private Message message_;

    @InjectView( R.id.fullscreen_content)
    private GalleryViewPager galleryViewPager_;

    private final Handler handler_ = new Handler() {
        @Override
        public void handleMessage(Message message) {

            super.handleMessage(message);
            currentSelected_++;
            if (currentSelected_ < count_)
                galleryViewPager_.setCurrentItem(currentSelected_);
            else {
                currentSelected_ = 0;
                galleryViewPager_.setCurrentItem(currentSelected_);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
      /*  try {
            Runtime.getRuntime().exec("service call activity 42 s16 com.android.systemui");
            *//*Runtime.getRuntime().exec("am startservice --user 0 -n com.android.systemui/.SystemUIService");*//*
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        initialize();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_auto_fing) {

            autoFing_ = !autoFing_;
            if (autoFing_) {
                handler_.sendEmptyMessage(0);
            } else {
                handler_.removeMessages(0);
            }
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void initialize() {
        message_ = new Message();
        message_.what = 0;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        files_ = getIntent().getStringArrayListExtra(Constants.GALLERY_LIST);
        filePagerAdapter_ = new FilePagerAdapter(this, files_);
        galleryViewPager_.setOffscreenPageLimit(3);
        galleryViewPager_.setAdapter(filePagerAdapter_);
        count_ = files_.size();
        galleryViewPager_.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {


            }

            @Override
            public void onPageSelected(int i) {
                currentSelected_ = i;
                if (autoFing_) {
                    handler_.removeMessages(0);
                    handler_.sendEmptyMessageDelayed(0, 2000);
                }
                if (i == count_ - 1 && !autoFing_) {
                    Toast.makeText(GalleryActivity.this, "已是最后一页", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

}
