package com.samsung.sdpdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// SDP Engine
import com.sec.sdp.SdpDatabase;
import com.sec.sdp.SdpFileSystem;
import com.sec.sdp.engine.SdpDomain;
import com.sec.sdp.engine.SdpEngine;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpCreationParam;
import com.sec.sdp.engine.SdpCreationParamBuilder;
import com.sec.sdp.engine.SdpEngineInfo;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineExistsException;
import com.sec.sdp.exception.SdpEngineLockedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;
import com.sec.sdp.exception.SdpInternalException;
import com.sec.sdp.SdpErrno;
import com.sec.sdp.exception.SdpInvalidPasswordException;
import com.sec.sdp.exception.SdpInvalidResetTokenException;
import com.sec.sdp.exception.SdpNotSupportedException;

import java.io.File;
import java.util.List;

public class AccountSetup extends ActionBarActivity implements View.OnClickListener {

    private Context mContext = null;
    private EditText mEdtAlias = null;
    private EditText mEdtPassword = null;
    private EditText mEdtChamberPath = null;
    private EditText mEdtResetTkn = null;
    private Button mBtnCreate = null;
    private Button mBtnReset = null;

    private String mEngineAlias = "";

    private final static String TAG = "AccountSetup.truman";

    private static final int SHOW_DIALOG = 0;
    private static final int DISMISS_DIALOG = 1;
    private ProgressDialog progressDialog = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_DIALOG:
                    progressDialog = new ProgressDialog(AccountSetup.this);
                    progressDialog.setMessage("Creating Engine...");
                    progressDialog.setTitle("Please wait!");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    break;
                case DISMISS_DIALOG:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEngineAlias = getIntent().getStringExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS);

        setContentView(R.layout.activity_account_setup);
        Log.d(TAG, "onCreate()");
        initialize();
    }

    private int createSdpEngine() {
        Log.d(TAG, "sdpEngineCreation()");
        int result = SdpErrno.FAILED;

        if (mEdtPassword.getText() == null || mEdtResetTkn.getText() == null)
            return SdpErrno.FAILED;

        String password = mEdtPassword.getText().toString();
        String resetTkn = mEdtResetTkn.getText().toString();

        try {
            Log.d(TAG, "sdpEngineCreation() " + password + " " + resetTkn);
            SdpEngine.getInstance().addEngine(loadDefaultParam(), password, resetTkn);
            //generateDummyData();
            result = SdpErrno.SUCCESS;
        } catch (SdpInternalException e) {
            e.printStackTrace();
            result = SdpErrno.FAILED;
        } catch (Exception e) {
            e.printStackTrace();
            result = SdpErrno.FAILED;
        }
        Log.d(TAG, "sarang sdpEngineCreation() " + result);
        return result;

    }

//    public void generateDummyData() {
//
//        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
//        boolean isDummyDataPresent = preferences.getBoolean("dummyDataPresent_custom", false);
//        if (isDummyDataPresent == false) {
//            CustomEngineDatabase mCustomEngineDatabase = new CustomEngineDatabase(this);
//
//            Log.d(TAG, "inside generateDummyData creating CUSTOM db... ");
//
//            mCustomEngineDatabase.createDB(CustomEngineDatabase.DBFILE);
//            Log.d(TAG, "inside generateDummyData setting sensitive column for CustomEngineDatabase... ");
//            mCustomEngineDatabase.setSensitive(CustomEngineDatabase.DBFILE);
//
//            mCustomEngineDatabase.closeDb();
//
//            preferences.edit().putBoolean("dummyDataPresent_custom", true).commit();
//            preferences.edit().putBoolean("setSensitive_custom", true).commit();
//        }
//    }

    private SdpCreationParam loadDefaultParam() {
        int flag = SdpEngineConstants.Flags.SDP_MDFPP_DEFAULT;
//        if (mEdtResetTkn.getText().toString() != null && !mEdtResetTkn.getText().toString().isEmpty()) {
//            flag = SdpEngineConstants.Flags.SDP_MDFPP_DEFAULT;
//        }
        SdpCreationParamBuilder sdpCreationParamBuilder =
                new SdpCreationParamBuilder(mEngineAlias, flag);
        return sdpCreationParamBuilder.getParam();
    }

    private void initialize() {
        mContext = getApplicationContext();
        initViews();


    }

    private void initViews() {
        mEdtAlias = (EditText) findViewById(R.id.alias_edt);
        mEdtPassword = (EditText) findViewById(R.id.password_edt);
        mEdtChamberPath = (EditText) findViewById(R.id.chambar_edt);
        mEdtResetTkn = (EditText) findViewById(R.id.reset_tkn_edt);
        mEdtAlias.setText(mEngineAlias);
        mEdtAlias.setEnabled(false);
        mEdtPassword.setFocusable(true);
        mBtnCreate = (Button) findViewById(R.id.create_btn);
        mBtnReset = (Button) findViewById(R.id.reset_btn);
        mBtnCreate.setOnClickListener(this);
        mBtnReset.setOnClickListener(this);
    }

    private void resetViews() {
        mEdtAlias.setText("");
        mEdtPassword.setText("");
        mEdtChamberPath.setText("");
    }

    private void showToast(String msg) {
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(mContext, text, duration);
        toast.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_btn:
                asyncTask.execute(null, null, null);
                break;
            case R.id.reset_btn:
                resetViews();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            handler.sendEmptyMessage(SHOW_DIALOG);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return createSdpEngine();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.d("sarang ", "onpost excute " + result.intValue());
            if (result.intValue() == SdpErrno.SUCCESS) {
                showToast("Engine Creation Successful!!!");
                SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
                preferences.edit().putBoolean("isLoginRequired", true).apply();
                preferences.edit().putBoolean("needSetup", false).apply();
                Intent intent = new Intent(AccountSetup.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                showToast("Engine Creation Failed...");
            }

            handler.sendEmptyMessage(DISMISS_DIALOG);

        }
    };
}

