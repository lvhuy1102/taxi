package com.hcpt.taxinear.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.adapters.StateAdapter;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.StateObj;
import com.hcpt.taxinear.utility.NetworkUtil;
import com.hcpt.taxinear.widget.CircleImageView;
import com.hcpt.taxinear.widget.TextViewPixeden;
import com.soundcloud.android.crop.Crop;

import org.hybridsquad.android.library.CropHandler;
import org.hybridsquad.android.library.CropHelper;
import org.hybridsquad.android.library.CropParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 10/26/2016.
 */
public class SignUpActivity extends BaseActivity implements View.OnClickListener, CropHandler {
    private EditText txtUpdateNameDrive, txtUpdatePhone, txtUpdateEmail, txtPassword, txtUpdateAddress, sp_city, txtUpdatePostCode, txtAccount;
    private Spinner sp_state;
    private CircleImageView imgProfile;
    public static int SELECT_PHOTO = 1000;
    ArrayList<StateObj> listStates;
    private TextViewPixeden btnSave;
    private Bitmap yourSelectedImage;
    private ImageButton btnBackUpdate;
    CropParams mCropParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mCropParams = new CropParams();
        initUI();
        initControl();
        initData();
    }

    public void initUI() {
        txtUpdateNameDrive = (EditText) findViewById(R.id.txtUpdateNameDrive);
        txtUpdatePhone = (EditText) findViewById(R.id.txtUpdatePhone);
        txtAccount = (EditText) findViewById(R.id.txtAccount);
        txtUpdateEmail = (EditText) findViewById(R.id.txtUpdateEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtUpdateAddress = (EditText) findViewById(R.id.txtUpdateAddress);
        sp_city = (EditText) findViewById(R.id.sp_city);
        txtUpdatePostCode = (EditText) findViewById(R.id.txtUpdatePostCode);
        sp_state = (Spinner) findViewById(R.id.sp_state);
        imgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        btnSave = (TextViewPixeden) findViewById(R.id.btnSave);
        btnBackUpdate = (ImageButton) findViewById(R.id.btnBackUpdate);

    }

    public void initControl() {
        imgProfile.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnBackUpdate.setOnClickListener(this);
    }

    public void initData() {
        setDateStates();
    }

    public boolean validate() {
        if (listStates == null) {
            Toast.makeText(SignUpActivity.this, getString(R.string.messsage_state_empty), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (yourSelectedImage == null) {
            Toast.makeText(SignUpActivity.this, getString(R.string.lblValidateImage), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdateNameDrive.getText().toString().equals("")) {
            txtUpdateNameDrive.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_name), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdatePhone.getText().toString().equals("")) {
            txtUpdatePhone.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_phone), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtUpdateEmail.getText().toString().equals("")) {
            txtUpdateEmail.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmailAddress(txtUpdateEmail.getText().toString())) {
            txtUpdateEmail.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_email2), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtPassword.getText().toString().equals("")) {
            txtPassword.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_password), Toast.LENGTH_SHORT).show();
            return false;

        }
        if (txtUpdateAddress.getText().toString().equals("")) {
            txtUpdateAddress.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_address), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sp_city.getText().toString().equals("")) {
            Toast.makeText(SignUpActivity.this, getString(R.string.message_State), Toast.LENGTH_SHORT).show();
            sp_city.requestFocus();
            return false;
        }
        if (txtUpdatePostCode.getText().toString().equals("")) {
            txtUpdatePostCode.requestFocus();
            Toast.makeText(SignUpActivity.this, getString(R.string.message_postcode), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!txtAccount.getText().toString().equals("")) {
            if (!isValidEmailAddress(txtAccount.getText().toString())) {
                txtAccount.requestFocus();
                Toast.makeText(SignUpActivity.this, getString(R.string.message_Acount2), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
//        if (listStates.size() <= 0){
//            Toast.makeText(SignUpActivity.this,getString(R.string.load_state_error),Toast.LENGTH_SHORT).show();
//            return false;
//        }
        return true;
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    public void sendData() {
        if (validate()) {

            ModelManager.registerAccount(this, txtUpdateNameDrive.getText().toString(), txtUpdatePhone.getText().toString(), txtUpdateEmail.getText().toString(),
                    txtPassword.getText().toString(), txtUpdateAddress.getText().toString(), listStates.get(sp_state.getSelectedItemPosition()).getStateId(), sp_city.getText().toString(), txtUpdatePostCode.getText().toString(), txtAccount.getText().toString(),
                    yourSelectedImage, true, new ModelManagerListener() {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                finish();
                            }
                            Toast.makeText(SignUpActivity.this, ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }


    public void setDateStates() {
        if (NetworkUtil.checkNetworkAvailable(getBaseContext())) {
            ModelManager.getAllSates(this, true, new ModelManagerListener() {
                @Override
                public void onError() {
                    Toast.makeText(self, getString(R.string.loadding_state_error), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String json) {
                    listStates = new ArrayList<StateObj>();
                    listStates = ParseJsonUtil.parseListStates(json);
                    StateAdapter adapter = new StateAdapter(SignUpActivity.this, listStates);
                    sp_state.setAdapter(adapter);
                }
            });
        } else {
            Toast.makeText(self, getString(R.string.no_intenet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgProfile:
                Crop.pickImage(SignUpActivity.this);
//                Intent intent = CropHelper.buildCropFromGalleryIntent(mCropParams);
//                startActivityForResult(intent, CropHelper.REQUEST_CROP);
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                break;
            case R.id.btnSave:
                sendData();
                break;
            case R.id.btnBackUpdate:
                finish();
                break;
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(Crop.getOutput(result));
                yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                AQuery aQuery = new AQuery(this);
                aQuery.id(R.id.imgProfile).image(yourSelectedImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(imageReturnedIntent.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, imageReturnedIntent);
        }
//        CropHelper.handleResult(this, requestCode, resultCode, imageReturnedIntent);
//        switch (requestCode) {
//            case 1000:
//                if (resultCode == RESULT_OK) {
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    InputStream imageStream = null;
//                    try {
//                        imageStream = getContentResolver().openInputStream(selectedImage);
//                        yourSelectedImage = BitmapFactory.decodeStream(imageStream);
//                        AQuery aQuery = new AQuery(this);
//                        aQuery.id(R.id.imgProfile).image(yourSelectedImage);
//
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//        }
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(uri);
            yourSelectedImage = BitmapFactory.decodeStream(imageStream);
            AQuery aQuery = new AQuery(this);
            aQuery.id(R.id.imgProfile).image(yourSelectedImage);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        CropHelper.clearCachedCropFile(uri);
    }

    @Override
    public void onCropCancel() {

    }

    @Override
    public void onCropFailed(String message) {

    }

    @Override
    public CropParams getCropParams() {
        mCropParams = new CropParams();
        return mCropParams;
    }

    @Override
    public Activity getContext() {
        return this;
    }

//    @Override
//    public void handleIntent(Intent intent, int requestCode) {
//        startActivityForResult(intent, requestCode);
//    }
}
