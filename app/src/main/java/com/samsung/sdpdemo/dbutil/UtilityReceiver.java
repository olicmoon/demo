package com.samsung.sdpdemo.dbutil;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.samsung.sdpdemo.Constants;
import com.samsung.sdpdemo.CustomEngineDatabase;
import com.samsung.sdpdemo.DbRow;
import com.samsung.sdpdemo.DefaultEngineDatabase;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpEngineInfo;

import java.text.DateFormat;

public class UtilityReceiver extends BroadcastReceiver {
    private Context mContext = null;
    private boolean mIsDefaultEngineSelected = false;
    private SQLiteDatabase mCustomDb = null;
    private SQLiteDatabase mDefaultDb = null;

    public UtilityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mIsDefaultEngineSelected = intent.getBooleanExtra(Constants.INTENT_EXTRAS_IS_DEFAULT_ENGINE, false);
        Log.d("UtilityReceiver", "onReceive::mIsDefaultEngineSelected " + mIsDefaultEngineSelected + " " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //TODO: investigate the eroor coming during boot complete
            Log.d("UtilityReceiver", "onReceive:: " + Intent.ACTION_BOOT_COMPLETED);
            Intent serviceIntent = new Intent(context, UtilityService.class);
            serviceIntent.putExtra(Constants.INTENT_EXTRAS_FROM_RECEIVER, true);
            context.startService(serviceIntent);
            return;
        }

        int defaultRecordSize = 5;
        DefaultEngineDatabase sensitiveDatabase = new DefaultEngineDatabase(mContext);
        mDefaultDb = sensitiveDatabase.openDatabase();

        CustomEngineDatabase sensitiveDatabase1 = new CustomEngineDatabase(mContext);
        mCustomDb = sensitiveDatabase1.openDatabase();

        if (Constants.INTENT_DELETE_DB_RECORDS.equals(intent.getAction())) {
            handleDeleteDbRecords(defaultRecordSize);
        } else if (Constants.INTENT_DISPLAY_DB_RECORDS.equals(intent.getAction())) {
            Cursor cursor = null;
            if (mIsDefaultEngineSelected) {
                cursor = sensitiveDatabase.getDbRecords();
                handleShowDbRecords(cursor, null);
            } else {
                cursor = sensitiveDatabase1.getDbRecords();
                handleShowDbRecords(cursor, Constants.ALIAS);
            }

        } else if (Constants.INTENT_INSERT_DB_RECORDS.equals(intent.getAction())) {
            handleInsertDbRecords(defaultRecordSize);
        } else if (Constants.INTENT_UPDATE_DB_RECORDS.equals(intent.getAction())) {
            handleUpdateDbRecords(defaultRecordSize);
        }

        if (sensitiveDatabase != null) sensitiveDatabase.closeDb();
        if (sensitiveDatabase1 != null) sensitiveDatabase1.closeDb();
    }

    private void handleShowDbRecords(Cursor cursor, String alias) {
        if (cursor == null) {
            Log.d("UtilityReceiver", "NO DATA FOUND!!!!");
        } else {
            boolean isSensitiveSet = false;
            SharedPreferences preferences = mContext.getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            isSensitiveSet = preferences.getBoolean("setSensitive", true);
            Log.d("UtilityReceiver", "found records... isSensitiveSet: " + isSensitiveSet);
            boolean isLocked = false;
            SdpEngineInfo info = null;
            try {
                info = SdpUtil.getInstance().getEngineInfo(alias);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (info != null && info.getState() == SdpEngineConstants.State.LOCKED) {
                isLocked = true;
            }

            String temp = null;
            String temp2 = null;
            String temp3 = null;

            cursor.moveToFirst();
            Log.d("UtilityReceiver", "Table name:: " + DefaultEngineDatabase.TABLE_ABC);
            Log.d("UtilityReceiver", "Sensitive columns:: " + DefaultEngineDatabase.TABLE_ABC_COL1 + " & " + DefaultEngineDatabase.TABLE_ABC_COL2);
            while (cursor.isAfterLast() == false) {
                DbRow row = new DbRow();

                if (isSensitiveSet || isLocked) {
                    Log.d("UtilityReceiver", "dumpDatabaseRecords getting blob... ");
                    byte[] bdata = cursor.getBlob(cursor.getColumnIndex("col1"));
                    temp = new String(bdata);

                    bdata = cursor.getBlob(cursor.getColumnIndex("col2"));
                    temp2 = new String(bdata);

                } else {
                    Log.d("UtilityReceiver", "dumpDatabaseRecords getting string... ");
                    temp = cursor.getString(cursor.getColumnIndex("col1"));
                    temp2 = cursor.getString(cursor.getColumnIndex("col2"));
                }
                temp3 = cursor.getString(cursor.getColumnIndex("col3"));
                int iid = cursor.getInt(cursor.getColumnIndex("id"));
                Log.d("UtilityReceiver", "Record id= " + iid + " col1= " + temp + " col2= " + temp2 + " col3 " + temp3);
                cursor.moveToNext();

            }
            cursor.close();
        }
    }

    private void handleUpdateDbRecords(int noOfRecords) {
        Log.d("UtilityReceiver", "handleUpdateDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);
        int[] rowId = new int[noOfRecords];

        if (mIsDefaultEngineSelected) {

            //getting first N rows for update
            String[] colms = {DefaultEngineDatabase.TABLE_ABC_COL_ID};
            Cursor cursor = mDefaultDb.query(DefaultEngineDatabase.TABLE_ABC, colms, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int row = 0;
                while (cursor.isAfterLast() == false && row < noOfRecords) {
                    rowId[row] = cursor.getInt(0);
                    row++;
                    cursor.moveToNext();
                }
                cursor.close();
            }

            // updating fist N rows
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "custom_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL3, newStr);
                String whereClause = DefaultEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mDefaultDb.update(DefaultEngineDatabase.TABLE_ABC, contentValues, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityReceiver", "failed to update data!");
                }
            }
            //Toast.makeText(mContext, noOfRecords + " rows has been updated to table: " + DefaultEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        } else {

            //getting first N rows for update
            String[] colms = {CustomEngineDatabase.TABLE_ABC_COL_ID};
            Cursor cursor = mCustomDb.query(CustomEngineDatabase.TABLE_ABC, colms, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int row = 0;
                while (cursor.isAfterLast() == false && row < noOfRecords) {
                    rowId[row] = cursor.getInt(0);
                    Log.d("UtilityReceiver", "getting ID= " + rowId[row] + " row= " + row);
                    row++;
                    cursor.moveToNext();
                }
                cursor.close();
            }

            // updating fist N rows
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "default_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL3, newStr);

                String whereClause = CustomEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mCustomDb.update(CustomEngineDatabase.TABLE_ABC, contentValues, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityReceiver", "failed to update data!");
                }
            }
            //Toast.makeText(mContext, noOfRecords + " rows has been updated to table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }

    }

    private void handleInsertDbRecords(int noOfRecords) {
        Log.d("UtilityReceiver", "handleInsertDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);
        if (mIsDefaultEngineSelected) {
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "custom_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL3, newStr);

                long rowId = mDefaultDb.insert(DefaultEngineDatabase.TABLE_ABC, null, contentValues);
                if (rowId < 0) {
                    Log.d("UtilityReceiver", "failed to insert data!");
                } else {
                    Log.d("UtilityReceiver", "inserted " + rowId);
                }
            }
            Log.d("UtilityReceiver", "inserted show tost");
            //Toast.makeText(mContext, noOfRecords + " rows has been inserted from table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "default_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL3, newStr);

                long rowId = mCustomDb.insert(CustomEngineDatabase.TABLE_ABC, null, contentValues);
                if (rowId < 0) {
                    Log.d("UtilityReceiver", "failed to insert data!");
                } else {
                    Log.d("UtilityReceiver", "inserted " + rowId);
                }
            }
            Log.d("UtilityReceiver", "inserted show tost");
            //Toast.makeText(mContext, noOfRecords + " rows has been inserted to table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }

    }

    private void handleDeleteDbRecords(int noOfRecords) {
        Log.d("UtilityReceiver", "handleDeleteDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);

        int[] rowId = new int[noOfRecords];

        if (mIsDefaultEngineSelected) {

            //getting first N rows for update
            String[] colms = {DefaultEngineDatabase.TABLE_ABC_COL_ID};
            Cursor cursor = mDefaultDb.query(DefaultEngineDatabase.TABLE_ABC, colms, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int row = 0;
                while (cursor.isAfterLast() == false && row < noOfRecords) {
                    rowId[row] = cursor.getInt(0);
                    row++;
                    cursor.moveToNext();
                }
                cursor.close();
            }

            // updating fist N rows
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < noOfRecords; i++) {
                String whereClause = DefaultEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mDefaultDb.delete(DefaultEngineDatabase.TABLE_ABC, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityReceiver", "failed to update data!");
                }
            }
            //Toast.makeText(mContext, noOfRecords + " rows has been deleted from table: " + DefaultEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        } else {

            //getting first N rows for update
            String[] colms = {CustomEngineDatabase.TABLE_ABC_COL_ID};
            Cursor cursor = mCustomDb.query(CustomEngineDatabase.TABLE_ABC, colms, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int row = 0;
                while (cursor.isAfterLast() == false && row < noOfRecords) {
                    rowId[row] = cursor.getInt(0);
                    row++;
                    cursor.moveToNext();
                }
                cursor.close();
            }

            // updating fist N rows
            long currentTime = System.currentTimeMillis();
            for (int i = 0; i < noOfRecords; i++) {

                String whereClause = CustomEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mCustomDb.delete(CustomEngineDatabase.TABLE_ABC, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityReceiver", "failed to update data!");
                }
            }
            //Toast.makeText(mContext, noOfRecords + " rows has been deleted from table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }
    }
}
