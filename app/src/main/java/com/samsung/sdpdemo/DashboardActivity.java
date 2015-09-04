package com.samsung.sdpdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.samsung.sdpdemo.dbutil.Utils;
import com.samsung.sdpdemo.filebrowser.FileChooser;
import com.sec.sdp.SdpDatabase;
import com.sec.sdp.SdpFileSystem;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngine;
import com.sec.sdp.engine.SdpEngineConstants;
import com.sec.sdp.engine.SdpEngineInfo;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineLockedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;
import com.sec.sdp.exception.SdpInternalException;
import com.sec.sdp.exception.SdpInvalidPasswordException;

import java.io.File;


public class DashboardActivity extends ActionBarActivity implements View.OnClickListener {

    private Button dbBtn = null;
    private Button filesBtn = null;
    private Button lockBtn = null;
    private Button changePwdBtn = null;

    private String mEngineAlias = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEngineAlias =
                getIntent().getStringExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS);

        setContentView(R.layout.activity_dashboard);
        dbBtn = (Button) findViewById(R.id.db_btn);
        filesBtn = (Button) findViewById(R.id.file_btn);
        changePwdBtn = (Button) findViewById(R.id.change_pwd_btn);
        lockBtn = (Button) findViewById(R.id.lock_btn);
        if(SdpUtil.isAndroidDefaultAlias(mEngineAlias)) {
            changePwdBtn.setVisibility(View.GONE);
            lockBtn.setVisibility(View.GONE);
        } else {
            changePwdBtn.setOnClickListener(this);
            lockBtn.setOnClickListener(this);
        }

        filesBtn.setOnClickListener(this);
        dbBtn.setOnClickListener(this);
        Utils.resetTimer(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.db_btn:
                showDbRecords();
                break;
            case R.id.file_btn:
                showFileBrowser();
                break;
            case R.id.lock_btn:
                handleLock();
                break;
            case R.id.change_pwd_btn:
                showChangePwdDialog();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Utils.resetTimer(this);
        }

        return super.onTouchEvent(event);
    }

    private void handleChangePwd(String oldPwd, String newPwd) {
        SdpEngine sdpEngine;
        boolean result = false;

        try {
            sdpEngine = SdpEngine.getInstance();
            Log.d("DashboardActivity "," unlock "+oldPwd+" "+newPwd);
            sdpEngine.unlock(mEngineAlias, oldPwd);
            Log.d("DashboardActivity ", " calling  setPassword " + newPwd);
            sdpEngine.setPassword(mEngineAlias, newPwd);
            result = true;
        } catch (SdpAccessDeniedException e) {
            e.printStackTrace();
        } catch (SdpInternalException e) {
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (SdpEngineLockedException e) {
            e.printStackTrace();
        } catch (SdpInvalidPasswordException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result)
            Toast.makeText(this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Password Change Failed!", Toast.LENGTH_SHORT).show();
    }

    private void showChangePwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.activity_change_pwd, null);
        final EditText newPwdEdt = (EditText) view.findViewById(R.id.new_password_edt);
        final EditText oldPwdEdt = (EditText) view.findViewById(R.id.old_password_edt);
        builder.setView(view);
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newPwd = newPwdEdt.getText().toString();
                String oldPwd = oldPwdEdt.getText().toString();
                if (newPwd != null && newPwd.length() > 0 && oldPwd != null && oldPwd.length() > 0) {
                    handleChangePwd(oldPwd, newPwd);
                } else {
                    Toast.makeText(DashboardActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void handleLock() {
        SdpEngine sdpEngine = null;
        boolean result = false;
        try {
            sdpEngine = SdpEngine.getInstance();
            sdpEngine.lock(mEngineAlias);
            result = true;
            SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            preferences.edit().putBoolean("isLoginRequired", true).apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            SdpDatabase sdpDatabase = new SdpDatabase(mEngineAlias);
            CustomEngineDatabase customEngineDatabase = new CustomEngineDatabase(this, mEngineAlias);
            SQLiteDatabase db = customEngineDatabase.openDatabase();
            // only custom engine db needs to know state changes
            sdpDatabase.updateStateToDB(db, SdpEngineConstants.State.LOCKED);
            Utils.dumpEngineInfo(this, mEngineAlias);
            customEngineDatabase.closeDb();
            finish();
        } catch (SdpInternalException e) {
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result) Toast.makeText(this, "Locked Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showDbRecords() {
        Intent i = new Intent(this, DbResultActivity.class);
        i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, mEngineAlias);
        startActivity(i);
    }

    private static String FILES_BASE_DIR = null;

    private void showFileBrowser() {
        File filesDir = null;
        try {
            SdpFileSystem secureFileSystem = new SdpFileSystem(this, mEngineAlias);
            filesDir = secureFileSystem.getFilesDir();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("sarang", "mEngineAlias "+mEngineAlias);
        if (filesDir != null) {
            FILES_BASE_DIR = filesDir.getAbsolutePath();
        }
        Log.d("sarang", "showFilesDir "+filesDir+" "+FILES_BASE_DIR);
        Intent i = new Intent(this, FileChooser.class);
        i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, mEngineAlias);
        i.putExtra(Constants.INTENT_EXTRAS_BASE_DIR, FILES_BASE_DIR);
        startActivity(i);
    }
}
