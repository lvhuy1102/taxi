package com.hcpt.taxinear.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.googledirections.Route;
import com.hcpt.taxinear.googledirections.Routing;
import com.hcpt.taxinear.googledirections.RoutingListener;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.service.FusedLocationService;
import com.hcpt.taxinear.service.FusedLocationService.MyBinder;
import com.hcpt.taxinear.service.GPSTracker;
import com.hcpt.taxinear.utility.NetworkUtil;
import com.hcpt.taxinear.utility.NumberUtil;
import com.hcpt.taxinear.widget.TextViewFontAwesome;
import com.hcpt.taxinear.widget.TextViewRaleway;
import com.paypal.android.sdk.G;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartTripForDriverActivity extends BaseActivity implements RoutingListener, LocationListener {

    private TextViewRaleway lblName;
    private TextView lblPhone;
    private RatingBar ratingBar;
    private TextViewRaleway tvSeat;
    private TextViewRaleway lblStartLocation, lblDistance;
    private TextViewRaleway lblEndLocation, lblTimes;
    TextView txtStar;
    private TextView btnStartTrip;
    private TextView btnEndTrip;
    private ImageView imgPassenger;
    //    private TextViewFontAwesome step4, step5;
    AQuery aq;
    LocationManager locationManager;

    private GoogleMap mMap;
    private GPSTracker gps;
    private double lat;
    private double lnt;
    Handler handler;
    private ScrollView scrollView;
    LatLng startLocation, endLocation, startLocationA;
    Bitmap iconMarker;
    private CardView imgBack;
    // For timer
    private int mInterval = 1; // 5 seconds by default, can be changed later
    private Timer mTimer;
    private Routing routing;
    private Marker mMarkerStartLocation, mMarkerDriverLocation, mMarkerBeginLocation, mMarkerDriverLocationA;
    Runnable runnable;
    private CardView llHelp;
    private String phoneAdmin = "";
    private ImageView imgCall, imgSms;
    //time waiting
    private ImageButton btn_start_waiting;
    private ImageButton btn_end_waiting;
    private long a = 0, b = 0, c = 0;
    private float time_waiting = 0;
    private boolean check_end_waiting = true;
    private boolean check_start_waiting = false;
    private String time_waitting = "0";
    private TextView tv_cowndown_time1, tv_cowndown_time;

    private static final String KEY_WAITTING_TIME = "waitting_time";


    /*
    todo heith and width
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_for_driver_trip);
        aq = new AQuery(this);
        gps = new GPSTracker(this);
        initUI();
        initView();
        Maps();
        initControl();


        locationManager = (LocationManager) getSystemService(StartTripForDriverActivity.this.LOCATION_SERVICE);
        startLocationService();
        initLocalBroadcastManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateData();

        btn_start_waiting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                check_start_waiting = true;
                check_end_waiting = false;
                Calendar calendar = Calendar.getInstance();
                a = calendar.getTimeInMillis();
                Log.e("A", String.valueOf(a));
                btn_start_waiting.setVisibility(View.GONE);
                btn_end_waiting.setVisibility(View.VISIBLE);
                Toast.makeText(StartTripForDriverActivity.this, "start counting waiting time", Toast.LENGTH_SHORT).show();
            }
        });
        btn_end_waiting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long time_waiting_preferrences = preferencesManager.getLongValue(KEY_WAITTING_TIME);
                check_start_waiting = false;
                check_end_waiting = true;
                Calendar calendar = Calendar.getInstance();
                b = calendar.getTimeInMillis();
                Log.e("B", String.valueOf(b));
                btn_end_waiting.setVisibility(View.GONE);
                btn_start_waiting.setVisibility(View.VISIBLE);
                if (a != 0 && b != 0) {
                    c = b - a;
                    c = c + time_waiting_preferrences;
                    Log.e("C", String.valueOf(c));
                    time_waiting = ((float) c / (1000*60));
                    //time_waiting = (int) (c / 1000) % 60;
                    //float s = (float) time_waiting;
                    //s = s + time_waiting_preferrences;
                    //Log.e("TIME", String.valueOf(s));

                    time_waitting = time_waiting + "";
                    tv_cowndown_time.setVisibility(View.VISIBLE);
                    tv_cowndown_time1.setVisibility(View.VISIBLE);
                    tv_cowndown_time.setText( ((int)c/(1000*60))+"min:"+((int)c/1000)%60 + "'");

                    btn_end_waiting.setVisibility(View.GONE);
                    Log.e("TIME", String.valueOf(time_waiting));
                    Toast.makeText(StartTripForDriverActivity.this, "stop counting waiting time, start the trip", Toast.LENGTH_SHORT).show();
                    preferencesManager.putLongValue(KEY_WAITTING_TIME, c);
                }
            }
        });


    }

    public void updateData() {
        handler = new Handler();

        runnable = new Runnable() {

            @Override
            public void run() {
                Log.e("update location", "update location:");
                if (globalValue.getCurrentOrder() != null) {
                    updatePositionForPassenger(globalValue.getCurrentOrder().getDriverId());
                }
                handler.postDelayed(runnable, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (preferencesManager.getDriverCurrentScreen().equals("")) {
            showToastMessage(R.string.message_you_trip_cancel_by_passenger);
            gotoActivity(OnlineActivity.class);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (gps != null) {
            gps.stopSelf();
        }
        handler.removeCallbacks(runnable);
        super.onDestroy();
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
                    startLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude))
                    ;
//                    setDriverMarkerNoUpdateDirection();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void initView() {
        //setHeaderTitle(R.string.lbl_title_start_trip);
        lblName = (TextViewRaleway) findViewById(R.id.lblName);
        tvSeat = (TextViewRaleway) findViewById(R.id.tvSeat);
//        tvStepName = (TextViewRaleway) findViewById(R.id.tv_step_name);
//        tvStepDes = (TextViewRaleway) findViewById(R.id.tv_step_des);
        lblPhone = (TextView) findViewById(R.id.lblPhone);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        lblStartLocation = (TextViewRaleway) findViewById(R.id.lblStartLocation);
        lblStartLocation.setSelected(true);
        lblDistance = (TextViewRaleway) findViewById(R.id.lblDistance);
        lblTimes = (TextViewRaleway) findViewById(R.id.lblTimes);
        lblEndLocation = (TextViewRaleway) findViewById(R.id.lblEndLocation);
        txtStar = (TextView) findViewById(R.id.txtStar);
        imgPassenger = (ImageView) findViewById(R.id.imgPassenger);

        btnStartTrip = (TextView) findViewById(R.id.btnStartTrip);
        btnEndTrip = (TextView) findViewById(R.id.btnEndTrip);
        llHelp = (CardView) findViewById(R.id.cv_help);
        btnEndTrip.setVisibility(View.GONE);
        imgBack = (CardView) findViewById(R.id.cv_back);
        imgCall = (ImageView) findViewById(R.id.imgCall);
        imgSms = (ImageView) findViewById(R.id.imgSms);
        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showCancelTripDialog();
            }
        });

        btn_start_waiting = findViewById(R.id.btn_start_waiting);
        btn_end_waiting = findViewById(R.id.btn_end_waiting);
        tv_cowndown_time = findViewById(R.id.tv_cowndown_time);
        tv_cowndown_time1 = findViewById(R.id.tv_cowndown_time2);

//        step4 = (TextViewFontAwesome) findViewById(R.id.step4);
//        step5 = (TextViewFontAwesome) findViewById(R.id.step5);
//        step4.setVisibility(View.GONE);
    }

    public void sendLocationDriver() {
        ModelManager.updateCoordinate(preferencesManager.getToken(), gps.getLatitude() + "", gps.getLongitude() + "", this, true, new ModelManagerListener() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(String json) {
                if (!locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showSettingsAlert();
                    startTrip(globalValue.getCurrentOrder().getId());
                } else {
                    startTrip(globalValue.getCurrentOrder().getId());
                }
            }
        });
    }

    public void initControl() {
        btnStartTrip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check_start_waiting == true && check_end_waiting == false) {
                    Toast.makeText(StartTripForDriverActivity.this, "Please turn off the waiting time", Toast.LENGTH_SHORT).show();

                } else if (check_start_waiting == true && check_end_waiting == true) {
                    Log.e("HUY", "click start trip");
//                    btn_start_waiting.setVisibility(View.GONE);
//                    btn_end_waiting.setVisibility(View.GONE);
                    sendLocationDriver();
                } else {
                    Log.e("HUY", "click start trip2");
//                    btn_start_waiting.setVisibility(View.GONE);
//                    btn_end_waiting.setVisibility(View.GONE);
                    sendLocationDriver();
                }

            }
        });
        btnEndTrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (check_start_waiting == true && check_end_waiting == false) {
                    Toast.makeText(StartTripForDriverActivity.this, "Please turn off the waiting time", Toast.LENGTH_SHORT).show();
                } else {
                    //btnEndTrip.setEnabled(false);
                    endTripAndGetDistance();
                }
            }
        });
        lblPhone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getCurrentOrder().getPassenger_phone(false).length() > 0) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"
                            + globalValue.getCurrentOrder()
                            .getPassenger_phone(false)));
                    startActivity(callIntent);
                } else {
                    showToastMessage(R.string.msg_call_phone);
                }
            }
        });
        llHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtil.checkNetworkAvailable(StartTripForDriverActivity.this)) {
                    ModelManager.sendNeedHelp(preferencesManager.getToken(), preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false, new ModelManagerListener() {
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
        imgCall.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + globalValue.getCurrentOrder()
                        .getPassenger_phone(false)));
                startActivity(intent);
            }
        });
        imgSms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.putExtra("address", globalValue.getCurrentOrder()
                        .getPassenger_phone(false));
                smsIntent.putExtra("sms_body", "message");
                try {
                    startActivity(smsIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(gps, getString(R.string.message_sms), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initData() {
        ModelManager.getGeneralSettings(preferencesManager.getToken(),
                self, false, new ModelManagerListener() {
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
        if (getPreviousActivityName().equals(
                SplashActivity.class.getSimpleName())) {
            ModelManager.showTripDetail(preferencesManager.getToken(),
                    preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false,
                    new ModelManagerListener() {
                        @Override
                        public void onSuccess(String json) {

                            if (ParseJsonUtil.isSuccess(json)) {
                                globalValue.setCurrentOrder(ParseJsonUtil
                                        .parseCurrentOrder(json));
                                endLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getEndLat()), Double.parseDouble(ParseJsonUtil
                                        .parseCurrentOrder(json).getEndLong()));
                                lblName.setText(globalValue.getCurrentOrder()
                                        .getPassengerName());
                                tvSeat.setText(GlobalValue.convertLinkToString(StartTripForDriverActivity.this, globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
                                lblPhone.setText(globalValue.getCurrentOrder()
                                        .getPassenger_phone(false));
                                lblStartLocation.setText(globalValue
                                        .getCurrentOrder().getEndLocation());
//                                lblEndLocation.setText(globalValue
//                                        .getCurrentOrder().getEndLocation());

                                if (globalValue.getCurrentOrder()
                                        .getPassenger_rate().isEmpty()) {
                                    txtStar.setText("0");
//                                    ratingBar.setRating(0);
                                } else {
                                    txtStar.setText("" + Float
                                            .parseFloat(globalValue
                                                    .getCurrentOrder()
                                                    .getPassenger_rate()) / 2);
//                                    ratingBar.setRating(Float
//                                            .parseFloat(globalValue
//                                                    .getCurrentOrder()
//                                                    .getPassenger_rate()) / 2);
                                }

                                aq.id(R.id.imgPassenger).image(
                                        globalValue.getCurrentOrder().getImagePassenger());

                                if (preferencesManager.driverIsStartTrip()) {
                                    btnStartTrip.setVisibility(View.GONE);
                                    imgBack.setVisibility(View.GONE);
                                    btnEndTrip.setVisibility(View.VISIBLE);
                                    llHelp.setVisibility(View.VISIBLE);
//                                    step4.setVisibility(View.VISIBLE);
//                                    step5.setVisibility(View.GONE);
//                                    tvStepName.setText(getString(R.string.lbl_request_by_driving));
//                                    tvStepDes.setText(getString(R.string.lbl_notify_pasenger_arriving1));
                                    startLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                            .parseCurrentOrder(json).getEndLat()), Double.parseDouble(ParseJsonUtil
                                            .parseCurrentOrder(json).getEndLong()));
                                } else {
                                    startLocation = new LatLng(Double.parseDouble(ParseJsonUtil
                                            .parseCurrentOrder(json).getStartLat()), Double.parseDouble(ParseJsonUtil
                                            .parseCurrentOrder(json).getStartLong()));
                                }
                                getDistance();
//                                setLocationLatLong(startLocation);
                                setStartMarker();
//                                setDriverMarker();
                                setDriverMarkerA();
                                if (startLocation != null) {
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
//                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 16));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14.0f));

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

        } else {
            lblName.setText(globalValue.getCurrentOrder().getPassengerName());
            tvSeat.setText(GlobalValue.convertLinkToString(StartTripForDriverActivity.this, globalValue.convertToInt(globalValue.getCurrentOrder().getLink()) + ""));
            lblPhone.setText(globalValue.getCurrentOrder().getPassenger_phone(false));
            lblStartLocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());
            lblEndLocation.setText(globalValue.getCurrentOrder()
                    .getEndLocation());
            aq.id(R.id.imgPassenger).image(
                    globalValue.getCurrentOrder().getImagePassenger());
            if (globalValue.getCurrentOrder()
                    .getPassenger_rate().isEmpty()) {
                txtStar.setText("0");
//                                    ratingBar.setRating(0);
            } else {
                txtStar.setText("" + Float
                        .parseFloat(globalValue
                                .getCurrentOrder()
                                .getPassenger_rate()) / 2);
//                                    ratingBar.setRating(Float
//                                            .parseFloat(globalValue
//                                                    .getCurrentOrder()
//                                                    .getPassenger_rate()) / 2);
            }
            getDistance();
            endLocation = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getEndLat()), Double.parseDouble(globalValue.getCurrentOrder().getEndLong()));
//            setLocationLatLong(startLocation);
//            setDriverMarker();
            setStartMarker();
            setDriverMarkerA();
            if (startLocation != null) {
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 16));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 14.0f));
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    public void startTrip(String tripId) {

        ModelManager.startTrip(preferencesManager.getToken(), tripId, StartTripForDriverActivity.this,
                true, new ModelManagerListener() {
                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
                            fusedLocationService.connectionApi();
                            btnStartTrip.setVisibility(View.GONE);
                            btnEndTrip.setVisibility(View.VISIBLE);
                            llHelp.setVisibility(View.VISIBLE);
                            imgBack.setVisibility(View.GONE);
//                            step4.setVisibility(View.VISIBLE);
//                            step5.setVisibility(View.GONE);
//                            tvStepName.setText(getString(R.string.lbl_request_by_driving));
//                            tvStepDes.setText(getString(R.string.lbl_notify_pasenger_arriving1));
                            preferencesManager.setDriverStartTrip(true);
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

    public void endTrip(String tripId, String distance, String time_waitting) {
        ModelManager.endTrip(preferencesManager.getToken(), tripId, distance, time_waitting,
                StartTripForDriverActivity.this, false, new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {

                            preferencesManager.setDriverStartTrip(false);
                            gotoActivity(RatingPassengerActivity.class);
                            preferencesManager
                                    .setDriverCurrentScreen(RatingPassengerActivity.class
                                            .getSimpleName());
                            preferencesManager.putLongValue(KEY_WAITTING_TIME, 0);
                            finish();

                        } else {
                            btnEndTrip.setEnabled(false);
                            showToastMessage(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {
                        btnEndTrip.setEnabled(false);
                        Toast.makeText(self, getString(R.string.message_have_some_error), Toast.LENGTH_SHORT).show();
                        // enterRealDistance();
                    }
                });
    }

    private void endTripAndGetDistance() {
        final String distance = NumberUtil
                .getNumberFomatDistance(fusedLocationService
                        .getCurentDistance());
        String distanceNew = "";
        if (Double.parseDouble(distance) < 1) {
            double distanceMeter = Double.parseDouble(distance) * 1000;
            distanceNew = distanceMeter + " m";
        } else {
            distanceNew = distance + " km";
        }

        final Builder dialog = new AlertDialog.Builder(StartTripForDriverActivity.this);
        dialog.setTitle(R.string.lbl_message_real_distance);
        dialog.setMessage("The distance is: " + distanceNew);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopLocationService();
                endTrip(globalValue.getCurrentOrder().getId(), distance, time_waitting);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void initLocalBroadcastManager() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter(Constant.ACTION_CANCEL_TRIP));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constant.KEY_ACTION);
            if (action.equals(Constant.ACTION_CANCEL_TRIP)) {
                showToastMessage(R.string.message_you_trip_cancel_by_passenger);
                finish();
            }
        }
    };

    private void showCancelTripDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msg_do_you_cancel)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                cancelTrip();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void cancelTrip() {
        ModelManager.cancelTrip(preferencesManager.getToken(), globalValue
                        .getCurrentOrder().getId(), StartTripForDriverActivity.this, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {
                        if (ParseJsonUtil.isSuccess(json)) {
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

    FusedLocationService fusedLocationService;
    boolean mBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected ..............");
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinder binder = (MyBinder) service;
            fusedLocationService = binder.getService();
            mBound = true;

        }
    };

    private void startLocationService() {
        Intent intent = new Intent(StartTripForDriverActivity.this, FusedLocationService.class);
        startService(intent);
        bindService(intent, serviceConnection, StartTripForDriverActivity.this.BIND_AUTO_CREATE);
    }

    private void stopLocationService() {
        // Unbind from the service
        if (mBound) {
            unbindService(serviceConnection);
            mBound = false;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog
                .setMessage(getString(R.string.alert_gps));
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onRoutingFailure() {
        showDirection();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (startLocationA != null || endLocation != null) {
//            mMap.clear();
            setDriverMarkerAAgain();
//            setLocationLatLong(startLocation);
//            setDriverMarker();
            setStartMarker();
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.second_primary);
            polyOptions.width(10);
            polyOptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyOptions);
            lblTimes.setText(route.getDurationText());
        }
    }

    private void Maps() {
        //initData();
        setUpMap();
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

                    if (gps.canGetLocation()) {
                        startLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
                        ModelManager.showTripDetail(preferencesManager.getToken(),
                                preferencesManager.getCurrentOrderId(), StartTripForDriverActivity.this, false,
                                new ModelManagerListener() {

                                    @Override
                                    public void onSuccess(String json) {
                                        if (ParseJsonUtil.isSuccess(json)) {
                                            globalValue.setCurrentOrder(ParseJsonUtil
                                                    .parseCurrentOrder(json));
                                            startLocationA = new LatLng(Double.parseDouble(globalValue.getCurrentOrder().getStartLat()), Double.parseDouble(globalValue.getCurrentOrder().getStartLong()));
                                            initData();
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
                        gps.showSettingsAlert();
                    }
                }
            });

        }

    }

    protected void showDirection() {
        if (startLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocationA, endLocation);
        }
    }

//    public void setDriverMarker() {
//        if (startLocation != null) {
//            if (mMarkerDriverLocation != null) {
//                mMarkerDriverLocation.remove();
//            }
//            iconMarker = BitmapFactory.decodeResource(
//                    getResources(), R.drawable.ic_driver);
//            iconMarker = Bitmap.createScaledBitmap(iconMarker,
//                    iconMarker.getWidth(), iconMarker.getHeight(),
//                    false);
//            mMarkerDriverLocation = mMap.addMarker(new MarkerOptions().position(
//                    startLocation).icon(
//                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
////            showDirection();
//        }
//    }

    public void setDriverMarkerA() {
        if (startLocationA != null) {
            if (mMarkerDriverLocationA != null) {
                mMarkerDriverLocationA.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerDriverLocationA = mMap.addMarker(new MarkerOptions().position(
                    startLocationA).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
            showDirection();
        }
    }

    public void setDriverMarkerAAgain() {
        if (startLocationA != null) {
            if (mMarkerDriverLocationA != null) {
                mMarkerDriverLocationA.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerDriverLocationA = mMap.addMarker(new MarkerOptions().position(
                    startLocationA).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

//    public void setDriverMarkerNoUpdateDirection() {
//        if (startLocation != null) {
//            if (mMarkerDriverLocation != null) {
//                mMarkerDriverLocation.remove();
//            }
//            iconMarker = BitmapFactory.decodeResource(
//                    getResources(), R.drawable.ic_driver);
//            iconMarker = Bitmap.createScaledBitmap(iconMarker,
//                    iconMarker.getWidth(), iconMarker.getHeight(),
//                    false);
//            mMarkerDriverLocation = mMap.addMarker(new MarkerOptions().position(
//                    startLocation).icon(
//                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
//        }
//    }

    public void setStartMarker() {
        if (endLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
//            showDirection();
        }
    }

    public void setStartMarkerNoUpdate() {
        if (endLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        startLocation = new LatLng(location.getLatitude(), location.getLongitude());
        updateCoordinate(location.getLatitude() + "", location.getLongitude()
                + "");
//        getDistance();
//        setDriverMarkerNoUpdateDirection();
        setStartMarkerNoUpdate();
    }

    private void getDistance() {
        if (globalValue.getCurrentOrder() != null) {
            ModelManager.showDistance(preferencesManager.getToken(),
                    globalValue.getCurrentOrder().getId(), StartTripForDriverActivity.this, false,
                    new ModelManagerListener() {

                        @Override
                        public void onSuccess(String json) {
                            if (ParseJsonUtil.isSuccess(json)) {
                                try {
                                    Float temp = Float.parseFloat(ParseJsonUtil
                                            .getDistance(json));
                                    if (temp.toString().length() > 6) {
                                        lblDistance.setText(round(Double.parseDouble(temp.toString().substring(
                                                0, 6)), 1)
                                                + " " + getString(R.string.unit_measure));
                                    } else {
                                        lblDistance.setText(round(Double.parseDouble(temp.toString()), 1)
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
