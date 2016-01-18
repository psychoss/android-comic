package euphoria.psycho.comic.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import euphoria.psycho.comic.R;
import euphoria.psycho.downloader.DownloadListener;
import euphoria.psycho.downloader.DownloaderRequest;
import euphoria.psycho.downloader.LProgressBar;
import euphoria.psycho.downloader.StopRequestException;
import euphoria.psycho.downloader.Utilities;

/**
 * Created by Administrator on 2015/1/10.
 */
public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {
    private final static int mLayoutId = R.layout.download_item;
    private List<DownloaderRequest> mDownloaderRequests;

    private final LayoutInflater mLayoutInflater;

    public DownloadListAdapter(Context context, List<DownloaderRequest> list) {

        mDownloaderRequests = list;
        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = mLayoutInflater.inflate(mLayoutId, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (holder.isNoDownloaderRequest()) {
            holder.setDownloaderRequest(mDownloaderRequests.get(position));

        }

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mDownloaderRequests.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements DownloadListener {

        public final TextView mTextViewTitle;
        public final TextView mTextViewSpeed;
        public final TextView mTextViewTotal;
        public final LProgressBar mProgressBar;
        public final TextView mTextViewSpeedSecond;

        final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {

             /*   if (message.what == 0) {*/
                    mProgressBar.setProgress(mDownloaderRequest.getPercent());

                    mTextViewSpeed.setText(Utilities.formatFileSize(mDownloaderRequest.getCurrentByte()));
                    mTextViewTotal.setText(Utilities.formatFileSize(mDownloaderRequest.getTotalBytes()));
                    if (mDownloaderRequest.getSpeed() > 0)
                        mTextViewSpeedSecond.setText(Utilities.formatFileSize(mDownloaderRequest.getSpeed()) + "/秒");
             /*   } else {*/

              /*  }*/
            }
        };

        private DownloaderRequest mDownloaderRequest;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextViewTitle = (TextView) itemView.findViewById(R.id.ui_downloader_title);
            mTextViewSpeed = (TextView) itemView.findViewById(R.id.ui_downloader_speed);
            mTextViewTotal = (TextView) itemView.findViewById(R.id.ui_downloader_total);
            mTextViewSpeedSecond = (TextView) itemView.findViewById(R.id.ui_downloader_speed_second);
            mProgressBar = (LProgressBar) itemView.findViewById(R.id.ui_downloader_progress);
        }


        public void setDownloaderRequest(DownloaderRequest downloaderRequest) {
            mDownloaderRequest = downloaderRequest;
            /*mHandler.sendEmptyMessage(1);*/
            mTextViewTitle.setText(Utilities.parseUriForFileName(mDownloaderRequest.getUrl_()));
            mDownloaderRequest.setDownloadListener(this);
        }

        public boolean isNoDownloaderRequest() {
            return mDownloaderRequest == null;
        }

        @Override
        public void onStatusChanged() {


            mHandler.sendEmptyMessage(0);
        }

        @Override
        public void onErrorOccurred(StopRequestException e) {

        }
    }
}
