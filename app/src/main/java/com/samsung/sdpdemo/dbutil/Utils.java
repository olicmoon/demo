package com.samsung.sdpdemo.dbutil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.sec.sdp.SdpUtil;
import com.sec.sdp.engine.SdpEngineInfo;
import com.sec.sdp.exception.SdpAccessDeniedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sarang on 6/3/15.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static void resetTimer(Context context) {
        killTimer(context);
        triggerTimer(context);
    }

    private static void triggerTimer(Context context) {
//
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(Constants.INTENT_TIMEOUT);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        long timeout = SystemClock.elapsedRealtime() + Constants.DEFAULT_TIMEOUT;
//        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeout, pendingIntent);
//        Log.d(TAG, "triggerTimer timeout= " + timeout);
    }

    private static void killTimer(Context context) {
//        Intent intent = new Intent(Constants.INTENT_TIMEOUT);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.cancel(pendingIntent);
//        Log.d(TAG, "killTimer DONE..");
    }

    public static void dumpEngineInfo(Context context, String alias) {

        try {
            SdpUtil sdpUtil = SdpUtil.getInstance();
            SdpEngineInfo info = sdpUtil.getEngineInfo(alias);
            if (info == null || (info.toString() == null)) {
                Log.d("Utils", "failed to update engineInfo " + info);
                return;
            }

            File engineInfoFile = new File(context.getFilesDir() + File.separator + info.getId() + "_engineInfo.txt");
            if (engineInfoFile == null) return;

            FileOutputStream fileOutputStream = new FileOutputStream(engineInfoFile);
            byte[] infoBytes = info.toString().getBytes();
            fileOutputStream.write(infoBytes);
            fileOutputStream.flush();
            Log.d("Utils", "engineInfo has been updated successfully.. " + info.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}

