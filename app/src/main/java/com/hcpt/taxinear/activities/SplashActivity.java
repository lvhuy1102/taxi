package com.hcpt.taxinear.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.ServiceUpdateLocation;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.SettingObj;
import com.hcpt.taxinear.service.GPSTracker;
import com.hcpt.taxinear.utility.NetworkUtil;
import com.splunk.mint.Mint;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class SplashActivity extends BaseActivity {

    private final int SPLASH_DISPLAY_LENGHT = 1000;
    Locale myLocale;
    private GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        preferencesManager.putStringValue(Constant.CURRENCY, Constant.LKR);
        Mint.initAndStartSession(this, "1790f892");
//        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
//                SplashActivity.class));
//        Intent intent = new Intent();
//        intent.setAction("com.htcp.taxinear.ACTION_REQUEST");
//        sendBroadcast(intent);
        if (preferencesManager.driverIsOnline()) {
            Intent intent1 = new Intent(this, ServiceUpdateLocation.class);
            startService(intent1);
        }
        // Registering BroadcastReceiver
//        if (AppUtil.checkPlayServices(this)) {
//            // Start IntentService to register this application with GCM.
//            Intent intent = new Intent(this, RegistrationIntentService.class);
//            startService(intent);
//        }

        NetworkUtil.enableStrictMode();
        // For Facebook
        getAppKeyHash();
        getLanguage();
    }

    public void getLanguage() {
        if (preferencesManager.isChinase()) {
            myLocale = new Locale("zh");
            preferencesManager.setIsChinese();
        } else {
            myLocale = new Locale("en");
            preferencesManager.setIsEnglish();
        }
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onResume() {
        super.onResume();
        /* CLEAR PUSH NOTIFICATION */
        NotificationManager notifManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        /*Check network & GPS*/
        gps = new GPSTracker(this);
        checkBaseCondition();
    }

    private void checkBaseCondition() {
        if (NetworkUtil.checkNetworkAvailable(this)) {

            if (!gps.canGetLocation()) {
                gps.showSettingsAlert();
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        processNextScreen();
                    }
                }, SPLASH_DISPLAY_LENGHT);
            }
        } else {
            showWifiSetting(this);
        }
    }

    public void showWifiSetting(final Context act) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);

        // Setting Dialog Title
        alertDialog.setTitle("Wifi is settings");

        // Setting Dialog Message
        alertDialog
                .setMessage("Wifi is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_SETTINGS);
                        act.startActivity(intent);
                        dialog.dismiss();
                    }
                });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


    private void processNextScreen() {
        // Check user login or not
//        if ((preferencesManager.isAlreadyLogin())) {
//            firstLogin();
//        } else {
//            firstLogin();
//            gotoActivity(LoginActivity.class);
//            finish();
//        }

//        if ((preferencesManager.isAlreadyLogin())) {
//            preferencesManager.putStringValue(Constant.CURRENCY, Constant.USD);
//        }
        firstLogin();
    }

    // Start code check for user
    public void firstLogin() {
        generalSettings();
    }

    public void generalSettings() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            Log.e("JSON", json);
                            SettingObj settingObj = ParseJsonUtil.getGeneralSettings(json);
                            preferencesManager.setDataSetting(settingObj);
                            ModelManager.showInfoProfile(preferencesManager.getToken(), self, true, new ModelManagerListener() {
                                @Override
                                public void onSuccess(String json) {
                                    globalValue.setUser(ParseJsonUtil.parseInfoProfile(json));
                                    if (globalValue.getUser() != null && globalValue.getUser().getIsActive() != null && globalValue.getUser().getIsActive().equals("1")) {
                                        if (ParseJsonUtil.isSuccess(json)) {
                                            if (ParseJsonUtil.isDriverFromSplash(json)) {
                                                preferencesManager.setIsDriver();
                                                // If is driver check active or not
                                                if (ParseJsonUtil.driverIsActiveFromSplash(json)) {
                                                    preferencesManager.setDriverIsActive();
                                                } else {
                                                    preferencesManager.setDriverIsUnActive();
                                                }
                                            } else {
                                                preferencesManager.setIsUser();
                                            }

                                            // Check is normal user or driver user
                                            if (preferencesManager.isDriver()) {
                                                checkDriverOnline();
                                            } else {
                                                checkUserWithOutApi();
                                            }
                                        } else {
                                            preferencesManager.setLogout();
                                            gotoActivity(LoginActivity.class);
                                            finish();
                                        }
                                    } else {
                                        showToastMessage(getString(R.string.msgAccountInActive));
                                        preferencesManager.setLogout();
                                        gotoActivity(LoginActivity.class);
                                        finish();
                                    }

                                }

                                @Override
                                public void onError() {
                                    preferencesManager.setLogout();
                                    gotoActivity(LoginActivity.class);
                                    finish();
                                }
                            });
                        } else {
                            preferencesManager.setLogout();
                            gotoActivity(LoginActivity.class);
                            finish();
                        }
                    }

                    @Override
                    public void onError() {
                        preferencesManager.setLogout();
                        gotoActivity(LoginActivity.class);
                        finish();
                    }
                });
    }

    // Start code check for user
    public void checkUserWithOutApi() {
        Log.e("token check", "token check:" + preferencesManager.getToken());
        ModelManager.showMyRequestForUser(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e("json data", "json data checkUserWithOutApi:" + json);
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (ParseJsonUtil.parseMyRequest(json).size() > 0) {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(WaitDriverConfirmActivity.class);
                                finish();
                            } else {
                                checkUserInTrip();
                            }
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

    // Start code check for Driver
    public void checkDriverOnline() {
        if (globalValue.getUser().getDriverObj().getIsOnline()
                .equals(Constant.STATUS_IDLE)) {
            checkDriverInTrip();
        } else {
            checkUserWithOutApi();
        }
    }

    private void checkDriverInTrip() {
        ModelManager.showTripHistory(preferencesManager.getToken(), "1", self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {
                Log.e("json data", "json data in trip:" + json);
                if (ParseJsonUtil.isSuccess(json)) {
                    String status = ParseJsonUtil.parseTripStatus(json);
                    String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                    preferencesManager.setCurrentOrderId(ParseJsonUtil
                            .parseTripId(json));
                    if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                        gotoActivity(Ac_ConfirmPayByCash.class);
                    } else {
                        if (globalValue.getUser().getDriverObj().getStatus()
                                .equals(Constant.STATUS_IDLE)) {
                            countMyRequest();
                        } else {
                            if (globalValue.getUser().getDriverObj().getStatus().equals(Constant.STATUS_BUSY)) {
                                switch (status) {
                                    case Constant.TRIP_STATUS_APPROACHING:
                                        preferencesManager.setStartWithOutMain(true);
                                        preferencesManager
                                                .setDriverCurrentScreen(ShowPassengerActivity.class
                                                        .getSimpleName());
                                        gotoActivity(ShowPassengerActivity.class);
                                        finish();
                                        break;
                                    case Constant.TRIP_STATUS_IN_PROGRESS:
                                        preferencesManager.setDriverStartTrip(true);
                                        preferencesManager.setStartWithOutMain(true);
                                        preferencesManager
                                                .setDriverCurrentScreen(StartTripForDriverActivity.class
                                                        .getSimpleName());
                                        gotoActivity(StartTripForDriverActivity.class);
                                        finish();
                                        break;
                                    case Constant.TRIP_STATUS_ARRIVED:
                                        preferencesManager.setStartWithOutMain(true);
                                        preferencesManager
                                                .setDriverCurrentScreen(StartTripForDriverActivity.class
                                                        .getSimpleName());
                                        gotoActivity(StartTripForDriverActivity.class);
                                        finish();
                                        break;
                                    default:
                                        if (globalValue.getUser() != null && globalValue.getUser().getPhone() != null && !globalValue.getUser().getPhone().equals("")) {
                                            gotoActivity(MainActivity.class);
                                            finish();
                                        } else {
                                            gotoActivity(EditProfileFirstActivity.class);
                                            finish();
                                        }
                                        break;
                                }
                            }
                        }
                    }
                } else {
                    showToastMessage(ParseJsonUtil.getMessage(json));
                }
            }

            @Override
            public void onError() {
                showToastMessage(getResources().getString(
                        R.string.message_have_some_error));
            }
        });
    }

    private void checkUserInTrip() {
        ModelManager.showTripHistory(preferencesManager.getToken(), "1", self, true, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    String status = ParseJsonUtil.parseTripStatus(json);
                    preferencesManager.setCurrentOrderId(ParseJsonUtil
                            .parseTripId(json));
                    String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                    Log.e("json data", "json data:" + status);
                    if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                        preferencesManager.setPassengerCurrentScreen("");
                        gotoActivity(MainActivity.class);
                        finish();
                    } else {
                        switch (status) {
                            case Constant.TRIP_STATUS_APPROACHING:
                                preferencesManager.setStartWithOutMain(true);
                                preferencesManager.setPassengerCurrentScreen(ConfirmActivity.class.getSimpleName());
                                gotoActivity(ConfirmActivity.class);
                                finish();
                                break;
                            case Constant.TRIP_STATUS_ARRIVED:
                                preferencesManager.setStartWithOutMain(true);
                                preferencesManager.setPassengerCurrentScreen(ConfirmActivity.class.getSimpleName());
                                gotoActivity(ConfirmActivity.class);
                                finish();
                                break;
                            case Constant.TRIP_STATUS_IN_PROGRESS:
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(StartTripForPassengerActivity.class);
                                finish();
                                break;
                            case Constant.TRIP_STATUS_PENDING_PAYMENT:
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(RateDriverActivity.class);
                                finish();
                                break;
                            default:
                                preferencesManager.setPassengerCurrentScreen("");
                                gotoActivity(MainActivity.class);
                                finish();
                                break;
                        }
                    }
                } else {
                    showToastMessage(ParseJsonUtil.getMessage(json));
                }
            }

            @Override
            public void onError() {
                showToastMessage(getResources().getString(
                        R.string.message_have_some_error));
            }
        });
    }

    private void countMyRequest() {
        ModelManager.showMyRequest(preferencesManager.getToken(), self, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (ParseJsonUtil.parseMyRequest(json).size() > 0) {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(RequestPassengerActivity.class);
                                finish();
                            } else {
                                preferencesManager.setStartWithOutMain(true);
                                gotoActivity(OnlineActivity.class);
                                finish();
                            }
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }


    private void showDialogEnableNetwork() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_check_net_work)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                Intent intent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                onBackPressed();
                            }
                        }).create().show();
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.hcpt.taxinear", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("Hash key", "HASH KEY : " + something);
            }
        } catch (NameNotFoundException e1) {

        } catch (NoSuchAlgorithmException e) {

        } catch (Exception e) {
        }

    }
}
