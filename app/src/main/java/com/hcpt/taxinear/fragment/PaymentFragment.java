package com.hcpt.taxinear.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hcpt.taxinear.BaseFragment;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.activities.CurrencyConverterActivity;
import com.hcpt.taxinear.activities.PaymentPointActivity;
import com.hcpt.taxinear.activities.PaymentHistoryActivity;
import com.hcpt.taxinear.activities.PaymentTransferActivity;
import com.hcpt.taxinear.activities.PayoutActivity;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.User;
import com.hcpt.taxinear.widget.CircleImageView;
import com.paypal.android.sdk.p;

public class PaymentFragment extends BaseFragment implements OnClickListener {
    Button btnPayment, btnPayout, btnTransfers, btnHistory, btnConverterCurrency;
    TextView lblTotalPoint, lblName, lblBalance;
    RatingBar ratingBar;
    CircleImageView imgProfile;
    private String exchangeCurrency;
    User user = new User();
    static AQuery aq;

    int Total = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container,
                false);
        getDataFromGlobal();
        initUI(view);
        initControl();
        initMenuButton(view);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getData();
        }
    }

    ;

    public void changeLanguage() {
        lblBalance.setText(R.string.lbl_balance);
        btnTransfers.setText(R.string.lbl_transfers);
        btnHistory.setText(R.string.lbl_payment_history);
        btnPayout.setText(R.string.lbl_payout);
    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public void initUI(View view) {
        btnHistory = (Button) view.findViewById(R.id.btnHistory);
        btnPayment = (Button) view.findViewById(R.id.btnPayment);
        btnPayout = (Button) view.findViewById(R.id.btnPayout);
        btnTransfers = (Button) view.findViewById(R.id.btnTrasfers);
        lblTotalPoint = (TextView) view.findViewById(R.id.txtTotalPoint);
        lblName = (TextView) view.findViewById(R.id.lblFullName);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        imgProfile = (CircleImageView) view.findViewById(R.id.imgProfile);
        lblBalance = (TextView) view.findViewById(R.id.lblBalance);
        btnConverterCurrency = view.findViewById(R.id.btnConverterCurrency);
        aq = new AQuery(self);
        exchangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        // = aq.recycle(view);
    }

    public void initControl() {
        btnHistory.setOnClickListener(this);
        btnPayment.setOnClickListener(this);
        btnPayout.setOnClickListener(this);
        btnTransfers.setOnClickListener(this);
        btnConverterCurrency.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPayment:
                showDialogPayment();
                break;
            case R.id.btnPayout:
                showDialogPayout();
                break;
            case R.id.btnTrasfers:
                showTrasfers();
                break;
            case R.id.btnHistory:
                showHistory();
                break;
            case R.id.btnConverterCurrency:
                setCurrencyConverte();
                break;

        }
    }

    private void setCurrencyConverte() {
        Intent intent = new Intent(getContext(), CurrencyConverterActivity.class);
        startActivity(intent);
    }

    private void showTrasfers() {
        startActivity(PaymentTransferActivity.class);
    }

    private void showHistory() {
        startActivity(PaymentHistoryActivity.class);
    }

    private void showDialogPayout() {
        startActivity(PayoutActivity.class);

    }

    //////////////////////////////////
    private void showDialogPayment() {
        startActivity(PaymentPointActivity.class);
    }

    // get data
    public void getData() {
        ModelManager.showInfoProfile(
                preferencesManager.getToken(), self,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            GlobalValue.getInstance().setUser(
                                    ParseJsonUtil.parseInfoProfile(json));
                            user = ParseJsonUtil.parseInfoProfile(json);

                            lblName.setText(user.getFullName());

                            if (user.getDriverObj() != null) {
                                if (user.getDriverObj().getDriverRate() != null) {
                                    if (!user.getDriverObj().getDriverRate().equals(""))
                                        ratingBar.setRating(Float.parseFloat(user.getDriverObj().getDriverRate()) / 2);
                                } else {
                                    if (!user.getPassengerRate().equals("")) {
                                        ratingBar.setRating(Float.parseFloat(user.getPassengerRate()) / 2);
                                    }
                                }
                            } else {
                                if (!user.getPassengerRate().equals("")) {
                                    ratingBar.setRating(Float.parseFloat(user.getPassengerRate()) / 2);
                                }
                            }

                            if (exchangeCurrency.equals(Constant.LKR)) {
                                double total = user.getBalance() * Double.valueOf(preferencesManager.getDataSettings().getExchange_rate());
                                lblTotalPoint.setText(getString(R.string.lblCurrencyLKR) + " " + Math.floor(total));
                            } else {
                                lblTotalPoint.setText(getString(R.string.lblCurrencyUSD) + " " + String.valueOf(user.getBalance()));
                            }

                            aq.id(imgProfile).image(user.getLinkImage());

                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }

                    }

                    @Override
                    public void onError() {
                        showToast(getResources().getString(
                                R.string.message_have_some_error));

                    }
                });
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        String currency = preferencesManager.getStringValue(Constant.CURRENCY);
        if (currency.equals(Constant.LKR)) {
            Double total = GlobalValue.getInstance().getUser().getBalance() * Double.valueOf(preferencesManager.getDataSettings().getExchange_rate());
            lblTotalPoint.setText(getString(R.string.lblCurrencyLKR) + " " + Math.floor(total));
        } else {
            lblTotalPoint.setText(getString(R.string.lblCurrencyUSD) + " " + GlobalValue.getInstance().getUser().getBalance());
        }
//        lblTotalPoint.setText(getString(R.string.lblCurrency) + " " + GlobalValue.getInstance().getUser().getBalance()
//                + "");
    }
}
