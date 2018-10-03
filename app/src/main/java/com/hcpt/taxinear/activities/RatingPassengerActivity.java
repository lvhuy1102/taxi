package com.hcpt.taxinear.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.hcpt.taxinear.widget.TextViewRaleway;

import java.text.DecimalFormat;

public class RatingPassengerActivity extends BaseActivity {

    private TextViewRaleway lblName, lblPhone;
    private TextViewRaleway lblPoint;
    private RatingBar ratingBar, ratingBarUser;
    private TextView btnSend, tvSeat;

    private TextViewRaleway lblStartLocation, txtExplain;
    private TextViewRaleway lblEndLocation;
    private ImageView imgPassenger;
    private TextView txtStar;
    private LinearLayout llHelp;
    AQuery aq;
    private String phoneAdmin = "";
    private double exchange_rate;
    private String exChangeCurrency;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_passenger);
        aq = new AQuery(this);

        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        exchange_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
        initUI();
        findViews();
        getDriverEarn();
        initControl();

    }

    private void findViews() {
        lblStartLocation = (TextViewRaleway) findViewById(R.id.lblStartLocation);
        lblEndLocation = (TextViewRaleway) findViewById(R.id.lblEndLocation);
        txtExplain = (TextViewRaleway) findViewById(R.id.txtExplain);
        imgPassenger = (ImageView) findViewById(R.id.imgPassenger);
        tvSeat = (TextView) findViewById(R.id.tvSeat);
        txtStar = (TextView) findViewById(R.id.txtStar);
        lblName = (TextViewRaleway) findViewById(R.id.lblName);
        lblPhone = (TextViewRaleway) findViewById(R.id.lblPhone);
        lblPoint = (TextViewRaleway) findViewById(R.id.lblPoint);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBarUser = (RatingBar) findViewById(R.id.ratingBarUser);
        llHelp = (LinearLayout) findViewById(R.id.llHelp);
        btnSend = (TextView) findViewById(R.id.btnSend);
        if (!globalValue
                .getCurrentOrder()
                .getPassengerRate().equals(""))
            ratingBarUser.setRating(Float
                    .parseFloat(globalValue
                            .getCurrentOrder()
                            .getPassengerRate()) / 2);
    }

    private void initControl() {
        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ratingBar.getRating() * 2 != 0) {
                    rateCustomer(globalValue.getCurrentOrder().getId(),
                            ratingBar.getRating() * 2 + "");

                    preferencesManager.setDriverCurrentScreen("");
                    preferencesManager.setDriverIsNotInTrip();
                    preferencesManager.setCurrentOrderId("");
                } else {
                    Toast.makeText(self, getString(R.string.lblValidateRate), Toast.LENGTH_SHORT).show();
                }

            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(RatingPassengerActivity.this)) {
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
        ModelManager.showTripDetail(preferencesManager.getToken(),
                preferencesManager.getCurrentOrderId(), context, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        // TODO Auto-generated method stub
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setCurrentOrder(ParseJsonUtil
                                    .parseCurrentOrder(json));
                            if (exChangeCurrency.equals(Constant.LKR)) {
                                lblPoint.setText(getString(R.string.lblCurrencyLKR) + Math.floor(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) * exchange_rate));
                                DecimalFormat df = new DecimalFormat("#.####");

                                String temp = df.format(Math.floor(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) * globalValue.getDriver_earn() * exchange_rate));
                                String deduct = df.format(Math.floor(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) * exchange_rate) - Double.parseDouble(temp));
                                Log.e("TEXT", temp);
                                Log.e("TEXT", deduct);

                                String text = String.format(getString(R.string.explain), deduct, temp);
                                Log.e("TEXT", text);
                                txtExplain.setText(text);
                            } else {
                                lblPoint.setText(getString(R.string.lblCurrencyUSD) + globalValue.getCurrentOrder().getActualFare());
                                DecimalFormat df = new DecimalFormat("#.####");

                                String temp = df.format(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) * globalValue.getDriver_earn());
                                String deduct = df.format(Double.parseDouble(globalValue.getCurrentOrder().getActualFare()) - Double.parseDouble(temp));
                                Log.e("TEXT", temp);
                                Log.e("TEXT", deduct);

                                String text = String.format(getString(R.string.explain), deduct, temp);
                                text = text.replaceAll("LKR","USD");
                                Log.e("TEXT", text);
                                txtExplain.setText(text);
                            }


                            tvSeat.setText(convertLinkToString(globalValue.getCurrentOrder().getLink()) + "");
                            lblName.setText(globalValue.getCurrentOrder()
                                    .getPassengerName());
                            lblPhone.setText(globalValue.getCurrentOrder().getPassenger_phone(true));
                            lblStartLocation.setText(globalValue
                                    .getCurrentOrder().getStartLocation());
                            lblEndLocation.setText(globalValue
                                    .getCurrentOrder().getEndLocation());

                            aq.id(R.id.imgPassenger).image(
                                    globalValue.getCurrentOrder().getImagePassenger());
                            if (globalValue.getCurrentOrder()
                                    .getPassengerRate().isEmpty()) {
                                txtStar.setText("0");
                            } else {
                                txtStar.setText("" + Float
                                        .parseFloat(globalValue
                                                .getCurrentOrder()
                                                .getPassengerRate()) / 2);
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

    public String convertLinkToString(String link) {
        switch (link) {
            case "I":
                return getString(R.string.sedan4);

            case "II":
                return getString(R.string.suv6);

            case "III":
                return getString(R.string.lux);
            case "IV":
                return getString(R.string.car);
            case "V":
                return getString(R.string.van);
        }
        return link;
    }

    private void rateCustomer(String tripId, String rate) {
        ModelManager.rateCustomer(preferencesManager.getToken(), tripId, rate,
                self, true, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager.setDriverCurrentScreen("");
                            preferencesManager.setDriverIsNotInTrip();
                            gotoActivity(OnlineActivity.class);
                            finish();
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

    public void getDriverEarn() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            globalValue.setDriver_earn(ParseJsonUtil.getDriverEarn(json));
                            initData();
                        }
                    }

                    @Override
                    public void onError() {
                        showToastMessage(getResources().getString(
                                R.string.message_have_some_error));
                    }
                });
    }

}
