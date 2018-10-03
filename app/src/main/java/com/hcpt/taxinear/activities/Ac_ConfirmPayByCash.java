package com.hcpt.taxinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.ServiceUpdateLocation;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.fragment.BeforeOnlineFragment;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;


public class Ac_ConfirmPayByCash extends BaseActivity {
    private TextView btnConfirm, btnCancel;
    private String class_from = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac__confirm_pay_by_cash);

        init();
        initControl();
    }

    public void init() {
        btnConfirm = (TextView) findViewById(R.id.btnConfirm);
        btnCancel = (TextView) findViewById(R.id.btnCancel);
        if (getIntent().getStringExtra("classFrom") != null) {
            class_from = getIntent().getStringExtra("classFrom");
        }

    }

    public void initControl() {
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm(Constant.ACTION_CONFIRM);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm(Constant.ACTION_CANCEL);
                btnConfirm.setEnabled(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void confirm(String action) {
        ModelManager.confirmPayByCash(preferencesManager.getCurrentOrderId(), action, this, false, new ModelManagerListener() {
            @Override
            public void onError() {
                showToastMessage(getString(R.string.message_have_some_error));
                btnConfirm.setEnabled(true);
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    preferencesManager.isFromBeforeOnline(false);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                } else {
                    showToastMessage(ParseJsonUtil.getMessage(json));
                }
                btnConfirm.setEnabled(true);
            }
        });
    }

}
