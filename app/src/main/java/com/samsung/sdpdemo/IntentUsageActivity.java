package com.samsung.sdpdemo;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;


public class IntentUsageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_usage);
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/intent_use.html");
    }

}
