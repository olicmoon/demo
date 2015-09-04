package com.samsung.sdpdemo.license;

import android.app.ProgressDialog;

import com.samsung.sdpdemo.EngineChooserActivity;
import com.samsung.sdpdemo.dbutil.UtilityService;
import com.sec.sdp.SdpFileSystem;
import com.sec.sdp.SdpLicenseManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.samsung.sdpdemo.R;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LicenseValidatorActivity extends ActionBarActivity implements View.OnClickListener {
    private Button activateLicenseBtn = null;
    private LicenseStateReceiver mReceiver = null;
    private Context mContext = null;
    private ProgressDialog mProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Log.d("sarang", "################################### " + getPackageName());
        setContentView(R.layout.activity_license_validator);

        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        boolean isLicenseValidated = preferences.getBoolean("isLicenseValidated", false);
        if (isLicenseValidated) {
            Intent serviceIntent = new Intent(this, UtilityService.class);
            startService(serviceIntent);

//            Utils.dumpEngineInfo(this, Constants.ALIAS);
//            Utils.dumpEngineInfo(this, null);
            SdpFileSystem fileSystem = null;
            try {
                fileSystem = new SdpFileSystem(this, null);
            } catch (SdpEngineNotExistsException e) {
                e.printStackTrace();
            } catch (SdpAccessDeniedException e) {
                e.printStackTrace();
            }

            fileSystem.getExternalStorageDirectory();

            startEngineChooserActivity();
        } else {
            activateLicenseBtn = (Button) findViewById(R.id.activate_license_btn);
            activateLicenseBtn.setOnClickListener(this);

            Button skipBtn = (Button) findViewById(R.id.skip_btn);
            skipBtn.setOnClickListener(this);

            mReceiver = new LicenseStateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SdpLicenseManager.ACTION_LICENSE_STATUS);
            registerReceiver(mReceiver, intentFilter);
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.activate_license_btn) {
            mProgressDialog = ProgressDialog.show(mContext, "Please wait", "Validating license.", true, false);
            SdpLicenseManager enterpriseLicenseManager = SdpLicenseManager.getInstance(mContext);
            String elmKey = getElmKey();
            if (elmKey == null) {
                Toast.makeText(mContext, "License key file not fount.. Please push license.txt to SDCARD!", Toast.LENGTH_SHORT).show();
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            } else {
                enterpriseLicenseManager.activateLicense(elmKey, getPackageName());
            }
        } else if (view.getId() == R.id.skip_btn) {
            Toast.makeText(mContext, "For negative test cases only!!", Toast.LENGTH_SHORT).show();
            startEngineChooserActivity();
        }
    }

    private class LicenseStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle extras = intent.getExtras();
            String status = extras
                    .getString(SdpLicenseManager.EXTRA_LICENSE_STATUS);
            int errocode = extras
                    .getInt(SdpLicenseManager.EXTRA_LICENSE_ERROR_CODE, -1);
            Toast toast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
            toast.setText(status + " errocode = " + errocode);
            toast.show();

            if (errocode == SdpLicenseManager.ERROR_NONE) {
                Toast.makeText(mContext, "License validation sucessful.", Toast.LENGTH_SHORT).show();

                Intent serviceIntent = new Intent(LicenseValidatorActivity.this, UtilityService.class);
                startService(serviceIntent);

                SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
                preferences.edit().putBoolean("isLicenseValidated", true).commit();
                startEngineChooserActivity();

                SdpFileSystem fileSystem = null;
                try {
                    fileSystem = new SdpFileSystem(LicenseValidatorActivity.this, null);
                    fileSystem.getExternalStorageDirectory();
                } catch (SdpEngineNotExistsException e) {
                    e.printStackTrace();
                } catch (SdpAccessDeniedException e) {
                    e.printStackTrace();
                }



//                Utils.dumpEngineInfo(LicenseValidatorActivity.this, Constants.ALIAS);
//                Utils.dumpEngineInfo(LicenseValidatorActivity.this, null);

            } else {
                Toast.makeText(mContext, "License validation failed!", Toast.LENGTH_SHORT).show();
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
        }
    }

    private void startEngineChooserActivity() {
        Intent intent = new Intent(this, EngineChooserActivity.class);
        startActivity(intent);
        finish();
    }

    private String getElmKey() {
        String elmKey = null;
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "license.txt";
            Log.d("sarang ", " GetElmKey path === " + path);
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            if ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            fileReader.close();
            elmKey = stringBuffer.toString();
            System.out.println("Contents of file:");
            System.out.println(elmKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elmKey;
    }

}
