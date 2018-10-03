package com.hcpt.taxinear.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.SettingObj;
import com.hcpt.taxinear.object.User;
import com.hcpt.taxinear.widget.TextViewRaleway;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONObject;

//import com.paypal.android.sdk.payments.PayPalConfiguration;

public class PaymentPointActivity extends BaseActivity {

    private TextViewRaleway lblBalance;
    private EditText txtPoint;
    private LinearLayout btnPayment, btnStripe;
    private TextView lblError;
    static String paymentId;
    private String point;
    private String Exchange_rate;
    private String exChangeCurrency;
    private TextView tvAmount;
    User user;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private int MY_SCAN_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initAndSetHeader(R.string.title_payment);
        getDataFromGlobal();
        initView();
        initControl();

        // Starting PayPal service
        startPaypalService();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public void initView() {
        initUI();
        lblBalance = (TextViewRaleway) findViewById(R.id.lblBalance);
        txtPoint = (EditText) findViewById(R.id.txtPoint);
        btnPayment = (LinearLayout) findViewById(R.id.btnPayment);
        btnStripe = (LinearLayout) findViewById(R.id.btnStripe);
        //lblError = (TextView) findViewById(R.id.lbl_Error);
        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        tvAmount = findViewById(R.id.tvAmount);
        // get balance
        if (exChangeCurrency.equals(Constant.LKR)) {
            Double total = user.getBalance() * Double.valueOf(preferencesManager.getDataSettings().getExchange_rate());
            lblBalance.setText(getString(R.string.lblCurrencyLKR) + " " + Math.floor(total));
            tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyLKR) + ")");
        } else {
            lblBalance.setText(getString(R.string.lblCurrencyUSD) + user.getBalance());
            tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyUSD) + ")");
        }
        Log.e("BALANCE", String.valueOf(user.getBalance()));
    }

    public void initControl() {
        btnPayment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                payment();
            }
        });
        btnStripe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtPoint.getText()
                        .toString().trim().equals("")) {

                    Double amount1;
                    if (exChangeCurrency.equals(Constant.LKR)) {
                        SettingObj settingObj = preferencesManager.getDataSettings();
                        String ex = settingObj.getExchange_rate();
                        amount1 = Double.parseDouble(txtPoint.getText().toString().trim());
                        amount1 = amount1 / Double.parseDouble(ex);
                    } else {
                        amount1 = Double.parseDouble(txtPoint.getText().toString().trim());
                    }
                    Intent intent = new Intent(PaymentPointActivity.this, PaymentStripeActivity.class);
                    intent.putExtra("amount", amount1 + "");
                    startActivity(intent);
                } else {
                    showToastMessage(getString(R.string.lblAmountNotBlank));
                }


            }
        });
    }

    private void setPayment(String point, String paymentId, String paymentMethod) {

        ModelManager.payment(preferencesManager.getToken(), txtPoint.getText()
                        .toString(), paymentId, paymentMethod, self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            showToastMessage(R.string.lbl_success);
                            if (ParseJsonUtil.getMessage(json)
                                    .equalsIgnoreCase("ok")) {
                                txtPoint.setText("");
                                // finish();
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

    public void payment() {
        if (txtPoint.getText().toString().trim().length() != 0) {
            Double moneyInt;
            if (exChangeCurrency.equals(Constant.LKR)) {
                SettingObj settingObj = preferencesManager.getDataSettings();
                String ex = settingObj.getExchange_rate();
                moneyInt = Double.parseDouble(txtPoint.getText().toString().trim());
                moneyInt = moneyInt / Double.parseDouble(ex);
            } else {
                moneyInt = Double.parseDouble(txtPoint.getText().toString().trim());
            }
            Log.e("USD", String.valueOf(moneyInt));
            if (moneyInt < 0) {
                showToastMessage(getString(R.string.lblPointInvalid));
            } else {
                requestPaypalPayment(moneyInt, "GET POINT", getString(R.string.currency));
            }
        } else {
            showToastMessage(R.string.message_please_enter_amount);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("eee", "onActivityResult");
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data
                    .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample",
                            "Payment OK. Response :" + confirm.toJSONObject());
                    JSONObject json = confirm.toJSONObject();

                    setPayment(point,
                            ParseJsonUtil.getTransactionFromPaypal(json),
                            Constant.PAYMENT_BY_PAYPAL);
                    getData();

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

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e("eee", "onResume" + "  getData");
        getData();
        txtPoint.setText("");
    }

    public void getData() {
        ModelManager.showInfoProfile(
                PreferencesManager.getInstance(mainActivity).getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        Log.e("json", "json:" + json);
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            user = ParseJsonUtil.parseInfoProfile(json);
                            if (exChangeCurrency.equals(Constant.LKR)) {
                                Double total = user.getBalance() * Double.valueOf(preferencesManager.getDataSettings().getExchange_rate());
                                lblBalance.setText(getString(R.string.lblCurrencyLKR) + " " + Math.floor(total));
                                tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyLKR) + ")");
                            } else {
                                lblBalance.setText(getString(R.string.lblCurrencyUSD) + user.getBalance());
                                tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyUSD) + ")");
                            }
                            Log.e("eee", "getData" + "  lblBalance");
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}
