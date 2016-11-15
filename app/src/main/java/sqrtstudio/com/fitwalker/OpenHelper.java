package sqrtstudio.com.fitwalker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Faisal Syaiful Anwar on 10/30/2016.
 */

public class OpenHelper extends SQLiteOpenHelper {


    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbRGR.db";
    public static final String TABLE = "POI";
    public static final String KEY_ID = "ID";
    public static final String KEY_DESC = "DESC";
    public static final String KEY_LAT = "LAT";
    public static final String KEY_LONG = "LNG";
    public static final String KEY_POINT = "POINT";


    public OpenHelper (Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DESC + " TEXT,"
                + KEY_LAT + " DOUBLE," + KEY_LONG + " DOUBLE," + KEY_POINT + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS" + TABLE);
        // Creating tables again
        onCreate(db);
    }
}