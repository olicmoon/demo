package com.samsung.sdpdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.sdpdemo.dbutil.Utils;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpEngineInfo;

import java.util.ArrayList;

/**
 * Created by sarang on 4/13/15.
 */
public class DbResultActivity extends ActionBarActivity {

    private TableLayout layout = null;
    private CustomEngineDatabase mCustomEngineDatabase;
    private DefaultEngineDatabase mDefaultEngineDatabase = null;
    private static final String TAG = "DbResultActivity";
    private boolean mIsDefaultEngine = false;
    private static final int COLUMN_WIDTH = 120;

    private float convertDpToPixel(float dp) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    private String mEngineAlias = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        mIsDefaultEngine = getIntent().getBooleanExtra(Constants.INTENT_EXTRAS_IS_DEFAULT_ENGINE, false);
        mEngineAlias = getIntent().getStringExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS);
        mCustomEngineDatabase = new CustomEngineDatabase(this, mEngineAlias);
        mDefaultEngineDatabase = new DefaultEngineDatabase(this);

        Utils.resetTimer(this);

        TextView textView = (TextView) findViewById(R.id.engine_type_tv);
        TextView columnsTv = (TextView) findViewById(R.id.sen_col_tv);
        if (mIsDefaultEngine) {
            textView.setText("Engine type: DEFAULT");
        } else {
            textView.setText("Engine type: CUSTOM");
        }
        columnsTv.setText("Sensitive columns: col1 & col2");
        layout = (TableLayout) findViewById(R.id.resultTable);

        ArrayList<DbRow> rowList = dumpDatabaseRecords();
        if (rowList == null || rowList.size() == 0) {

        } else {
            TableRow row = new TableRow(this);
            row.setPadding(15, 15, 15, 15);

            TextView id = new TextView(this);
            id.setText("Id");
            id.setPadding(15, 15, 15, 15);
            id.setTypeface(null, Typeface.BOLD);
            id.setBackgroundResource(R.drawable.shape_col);

            TextView col1 = new TextView(this);
            col1.setText("col1");
            col1.setPadding(15, 15, 15, 15);
            col1.setTypeface(null, Typeface.BOLD);
            col1.setBackgroundResource(R.drawable.shape_col);

            TextView col2 = new TextView(this);
            col2.setText("col2");
            col2.setPadding(15, 15, 15, 15);
            col2.setTypeface(null, Typeface.BOLD);
            col2.setBackgroundResource(R.drawable.shape_col);

            TextView col3 = new TextView(this);
            col3.setText("col3");
            col3.setPadding(15, 15, 15, 15);
            col3.setTypeface(null, Typeface.BOLD);
            col3.setBackgroundResource(R.drawable.shape_col);

            row.addView(id);
            row.addView(col1);
            row.addView(col2);
            row.addView(col3);
            layout.addView(row);


            for (int i = 0; i < rowList.size(); i++) {
                DbRow rowRecord = rowList.get(i);
                Log.d("SDP ", " rowRecord " + rowRecord.toString());
                row = new TableRow(this);
                row.setPadding(15, 15, 15, 15);

                id = new TextView(this);
                id.setText(rowRecord.id);
                id.setPadding(15, 15, 15, 15);
                id.setBackgroundResource(R.drawable.shape_col);

                col1 = new TextView(this);
                col1.setText(rowRecord.col1);
                col1.setPadding(15, 15, 15, 15);
                col1.setLines(1);
                col1.setWidth((int) convertDpToPixel(COLUMN_WIDTH));
                col1.setBackgroundResource(R.drawable.shape_col);

                col2 = new TextView(this);
                col2.setText(rowRecord.col2);
                col2.setPadding(15, 15, 15, 15);
                col2.setLines(1);
                col2.setWidth((int) convertDpToPixel(COLUMN_WIDTH));
                col2.setBackgroundResource(R.drawable.shape_col);

                col3 = new TextView(this);
                col3.setText(rowRecord.col3);
                col3.setPadding(15, 15, 15, 15);
                col3.setLines(1);
                col3.setWidth((int) convertDpToPixel(COLUMN_WIDTH));
                col3.setBackgroundResource(R.drawable.shape_col);

                row.addView(id);
                row.addView(col1);
                row.addView(col2);
                row.addView(col3);
                layout.addView(row);
            }
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Utils.resetTimer(this);
        }

        return super.onTouchEvent(event);
    }

    public ArrayList<DbRow> dumpDatabaseRecords() {
        Log.d(TAG, "inside dumpDatabaseRecords... ");
        Cursor cursor;
        if (mIsDefaultEngine) {
            cursor = mDefaultEngineDatabase.getDbRecords();
        } else {
            cursor = mCustomEngineDatabase.getDbRecords();
        }
        ArrayList<DbRow> rowList = new ArrayList<DbRow>();
        if (cursor == null) {
            Toast.makeText(this, "No Data Found!!!", Toast.LENGTH_LONG).show();
        } else {

            boolean isSensitiveSet = false;
            SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            if (mIsDefaultEngine) {
                isSensitiveSet = preferences.getBoolean("setSensitive_def", true);
            } else {
                isSensitiveSet = preferences.getBoolean("setSensitive_custom", true);
            }
            Log.d(TAG, "found records... isSensitiveSet: " + isSensitiveSet);
            boolean isLocked = false;
            SdpEngineInfo info = null;
            try {
                if (mIsDefaultEngine)
                    info = SdpUtil.getInstance().getEngineInfo(null);
                else
                    info = SdpUtil.getInstance().getEngineInfo(Constants.ALIAS);
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

            while (cursor.isAfterLast() == false) {
                DbRow row = new DbRow();

                if (isSensitiveSet || isLocked) {
                    Log.d(TAG, "dumpDatabaseRecords getting blob... ");
                    byte[] bdata = cursor.getBlob(cursor.getColumnIndex("col1"));
                    temp = new String(bdata);

                    bdata = cursor.getBlob(cursor.getColumnIndex("col2"));
                    temp2 = new String(bdata);

                } else {
                    Log.d(TAG, "dumpDatabaseRecords getting string... ");
                    temp = cursor.getString(cursor.getColumnIndex("col1"));
                    temp2 = cursor.getString(cursor.getColumnIndex("col2"));
                }
                temp3 = cursor.getString(cursor.getColumnIndex("col3"));
                row.id = "" + cursor.getInt(cursor.getColumnIndex("id"));

                row.col1 = temp;
                row.col2 = temp2;
                row.col3 = temp3;
                rowList.add(row);
                cursor.moveToNext();

            }
            cursor.close();
        }

        return rowList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCustomEngineDatabase != null) {
            mCustomEngineDatabase.closeDb();
        }
        if (mDefaultEngineDatabase != null) {
            mDefaultEngineDatabase.closeDb();
        }
    }
}
