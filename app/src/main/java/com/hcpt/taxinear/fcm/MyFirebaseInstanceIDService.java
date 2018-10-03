package com.hcpt.taxinear.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        saveToken(token);
    }

    private void saveToken(String token) {
        if (!MyGcmSharedPrefrences.getToken(this).equals(token)) {
            MyGcmSharedPrefrences.saveToken(MyFirebaseInstanceIDService.this, token);
        }
    }
}
