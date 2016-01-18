package euphoria.psycho.comic.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.comic.util.Utilities;

/**
 * Created by Administrator on 2015/1/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    private final String uri_;

    public DatabaseHelper(Context context, String name, String uri) {

        super(context, name, null, 5);
        uri_ = uri;
     /*   Utilities.pushLogToError(Integer.parseInt(getSQLiteVersion()));*/
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PICTURES(" +
                "TITLE TEXT PRIMARY KEY," +
                "HREF TEXT," +
                "CATEGORY TEXT" +
                ");");
/*
        create();*/
    }

    private void create() {


              /*  sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PICTURES(" +
                        "TITLE TEXT PRIMARY KEY," +
                        "HREF TEXT," +
                        "CATEGORY TEXT," +
                        ");");
*/


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


    public String getUri(String title) {
        final SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("PICTURES", new String[]{ "HREF"}, "TITLE=?", new String[]{title}, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getString(0);
        }

        return "";
    }

    public List<Pair<String, String>> getRecords(String uri) {
        final SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        final String category =  getHashCode(uri);
        final List<Pair<String, String>> pairs = new ArrayList<>();
        if (sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {

                Cursor cursor = sqLiteDatabase.query("PICTURES", new String[]{"TITLE", "HREF"}, "CATEGORY=?", new String[]{category}, null, null, null);

                if (cursor == null)
                    return pairs;
                while (cursor.moveToNext()) {

                    final Pair<String, String> pair = Pair.create(cursor.getString(0), cursor.getString(1));
                    pairs.add(pair);
                }

                cursor.close();
            }
        }
        return pairs;
    }

    public void insert(List<Pair<String, String>> pairs) {
        final SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        if (pairs != null && sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {


                final int length = pairs.size();
                if (length < 1)
                    return;
                final String category = getHashCode(uri_) ;
                final SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement("REPLACE INTO PICTURES(TITLE, HREF, CATEGORY) VALUES (?, ?, ?);");
                sqLiteDatabase.beginTransaction();
                try {
                    for (int i = 0; i < length; i++) {
                        sqLiteStatement.bindString(1, pairs.get(i).first);
                        sqLiteStatement.bindString(2, pairs.get(i).second);
                        sqLiteStatement.bindString(3, category);
                        sqLiteStatement.executeInsert();
                    }
                    sqLiteDatabase.setTransactionSuccessful();
                } finally {
                    sqLiteDatabase.endTransaction();
                }
            }
        }
    }

    public static String getHashCode(String uri) {

        if (Utilities.isEmpty(uri))
            return "-1";
        int firstHalfLength = uri.length() / 2;
        return String.valueOf(uri.substring(0, firstHalfLength).hashCode()) + String.valueOf(uri.substring(firstHalfLength).hashCode());
    }

    public static String getSQLiteVersion() {
        final Cursor cursor = SQLiteDatabase.openOrCreateDatabase(":memory:", null).rawQuery("select sqlite_version() AS sqlite_version", null);
        String sqliteVersion = "";
        while (cursor.moveToNext()) {
            sqliteVersion += cursor.getString(0);
        }
        return sqliteVersion;
    }

}
