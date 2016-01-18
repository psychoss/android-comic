package euphoria.psycho.comic;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import euphoria.psycho.comic.adapter.DownloadListAdapter;
import euphoria.psycho.comic.util.Constants;
import euphoria.psycho.comic.util.ContentView;
import euphoria.psycho.comic.util.InjectView;
import euphoria.psycho.comic.util.Injector;
import euphoria.psycho.downloader.Downloader;

/**
 * Created by Administrator on 2015/1/16.
 */
@ContentView(R.layout.download_list)
public class DownloadActivity extends ActionBarActivity {

    private DownloadListAdapter mDownloadListAdapter;
    @InjectView(R.id.ui_downloader_list)
    private RecyclerView mRecyclerView;
    private boolean mBound;
    private RecyclerView.LayoutManager mLayoutManager;


    private final Downloader mDownloader = Downloader.getInstance();
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            mBound = false;

        }
    };
    /*private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mDownloadListAdapter != null) {
                mDownloadListAdapter.notifyDataSetChanged();
            }
        }
    };*/


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    private void fillListView() {
        mDownloadListAdapter = new DownloadListAdapter(this, mDownloader.getCurrentDownloadRequests());
        if (mLayoutManager == null)
            mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDownloadListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBound) {
            final Intent intent = new Intent(this, DownloaderService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        }
        if (mDownloader != null)

            fillListView();
      /*  registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.UPDATE_DOWNLOAD_LIST));*/
    }

    @Override
    protected void onPause() {
        super.onPause();


        unbindService(mServiceConnection);

       /* unregisterReceiver(mBroadcastReceiver);*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);

        initializeActionBar();
    }

    @Override
    public void onStop() {

        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    private void initializeActionBar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }


}
