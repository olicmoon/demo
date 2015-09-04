package com.samsung.sdpdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.samsung.sdpdemo.dbutil.Utils;
import com.sec.sdp.engine.SdpEngine;
import com.sec.sdp.exception.SdpAccessDeniedException;
import com.sec.sdp.exception.SdpEngineNotExistsException;
import com.sec.sdp.exception.SdpInternalException;

public class TimeOutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TimeOutReceiver", "onreceive TIMEOUT.. LOCKING SDP !! ");
        try {
            SdpEngine sdpEngine = SdpEngine.getInstance();
            sdpEngine.lock(Constants.ALIAS);
            SharedPreferences preferences = context.getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            preferences.edit().putBoolean("isLoginRequired", true).apply();
            Intent loginIntent = new Intent(context, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            loginIntent.putExtra(Constants.INTENT_EXTRAS_IS_TIMED_OUT, true);
            context.startActivity(loginIntent);
            Utils.dumpEngineInfo(context, Constants.ALIAS);
        } catch (SdpAccessDeniedException e) {
            e.printStackTrace();
        } catch (SdpEngineNotExistsException e) {
            e.printStackTrace();
        } catch (SdpInternalException e) {
            e.printStackTrace();
        }

    }
}
