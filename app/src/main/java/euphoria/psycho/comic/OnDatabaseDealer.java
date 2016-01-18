package euphoria.psycho.comic;

import android.util.Pair;

import java.util.List;

/**
 * Created by Administrator on 2015/1/11.
 */
public interface OnDatabaseDealer {
    List<Pair<String, String>> getCacheFromDatabase(String absolutePath);

    void insert(List<Pair<String, String>> list);
}
