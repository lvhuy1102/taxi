package com.hcpt.taxinear.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.androidquery.AQuery;
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
import com.hcpt.taxinear.widget.CircleImageView;
import com.hcpt.taxinear.widget.TextViewPixeden;
import com.hcpt.taxinear.widget.TextViewRaleway;

public class DetailTransferActivity extends BaseActivity {

    TextViewPixeden btnback;
    TextViewRaleway lblBalance, lblDriverId, lblPoint;
    TextView lblName, btnTransfers;
    EditText lblNote, lblEmail, lblGenger, lblAmount;
    CircleImageView imgProfile;

    User user;
    Transfer transfer;
    AQuery aq;
    String minTranfer;

    private String exChangeCurrency;
    private double exchange_rate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_transfers);
        getDataFromGlobal();

        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        exchange_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
        // hide keyboard with edittext
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initUIInThis();
        getMinTranfer();

    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
        transfer = GlobalValue.getInstance().transfer;
    }

    public void initUIInThis() {

        lblNote = (EditText) findViewById(R.id.lblNote);
        lblEmail = (EditText) findViewById(R.id.lblEmail);
        // lblGenger = (EditText) findViewById(R.id.lblGender);
        lblAmount = (EditText) findViewById(R.id.lblPoint);
        lblName = (TextView) findViewById(R.id.txtNameDriver);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        btnTransfers = (TextView) findViewById(R.id.btnTransfers);
        lblPoint = findViewById(R.id.lblPoint);
        btnback = (TextViewPixeden) findViewById(R.id.btnBack);
        btnback.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onBackPressed();
            }
        });

        if (exChangeCurrency.equals(Constant.LKR)){
            lblPoint.setText(getString(R.string.lblCurrencyLKR));
        }else {

            lblPoint.setText(getString(R.string.lblCurrencyUSD));
        }
        btnTransfers.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (lblAmount.getText().length() != 0) {
                    if (exChangeCurrency.equals(Constant.LKR)) {
                        Double amount = Double.parseDouble(lblPoint.getText().toString().trim());
                        Double balance = user.getBalance();
                        if (amount > Math.floor(user.getBalance() * exchange_rate)) {
                            showToastMessage(getString(R.string.message_point_invalid).replace("USD", getString(R.string.lblCurrencyLKR)));
                        } else if (amount < Math.floor(Double.parseDouble(minTranfer) * exchange_rate)) {
                            showToastMessage(self.getResources()
                                    .getString(R.string.message_point_min)
                                    .replace("USD100", getString(R.string.lblCurrencyLKR) + Math.floor(Double.parseDouble(minTranfer) * exchange_rate)));
                        } else {
                            transfer();
                        }
                    } else {
                        Double amount = Double.parseDouble(lblAmount.getText().toString());
                        if (amount > user.getBalance()) {
                            showToastMessage(R.string.message_point_invalid);
                        } else if (amount < Double.parseDouble(minTranfer)) {
                            showToastMessage(self.getResources()
                                    .getString(R.string.message_point_min)
                                    .replace("100", minTranfer));
                        } else {
                            transfer();
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

        // get data
        lblEmail.setText(transfer.getReceiverEmail());
        lblEmail.setEnabled(false);
        // lblGenger.setText(transfer.getReceiverGender());
        // lblGenger.setEnabled(false);

        lblName.setText(transfer.getReceiverName());
        aq = new AQuery(self);
        aq.id(imgProfile).image(transfer.getReceiverProfile());
    }

    public void transfer() {

        double value = 0;
        if (exChangeCurrency.equals(Constant.LKR)) {
            value = Double.parseDouble(lblAmount.getText().toString()) / exchange_rate;
        } else {
            value = Double.parseDouble(lblAmount.getText().toString());
        }


        ModelManager.transfer(PreferencesManager.getInstance(self).getToken(),
                "" + value, transfer.getReceiverEmail(),
                lblNote.getText().toString(), self, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            showToastMessage(R.string.lbl_success);
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

    private void getMinTranfer() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            minTranfer = ParseJsonUtil.getMinTranfer(json);
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
