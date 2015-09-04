package com.samsung.sdpdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.samsung.sdpdemo.dbutil.Utils;
import com.sec.sdp.SdpDatabase;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngine;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpEngineInfo;
import com.sec.sdp.exception.SdpEngineNotExistsException;
import com.sec.sdp.exception.SdpInternalException;
import com.sec.sdp.exception.SdpInvalidPasswordException;


public class LoginActivity extends ActionBarActivity implements View.OnClickListener {
    private EditText passwordEdt = null;
    private Button loginBtn = null;
    private Button removeBtn = null;
    private final static String TAG = "LoginActivity";
    private String mEngineAlias = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        passwordEdt = (EditText) findViewById(R.id.password_edt);
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        removeBtn = (Button) findViewById(R.id.remove_btn);
        removeBtn.setOnClickListener(this);
        boolean isLoginRequired = preferences.getBoolean("isLoginRequired", true);
        boolean needSetup = preferences.getBoolean("needSetup", true);
        /*If account/Engine is not configured, create one with password */
        SdpUtil sdpUtil = SdpUtil.getInstance();

        mEngineAlias = getIntent().getStringExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS);
        if (needSetup) {
            Intent i = new Intent(this, AccountSetup.class);
            i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, mEngineAlias);
            startActivity(i);
            finish();
        } else {
            Log.d("LoginActivity", "isLoginRequired " + isLoginRequired);
            SdpEngineInfo info = null;
            try {
                info = sdpUtil.getEngineInfo(mEngineAlias);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int engineState = SdpEngineConstants.State.LOCKED;
            if (info != null) {
                engineState = info.getState();
                if (engineState == SdpEngineConstants.State.LOCKED) {
                    preferences.edit().putBoolean("isLoginRequired", true).commit();
                    isLoginRequired = true;
                }
            }
            Log.d("Sarang", "LOginactivityu " + engineState + " info " + info);

            if (isLoginRequired == false) {
            /*If engine state is locked then only user needs to input password*/
                Log.d("LOginactivityu", "starting DashboardActivity ");
                Intent i = new Intent(this, DashboardActivity.class);
                i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, Constants.ALIAS);
                startActivity(i);
                finish();
            }
        }
    }

    private void handleLogin() {
        SdpEngine sdpEngine;
        boolean result = false;
        try {
            Intent i = getIntent();
            sdpEngine = SdpEngine.getInstance();
            sdpEngine.unlock(Constants.ALIAS, passwordEdt.getText().toString());

            generateDummyData();

            if (i != null && !i.getBooleanExtra(Constants.INTENT_EXTRAS_IS_TIMED_OUT, false)) {
                i = new Intent(this, DashboardActivity.class);
                i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, Constants.ALIAS);
                startActivity(i);
            }
            SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            preferences.edit().putBoolean("isLoginRequired", false).commit();
            Utils.dumpEngineInfo(this, Constants.ALIAS);
            SdpDatabase sdpDatabase = new SdpDatabase(Constants.ALIAS);
            CustomEngineDatabase customEngineDatabase = new CustomEngineDatabase(this, mEngineAlias);
            SQLiteDatabase db = customEngineDatabase.openDatabase();

            sdpDatabase.updateStateToDB(db, SdpEngineConstants.State.UNLOCKED);
            result = true;
            customEngineDatabase.closeDb();
            finish();
        } catch (SdpInternalException e) {
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (SdpInvalidPasswordException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!result) {
            Toast.makeText(this, "Login failed!!", Toast.LENGTH_SHORT).show();
            passwordEdt.setText("");
        }
    }

    private void handleRemoveEngine() {
        SdpEngine sdpEngine;
        boolean result = false;
        try {
            sdpEngine = SdpEngine.getInstance();
            sdpEngine.removeEngine(Constants.ALIAS);
            result = true;
        } catch (SdpInternalException e) {
            result = false;
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            result = false;
            e.printStackTrace();
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }

        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("needSetup", true).apply();
        preferences.edit().putBoolean("isLoginRequired", true).apply();
        preferences.edit().putBoolean("setSensitive_custom", false).apply();
        preferences.edit().putBoolean("dummyDataPresent_custom", false).apply();

        Intent intent = new Intent(this, AccountSetup.class);
        startActivity(intent);
        finish();
        if (result)
            Toast.makeText(this, "Engine removed successfully!!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_btn) {
            handleLogin();
        } else if (view.getId() == R.id.remove_btn) {
            handleRemoveEngine();
        }
    }

    public void generateDummyData() {

        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        boolean isDummyDataPresent = preferences.getBoolean("dummyDataPresent_custom", false);
        if (isDummyDataPresent == false) {
            CustomEngineDatabase mCustomEngineDatabase = new CustomEngineDatabase(this, mEngineAlias);

            Log.d(TAG, "inside generateDummyData creating CUSTOM db... ");

            mCustomEngineDatabase.createDB(CustomEngineDatabase.DBFILE);
            Log.d(TAG, "inside generateDummyData setting sensitive column for CustomEngineDatabase... ");
            mCustomEngineDatabase.setSensitive(CustomEngineDatabase.DBFILE);

            mCustomEngineDatabase.closeDb();

            preferences.edit().putBoolean("dummyDataPresent_custom", true).commit();
            preferences.edit().putBoolean("setSensitive_custom", true).commit();
        }
    }
}
