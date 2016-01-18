package euphoria.psycho.comic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

/**
 * Created by Administrator on 2015/1/14.
 */
public class LogUtilities {
    public static SharedPreferences mSharedPreferences;
    private static final String LOG_PRE = "log_pre";

    public interface OnLogListener {
        void flushToDataBase(Pair<String, String> pair);

    }

    public void writeToSharedPreferences(String name, Object o) {

        if (mSharedPreferences == null) {
            mSharedPreferences = Utilities.getContext().getSharedPreferences(LOG_PRE, Context.MODE_MULTI_PROCESS);

        }
        mSharedPreferences.edit().putString(String.format("%s", System.currentTimeMillis()), String.format("%s=%s", name, o));
    }

}
