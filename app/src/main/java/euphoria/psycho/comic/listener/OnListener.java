package euphoria.psycho.comic.listener;

/**
 * Created by Administrator on 2015/1/13.
 */
public interface OnListener<T> {

    void onFetchDataFinished(T t);

    void onErrorOccurred(String message);


}
