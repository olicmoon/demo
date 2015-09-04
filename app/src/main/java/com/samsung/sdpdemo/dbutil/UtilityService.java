package com.samsung.sdpdemo.dbutil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.samsung.sdpdemo.DefaultEngineDatabase;
import com.sec.sdp.SdpDatabase;
import com.sec.sdp.SdpStateListener;
import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngineConstants;

public class UtilityService extends Service {
    private StateListener mStateListener = null;

    public UtilityService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UtilityService", "onStartCommand");

        SdpUtil sdpUtil = SdpUtil.getInstance();
        mStateListener = new StateListener();
        try {
            Log.d("UtilityService", "registerListener for defualt engine.. " + SdpUtil.getInstance().getEngineInfo(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.dumpEngineInfo(UtilityService.this, null);
        sdpUtil.registerListener(null, mStateListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("UtilityService", "onDestroy calliing unregisterListener..");
        SdpUtil sdpUtil = SdpUtil.getInstance();
        sdpUtil.unregisterListener(null, mStateListener);
    }

    private class StateListener extends SdpStateListener {

        @Override
        public void onEngineRemoved() {
            super.onEngineRemoved();
            Log.d("UtilityService", "onEngineRemoved");
        }

        @Override
        public void onStateChange(int state) {
            super.onStateChange(state);

            SharedPreferences preferences = getSharedPreferences("sdp_demo", Context.MODE_PRIVATE);
            boolean isDummyDataPresent = preferences.getBoolean("dummyDataPresent_def", false);
            if (isDummyDataPresent == false) {
                Log.d("UtilityService", "onStateChange SENSITIVE DATABASE NOT PRESENT!");
                return;
            }

            Log.d("UtilityService", "onStateChange " + state);

            Utils.dumpEngineInfo(UtilityService.this, null);
            try {
                if (state == SdpEngineConstants.State.LOCKED) {
                    SdpUtil sdpUtil = SdpUtil.getInstance();
                    SdpDatabase sdpDatabase = new SdpDatabase(null);
                    DefaultEngineDatabase defaultEngineDatabase = new DefaultEngineDatabase(UtilityService.this);
                    SQLiteDatabase db = defaultEngineDatabase.openDatabase();
                    Log.d("UtilityService", "onStateChange LOCKED ");
                    sdpDatabase.updateStateToDB(db, state);
                } else if (state == SdpEngineConstants.State.UNLOCKED) {
                    SdpDatabase sdpDatabase = new SdpDatabase(null);
                    DefaultEngineDatabase defaultEngineDatabase = new DefaultEngineDatabase(UtilityService.this);
                    SQLiteDatabase db = defaultEngineDatabase.openDatabase();
                    Log.d("UtilityService", "onStateChange UNLOCKED ");
                    sdpDatabase.updateStateToDB(db, state);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
