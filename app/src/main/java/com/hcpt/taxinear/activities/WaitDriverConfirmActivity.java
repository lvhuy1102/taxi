package com.hcpt.taxinear.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.RequestService;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.CurrentOrder;
import com.hcpt.taxinear.object.SettingObj;
import com.hcpt.taxinear.widget.TextViewRaleway;

import java.math.BigDecimal;

public class WaitDriverConfirmActivity extends BaseActivity implements
        OnClickListener {

    private final int SPLASH_DISPLAY_LENGHT = 60000;
    Button btnCancelTrip;
    private TextViewRaleway txtEstimateFare, txtCountDriver;

    // private Handler myHandler;
    // private Runnable myRunnable = new Runnable() {
    // @Override
    // public void run() {
    // cancelRequestByPassenger();
    // }
    // };
    private String exChangeCurrency;
    private double change_rate;

    @Override
    public void onResume() {
        if (preferencesManager.getPassengerCurrentScreen().equals("ConfirmActivity")) {
            gotoActivity(ConfirmActivity.class);
            finish();
        } else if (preferencesManager.getPassengerCurrentScreen().equals(
                "StartTripForPassengerActivity")) {
            gotoActivity(StartTripForPassengerActivity.class);
            finish();
        } else if (preferencesManager.getPassengerCurrentScreen().equals(
                "RateDriverActivity")) {
            gotoActivity(RateDriverActivity.class);
            finish();
        } else {
            if (preferencesManager.getStringValue("checkCancel") != null && preferencesManager.getStringValue("checkCancel").equals("1")) {
                preferencesManager.putStringValue("checkCancel", "");
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                preferencesManager.setPassengerWaitConfirm(true);
            }

        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // myHandler = new Handler();
        // myHandler.postDelayed(myRunnable, SPLASH_DISPLAY_LENGHT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // myHandler.removeCallbacks(myRunnable);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_driver_confirm);
        preferencesManager.putStringValue("checkCancel", "");
        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        change_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
        initWithoutHeader();
        initView();
        initControl();
        initLocalBroadcastManager();
        registerReceiver(finishActivity, new IntentFilter("com.hcpt.taxinear.FINISH"));
        registerReceiver(countDriver, new IntentFilter("com.hcpt.taxinear.COUNTDRIVER"));
        if (exChangeCurrency.equals(Constant.LKR)) {
            if (globalValue.getInstance().getEstimate_fare() != null) {
//            Double toBeTruncated = new Double(globalValue.getInstance().getEstimate_fare());
//            Double truncatedDouble = new BigDecimal(toBeTruncated).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//            txtEstimateFare.setText(getString(R.string.pointEstimated) + truncatedDouble + " KM");
                String price1 = globalValue.getInstance().getEstimate_fare().split("~")[0].trim();
                String km = globalValue.getInstance().getEstimate_fare().split("~")[1].trim();
                double price = Double.parseDouble(price1) * change_rate;
                txtEstimateFare.setText(" " + getString(R.string.lblCurrencyLKR)+ Math.floor(price) + " ~ "+ km);
            } else {
                if (preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate") != null && !preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate").equals("")) {
//                Double toBeTruncated = new Double(preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate"));
//                Double truncatedDouble = new BigDecimal(toBeTruncated).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//                txtEstimateFare.setText(getString(R.string.pointEstimated) + truncatedDouble + " KM");

                    String price1 = preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate").split("~")[0].trim();
                    String km = preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate").split("~")[1].trim();
                    double price = Double.parseDouble(price1) * change_rate;
                    txtEstimateFare.setText(" " + getString(R.string.lblCurrencyLKR)+ Math.floor(price) + " ~ "+ km);
                } else {

                    String price1 = globalValue.getInstance().getEstimate_fare().split("~")[0].trim();
                    String km = globalValue.getInstance().getEstimate_fare().split("~")[1].trim();
                    double price = Double.parseDouble(price1) * change_rate;

                    txtEstimateFare.setText(" " + getString(R.string.lblCurrencyLKR)+ Math.floor(price) + " ~ "+ km);
                    //txtEstimateFare.setText(" " + (Double.parseDouble(globalValue.getInstance().getEstimate_fare()) * change_rate));
                }

            }
        } else {
            if (globalValue.getInstance().getEstimate_fare() != null) {
//            Double toBeTruncated = new Double(globalValue.getInstance().getEstimate_fare());
//            Double truncatedDouble = new BigDecimal(toBeTruncated).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//            txtEstimateFare.setText(getString(R.string.pointEstimated) + truncatedDouble + " KM");
                txtEstimateFare.setText(" " + getString(R.string.lblCurrencyUSD) + globalValue.getInstance().getEstimate_fare());
            } else {
                if (preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate") != null && !preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate").equals("")) {
//                Double toBeTruncated = new Double(preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate"));
//                Double truncatedDouble = new BigDecimal(toBeTruncated).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//                txtEstimateFare.setText(getString(R.string.pointEstimated) + truncatedDouble + " KM");
                    txtEstimateFare.setText(" " + getString(R.string.lblCurrencyUSD) + preferencesManager.getInstance(WaitDriverConfirmActivity.this).getStringValue("estimate"));
                } else {
                    txtEstimateFare.setText(" " + getString(R.string.lblCurrencyUSD) + globalValue.getInstance().getEstimate_fare());
                }

            }
        }
        String msg = getString(R.string.msgCountDriver);
        SettingObj settingObj = preferencesManager.getDataSettings();
        Log.e("HUY",preferencesManager.getArrived()+preferencesManager.getDriverCurrentScreen()+preferencesManager.toString());
        msg = msg.replace("[a]", preferencesManager.getStringValue("countDriver"));
        msg = msg.replace("[b]", settingObj.getTime_to_send_request_again());
        txtCountDriver.setText(msg);

    }

    private void initView() {
        btnCancelTrip = (Button) findViewById(R.id.btnCancelTrip);
        txtEstimateFare = (TextViewRaleway) findViewById(R.id.txtEstimateFare);
        txtCountDriver = (TextViewRaleway) findViewById(R.id.txtCountDriver);
    }

    private void initControl() {
        btnCancelTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuitDialog();
            }
        });
    }

    private void initLocalBroadcastManager() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                new IntentFilter(Constant.ACTION_DRIVER_CONFIRM));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(Constant.KEY_DATA);
            CurrentOrder currentOrder = new CurrentOrder();
            currentOrder.setId(ParseJsonUtil
                    .parseOrderIdFromDriverConfirm(json));
            globalValue.setCurrentOrder(currentOrder);
            getTripDetail();
        }
    };

    private void getTripDetail() {
        ModelManager.showTripDetail(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), context, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            try {
                                stopService(new Intent(WaitDriverConfirmActivity.this, RequestService.class));
                            } catch (Exception e) {
                                Log.e("exception", "exption:" + e.getMessage());
                            }
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));
                            gotoActivity(ConfirmActivity.class);
                            finish();
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(R.string.message_have_some_error);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        unregisterReceiver(finishActivity);
        unregisterReceiver(countDriver);
        super.onDestroy();
        preferencesManager.putStringValue("checkCancel", "");
    }

    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_do_you_cancel)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                cancelRequestByPassenger();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private final BroadcastReceiver finishActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    private final BroadcastReceiver countDriver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = getString(R.string.msgCountDriver);
            SettingObj settingObj = preferencesManager.getDataSettings();
            msg = msg.replace("[a]", preferencesManager.getStringValue("countDriver"));
            msg = msg.replace("[b]", settingObj.getTime_to_send_request_again());
            txtCountDriver.setText(msg);
        }
    };

    private void cancelRequestByPassenger() {
        try {
            stopService(new Intent(WaitDriverConfirmActivity.this, RequestService.class));
        } catch (Exception e) {
            Log.e("exception", "exption:" + e.getMessage());
        }
        ModelManager.cancelRequestByPassenger(
                PreferencesManager.getInstance(context).getToken(), context,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            if (getPreviousActivityName().equals(
                                    MainActivity.class.getSimpleName())) {
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
                            } else {
                                gotoActivity(MainActivity.class);
                                finish();
                            }
                        } else {
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(R.string.message_have_some_error);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        showQuitDialog();
    }

    @Override
    public void onClick(View v) {

    }
}
