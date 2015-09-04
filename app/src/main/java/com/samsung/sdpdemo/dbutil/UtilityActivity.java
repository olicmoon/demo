package com.samsung.sdpdemo.dbutil;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.sdpdemo.Constants;
import com.samsung.sdpdemo.CustomEngineDatabase;
import com.samsung.sdpdemo.DbResultActivity;
import com.samsung.sdpdemo.DefaultEngineDatabase;
import com.samsung.sdpdemo.IntentUsageActivity;
import com.samsung.sdpdemo.R;

import java.text.DateFormat;


public class UtilityActivity extends ActionBarActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private EditText dbInsertRowEdt = null;
    private EditText dbUpdateRowEdt = null;
    private EditText dbDeleteRowEdt = null;
    private EditText dbUpdateTextEdt = null;
    private Button dbUpdateBtn = null;
    private Button dbDeleteBtn = null;
    private Button dbInsertBtn = null;
    private RadioButton defaultEngineRadio = null;
    private RadioButton customEngineRadio = null;
    private boolean mIsDefaultEngineSelected = false;
    private SQLiteDatabase mCustomDb = null;
    private SQLiteDatabase mDefaultDb = null;
    private Button mShowDBRecordsBtn = null;
    private Button mShowIntentsBtn = null;

    private String mEngineAlias = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utility);

        DefaultEngineDatabase sensitiveDatabase = new DefaultEngineDatabase(this);
        mDefaultDb = sensitiveDatabase.openDatabase();

        CustomEngineDatabase sensitiveDatabase1 = new CustomEngineDatabase(this, mEngineAlias);
        mCustomDb = sensitiveDatabase1.openDatabase();

        TextView dbOperationTv = (TextView) findViewById(R.id.db_operations_tv);
        dbOperationTv.setText(Html.fromHtml("<b>DB operations:</b>  <small><i>Please not that only affect first N rows will be updated.</i></small>"));


        dbInsertRowEdt = (EditText) findViewById(R.id.insert_row_edt);
        dbUpdateRowEdt = (EditText) findViewById(R.id.update_row_edt);
        dbDeleteRowEdt = (EditText) findViewById(R.id.delete_row_edt);
        dbUpdateTextEdt = (EditText) findViewById(R.id.update_row_newtxt_edt);

        dbUpdateBtn = (Button) findViewById(R.id.update_row_btn);
        dbDeleteBtn = (Button) findViewById(R.id.delete_row_btn);
        dbInsertBtn = (Button) findViewById(R.id.insert_row_btn);
        mShowIntentsBtn = (Button) findViewById(R.id.db_operations_show_db_records);
        mShowDBRecordsBtn = (Button) findViewById(R.id.db_operations_show_intents);
        mShowIntentsBtn.setOnClickListener(this);
        mShowDBRecordsBtn.setOnClickListener(this);
        dbUpdateBtn.setOnClickListener(this);
        dbDeleteBtn.setOnClickListener(this);
        dbInsertBtn.setOnClickListener(this);

        defaultEngineRadio = (RadioButton) findViewById(R.id.default_engine_radio_btn);
        customEngineRadio = (RadioButton) findViewById(R.id.custom_engine_radio_btn);
        defaultEngineRadio.setChecked(true);
        mIsDefaultEngineSelected = true;
        defaultEngineRadio.setOnCheckedChangeListener(this);
        customEngineRadio.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_row_btn:
                String noOfRecordsStr = dbUpdateRowEdt.getText().toString();
                String newString = dbUpdateTextEdt.getText().toString();
                int noOfRecords = 5;
                if (noOfRecordsStr != null && !noOfRecordsStr.isEmpty()) {
                    noOfRecords = Integer.parseInt(noOfRecordsStr);
                }
                handleUpdateDbRecords(noOfRecords);
                break;
            case R.id.delete_row_btn:
                noOfRecordsStr = dbDeleteRowEdt.getText().toString();
                noOfRecords = 5;
                if (noOfRecordsStr != null && !noOfRecordsStr.isEmpty()) {
                    noOfRecords = Integer.parseInt(noOfRecordsStr);
                }
                handleDeleteDbRecords(noOfRecords);
                break;
            case R.id.insert_row_btn:
                noOfRecordsStr = dbInsertRowEdt.getText().toString();
                noOfRecords = 5;
                if (noOfRecordsStr != null && !noOfRecordsStr.isEmpty()) {
                    noOfRecords = Integer.parseInt(noOfRecordsStr);
                }
                handleInsertDbRecords(noOfRecords);
                break;
            case R.id.db_operations_show_db_records:
                handleShowDbRecords();
                break;
            case R.id.db_operations_show_intents:
                handleShowIntents();
                break;
        }
    }

    private void handleShowDbRecords() {
        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        boolean isDummyDataPresent = false;
        if (!mIsDefaultEngineSelected) {
            isDummyDataPresent = preferences.getBoolean("dummyDataPresent_custom", false);
        } else {
            isDummyDataPresent = preferences.getBoolean("dummyDataPresent_def", false);
        }
        if (isDummyDataPresent) {
            Intent i = new Intent(this, DbResultActivity.class);
            i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, mEngineAlias);
            startActivity(i);
        } else {
            Toast.makeText(this, "No DB record present!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShowIntents() {
        Intent intent = new Intent(this, IntentUsageActivity.class);
        startActivity(intent);
    }

    private void handleUpdateDbRecords(int noOfRecords) {
        Log.d("UtilityActivity", "handleUpdateDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);
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
            String newStr = "default_" + DateFormat.getInstance().format(currentTime);
            if (dbUpdateTextEdt.getText() != null && dbUpdateTextEdt.getText().toString() != null) {
                newStr = newStr + dbUpdateTextEdt.getText().toString();
            }
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL3, newStr);
                String whereClause = DefaultEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mDefaultDb.update(DefaultEngineDatabase.TABLE_ABC, contentValues, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityActivity", "failed to update data!");
                }
            }
            Toast.makeText(this, noOfRecords + " rows has been updated to table: " + DefaultEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        } else {

            //getting first N rows for update
            String[] colms = {CustomEngineDatabase.TABLE_ABC_COL_ID};
            Cursor cursor = mCustomDb.query(CustomEngineDatabase.TABLE_ABC, colms, null, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int row = 0;
                while (cursor.isAfterLast() == false && row < noOfRecords) {
                    rowId[row] = cursor.getInt(0);
                    Log.d("UtilityActivity", "getting ID= " + rowId[row] + " row= " + row);
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
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL3, newStr);

                String whereClause = CustomEngineDatabase.TABLE_ABC_COL_ID + "=" + rowId[i];
                int rowsUpdated = mCustomDb.update(CustomEngineDatabase.TABLE_ABC, contentValues, whereClause, null);
                if (rowsUpdated < 0) {
                    Log.d("UtilityActivity", "failed to update data!");
                }
            }
            Toast.makeText(this, noOfRecords + " rows has been updated to table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }

    }

    private void handleInsertDbRecords(int noOfRecords) {
        Log.d("UtilityActivity", "handleInsertDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);
        if (mIsDefaultEngineSelected) {
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "default_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(DefaultEngineDatabase.TABLE_ABC_COL3, newStr);

                long rowId = mDefaultDb.insert(DefaultEngineDatabase.TABLE_ABC, null, contentValues);
                if (rowId < 0) {
                    Log.d("UtilityActivity", "failed to insert data!");
                } else {
                    Log.d("UtilityActivity", "inserted " + rowId);
                }
            }
            Log.d("UtilityActivity", "inserted show tost");
            Toast.makeText(this, noOfRecords + " rows has been inserted from table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        } else {
            ContentValues contentValues = new ContentValues();
            long currentTime = System.currentTimeMillis();
            String newStr = "custom_" + DateFormat.getInstance().format(currentTime);
            for (int i = 0; i < noOfRecords; i++) {
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL1, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL2, newStr);
                contentValues.put(CustomEngineDatabase.TABLE_ABC_COL3, newStr);

                long rowId = mCustomDb.insert(CustomEngineDatabase.TABLE_ABC, null, contentValues);
                if (rowId < 0) {
                    Log.d("UtilityActivity", "failed to insert data!");
                } else {
                    Log.d("UtilityActivity", "inserted " + rowId);
                }
            }
            Log.d("UtilityActivity", "inserted show tost");
            Toast.makeText(this, noOfRecords + " rows has been inserted to table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }

    }

    private void handleDeleteDbRecords(int noOfRecords) {
        Log.d("UtilityActivity", "handleDeleteDbRecords " + mIsDefaultEngineSelected + " " + noOfRecords);

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
                    Log.d("UtilityActivity", "failed to update data!");
                }
            }
            Toast.makeText(this, noOfRecords + " rows has been deleted from table: " + DefaultEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
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
                    Log.d("UtilityActivity", "failed to update data!");
                }
            }
            Toast.makeText(this, noOfRecords + " rows has been deleted from table: " + CustomEngineDatabase.TABLE_ABC, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (!isChecked) return;
        if (R.id.default_engine_radio_btn == compoundButton.getId()) {
            mIsDefaultEngineSelected = true;
            Toast.makeText(UtilityActivity.this, "Default Engine selected.", Toast.LENGTH_SHORT).show();
        } else {
            mIsDefaultEngineSelected = false;
            Toast.makeText(UtilityActivity.this, "Custom Engine selected.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCustomDb != null) mCustomDb.close();
        if (mDefaultDb != null) mDefaultDb.close();
    }
}
