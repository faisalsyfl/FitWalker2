package sqrtstudio.com.fitwalker;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;


import java.util.ArrayList;


/**
 * Created by Faisal Syaiful Anwar on 10/30/2016.
 */

public class DbFitWalker{
    private String table = "POI";
    public static class POI{
        public int id;
        public String desc;
        public double lat;
        public double lng;
        public int point;

    }

    private SQLiteDatabase db;
    private final OpenHelper dbHelper;

    public DbFitWalker (Context c){
        dbHelper = new OpenHelper(c);

    }

    public void open(){
        db = dbHelper.getWritableDatabase();
    }

    public void close(){
        db.close();
    }

    public long insertNew(String desc,double lat, double lng, int point){
        ContentValues newValue = new ContentValues();
        newValue.put(dbHelper.KEY_DESC,desc);
        newValue.put(dbHelper.KEY_LAT,lat);
        newValue.put(dbHelper.KEY_LONG,lng);
        newValue.put(dbHelper.KEY_POINT,point);
        return db.insert(table,null,newValue);
    }

    public ArrayList<POI> selectAll() {
        ArrayList<POI> tabList = new ArrayList<POI>();
        String selectQuery = "SELECT * FROM POI";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                POI data = new POI();
                data.id = Integer.parseInt(cursor.getString(0));
                data.desc = cursor.getString(1);
                data.lat = Double.parseDouble(cursor.getString(2));
                data.lng = Double.parseDouble(cursor.getString(3));
                data.point = Integer.parseInt(cursor.getString(4));
//                Log.d("cursor",cursor.getString(2)+" -- "+ cursor.getString(3));
                tabList.add(data);
            } while (cursor.moveToNext());
        }
        return tabList;
    }

    //    public Tabungan getTabungan(String id){
//        Cursor cur = null;
//        Tabungan T = new Tabungan();
//        String[] cols = new String[]{"ID", "CATEGORY", "SPENT", "DESC","TGL"};
//
//        String[] param = {id};
//        cur = db.query("TABUNGAN",cols,"ID=?",param,null,null,null);
//
//        if(cur.getCount()>0){
//            cur.moveToFirst();
//            T.id = Integer.parseInt(cur.getString(0));
//            T.category = cur.getString(1);
//            T.spent = Integer.parseInt(cur.getString(2));
//            T.desc = cur.getString(3);
//            T.tgl = cur.getString(4);
//        }
//
//        return T;
//    }
//
    public boolean checkCoor(double lat,double lng){
        Cursor cur = null;
        POI T = new POI();
        String[] cols = new String[]{"ID", "DESC", "LAT", "LNG","POINT"};
        String[] param = {String.valueOf(lat),String.valueOf(lng)};
        cur = db.query(table,cols,"LAT=? AND LNG=?",param,null,null,null);

        if(cur.getCount()>0){
            return true;
        }else{
            return false;
        }
    }
    public void removeAll()
    {
        db.delete(table, null, null);
    }

//    public int updateShop(Tabungan tab) {
//        ContentValues values = new ContentValues();
//        values.put("CATEGORY", tab.getCategory());
//        values.put("SPENT", tab.getSpent());
//        values.put("DESC", tab.getDesc());
//        values.put("TGL", tab.getTgl());
//        Log.d("update:",tab.getTgl());
//        return db.update("TABUNGAN", values, "ID" + " = ?",
//                new String[]{String.valueOf(tab.getId())});
//    }
//    public void deleteTabungan(int id) {
////        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("TABUNGAN", "ID" + " = ?",
//                new String[] { String.valueOf(id) });
//        db.close();
//    }
}