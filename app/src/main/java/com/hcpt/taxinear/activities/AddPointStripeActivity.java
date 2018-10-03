package com.hcpt.taxinear.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;


public class AddPointStripeActivity extends Activity {
    TextView btnSubmit, lblTitle;
    EditText cardNumberField, monthField, yearField, cvcField;
    ImageView imgLogo;
    private String amount = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_payment);
        btnSubmit = (TextView) findViewById(R.id.submitButton);
        cardNumberField = (EditText) findViewById(R.id.cardNumber);
        monthField = (EditText) findViewById(R.id.month);
        yearField = (EditText) findViewById(R.id.year);
        cvcField = (EditText) findViewById(R.id.cvc);
        imgLogo = (ImageView) findViewById(R.id.btnBack);
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblTitle.setText(getString(R.string.credit_card));
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCard();
            }
        });
        amount = getIntent().getStringExtra("amount");
    }

    public void submitCard() {
        // TODO: replace with your own test key
        final String publishableApiKey = this.getString(R.string.stripe_publishable_key);
        Card card = new Card(cardNumberField.getText().toString(),
                Integer.valueOf(monthField.getText().toString()),
                Integer.valueOf(yearField.getText().toString()),
                cvcField.getText().toString());
        if (card.validateCard() && card.validateCVC() && card.validateExpMonth() && card.validateExpYear()) {
            Stripe stripe = new Stripe(this);
            stripe.createToken(card, publishableApiKey, new TokenCallback() {
                public void onSuccess(final Token token) {
                    // TODO: Send Token information to your backend to initiate a charge
                    Log.e("token", "token:" + token.getId());
                    sendRequest(token.getId(), GlobalValue.getInstance().getUser().getEmail(), amount);

                }

                public void onError(Exception error) {
                    Log.d("Stripe", error.getLocalizedMessage());
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.lblInfoInvalid), Toast.LENGTH_SHORT).show();
        }


    }

    public void sendRequest(String token, String email, String amount) {
        ModelManager.sendStripRequest(this, token, amount, email, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    Toast.makeText(AddPointStripeActivity.this, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    public static double round(double number, int digit) {
        if (digit > 0) {
            int temp = 1, i;
            for (i = 0; i < digit; i++)
                temp = temp * 10;
            number = number * temp;
            number = Math.round(number);
            number = number / temp;
            return number;
        } else
            return 0.0;
    }
}