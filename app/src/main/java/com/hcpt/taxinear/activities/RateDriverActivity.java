package com.hcpt.taxinear.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.utility.NetworkUtil;
import com.hcpt.taxinear.widget.TextViewFontAwesome;
import com.hcpt.taxinear.widget.TextViewRaleway;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONObject;

import me.drakeet.materialdialog.MaterialDialog;

public class RateDriverActivity extends BaseActivity {
    private TextView btnSend, btnRate, lblPoint;
    private TextViewRaleway lblNorify;
    private String point;
    private TextViewRaleway lblName, tvSeat, lblCarPlate;

    private LinearLayout loPayment, loRate, llHelp;
    private TextViewFontAwesome stepNext, stepCircle;

    private TextViewRaleway lblPhone, tvStep;
    private RatingBar ratingBar, ratingBarUser;
    private TextViewRaleway lblStartLocation;
    private TextViewRaleway lblEndlocation;
    private TextView txtStar;

    private TextView tv_time_waitting, tv_excharage;


    private ImageView imgPassenger;
    //    private ImageView imgCar;
    private AQuery aq;
    private String phoneAdmin = "";
    private boolean isRate = false;

    private double exchange_rate;
    private String exChangeCurrency;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_trip);
        aq = new AQuery(self);
        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        exchange_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
        initUI();
        initData();
        initLocalBroadcastManager();
        initView();
        initControl();
//

//		if (preferencesManager.passengerIsHaveDonePayment()) {
//			showRate(true);
//		}

        // Starting PayPal service
        startPaypalService();
    }

    @Override
    public void onResume() {
        preferencesManager.setPassengerCurrentScreen(RateDriverActivity.class
                .getSimpleName());
        super.onResume();
    }

    ;

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void initData() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            phoneAdmin = ParseJsonUtil
                                    .getPhoneAdmin(json);
                        }
                    }

                    @Override
                    public void onError() {
                    }
                });
        if (!getPreviousActivityName().equals(
                StartTripForPassengerActivity.class.getName())) {
            ModelManager.showTripDetail(preferencesManager.getToken(),
                    preferencesManager.getCurrentOrderId(), context, true,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));

                                if (exChangeCurrency.equals(Constant.LKR)) {
                                    lblPoint.setText(getString(R.string.lblCurrencyLKR) + Math.floor(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) * exchange_rate));
                                    tv_excharage.setText(Math.floor((Double.parseDouble(globalValue.getCurrentOrder().getWaitFare()) * exchange_rate)) + getString(R.string.lblCurrencyLKR));
                                } else {
                                    lblPoint.setText(getString(R.string.lblCurrencyUSD) + globalValue.getCurrentOrder().getActualFare());
                                    tv_excharage.setText(globalValue.getCurrentOrder().getWaitFare() + getString(R.string.lblCurrencyUSD));
                                }
                                tv_time_waitting.setText(globalValue.getCurrentOrder().getTime_waitting() + "minutes");

                                tvSeat.setText(convertLinkToString(globalValue.getCurrentOrder().getLink() + ""));
                                lblPhone.setText(globalValue.getCurrentOrder().getDriver_phone(true));
                                lblName.setText(globalValue.getCurrentOrder().getDriverName());
                                lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());
                                lblStartLocation.setText(globalValue.getCurrentOrder().getStartLocation());
                                lblEndlocation.setText(globalValue.getCurrentOrder().getEndLocation());
                                if (globalValue.getCurrentOrder().getDriverRate().isEmpty()) {
                                    ratingBarUser.setRating(0);
                                    txtStar.setText("0");
                                } else {
                                    ratingBarUser.setRating(Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getDriverRate()) / 2);
                                    txtStar.setText("" + Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getDriverRate()) / 2);
                                }
//                                aq.id(imgCar).image(
//                                        globalValue.getCurrentOrder()
//                                                .getCarImage());
                                Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
                                        .getCarImage());
                                aq.id(imgPassenger).image(
                                        globalValue.getCurrentOrder()
                                                .getImageDriver());
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }

                        @Override
                        public void onError() {
                            showToastMessage(R.string.message_have_some_error);
                        }
                    });
        } else {
            lblPoint.setText(getString(R.string.lblCurrency) + globalValue.getCurrentOrder().getActualFare()
            );
            tvSeat.setText(convertLinkToString(globalValue.getCurrentOrder().getLink()));
            lblPhone.setText(globalValue.getCurrentOrder()
                    .getDriver_phone(false));
            lblName.setText(globalValue.getCurrentOrder()
                    .getDriverName());
            lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());
            lblStartLocation.setText(globalValue
                    .getCurrentOrder().getStartLocation());
            lblEndlocation.setText(globalValue
                    .getCurrentOrder().getEndLocation());
            if (globalValue.getCurrentOrder()
                    .getDriverRate().isEmpty()) {
                ratingBarUser.setRating(0);
            } else {
                ratingBarUser.setRating(Float
                        .parseFloat(globalValue
                                .getCurrentOrder()
                                .getDriverRate()) / 2);
            }
//            aq.id(imgCar).image(
//                    globalValue.getCurrentOrder()
//                            .getCarImage());
            Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
                    .getCarImage());
            aq.id(imgPassenger).image(
                    globalValue.getCurrentOrder()
                            .getImageDriver());
        }
    }

    public String convertLinkToString(String link) {
        switch (link) {
            case "I":
                return getString(R.string.sedan4);

            case "II":
                return getString(R.string.suv6);

            case "III":
                return getString(R.string.lux);
        }
        return link;
    }

    private void initView() {
        tv_excharage = findViewById(R.id.tv_excharage);
        tv_time_waitting = findViewById(R.id.tv_time_waitting);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnSend = (TextView) findViewById(R.id.btnSend);
        lblPoint = (TextView) findViewById(R.id.lblPoint);
        btnRate = (TextView) findViewById(R.id.btnRate);

        tvStep = (TextViewRaleway) findViewById(R.id.tv_step);
        //lblDrvierId = (TextViewRaleway) findViewById(R.id.lblDrvierId);
        lblPhone = (TextViewRaleway) findViewById(R.id.lblPhone);
        ratingBarUser = (RatingBar) findViewById(R.id.ratingBar_user);
        lblStartLocation = (TextViewRaleway) findViewById(R.id.lblStartLocation);
        lblEndlocation = (TextViewRaleway) findViewById(R.id.lblEndlocation);

        tvSeat = (TextViewRaleway) findViewById(R.id.tvSeat);
        lblName = (TextViewRaleway) findViewById(R.id.lblName);
        lblCarPlate = (TextViewRaleway) findViewById(R.id.lblCarPlate);

        stepNext = (TextViewFontAwesome) findViewById(R.id.step_full);
        stepCircle = (TextViewFontAwesome) findViewById(R.id.step_circle);
        txtStar = (TextView) findViewById(R.id.txtStar);

        loPayment = (LinearLayout) findViewById(R.id.lo_payment);
//		loRate = (LinearLayout) findViewById(R.id.lo_rate);

        imgPassenger = (ImageView) findViewById(R.id.imgPassenger);
//        imgCar = (ImageView) findViewById(R.id.imgCar);
        llHelp = (LinearLayout) findViewById(R.id.llHelp);
        lblNorify = (TextViewRaleway) findViewById(R.id.lblNorify);
    }

    private void initControl() {
        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRate) {
                    Toast.makeText(self, getString(R.string.rate_first), Toast.LENGTH_SHORT).show();
                } else {
                    btnSend.setEnabled(false);
                    showDialogPayment();
                }
            }
        });

        btnRate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                rate();
            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(RateDriverActivity.this)) {
                    ModelManager.sendNeedHelp(preferencesManager.getToken(), preferencesManager.getCurrentOrderId(), context, true, new ModelManagerListener() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(String json) {
                            Log.e("Success", "Success");
                        }
                    });
                }
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"
                        + phoneAdmin));
                startActivity(callIntent);
            }
        });
    }

    public void showDialogPayment() {
        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_payment, null, false);
        TextView byCash = (TextView) v.findViewById(R.id.btnPayCash);
        TextView byPaypal = (TextView) v.findViewById(R.id.btnPayPal);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(v)
                .create();

        byPaypal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                payment(Constant.PAY_TRIP_BY_BALANCE);
                alertDialog.dismiss();
            }
        });
        byCash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                payment(Constant.PAY_TRIP_BY_CASH);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void rate() {
        String rate = ratingBar.getRating() * 2 + "";
        if (Double.parseDouble(rate) != 0) {
            ModelManager.rateDriver(preferencesManager.getToken(), globalValue
                            .getCurrentOrder().getId(), rate, context, true,
                    new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                preferencesManager.setDriverCurrentScreen("");
                                preferencesManager.setPassengerIsInTrip(false);
                                isRate = true;
                                showToastMessage(getString(R.string.rate_success));
                                if (preferencesManager.IsStartWithOutMain()) {
//								gotoActivity(MainActivity.class);
//								finish();
                                } else {
//								finish();
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
        } else {
            Toast.makeText(self, getString(R.string.lblValidateRate), Toast.LENGTH_SHORT).show();
        }

    }

    private void showRate(boolean isRate) {
        if (isRate) {
//			loRate.setVisibility(View.VISIBLE);
            loPayment.setVisibility(View.GONE);
            stepNext.setVisibility(View.VISIBLE);
            stepCircle.setVisibility(View.GONE);
            tvStep.setText(getString(R.string.lbl_request_by_finished));
            lblNorify.setText(getString(R.string.lbl_notify_pasenger_finish));
        } else {
//			loRate.setVisibility(View.GONE);
            loPayment.setVisibility(View.VISIBLE);
            stepNext.setVisibility(View.GONE);
            stepCircle.setVisibility(View.VISIBLE);
            tvStep.setText(getString(R.string.lbl_request_by_arrived1));
            lblNorify.setText(getString(R.string.lbl_notify_pasenger_arriving));

        }

    }

    private void setPayment(String point, String paymentId, final String paymentMethod) {
        ModelManager.payment(preferencesManager.getToken(), point, paymentId,
                paymentMethod, self, true, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            payment(Constant.PAY_TRIP_BY_BALANCE);

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

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM);
        intentFilter.addAction(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CANCEL);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_DRIVER_CONFIRM_PAID_STATUS_CONFIRM)) {
                showToastMessage("Payment successfully");
                preferencesManager.setPassengerHaveDonePayment(true);
                preferencesManager.setPassengerCurrentScreen("");
                gotoActivity(MainActivity.class);
                finish();
            }
        }
    };

    private void payment(String payBy) {
        ModelManager.tripPayment(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), payBy, this, true,
                new ModelManagerListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onSuccess(String json) {

                        if (ParseJsonUtil.isSuccess(json)) {
//							showRate(true);

                            preferencesManager.setPassengerHaveDonePayment(true);
                            preferencesManager.setPassengerCurrentScreen("");
                            finishAffinity();
                            gotoActivity(MainActivity.class);
                            finish();

                        } else {
                            btnSend.setEnabled(true);
                            if (ParseJsonUtil.getMessage(json).endsWith(
                                    "Your balance is short")) {
                                String missingFare = "";
                                missingFare = ParseJsonUtil.getMissingFare(json);
                                buyPoint(missingFare);
                                point = missingFare;
                            } else {
                                showToastMessage(ParseJsonUtil.getMessage(json));
                            }
                        }
                    }

                    @Override
                    public void onError() {
                        btnSend.setEnabled(true);
                        showToastMessage(R.string.message_have_some_error);
                    }
                });
    }

    private void buyPoint(final String point) {
        final MaterialDialog dialog = new MaterialDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_payment_method, null);
        dialog.setContentView(view);
        String title;
        if (exChangeCurrency.equals(Constant.LKR)){
            title = getResources().getString(
                    R.string.message_your_balance_is_not_eounght)
                    + " "
                    + Math.floor(Double.parseDouble(point)*exchange_rate)
                    + " "
                    + getResources().getString(R.string.lblCurrencyLKR);
        }else {
            title = getResources().getString(
                    R.string.message_your_balance_is_not_eounght)
                    + " "
                    + point
                    + " "
                    + getResources().getString(R.string.lblCurrencyUSD);
        }

        dialog.setTitle(title);
        TextViewRaleway txtPaypal = (TextViewRaleway) view.findViewById(R.id.txtPaypal);
        TextViewRaleway txtStripe = (TextViewRaleway) view.findViewById(R.id.txtStripe);
        txtPaypal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                requestPaypalPayment(Double.parseDouble(point),
                        "GET POINT",
                        getString(R.string.currency));
            }
        });
        txtStripe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(RateDriverActivity.this, PaymentStripeFinishActivity.class);
                intent.putExtra("amount", point + "");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left2);
            }
        });
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data
                    .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample",
                            "Payment OK. Response :" + confirm.toJSONObject());
                    JSONObject json = confirm.toJSONObject();

                    // if (ParseJsonUtil.paymentIsPaypal(json)) {
                    setPayment(point,
                            ParseJsonUtil.getTransactionFromPaypal(json),
                            Constant.PAYMENT_BY_PAYPAL);
                    /*
                     * } else { setPayment(point,
                     * ParseJsonUtil.getTransactionFromCart(json),
                     * Constant.PAYMENT_BY_CART); }
                     */

                } catch (Exception e) {
                    Log.e("paymentExample",
                            "an extremely unlikely failure occurred: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample",
                    "An invalid payment was submitted. Please see the docs.");
        }
    }


}
