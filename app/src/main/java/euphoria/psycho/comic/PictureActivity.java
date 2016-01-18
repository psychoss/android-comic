package euphoria.psycho.comic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.comic.adapter.PictureFileListAdapter;
import euphoria.psycho.comic.adapter.PictureListAdapter;
import euphoria.psycho.comic.async.PictureDownloadAsyncTask;
import euphoria.psycho.comic.async.PictureParseAsyncTask;
import euphoria.psycho.comic.component.FlatDialog;
import euphoria.psycho.comic.db.DatabaseHelper;
import euphoria.psycho.comic.listener.OnListener;
import euphoria.psycho.comic.util.Constants;
import euphoria.psycho.comic.util.ContentView;
import euphoria.psycho.comic.util.InjectView;
import euphoria.psycho.comic.util.Injector;
import euphoria.psycho.comic.util.Utilities;

/**
 * Created by Administrator on 2015/1/11.
 */
@ContentView(R.layout.picture_layout)
public class PictureActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @InjectView(R.id.ui_picture_list_view)
    private ListView mListView;
    @InjectView(R.id.ui_picture_progress_layout)
    private LinearLayout mLinearLayout;
    private final static int mPARSEURI = 0;
    private final static int mDOWNLOADURI = 1;
    private boolean mIsRefresh = false;
    private ArrayList<String> mList;
    private Pair<String, ArrayList<String>> mPairDownloadList;
    private List<Pair<String, String>> mPairs;
    private PictureFileListAdapter mPictureListAdapter;
    private SharedPreferences mSharedPreferences;
    private String mTitle;
    private String mUri;
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            final int mes = message.what;
            //解析页面的超链接
            if (mes == mPARSEURI) {

                showProgressLayout();
                new PictureParseAsyncTask(new OnListener<List<Pair<String, String>>>() {
                    @Override
                    public void onFetchDataFinished(List<Pair<String, String>> list) {

                        mIsRefresh = false;
                        mPairs = list;
                        hideProgressLayout();
                        if (Utilities.isListEmpty(list)) {
                            reportError(getString(R.string.parse_no_data));
                        } else {
                            refreshListView();
                        }

                    }

                    @Override
                    public void onErrorOccurred(String message) {

                    }
                }, mIsRefresh).execute(mUri);
            } else if (mes == mDOWNLOADURI) {//下载图片


                if (mPairDownloadList == null) {
                    reportError(getString(R.string.parse_no_data));
                    hideProgressLayout();

                } else {
                    final ArrayList<String> list = mPairDownloadList.second;

                    if (Utilities.isListEmpty(list)) {
                        reportError(getString(R.string.parse_no_data));
                        hideProgressLayout();
                    } else {
                        final Intent intent = new Intent(PictureActivity.this, DownloaderService.class);
                        intent.putStringArrayListExtra(Constants.DOWNLOADER_LIST, list);
                        final String dir = Utilities.getPictureDirectory(mPairDownloadList.first);
                        Utilities.checkDirectory(dir, true);
                        intent.putExtra(Constants.DOWNLOADER_DIRECTORY, dir);
                        startService(intent);
                    }
                }
            }
            return false;
        }
    });


    private void checkSharedPreferences() {
        final String s = mSharedPreferences.getString(Constants.PRE_PICTURE_URI, "");
        if (!Utilities.isEmpty(s))
            mUri = s;
    }

    private void exit() {
        finish();
    }

    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPopupMenu(View view, boolean isShowDelete) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {
                mTitle = "";
            }
        });
        final Menu menu = popupMenu.getMenu();
        if (isShowDelete) {
            final MenuItem menuItemDelete = menu.add("删除");
            menuItemDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (!Utilities.isEmpty(mTitle)) {
                        final String dir = Utilities.getPictureDirectory(mTitle);
                        Utilities.deleteRecursive(dir);
                        if (mPictureListAdapter != null) {
                            mPictureListAdapter.remove(mTitle);
                            mTitle = "";
                            mPictureListAdapter.notifyDataSetChanged();
                        }
                    }
                    return true;
                }
            });
        }
        final MenuItem menuItemRetry = menu.add("重新下载");
        menuItemRetry.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (Utilities.isEmpty(mTitle)) {
                    Toast.makeText(PictureActivity.this, "缓存数据丢失，无法下载。", Toast.LENGTH_LONG).show();
                }

                final DatabaseHelper databaseHelper = new DatabaseHelper(Utilities.getContext(), Utilities.getExternalStorageFileInDirectory(Constants.PICTURE_DIRECTORY_DATABASE).getAbsolutePath(), "");
                final String s = databaseHelper.getUri(mTitle);
                if (Utilities.isEmpty(s)) {
                    Toast.makeText(PictureActivity.this, "缓存数据丢失，无法下载。", Toast.LENGTH_LONG).show();
                } else {
                    final String dir = Utilities.getPictureDirectory(mTitle);
                    Utilities.deleteFiles(dir);
                    showProgressLayout();
                    new PictureDownloadAsyncTask(new OnListener<Pair<String, ArrayList<String>>>() {
                        @Override
                        public void onFetchDataFinished(Pair<String, ArrayList<String>> pair) {
                            mPairDownloadList = pair;
                            mHandler.sendEmptyMessage(mDOWNLOADURI);
                        }

                        @Override
                        public void onErrorOccurred(String message) {
                        }
                    }).execute(Pair.create(mTitle, s));
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void hideProgressLayout() {
        mLinearLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    private void initialize() {
        forceShowActionBarOverflowMenu();
        checkSharedPreferences();
        refreshFileListView();
    }

    private void initializeActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        mSharedPreferences = getSharedPreferences(Constants.PRE_NAME, MODE_MULTI_PROCESS);
        initializeActionBar();
        initialize();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mPairs != null) {
            if (mPairs.size() > 0) {
                final String dir = Utilities.getPictureDirectory(mPairs.get(i).first);
                if (!Utilities.checkIfHasImageFile(dir)) {
                    showProgressLayout();
                    new PictureDownloadAsyncTask(new OnListener<Pair<String, ArrayList<String>>>() {
                        @Override
                        public void onFetchDataFinished(Pair<String, ArrayList<String>> pair) {
                            mPairDownloadList = pair;
                            mHandler.sendEmptyMessage(mDOWNLOADURI);
                        }

                        @Override
                        public void onErrorOccurred(String message) {
                        }
                    }).execute(mPairs.get(i));
                } else {
                    startGallery(dir);
                }
            }
        } else if (mList.size() > 0) {
            final String dir = Utilities.getPictureDirectory(mList.get(i));
            startGallery(dir);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!Utilities.isListEmpty(mPairs)) {
            mTitle = mPairs.get(i).first;
            getPopupMenu(view, true);
        } else if (!Utilities.isListEmpty(mList)) {
            mTitle = mList.get(i);
            getPopupMenu(view, false);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            exit();
            return true;
        } else if (id == R.id.action_refresh) {
            refresh();
        } else if (id == R.id.action_download) {
            showDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        hideProgressLayout();
        super.onStop();

    }

    private void refresh() {
        mIsRefresh = true;
        if (!Utilities.isEmpty(mUri))
            mHandler.sendEmptyMessage(mPARSEURI);
        else {
            showDialog();
            mIsRefresh = false;
        }
    }

    private void refreshFileListView() {
        final File file = Utilities.getExternalStorageFileInDirectory(Constants.PICTURE_DIRECTORY);
        if (!file.exists())
            file.mkdirs();

        mList = Utilities.getDirectories(file.getAbsolutePath());
        if (mList != null && mList.size() > 0) {
            mPictureListAdapter = new PictureFileListAdapter(this, mList);
            mListView.setAdapter(mPictureListAdapter);
        }
    }

    private void refreshListView() {
        if (mPairs == null || mPairs.size() < 1) {
            return;
        }
        final PictureListAdapter pictureListAdapter = new PictureListAdapter(this, mPairs);
        mListView.setAdapter(pictureListAdapter);
    }

    private void reportError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showDialog() {
        final LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.dialog_content_view, null);
        final EditText editText = (EditText) view.findViewById(R.id.ui_dialog_edit_text);
        final String pre = mSharedPreferences.getString(Constants.PRE_PICTURE_URI, "");
        if (!Utilities.isEmpty(pre)) {
            editText.setText(pre);
        }
        final FlatDialog flatDialog = new FlatDialog(this);
        flatDialog.setTitle("设置页面网址")
                .setContentView(view)
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uri = editText.getText().toString().trim();
                        if (uri.startsWith("1")) {

                            final String dir = Utilities.getPictureDirectory("temporary");
                            final File dirFile = new File(dir);
                            if (!dirFile.isDirectory()) {
                                dirFile.mkdirs();
                            }
                            showProgressLayout();
                            new PictureDownloadAsyncTask(new OnListener<Pair<String, ArrayList<String>>>() {
                                @Override
                                public void onFetchDataFinished(Pair<String, ArrayList<String>> pair) {
                                    mPairDownloadList = pair;
                                    mHandler.sendEmptyMessage(mDOWNLOADURI);
                                }

                                @Override
                                public void onErrorOccurred(String message) {
                                }
                            }).execute(Pair.create("temporary", uri.substring(1)));
                            //   mSharedPreferences.edit().putString(Constants.PRE_PICTURE_URI, mUri).commit();
                            // mHandler.sendEmptyMessage(mPARSEURI);
                            flatDialog.dismiss();
                            return;
                        }
                        if (Utilities.isEmpty(uri)) {
                            editText.setError("网页地址不能为空。");
                            return;
                        }
                        if (!uri.toLowerCase().startsWith("http://") && !uri.toLowerCase().startsWith("https://")) {
                            uri = "http://" + uri;
                        }
                        if (Utilities.validateUri(uri)) {
                            mUri = uri;
                            mSharedPreferences.edit().putString(Constants.PRE_PICTURE_URI, mUri).commit();
                            mHandler.sendEmptyMessage(mPARSEURI);
                            flatDialog.dismiss();
                        } else {
                            editText.setError("您输入的不是合法网址。.");
                        }
                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flatDialog.dismiss();
            }
        }).setCanceledOnTouchOutside(false);
        flatDialog.show();
    }

    private void showProgressLayout() {
        mListView.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    private void startGallery(String dir) {
        final ArrayList<String> list = Utilities.getImageFiles(dir);
        if (list != null) {
            if (list.size() > 0) {
                final Intent intent = new Intent(this, GalleryActivity.class);
                intent.putStringArrayListExtra(Constants.GALLERY_LIST, list);
                startActivity(intent);
            }
        }
    }

}