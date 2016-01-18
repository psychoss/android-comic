package euphoria.psycho.downloader;

/**
 * Created by Administrator on 2015/1/10.
 */
public interface DownloadListener {

    void onStatusChanged();

    void onErrorOccurred(StopRequestException e);
}
