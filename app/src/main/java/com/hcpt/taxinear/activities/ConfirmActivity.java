package com.hcpt.taxinear.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.googledirections.Route;
import com.hcpt.taxinear.googledirections.Routing;
import com.hcpt.taxinear.googledirections.RoutingListener;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.service.GPSTracker;
import com.hcpt.taxinear.widget.TextViewRaleway;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConfirmActivity extends BaseActivity implements RoutingListener, GoogleMap.OnMyLocationChangeListener {
    private TextViewRaleway lblCarPlate;
    private TextViewRaleway lblName, tvSeat;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private ImageView imgPassenger, imgDriver;
    private ImageView imgCar;
    private TextView btnCancel;
    private TextViewRaleway lblDistance;
    private TextViewRaleway lblStartLocation;
    private TextViewRaleway lblEndlocation, lblTimes, lblDistanceTime;
    TextView txtStar;
    AQuery aq;

    // For timer
    private int mInterval = 1; // 5 seconds by default, can be changed later
    private Timer mTimer;
    private GoogleMap mMap;
    private GPSTracker gps;
    private double lat;
    private double lnt;
    private ScrollView scrollView;
    LatLng startLocation, endLocation;
    Bitmap iconMarker;
    Runnable runnable;

    private LatLngBounds latlngBounds;
    private Polyline newPolyline;

    private CardView imgBack;
    // For timer
    private Routing routing;
    private Marker mMarkerStartLocation, mMarkerEndLocation;
    List<LatLng> polyz;
    private TextViewRaleway lblTitlePassenger, lblTitleDriver, txtStatus;
    private int width, height;
    private boolean checkZoom = true;
    private ImageView imgCall, imgSms;
    private boolean checkFake = true;
    private boolean checkDataDistance = true;
    private String dataPath = "";
    private int dem = 0;
    Handler handler = new Handler();
    Runnable updateMarker;

    private void autoRefreshEvents() {
        if (mTimer == null) {
            mTimer = new Timer();
            RefreshEvents refresh = new RefreshEvents();
            try {
                mTimer.scheduleAtFixedRate(refresh, mInterval * 10 * 1000,
                        mInterval * 10 * 1000);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public void onRoutingFailure() {
    }

    @Override
    public void onRoutingStart() {
        lblDistanceTime.setText(dataPath);
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (startLocation != null || endLocation != null) {
            if (checkDataDistance) {
                if (mMap != null)
                    mMap.clear();
                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(R.color.second_primary);
                polyOptions.width(10);
                polyOptions.addAll(mPolyOptions.getPoints());
                mMap.addPolyline(polyOptions);
                setLocationLatLong(startLocation);
                setStartMarkerAgain();
                String msg = getString(R.string.msgDrivingComingDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                dataPath = msg;
            } else {
                String msg = getString(R.string.msgDrivingComingDistance);
                msg = msg.replace("[a]", route.getDistanceText());
                msg = msg.replace("[b]", route.getDurationText());
                lblDistanceTime.setText(msg);
                dataPath = msg;
            }

        }
    }

    @Override
    public void onMyLocationChange(Location location) {

    }

    private class RefreshEvents extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Do some thing here
                    getDistance();
                }
            });
        }

    }

    protected void showDirection() {
        if (startLocation != null && endLocation != null) {
//            findDirections(startLocation.latitude, startLocation.longitude, endLocation.latitude, endLocation.longitude, GMapV2Direction.MODE_DRIVING);
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, endLocation);
            checkDataDistance = true;
        }
    }

    protected void showDistanceAndTime() {
        if (startLocation != null && endLocation != null) {
//            findDirections(startLocation.latitude, startLocation.longitude, endLocation.latitude, endLocation.longitude, GMapV2Direction.MODE_DRIVING);
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, endLocation);
            checkDataDistance = false;
        }

    }

    public void setStartMarker() {
        if (endLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
            showDirection();
        }
    }

    public void setStartMarkerAgain() {
        if (endLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

    private void getDistance() {
        if (globalValue.getCurrentOrder() != null) {
            ModelManager.showDistance(preferencesManager.getToken(),
                    globalValue.getCurrentOrder().getId(), context, false,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                try {
                                    Float temp = Float.parseFloat(ParseJsonUtil
                                            .getDistance(json));
                                    if (temp.toString().length() > 6) {
                                        lblDistance.setText(temp.toString().substring(
                                                0, 6)
                                                + " " + getString(R.string.unit_measure));
                                    } else {
                                        lblDistance.setText(temp.toString()
                                                + " " + getString(R.string.unit_measure));
                                    }
                                } catch (NumberFormatException e) {
                                    lblDistance.setText("0"
                                            + " " + getString(R.string.unit_measure));
                                }


                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

    }

    /* OVERRIDE */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        aq = new AQuery(self);
//        getSreenDimanstions();
        initUI();
        initView();
        Maps();
        initData();
        initControl();
        initLocalBroadcastManager();
        autoRefreshEvents();
        preferencesManager.setPassengerWaitConfirm(false);
        preferencesManager.putStringValue("countDriver", "0");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void updateData() {
        preferencesManager.putStringValue("updateCofirm", "1");
        updateMarker = new Runnable() {
            @Override
            public void run() {
                updatePositionForPassengeNoUpdate(globalValue.getCurrentOrder().getDriverId());
            }
        };
        handler.postDelayed(updateMarker, 2000);
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        if (gps != null) {
            gps.stopSelf();
        }
        preferencesManager.putStringValue("updateCofirm", "0");
        handler.removeCallbacks(updateMarker);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onResume() {
        if (preferencesManager.getPassengerCurrentScreen().equals(
                StartTripForPassengerActivity.class.getSimpleName())) {
            gotoActivity(StartTripForPassengerActivity.class);
            finish();
        } else if (preferencesManager.getPassengerCurrentScreen().equals("")) {
            gotoActivity(MainActivity.class);
            finish();
        } else {
            preferencesManager.setPassengerWaitConfirm(false);
            preferencesManager.setPassengerCurrentScreen(ConfirmActivity.class
                    .getSimpleName());
            preferencesManager.setPassengerIsInTrip(true);
            initData();
        }
        super.onResume();
    }

    ;

    private void Maps() {
        //initData();
        setUpMap();
    }

    private void setLocationLatLong(LatLng location) {
        // set filter
        lat = location.latitude;
        lnt = location.longitude;
        LatLng latLng = new LatLng(location.latitude, location.longitude);
        if (mMarkerEndLocation != null) {
            mMarkerEndLocation.remove();
        }
        iconMarker = BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_driver);
        iconMarker = Bitmap.createScaledBitmap(iconMarker,
                iconMarker.getWidth(), iconMarker.getHeight(),
                false);
        mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                latLng).icon(
                BitmapDescriptorFactory.fromBitmap(iconMarker)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        // 2-21
    }

    private void setUpMap() {
        // TODO Auto-generated method stub

        if (mMap == null) {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragMaps);
            fm.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
        cancelTrip();
    }

    /* FUNCTION */
    private void initView() {
        //setHeaderTitle(R.string.lbl_order_confirm);
        tvSeat = (TextViewRaleway) findViewById(R.id.tvSeat);
        lblName = (TextViewRaleway) findViewById(R.id.lblName);
        lblCarPlate = (TextViewRaleway) findViewById(R.id.lblCarPlate);
        lblPhone = (TextView) findViewById(R.id.lblPhone);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        imgPassenger = (ImageView) findViewById(R.id.imgPassenger);
        imgCar = (ImageView) findViewById(R.id.imgCar);
        btnCancel = (TextView) findViewById(R.id.btnCancel);
        lblDistance = (TextViewRaleway) findViewById(R.id.lblDistance);
        lblStartLocation = (TextViewRaleway) findViewById(R.id.lblStartLocation);
        lblEndlocation = (TextViewRaleway) findViewById(R.id.lblEndlocation);
        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        lblTimes = (TextViewRaleway) findViewById(R.id.lblTimes);
        lblTitleDriver = (TextViewRaleway) findViewById(R.id.lblTitleDriver);
        txtStatus = (TextViewRaleway) findViewById(R.id.txtStatus);
        lblTitlePassenger = (TextViewRaleway) findViewById(R.id.lblTitlePassenger);
        txtStar = (TextView) findViewById(R.id.txtStar);
        imgBack = (CardView) findViewById(R.id.cv_back);
        lblDistanceTime = (TextViewRaleway) findViewById(R.id.lblDistanceTime);
        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        imgCall = (ImageView) findViewById(R.id.imgCall);
        imgSms = (ImageView) findViewById(R.id.imgSms);

    }

    private void initData() {
        if (!getPreviousActivityName().equals(
                WaitDriverConfirmActivity.class.getName())) {
            ModelManager.showTripDetail(preferencesManager.getToken(),
                    preferencesManager.getCurrentOrderId(), context, true,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));
                                endLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getStartLat()), Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getStartLong()));
                                lblName.setText(globalValue.getCurrentOrder()
                                        .getDriverName());
                                lblCarPlate.setText(globalValue
                                        .getCurrentOrder().getCarPlate());
                                lblPhone.setText(globalValue.getCurrentOrder()
                                        .getDriver_phone(true));
                                lblStartLocation.setText(globalValue
                                        .getCurrentOrder().getStartLocation());
                                lblEndlocation.setText(globalValue
                                        .getCurrentOrder().getEndLocation());
                                lblTitlePassenger.setText(globalValue
                                        .getCurrentOrder().getDriverName());
                                lblTitleDriver.setText(globalValue
                                        .getCurrentOrder().getPassengerName());
                                if (globalValue.getCurrentOrder()
                                        .getDriverRate().isEmpty()) {
                                    txtStar.setText("0");
//                                    ratingBar.setRating(0);
                                } else {
                                    txtStar.setText("" + Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getDriverRate()) / 2);
//                                    ratingBar.setRating(Float
//                                            .parseFloat(globalValue
//                                                    .getCurrentOrder()
//                                                    .getPassenger_rate()) / 2);
                                }
                                tvSeat.setText(convertLinkToString(globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
                                aq.id(imgCar).image(
                                        globalValue.getCurrentOrder()
                                                .getCarImage());
                                aq.id(imgPassenger).image(
                                        globalValue.getCurrentOrder()
                                                .getImageDriver());
                                aq.id(imgDriver).image(
                                        globalValue.getCurrentOrder()
                                                .getCarImage());
                                if (preferencesManager.getArrived() != null && preferencesManager.getArrived().equals("1")) {
                                    txtStatus.setText(getString(R.string.lbl_driver_arrived));
                                } else if (globalValue.getCurrentOrder().getStatus().equals(Constant.TRIP_STATUS_ARRIVED)) {
                                    txtStatus.setText(getString(R.string.lbl_driver_arrived));
                                }
                                getDistance();
                                updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 14.0f));

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
            tvSeat.setText(convertLinkToString(globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
            lblName.setText(globalValue.getCurrentOrder().getDriverName());
            lblCarPlate.setText(globalValue.getCurrentOrder().getCarPlate());
            lblPhone.setText(globalValue.getCurrentOrder().getDriver_phone(true));
            lblStartLocation.setText(globalValue
                    .getCurrentOrder().getStartLocation());
            lblEndlocation.setText(globalValue
                    .getCurrentOrder().getEndLocation());
            if (globalValue.getCurrentOrder()
                    .getDriverRate().isEmpty()) {
                txtStar.setText("0");
//                                    ratingBar.setRating(0);
            } else {
                txtStar.setText("" + Float
                        .parseFloat(globalValue
                                .getCurrentOrder()
                                .getDriver_rate()) / 2);
//                                    ratingBar.setRating(Float
//                                            .parseFloat(globalValue
//                                                    .getCurrentOrder()
//                                                    .getPassenger_rate()) / 2);
            }
            aq.id(imgCar).image(globalValue.getCurrentOrder().getCarImage());
            // TODO: 12/12/2015 need to update image for car. currently url image die
            Log.e("eeeeeeeeee", "image: " + globalValue.getCurrentOrder()
                    .getCarImage());
            aq.id(imgPassenger).image(
                    globalValue.getCurrentOrder().getImageDriver());
            if (preferencesManager.getArrived() != null && preferencesManager.getArrived().equals("1")) {
                txtStatus.setText(getString(R.string.lbl_driver_arrived));
            }
            getDistance();
            endLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLat()), Double.parseDouble(globalValue.getCurrentOrder().getStartLong()));
            updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endLocation, 14.0f));
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

    private void initControl() {
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTrip();
            }
        });
        lblPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getCurrentOrder().getDriver_phone(false).length() > 0) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"
                            + globalValue.getCurrentOrder().getDriver_phone(false)));
                    startActivity(callIntent);
                } else {
                    showToastMessage(R.string.msg_call_phone);
                }
            }
        });
        imgCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + globalValue.getCurrentOrder()
                        .getDriver_phone(false)));
                startActivity(intent);
            }
        });
        imgSms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", globalValue.getCurrentOrder()
                        .getDriver_phone(false));
                smsIntent.putExtra("sms_body", "message");
                try {
                    startActivity(smsIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(gps, getString(R.string.message_sms), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelTrip() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_do_you_cancel)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                cancelTripAPI();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void cancelTripAPI() {
        ModelManager.cancelTrip(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), context, true,
                new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        preferencesManager.setArrived("0");
                        if (ParseJsonUtil.isSuccess(json)) {
                            preferencesManager
                                    .setPassengerCurrentScreen("MainActivity");
                            if (preferencesManager.IsStartWithOutMain()) {
                                gotoActivity(MainActivity.class);
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
                            } else {
                                finish();
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_right);
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

    private void initLocalBroadcastManager() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_DRIVER_START_TRIP);
        intentFilter.addAction(Constant.ACTION_CANCEL_TRIP);
        intentFilter.addAction(Constant.ACTION_DRIVER_ARRIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver,
                intentFilter);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_CANCEL_TRIP)) {
                showToastMessage(R.string.message_you_trip_cancel_by_driver);
                preferencesManager.setPassengerCurrentScreen("");
                preferencesManager.setPassengerIsInTrip(false);
                preferencesManager.setPassengerHavePush(false);
                if (preferencesManager.IsStartWithOutMain()) {
                    gotoActivity(MainActivity.class);
                    finish();
                } else {
                    finish();
                }
            } else {
                if (action.equals(Constant.ACTION_DRIVER_START_TRIP)) {
                    gotoActivity(StartTripForPassengerActivity.class);
                    finish();
                } else if (action.equals(Constant.ACTION_DRIVER_ARRIVED)) {
                    txtStatus.setText(getString(R.string.lbl_driver_arrived));
                    if (globalValue.getCurrentOrder() != null) {
                        updatePositionNow(globalValue.getCurrentOrder().getId());
                    }
//                    checkFake = false;
//                    setLocationLatLong(endLocation);
                }
            }
        }
    };

    public void updatePositionNow(String driverId) {
        ModelManager.getLocationDriver(this, driverId, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String latitude = jsonObject1.getString("driverLat");
                    String longitude = jsonObject1.getString("driverLon");
                    startLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))
                    ;
                    showDistanceAndTime();
                    setLocationLatLong(startLocation);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private int convertToInt(String s) {
        switch (s) {
            case "I":
                return 1;

            case "II":
                return 2;

            case "III":
                return 3;
            case "IV":
                return 4;
            case "V":
                return 5;


        }
        return 0;
    }

    public void updatePositionForPassenger(String driverId) {
        ModelManager.getLocationDriver(this, driverId, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String latitude = jsonObject1.getString("driverLat");
                    String longitude = jsonObject1.getString("driverLon");
                    Log.e("startLocation", "startLocation:" + Double.parseDouble(latitude));
                    startLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))
                    ;
                    setLocationLatLong(startLocation);
                    setStartMarker();
                    updateData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void updatePositionForPassengeNoUpdate(String driverId) {
        ModelManager.getLocationDriver(this, driverId, false, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    String latitude = jsonObject1.getString("driverLat");
                    String longitude = jsonObject1.getString("driverLon");
//                    if (Double.parseDouble(latitude) != startLocation.latitude || Double.parseDouble(longitude) != startLocation.longitude) {
                    startLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))
                    ;
                    if (dem == 60) {
                        dem = 0;
                        showDistanceAndTime();
                        Log.e("data", "data: chay vao 30");
                    } else {
                        Log.e("data", "data:" + dem);
                        dem++;
                    }

//                    if (!checkFake) {
                    setLocationLatLong(startLocation);
//                    } else {

//                    if (preferencesManager.getStringValue("updateCofirm").equals("1")) {
                    Log.e("update pass", "update pass:" + Double.parseDouble(latitude) + "-" + Double.parseDouble(longitude));
                    updateData();

//                    }
//                    }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
