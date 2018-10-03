package com.hcpt.taxinear.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.Transfer;
import com.hcpt.taxinear.object.User;
import com.hcpt.taxinear.widget.TextViewFontAwesome;
import com.hcpt.taxinear.widget.TextViewRaleway;

public class PayoutActivity extends BaseActivity {

    TextViewFontAwesome lbl_information;
    TextView lblError;
    LinearLayout btnContinue, btnStripe;
    TextViewRaleway lblBalance;
    EditText lblPoint;
    User user;
    Transfer payment = new Transfer();
    String minRedeem;
    private String exChangeCurrency;
    private double exchange_rate;
    TextView tvAmount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout);
        initUI();
        getDataFromGlobal();
        initUIInThis();
        getMinRedeem();
        initAndSetHeader(R.string.title_redeem);

    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void initUIInThis() {
        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        exchange_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
        lblBalance = (TextViewRaleway) findViewById(R.id.lbl_Balance);
        lblPoint = (EditText) findViewById(R.id.lbl_Point);
        lblError = (TextView) findViewById(R.id.lbl_Error);
        lbl_information = (TextViewFontAwesome) findViewById(R.id.lbl_information);
        btnStripe = (LinearLayout) findViewById(R.id.btnStripe);
        tvAmount = findViewById(R.id.tvAmount);
        if (exChangeCurrency.equals(Constant.LKR)) {
            Double total = user.getBalance() * exchange_rate;
            lblBalance.setText(getString(R.string.lblCurrencyLKR) + " " + Math.floor(total));
            tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyLKR) + ")");
        } else {
            lblBalance.setText(getString(R.string.lblCurrencyUSD) + user.getBalance());
            tvAmount.setText(getString(R.string.lbl_amount) + "(" + getString(R.string.lblCurrencyUSD) + ")");
        }

        //lblBalance.setText(getString(R.string.lblCurrency) + String.valueOf(user.getBalance()));

        btnContinue = (LinearLayout) findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (lblPoint.getText().toString().length() != 0) {
                    if (exChangeCurrency.equals(Constant.LKR)) {
                        Double amount = Double.parseDouble(lblPoint.getText().toString().trim());
                        Double balance = user.getBalance();
                        if (amount > Math.floor(balance * exchange_rate)) {
                            showToastMessage(getString(R.string.message_point_invalid).replace("USD", getString(R.string.lblCurrencyLKR)));
                        } else if (amount >= Math.floor(Double.parseDouble(minRedeem) * exchange_rate)) {
                            payOut();
                        } else {
                            showToastMessage(self.getResources()
                                    .getString(R.string.message_point_min)
                                    .replace("USD100", getString(R.string.lblCurrencyLKR) + Math.floor(Double.parseDouble(minRedeem) * exchange_rate)));
                        }
                    } else {
                        Double amount = Double.parseDouble(lblPoint.getText()
                                .toString().trim());
                        Double balance = user.getBalance();
                        if (amount > balance) {
                            showToastMessage(R.string.message_point_invalid);
                        } else if (amount >= Double.parseDouble(minRedeem)) {
                            payOut();
                        } else {
                            showToastMessage(self.getResources()
                                    .getString(R.string.message_point_min)
                                    .replace("100", minRedeem));
                        }
                    }

                } else {
                    if (exChangeCurrency.equals(Constant.LKR)) {
                        showToastMessage(getString(R.string.message_please_enter_point).replace("USD", "LKR"));
                    } else {
                        showToastMessage(getString(R.string.message_please_enter_point));
                    }
                }
            }
        });


        lbl_information.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("http://www.linkrider.net/#!faq/c1yvs"));
                startActivity(browserIntent);
            }
        });
    }

    private void payOut() {
        double value = 0;
        if (exChangeCurrency.equals(Constant.LKR)) {
            value = Double.parseDouble(lblPoint.getText().toString()) / exchange_rate;
        } else {
            value = Double.parseDouble(lblPoint.getText().toString());
        }
        ModelManager.payout(PreferencesManager.getInstance(self).getToken(),
                "" + value, self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {

                            showToastMessage(R.string.lblPayoutSuccess);
                            onBackPressed();
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

    private void getMinRedeem() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            minRedeem = ParseJsonUtil.getMinRedeem(json);
                            // minRedeem = mainActivity.getResources()
                            // .getString(R.string.message_point_min)
                            // .replace("500", minRedeem);
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
}
