package euphoria.psycho.comic.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.comic.util.Triple;
import euphoria.psycho.comic.util.Utilities;

/**
 * Created by Administrator on 2015/1/14.
 */
public class OneDatabaseHelper extends SQLiteOpenHelper {


    private final String uri_;

    public OneDatabaseHelper(Context context, String name, String uri) {

        super(context, name, null, 5);
        uri_ = uri;
     /*   Utilities.pushLogToError(Integer.parseInt(getSQLiteVersion()));*/
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PICTURES(" +
                "TITLE TEXT PRIMARY KEY," +
                "HREF TEXT," +
                "THUMBNAIL TEXT," +
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
        Cursor cursor = sqLiteDatabase.query("PICTURES", new String[]{"HREF"}, "TITLE=?", new String[]{title}, null, null, null);
        while (cursor.moveToNext()) {
            return cursor.getString(0);
        }

        return "";
    }

    public List<Triple<String, String, String>> getRecords(String uri) {
        final SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        final String category = getHashCode(uri);
        final List<Triple<String, String, String>> triples = new ArrayList<>();
        if (sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {

                Cursor cursor = sqLiteDatabase.query("PICTURES", new String[]{"TITLE", "HREF", "THUMBNAIL"}, "CATEGORY=?", new String[]{category}, null, null, null);

                if (cursor == null)
                    return triples;
                while (cursor.moveToNext()) {

                    final Triple<String, String, String> triple = Triple.create(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                    triples.add(triple);
                }

                cursor.close();
            }
        }
        return triples;
    }

    public void insert(List<Triple<String, String, String>> triples) {
        final SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        if (triples != null && sqLiteDatabase != null) {
            if (sqLiteDatabase.isOpen()) {


                final int length = triples.size();
                if (length < 1)
                    return;
                final String category = getHashCode(uri_);
                final SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement("REPLACE INTO PICTURES(TITLE, HREF, THUMBNAIL, CATEGORY) VALUES (?, ?, ?, ?);");
                sqLiteDatabase.beginTransaction();
                try {
                    for (int i = 0; i < length; i++) {
                        sqLiteStatement.bindString(1, triples.get(i).first);
                        sqLiteStatement.bindString(2, triples.get(i).second);
                        sqLiteStatement.bindString(3, triples.get(i).third);
                        sqLiteStatement.bindString(4, category);
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
