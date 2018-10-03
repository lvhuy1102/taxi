package com.hcpt.taxinear.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;

public class CurrencyConverterActivity extends BaseActivity {

    private RadioGroup rdgCurrency;
    private TextView btnConverter;
    private RadioButton currencyUSD, currencyLKR;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);
        initAndSetHeader(R.string.lbl_currency_converter);

        rdgCurrency = findViewById(R.id.rdgCurrency);
        btnConverter = findViewById(R.id.btnConverter);
        currencyUSD = findViewById(R.id.currencyUSD);
        currencyLKR = findViewById(R.id.currencyLKR);

        String currency = preferencesManager.getStringValue(Constant.CURRENCY);
        if (currency.equals(Constant.USD)) {
            currencyUSD.setChecked(true);
        } else {
            currencyLKR.setChecked(true);
        }

//
//        rdgCurrency.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//
//            }
//        });

        btnConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rdgCurrency.getCheckedRadioButtonId()) {
                    case R.id.currencyUSD:
                        preferencesManager.putStringValue(Constant.CURRENCY, Constant.USD);
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        break;

                    case R.id.currencyLKR:
                        preferencesManager.putStringValue(Constant.CURRENCY, Constant.LKR);
                        Intent i2 = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i2);
                        break;
                }
            }
        });
    }

}
