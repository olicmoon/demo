package com.samsung.sdpdemo;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.sec.sdp.SdpDatabase;
import com.sec.sdp.SdpFileSystem;
import com.sec.sdp.SdpStateListener;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpEngineInfo;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DefaultEngineDatabase {

    public final static String DBFILE = "test_default_engine.db";
    public final static int DB_VERSION = 1;
    public final static String TAG = "SDP.SensitiveDatabase";
    private SensitiveDatabaseHelper sensitiveDatabaseHelper = null;
    private Context mContext = null;
    private SQLiteDatabase mDb = null;

    public static final String TABLE_ABC = "default_engine_table";
    public static final String TABLE_PQR = "default_engine_table2";

    public static final String TABLE_PQR_COL_ID = "id";
    public static final String TABLE_PQR_COL2 = "col2";
    public static final String TABLE_PQR_COL3 = "col3";

    public static final String TABLE_ABC_COL_ID = "id";
    public static final String TABLE_ABC_COL1 = "col1";
    public static final String TABLE_ABC_COL2 = "col2";
    public static final String TABLE_ABC_COL3 = "col3";


    private class SensitiveDatabaseHelper extends SQLiteOpenHelper {

        SensitiveDatabaseHelper(Context context) {
            super(context, DBFILE, null, DB_VERSION);
        }

        void executeSQLStatement(SQLiteDatabase dbhdl, String stmt) {
            Log.d(TAG, "executeSQLStatement  : start");
            if (stmt == null) {
                Log.e(TAG, "Error: Can not execute Statement stmt = 0");
                return;
            }

            int len = stmt.length();
            if (len == 0) {
                Log.e(TAG, "Error: Can not execute Statement len = 0");
                return;
            }

            len = (len > 1000) ? 1000 : len;
            Log.d(TAG, "executeSQLStatement  : " + stmt.substring(0, len) + "...");
            Cursor c = null;
            try {
                c = dbhdl.rawQuery(stmt, null);
                if (c.moveToFirst()) {
                    do {
                        String row = "";
                        int k = c.getColumnCount();
                        for (int l = 0; l < k; l++)
                            row += c.getString(l) + "\t";
                        row += "\n";
                        Log.e(TAG, "<<SDPDemoLog>>: Result = " + row);
                    } while (c.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (c != null)
                c.close();

            Log.d(TAG, "executeSQLStatement  : end");
        }

        @Override
        public void onCreate(SQLiteDatabase dbhdl) {
            Log.d(TAG, "createDB ");
            try {
                mDb = dbhdl;

                Log.d(TAG, "Database is first time created");
                executeSQLStatement(mDb, "CREATE TABLE default_engine_table (id integer PRIMARY KEY AUTOINCREMENT, col1 text, col2 text, col3 text);");
                executeSQLStatement(mDb, "CREATE INDEX default_idx ON default_engine_table(col1);");

                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (1, 'default_col1_text1', 'default_col2_text1', 'plain');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (2, 'default_col1_text2', 'default_col2_text2', 'plain');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (3, 'default_col1_text3', 'default_col2_text3', 'plain');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (4, 'default_col1_text4', 'default_col2_text4', 'plain');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (5, 'default_col1_text5', 'default_col2_text5', 'plain');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table VALUES (6, 'default_col1_text6', 'default_col2_text6', 'plain');");


                executeSQLStatement(mDb, "CREATE TABLE default_engine_table2 (id integer PRIMARY KEY AUTOINCREMENT, col1 text, col2 text);");
                executeSQLStatement(mDb, "CREATE INDEX default_idx1 ON default_engine_table2(col1);");

                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (1, 'default_col1_text1', 'default_col2_text1');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (2, 'default_col1_text2', 'default_col2_text2');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (3, 'default_col1_text3', 'default_col2_text3');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (4, 'default_col1_text4', 'default_col2_text4');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (5, 'default_col1_text5', 'default_col2_text5');");
                executeSQLStatement(mDb, "INSERT INTO default_engine_table2 VALUES (6, 'default_col1_text6', 'default_col2_text6');");

            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + "default_engine_table");
            db.execSQL("DROP TABLE IF EXISTS " + "default_engine_table2");
            onCreate(db);
        }
    }

    public DefaultEngineDatabase(Context context) {
        mContext = context;
        sensitiveDatabaseHelper = new SensitiveDatabaseHelper(context);
    }

    public SQLiteDatabase openDatabase() {
        mDb = sensitiveDatabaseHelper.getWritableDatabase();
        return mDb;
    }

    public void setSensitive(String dbFileName) {
        Log.d(TAG, "setSensitive ");
        openDatabase();

        try {
            SdpDatabase sdpColumn = new SdpDatabase(null);
            List<String> columnsList = new ArrayList<String>();
            columnsList.add("col1");
            columnsList.add("col2");
            sdpColumn.setSensitive(mDb, null, "default_engine_table", columnsList);

            sdpColumn = new SdpDatabase(null);
            columnsList = new ArrayList<String>();
            columnsList.add("col1");
            sdpColumn.setSensitive(mDb, null, "default_engine_table2", columnsList);

            mDb.execSQL("pragma sdp_set_flag(\"decrypt_fail_flag=3\");");
        } catch (SQLiteException e) {
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getDbRecords() {
        SQLiteDatabase db = openDatabase();
        return db.query("default_engine_table", null, null, null, null, null, null);
    }

    public void createDB(String dbFileName) {

        closeDb();
        File database = mContext.getDatabasePath(dbFileName);
        if (database.exists()) {
            database.delete();
        }
        openDatabase();
    }

    public static final int SDP_STATE_ACTIVE = 1;
    public static final int SDP_STATE_INACTIVE = 2;
    private static final boolean runAllConvert = false;

    public boolean updateSDPStateToDB(String dbalias, int sdpState) {
        Cursor cursor = null;
        openDatabase();
        try {
            Log.e(TAG, "updateSDPStateToDB called with dbalias = " + dbalias + " sdpState = " + sdpState);
            String dbaliasprefix = (dbalias == null) ? "" : (dbalias + ".");
            switch (sdpState) {
                case SDP_STATE_ACTIVE:
                    mDb.execSQL("pragma " + dbaliasprefix + "sdp_locked;");
                    break;
                case SDP_STATE_INACTIVE:
                    mDb.execSQL("pragma " + dbaliasprefix + "sdp_unlocked;");
                    if (runAllConvert) {
                        mDb.execSQL("pragma " + dbaliasprefix + "sdp_run_all_convert;");
                    } else {
                        int rows = 1;
                        while (rows > 0) {
                            Log.e(TAG, "calling next : pragma runoneconvert  in sdpState = " + sdpState);
                            cursor = mDb.rawQuery("pragma " + dbaliasprefix + "sdp_run_one_convert", null);
                            if (cursor.moveToFirst()) {
                                rows = cursor.getInt(0);
                            }
                            Thread.sleep(10);
                            cursor.close();
                        }
                        Log.e(TAG, "DONE calling all pragma runoneconvert  in sdpState = " + sdpState);
                    }
                    break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null)
                cursor.close();
        }

        return false;
    }

    public void closeDb() {
        if (mDb != null && mDb.isOpen())
            mDb.close();
    }
}
