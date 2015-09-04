package com.samsung.sdpdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class EngineChooserActivity extends ActionBarActivity implements View.OnClickListener {
    private Button defaultEngineBtn = null;
    private Button customEngineBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(EngineChooserActivity.class.getName(), " onCreate()");
        setContentView(R.layout.activity_engine_chooser);

        defaultEngineBtn = (Button) findViewById(R.id.default_engine_btn);
        customEngineBtn = (Button) findViewById(R.id.custom_engine_bnt);
        defaultEngineBtn.setOnClickListener(this);
        customEngineBtn.setOnClickListener(this);
        generateDummyData();
    }

    public void generateDummyData() {

        SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
        boolean isDummyDataPresent = preferences.getBoolean("dummyDataPresent_def", false);
        if (isDummyDataPresent == false) {
            DefaultEngineDatabase mDefaultEngineDatabase = new DefaultEngineDatabase(this);

            Log.d("EngineChooserActivity", "inside generateDummyData creating DEFAULT db... ");

            mDefaultEngineDatabase.createDB(DefaultEngineDatabase.DBFILE);
            Log.d("EngineChooserActivity", "inside generateDummyData setting sensitive column for DefaultEngineDatabase... ");
            mDefaultEngineDatabase.setSensitive(DefaultEngineDatabase.DBFILE);
            mDefaultEngineDatabase.closeDb();
            preferences.edit().putBoolean("dummyDataPresent_def", true).commit();
            preferences.edit().putBoolean("setSensitive_def", true).commit();
        }
    }

    private void showCustomEngineScreens() {
        Intent i = new Intent(this, LoginActivity.class);
        i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, Constants.ALIAS);
        startActivity(i);
        //finish();
    }

    private void showDefaultEngineScreens() {
        Intent i = new Intent(this, DashboardActivity.class);
        i.putExtra(Constants.INTENT_EXTRAS_ENGINE_ALIAS, "");
        startActivity(i);
        //finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.custom_engine_bnt:
                showCustomEngineScreens();
                break;
            case R.id.default_engine_btn:
                showDefaultEngineScreens();
                break;
        }
    }
}
