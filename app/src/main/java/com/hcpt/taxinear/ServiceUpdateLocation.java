package com.hcpt.taxinear;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.service.GPSOnlyTracker;
import com.hcpt.taxinear.service.GPSTracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 11/18/2016.
 */

public class ServiceUpdateLocation extends Service {
    public static String ACTION = "SEND_LOCATION_FROM_SERVICE";
    public static String LOCATION_LAT_LAST = "LOCATION_LAT_LAST";
    public static String LOCATION_LONG_LAST = "LOCATION_LONG_LAST";
    GPSTracker gpsTracker;

    Handler handler = new Handler();

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (gpsTracker == null) {
                gpsTracker = new GPSTracker(getApplicationContext());
            }

            if (gpsTracker.canGetLocation()) {
                if (PreferencesManager.getInstance(getApplicationContext()).isDriver()) {
                    if (PreferencesManager.getInstance(getApplicationContext()).driverIsOnline()) {
                        updateCoornidate(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    }
                }
            }
            handler.postDelayed(runnable, 30000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        gpsTracker = new GPSTracker(getApplicationContext());
        if (gpsTracker.canGetLocation()) {
            updateCoornidate(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        }
        handler.postDelayed(runnable, 5000);
        return START_NOT_STICKY;
    }

    public void updateCoornidate(final double latitude, final double longtitude) {
        ModelManager.updateCoordinate(PreferencesManager.getInstance(this).getToken(), latitude + "", longtitude + "", this, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
//                if (!PreferencesManager.getInstance(getApplicationContext()).getCloseService().equals("0")) {
//                    gpsTracker.stopSelf();
//                    gpsTracker = new GPSTracker(getApplicationContext());
//                    if (gpsTracker.canGetLocation()) {
//                        updateCoornidate(gpsTracker.getLatitude(), gpsTracker.getLongitude());
//                        Log.e("distance", "distance test : " + gpsTracker.getLatitude() + "-" + gpsTracker.getLongitude() + "======");
//                    }
//                }

            }
        });
    }

    @Override
    public void onDestroy() {
        if (gpsTracker != null){
            gpsTracker.stopSelf();
        }
        this.stopSelf();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (gpsTracker != null){
            gpsTracker.stopSelf();
        }
        this.stopSelf();
        handler.removeCallbacks(runnable);

        super.onTaskRemoved(rootIntent);
    }
}
