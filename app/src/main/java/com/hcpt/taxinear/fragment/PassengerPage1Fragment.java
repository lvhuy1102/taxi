package com.hcpt.taxinear.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.hcpt.taxinear.BaseFragment;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.RequestService;
import com.hcpt.taxinear.activities.PaymentPointActivity;
import com.hcpt.taxinear.activities.WaitDriverConfirmActivity;
import com.hcpt.taxinear.autocompleteaddress.PlacesAutoCompleteAdapter;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.googledirections.Route;
import com.hcpt.taxinear.googledirections.Routing;
import com.hcpt.taxinear.googledirections.RoutingListener;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.DriverOnlineObj;
import com.hcpt.taxinear.object.SettingObj;
import com.hcpt.taxinear.object.UserOnlineObj;
import com.hcpt.taxinear.service.GPSTracker;
import com.hcpt.taxinear.utility.IMaps;
import com.hcpt.taxinear.utility.MapsUtil;
import com.hcpt.taxinear.widget.TextViewPixeden;
import com.hcpt.taxinear.widget.TextViewRaleway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

public class PassengerPage1Fragment extends BaseFragment implements
        OnMyLocationChangeListener, RoutingListener {

    Handler handler;
    protected Double lat_start, lat_end, lng_start, lng_end;
    boolean processClick = true;

    // ===================== VARIABLE FOR LOG =====================
    private final String TAG = BaseFragment.class.getSimpleName();

    // ===================== VARIABLE FOR UI =====================
    private TextViewPixeden btnIcGPS, btnRefresh;
    private GoogleMap mMap;
    private TextViewRaleway lblAvailableVehicle, lbl_From, lbl_To;
    private Button btnBook;
    private Button btnBack;
    private AutoCompleteTextView txtFrom;
    private ImageView btnStart;
    private AutoCompleteTextView txtTo;
    private ImageView btnEnd;
    private TextViewRaleway btnLink1;
    private TextViewRaleway btnLink2;
    private TextViewRaleway btnLink3;
    private TextViewRaleway btnLink4;
    private TextViewRaleway btnLink5;


    // ======== VARIABLE FOR LOGIC START LOCATION AND END LOCATION ========
    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    private PlacesAutoCompleteAdapter mAdapter;
    private boolean selectFromMap = false;
    LatLng startLocation, endLocation;
    Bitmap iconMarker;
    private boolean txtFromIsSelected = false, txtToIsSelected = false;
    private Marker mMarkerStartLocation, mMarkerEndLocation;

    // ======== VARIABLE FOR DRAW DIRECTION ========

    private Routing routing;
    public PreferencesManager preferencesManager;
    protected GlobalValue globalValue;
    private IntentFilter mIntentFilter;
    Marker markerName;
    LatLngBounds.Builder builder;
    Circle circle;
    private HashMap<Integer, Marker> visibleMarkers = new HashMap<Integer, Marker>();
    ArrayList<Marker> listMarkers = new ArrayList<>();
    private boolean checkData = true;
    String estimateDistance = "";
    double price = 0;
    private View viewLink1, viewLink2, viewLink3, viewLink4, viewLink5;
    MaterialDialog dialog, dialogPayment;

    private String error_message;

    private Location mLocation;

    private String exChangeCurrency;
    private double exchange_rate;
    // ===================== @OVERRIDE =====================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_page1,
                container, false);
        preferencesManager = PreferencesManager.getInstance(getActivity());
//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(ServiceUpdateLocation.ACTION);
//        getActivity().registerReceiver(mReceiver, mIntentFilter);
        handler = new Handler();
        initUI(view);
        initView(view);
        initControl(view);
        initMenuButton(view);
        setupAutoComplete(view);
        setUpMap();
//        setUpMapOnClick();
//        goToMyLocation();
        globalValue = GlobalValue.getInstance();
        Log.e("phone", globalValue.getUser().getPhone() + "");

        return view;
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                addItemsToMap(link);
            }
        };
    }

    private void addItemsToMap(final String carType) {
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        final Location center = new Location("center");
        center.setLatitude(vr.latLngBounds.getCenter().latitude);
        center.setLongitude(vr.latLngBounds.getCenter().longitude);
        Location MiddleLeftCornerLocation = new Location("midleft");
        MiddleLeftCornerLocation.setLatitude(center.getLatitude());
        MiddleLeftCornerLocation.setLongitude(vr.latLngBounds.southwest.longitude);
        float dis = center.distanceTo(MiddleLeftCornerLocation);

        for (int i = 0; i < listMarkers.size(); i++) {
            listMarkers.get(i).remove();
            listMarkers.remove(i);
        }
        ModelManager.getTotalDriversAroundLocation(self, center.getLatitude(), center.getLongitude(), dis / 1000, carType, false, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        lblAvailableVehicle.setText(0 + "");
                    }

                    @Override
                    public void onSuccess(String json) {
                        ArrayList<DriverOnlineObj> listOnlines = new ArrayList<DriverOnlineObj>();
                        UserOnlineObj userOnlineObj = ParseJsonUtil.parseDriver(json);
                        if (userOnlineObj.getCount() != null) {
                            lblAvailableVehicle.setText(userOnlineObj.getCount());
                        } else {
                            lblAvailableVehicle.setText(0 + "");
                        }
                        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//                        listMarkers.clear();
//                        mMap.clear();
//                        if (userOnlineObj.getDriverOnlineObj() != null && userOnlineObj.getDriverOnlineObj().size() > 0) {
//                            for (int i = 0; i < listMarkers.size(); i++) {
//                                boolean checkData = true;
//                                for (DriverOnlineObj item : userOnlineObj.getDriverOnlineObj()) {
//                                    if (listMarkers.get(i).getPosition().latitude != Double.parseDouble(item.getLat()) && listMarkers.get(i).getPosition().longitude != Double.parseDouble(item.getLongitude())) {
//                                        checkData = false;
//                                    }
//                                }
//                                if (!checkData) {
//                                    listMarkers.get(i).remove();
//                                    listMarkers.remove(i);
//                                }
//                            }
                        for (DriverOnlineObj item : userOnlineObj.getDriverOnlineObj()) {
                            LatLng latLng = new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLongitude()));
                            if (bounds.contains(latLng)) {
//                                }
//                                    if (listMarkers.size() > 0) {
//                                        for (int i = 0; i < listMarkers.size(); i++) {
//                                            if (latLng.latitude != listMarkers.get(i).getPosition().latitude && latLng.longitude != listMarkers.get(i).getPosition().longitude) {
//                                                Marker marker = mMap.addMarker(new MarkerOptions()
//                                                        .position(new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLongitude()))));
//                                                switch (link) {
//                                                    case "I":
//                                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_a));
//                                                        break;
//                                                    case "II":
//                                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_b));
//                                                        break;
//                                                    case "III":
//                                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_c));
//                                                        break;
//                                                }
//
//                                                listMarkers.add(marker);
//                                            }
//                                        }
//                                    } else {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLongitude()))));
                                switch (link) {
                                    case "I":
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                        break;
                                    case "II":
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                        break;
                                    case "III":
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                        break;
                                    case "IV":
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                        break;
                                    case "V":
                                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                        break;
                                }
                                listMarkers.add(marker);
//                                    }

//                                }
//                            }
                            } /*else {
                                for (int i = 0; i < listMarkers.size(); i++) {
                                    listMarkers.get(i).remove();
                                    listMarkers.remove(i);
                                }
                            }*/
                            Log.e("list data", "list data:" + listMarkers.size());
                        }
                    }
                }

        );

    }

    @Override
    public void onResume() {
        if (mMap != null)
            mMap.clear();
        if (listMarkers.size() > 0) {
            setMarker(listMarkers);
        }
        super.onResume();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
            double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
            if (markerName != null) {
                markerName.remove();
            }
            markerName = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Driver"))
            ;
        }
    };

    public void changeLanguage() {

        btnBook.setText(R.string.btn_next);
        btnBack.setText(R.string.btn_back);
        lbl_From.setText(R.string.lbl_from);
        lbl_To.setText(R.string.lbl_to);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {

        } else {

        }
    }

    public void refreshDataDriver(String carType) {
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        final Location center = new Location("center");
        center.setLatitude(vr.latLngBounds.getCenter().latitude);
        center.setLongitude(vr.latLngBounds.getCenter().longitude);
        Location MiddleLeftCornerLocation = new Location("midleft");
        MiddleLeftCornerLocation.setLatitude(center.getLatitude());
        MiddleLeftCornerLocation.setLongitude(vr.latLngBounds.southwest.longitude);
        float dis = center.distanceTo(MiddleLeftCornerLocation);
        for (int i = 0; i < listMarkers.size(); i++) {
            listMarkers.get(i).remove();
            listMarkers.remove(i);
        }
        ModelManager.getTotalDriversAroundLocation(self, center.getLatitude(), center.getLongitude(), dis / 1000, carType, false, new ModelManagerListener() {
                    @Override
                    public void onError() {
                        lblAvailableVehicle.setText(0 + "");
                    }

                    @Override
                    public void onSuccess(String json) {
                        ArrayList<DriverOnlineObj> listOnlines = new ArrayList<DriverOnlineObj>();
                        UserOnlineObj userOnlineObj = ParseJsonUtil.parseDriver(json);
                        if (userOnlineObj.getCount() != null) {
                            lblAvailableVehicle.setText(userOnlineObj.getCount());
                        } else {
                            lblAvailableVehicle.setText(0 + "");
                        }
                        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                        if (userOnlineObj.getDriverOnlineObj() != null && userOnlineObj.getDriverOnlineObj().size() > 0) {
                            for (DriverOnlineObj item : userOnlineObj.getDriverOnlineObj()) {
                                LatLng latLng = new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLongitude()));
                                if (bounds.contains(latLng)) {
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(Double.parseDouble(item.getLat()), Double.parseDouble(item.getLongitude()))));
                                    switch (link) {
                                        case "I":
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                            break;
                                        case "II":
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                            break;
                                        case "III":
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                            break;
                                        case "IV":
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                            break;
                                        case "V":
                                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                                            break;
                                    }
                                    listMarkers.add(marker);
                                }
                            }
                        }
                    }
                }

        );

    }


    @Override
    public void onMyLocationChange(Location lastKnownLocation) {
        if (mMap != null)
            mMap.clear();
        CameraUpdate myLoc = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude())).zoom(12)
                        .build());
        mMap.moveCamera(myLoc);
        mMap.setOnMyLocationChangeListener(null);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getActivity().unregisterReceiver(mReceiver);
    }
// ===================== @OVERRIDE FOR DRAW ROUTING=====================

    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        if (startLocation != null || endLocation != null) {
            checkData = true;
            if (mMap != null)
                mMap.clear();
            setMarker(listMarkers);
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(R.color.second_primary);
            polyOptions.width(10);
            polyOptions.addAll(mPolyOptions.getPoints());
            mMap.addPolyline(polyOptions);
            setStartMarkerAgain();
            setEndMarkerAgain();
            if (mPolyOptions.getPoints().size() > 0) {
                checkData = false;
            }

        }
    }

    protected void showDirection() {
        if (startLocation != null && endLocation != null) {
            routing = new Routing(Routing.TravelMode.DRIVING);
            routing.registerListener(this);
            routing.execute(startLocation, endLocation);
        }
    }

    // ===================== FUNCTION BASE FOR FRAGMENT =====================
    public void initView(View view) {
        btnIcGPS = (TextViewPixeden) view.findViewById(R.id.btnIcGPS);
        btnRefresh = (TextViewPixeden) view.findViewById(R.id.btnRefresh);
        SupportMapFragment fm = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        fm.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });
        lblAvailableVehicle = (TextViewRaleway) view
                .findViewById(R.id.lblAvailableVehicle);
        btnBook = (Button) view.findViewById(R.id.btnBook);
        btnBack = (Button) view.findViewById(R.id.btnBack);
        txtFrom = (AutoCompleteTextView) view.findViewById(R.id.txtFrom);
        btnStart = (ImageView) view.findViewById(R.id.btnStart);
        txtTo = (AutoCompleteTextView) view.findViewById(R.id.txtTo);
        btnEnd = (ImageView) view.findViewById(R.id.btnEnd);
        btnLink1 = (TextViewRaleway) view.findViewById(R.id.btnLink1);
        btnLink1.setTypeface(null, Typeface.BOLD);
        btnLink2 = (TextViewRaleway) view.findViewById(R.id.btnLink2);
        btnLink3 = (TextViewRaleway) view.findViewById(R.id.btnLink3);
        btnLink4 = view.findViewById(R.id.btnLink4);
        btnLink5 = view.findViewById(R.id.btnLink5);
        viewLink1 = (View) view.findViewById(R.id.viewLink1);
        viewLink2 = (View) view.findViewById(R.id.viewLink2);
        viewLink3 = (View) view.findViewById(R.id.viewLink3);
        viewLink4 = view.findViewById(R.id.viewLink4);
        viewLink5 = view.findViewById(R.id.viewLink5);
        lbl_From = (TextViewRaleway) view.findViewById(R.id.lbl_From);
        lbl_To = (TextViewRaleway) view.findViewById(R.id.lbl_To);
        Log.e("HUY", preferencesManager.getStringValue(Constant.CURRENCY));
        exChangeCurrency = preferencesManager.getStringValue(Constant.CURRENCY);
        exchange_rate = Double.parseDouble(preferencesManager.getDataSettings().getExchange_rate());
    }

    public void createServiceRequest() {
//        Intent intent = new Intent();
//        intent.setAction("com.htcp.taxinear.ACTION_REQUEST");
//        getContext().sendBroadcast(intent);
        getMainActivity().startService(new Intent(getMainActivity(), RequestService.class));
    }

    private boolean isValidate() {

        if (txtFrom.getText().toString().trim().isEmpty()) {
            error_message = getString(R.string.message_enter_form);
            return false;
        } else if (txtTo.getText().toString().trim().isEmpty()) {
            error_message = getString(R.string.message_enter_to);
            return false;
        }

        return true;
    }

    public void initControl(View view) {
        btnBook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("BOOK", "CLICK");
                if (isValidate()) {
                    if (startLocation == null || endLocation == null) {
                        showToast(R.string.message_please_select_start_and_end);
                    } else {
                        if (globalValue.getUser().getPhone().length() > 0) {
                            Log.e("BOOK", "CLICK1");
//                        if (!checkData) {
                            showDialogCreateRequest(link);
//                            createRequest(link);
//                        } else {
//                            showToast(getString(R.string.checkDirection));
//                        }


                        } else {
                            showToast(R.string.msg_no_phone_number);
                        }
                    }
                } else {
                    Toast.makeText(self, error_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                btnBack.setVisibility(View.GONE);
            }
        });

        btnLink1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromLink();
                viewLink1.setVisibility(View.VISIBLE);
                viewLink2.setVisibility(View.GONE);
                viewLink3.setVisibility(View.GONE);
                viewLink4.setVisibility(View.GONE);
                viewLink5.setVisibility(View.GONE);
                btnBook.setBackgroundResource(R.drawable.tuk);
                selectPeople(1);
                addItemsToMap(link);
            }
        });

        btnLink2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromLink();
                viewLink1.setVisibility(View.GONE);
                viewLink2.setVisibility(View.VISIBLE);
                viewLink3.setVisibility(View.GONE);
                viewLink4.setVisibility(View.GONE);
                viewLink5.setVisibility(View.GONE);
                btnBook.setBackgroundResource(R.drawable.nano);
                selectPeople(2);
                addItemsToMap(link);
            }
        });

        btnLink3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromLink();
                viewLink1.setVisibility(View.GONE);
                viewLink2.setVisibility(View.GONE);
                viewLink3.setVisibility(View.VISIBLE);
                viewLink4.setVisibility(View.GONE);
                viewLink5.setVisibility(View.GONE);
                btnBook.setBackgroundResource(R.drawable.mini);
                selectPeople(3);
                addItemsToMap(link);
            }
        });
        btnLink4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromLink();
                viewLink1.setVisibility(View.GONE);
                viewLink2.setVisibility(View.GONE);
                viewLink3.setVisibility(View.GONE);
                viewLink4.setVisibility(View.VISIBLE);
                viewLink5.setVisibility(View.GONE);
                btnBook.setBackgroundResource(R.drawable.car);
                selectPeople(4);
                addItemsToMap(link);

            }
        });
        btnLink5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromLink();
                viewLink1.setVisibility(View.GONE);
                viewLink2.setVisibility(View.GONE);
                viewLink3.setVisibility(View.GONE);
                viewLink4.setVisibility(View.GONE);
                viewLink5.setVisibility(View.VISIBLE);
                btnBook.setBackgroundResource(R.drawable.van);
                selectPeople(5);
                addItemsToMap(link);
            }
        });

//        btnIcGPS.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                goToMyLocation();
//            }
//        });

        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                txtFrom.getText().clear();
//                txtTo.getText().clear();
//                if (mMarkerEndLocation != null) {
//                    mMarkerStartLocation.remove();
//                }
//                if (mMarkerEndLocation != null) {
//                    mMarkerEndLocation.remove();
//                }
//                startLocation = null;
//
//                if (circle != null) {
//                    circle.remove();
//                }

                refreshDriver();
            }
        });

        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mMap.getMyLocation();
                final GPSTracker tracker = new GPSTracker(mainActivity);
                if (tracker.canGetLocation() == false) {
                    tracker.showSettingsAlert();
                    showToast(R.string.message_wait_for_location);
                } else {
                    if (location != null) {
                        startLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(startLocation, 12));
                        setStartMarker();
                        txtFrom.setText(getCompleteAddressString(startLocation.latitude, startLocation.longitude));
                        closeKeyboard();
                        selectFromMap = true;

                        if (circle != null) {
                            circle.remove();
//                        mMap.clear();
                        }
                        circle = mMap.addCircle(new CircleOptions()
                                .center(startLocation)
                                .radius(3000)
                                .strokeWidth(0.5f)
                                .strokeColor(Color.rgb(0, 136, 255))
                                .fillColor(Color.argb(20, 0, 136, 255)));
                        txtFrom.setDropDownHeight(0);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.cannot_location), Toast.LENGTH_SHORT).show();
                    }


//                    new MapsUtil.GetAddressByLatLng(new IMaps() {
//                        @Override
//                        public void processFinished(Object obj) {
//                            String address = (String) obj;
//                            if (!address.isEmpty()) {
//                                // Set marker's title
//                                selectFromMap = true;
//                                txtFrom.setText(getCompleteAddressString(startLocation.latitude, startLocation.longitude));
//                                //txtFrom.setText(address);
//                            }
//                        }
//                    }).execute(startLocation);
                }
            }
        });

        btnEnd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Location location = mMap.getMyLocation();
                final GPSTracker tracker = new GPSTracker(mainActivity);
                if (tracker.canGetLocation() == false) {
                    tracker.showSettingsAlert();
                    showToast(R.string.message_wait_for_location);
                } else {
                    if (location != null) {
                        endLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(endLocation, 12));
                        setEndMarker();
                        setStartMarker();
                        txtTo.setText(getCompleteAddressString(endLocation.latitude, endLocation.longitude));
                        closeKeyboard();
                        selectFromMap = true;
                        txtTo.setDropDownHeight(0);

//                    new MapsUtil.GetAddressByLatLng(new IMaps() {
//                        @Override
//                        public void processFinished(Object obj) {
//                            String address = (String) obj;
//                            if (!address.isEmpty()) {
//                                // Set marker's title
//                                selectFromMap = true;
//                                txtTo.setText(address);
//                            }
//                        }
//                    }).execute(endLocation);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.cannot_location), Toast.LENGTH_SHORT).show();
                    }

                }

            }

        });

        lbl_From.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFrom.requestFocus();
                txtFromIsSelected = true;
                txtToIsSelected = false;
            }
        });
        lbl_To.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtTo.requestFocus();
                txtFromIsSelected = false;
                txtToIsSelected = true;
            }
        });

    }

    public void removeDataFromLink() {
        Log.e("marker", "marker:" + listMarkers.size());
        for (int i = 0; i < listMarkers.size(); i++) {
            Log.e("marker", "marker:" + listMarkers.get(i).getPosition().latitude + "-" + listMarkers.get(i).getPosition().longitude);
            listMarkers.get(i).remove();
            listMarkers.remove(i);
        }
        Log.e("marker", "marker after:" + listMarkers.size());
    }

    public void refreshDriver() {
        final GPSTracker tracker = new GPSTracker(mainActivity);

        if (tracker.canGetLocation() == false) {
            tracker.showSettingsAlert();
            showToast("Wait for location service");
        } else {
            LatLng currentLatLng = new LatLng(tracker.getLatitude(), tracker.getLongitude());
            CameraUpdate myLoc = CameraUpdateFactory
                    .newCameraPosition(new CameraPosition.Builder()
                            .target(currentLatLng).zoom(13).build());
            mMap.moveCamera(myLoc);
            refreshDataDriver(link);
        }

    }

    public void showDialogCreateRequest(final String link) {
        price = 0;
        String ppk = "0";
        String ppm = "0";
        String sf = "0";
        int linkPosition = 1;
        SettingObj settingObj = preferencesManager.getDataSettings();
        float[] results = new float[1];
        String msg = getString(R.string.msgEstimatedFare);
        Log.e("MSG", msg);
        Location.distanceBetween(startLocation.latitude, startLocation.longitude, endLocation.latitude, endLocation.longitude, results);
        estimateDistance = round(results[0] / 1000, 2) + "";
        Log.e("PRICE", estimateDistance);

        dialog = new MaterialDialog(getActivity());
        dialog.setPositiveButton("Book Now", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalValue.getUser().getBalance() != null) {
                    if (exChangeCurrency.equals(Constant.LKR)) {
                        if (globalValue.getUser().getBalance() >= price) {
                            createRequest(link);
                        } else {
                            String msg = getString(R.string.msgValidateBalance);
                            double price1 = globalValue.getUser().getBalance() * exchange_rate;
                            msg = msg.replace("[a]", price1 + "");
                            msg = msg.replace("[b]", getString(R.string.lbl_LKR) + Math.floor(round(price, 2) * exchange_rate) + "");
                            showDialogAddPaymentForRequest(msg);
                        }
                    } else {
                        if (globalValue.getUser().getBalance() >= price) {
                            createRequest(link);
                        } else {
                            String msg = getString(R.string.msgValidateBalance);
                            msg = msg.replace("[a]", globalValue.getUser().getBalance() + "");
                            msg = msg.replace("[b]", getString(R.string.lbl_USD) + round(price, 2) + "");
                            showDialogAddPaymentForRequest(msg);
                        }
                    }

                }
            }
        }).setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        switch (link) {
            case "I":
                ppk = settingObj.getPpk_of_link_i();
                ppm = settingObj.getPpm_of_link_i();
                sf = settingObj.getSf_of_link_i();
                linkPosition = 1;
                break;
            case "II":
                ppk = settingObj.getPpk_of_link_ii();
                ppm = settingObj.getPpm_of_link_ii();
                sf = settingObj.getSf_of_link_ii();
                linkPosition = 2;
                break;
            case "III":
                ppk = settingObj.getPpk_of_link_iii();
                ppm = settingObj.getPpm_of_link_iii();
                sf = settingObj.getSf_of_link_iii();
                linkPosition = 3;
                break;
            case "IV":
                ppk = settingObj.getPpk_of_link_iv();
                ppm = settingObj.getPpm_of_link_iv();
                sf = settingObj.getSf_of_link_iv();
                linkPosition = 4;
                break;
            case "V":
                ppk = settingObj.getPpk_of_link_v();
                ppm = settingObj.getPpm_of_link_v();
                sf = settingObj.getSf_of_link_v();
                linkPosition = 5;
                break;
        }
        if (settingObj != null) {
//            if (exChangeCurrency.equals(Constant.LKR)) {
//                price = Double.parseDouble(sf) + (round(results[0] / 1000, 2) / Double.parseDouble(settingObj.getEstimate_fare_speed())) * 60 * Double.parseDouble(ppm) + (round(results[0] / 1000, 2)) * Double.parseDouble(ppk) * change_rate;
//                Log.e("MONEY", String.valueOf(price));
//            } else {
            price = Double.parseDouble(sf) + (round(results[0] / 1000, 2) / Double.parseDouble(settingObj.getEstimate_fare_speed())) * 60 * Double.parseDouble(ppm) + (round(results[0] / 1000, 2)) * Double.parseDouble(ppk);
            Log.e("MONEY", String.valueOf(price));
            //}

        }
        Log.e("HUY", exChangeCurrency + "nulll");
        if (exChangeCurrency.equals(Constant.USD)) {
            preferencesManager.putStringValue("distanceLocation", round(price, 2) + "");
            msg = msg.replace("LKR %.2f", getString(R.string.lblCurrencyUSD) + " " + round(price, 2));

        } else {
            preferencesManager.putStringValue("distanceLocation", round(price, 2) + "");
            msg = msg.replace("LKR %.2f", getString(R.string.lblCurrencyLKR) + " " + Math.floor(round(price, 2) * exchange_rate));
        }
//        preferencesManager.putStringValue("distanceLocation", round(price, 2) + "");
//        msg = msg.replace("%.2f", round(price, 2) + "");
        dialog.setMessage(msg);
        dialog.show();
    }

    public void showDialogAddPaymentForRequest(String message) {
        dialogPayment = new MaterialDialog(getActivity());
        dialogPayment.setMessage(message);
        dialogPayment.setPositiveButton("Add Point", new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
//                Intent intent = new Intent(getActivity(), AddPointActivity.class);
//                intent.putExtra("point", round(price, 2));
                startActivity(PaymentPointActivity.class);
            }
        });
        dialogPayment.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPayment.dismiss();
            }
        });
        dialogPayment.show();
    }

    private void refreshData(String carType) {
        final GPSTracker tracker = new GPSTracker(mainActivity);
        if (tracker.canGetLocation() == false) {
            lblAvailableVehicle.setText(0 + "");
        } else {
            ModelManager.getTotalDriversAroundLocation(self, tracker.getLatitude(), tracker.getLongitude(), 3, carType, true, new ModelManagerListener() {
                @Override
                public void onError() {
                    lblAvailableVehicle.setText(0 + "");
                }

                @Override
                public void onSuccess(String json) {
                    if (ParseJsonUtil.isSuccess(json)) {
                        lblAvailableVehicle.setText(ParseJsonUtil.getDriverCount(json));
                    } else {
                        lblAvailableVehicle.setText(0 + "");
                    }
                }
            });

        }
    }

    String link = "I";

    private void selectPeople(int num) {
        if (num == 1) {
            link = "I";
            btnLink1.setTypeface(null, Typeface.BOLD);
            btnLink2.setTypeface(null, Typeface.NORMAL);
            btnLink3.setTypeface(null, Typeface.NORMAL);
            btnLink4.setTypeface(null, Typeface.NORMAL);
            btnLink5.setTypeface(null, Typeface.NORMAL);
        } else if (num == 2) {
            link = "II";
            btnLink1.setTypeface(null, Typeface.NORMAL);
            btnLink2.setTypeface(null, Typeface.BOLD);
            btnLink3.setTypeface(null, Typeface.NORMAL);
            btnLink4.setTypeface(null, Typeface.NORMAL);
            btnLink5.setTypeface(null, Typeface.NORMAL);
        } else if (num == 3) {
            link = "III";
            btnLink1.setTypeface(null, Typeface.NORMAL);
            btnLink2.setTypeface(null, Typeface.NORMAL);
            btnLink3.setTypeface(null, Typeface.BOLD);
            btnLink4.setTypeface(null, Typeface.NORMAL);
            btnLink5.setTypeface(null, Typeface.NORMAL);
        } else if (num == 4) {
            link = "IV";
            btnLink1.setTypeface(null, Typeface.NORMAL);
            btnLink2.setTypeface(null, Typeface.NORMAL);
            btnLink3.setTypeface(null, Typeface.NORMAL);
            btnLink4.setTypeface(null, Typeface.BOLD);
            btnLink5.setTypeface(null, Typeface.NORMAL);
        } else if (num == 5) {
            link = "V";
            btnLink1.setTypeface(null, Typeface.NORMAL);
            btnLink2.setTypeface(null, Typeface.NORMAL);
            btnLink3.setTypeface(null, Typeface.NORMAL);
            btnLink4.setTypeface(null, Typeface.NORMAL);
            btnLink5.setTypeface(null, Typeface.BOLD);
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void createRequest(final String link) {
        if (startLocation == null) {
            startLocation = new LatLng(Double.parseDouble(preferencesManager.getStringValue("STARTLATITUDE")), Double.parseDouble(preferencesManager.getStringValue("STARTLONGITUDE")));
        }
        if (endLocation == null) {
            endLocation = new LatLng(Double.parseDouble(preferencesManager.getStringValue("ENDLATITUDE")), Double.parseDouble(preferencesManager.getStringValue("ENDLONGITUDE")));
        }

        if (estimateDistance == null) {
            estimateDistance = preferencesManager.getStringValue("distanceLocation");
        }
        ModelManager.createRequest(preferencesManager.getToken(), link, startLocation.latitude + "",
                startLocation.longitude + "", txtFrom.getText().toString(),
                endLocation.latitude + "", endLocation.longitude + "", txtTo
                        .getText().toString(), estimateDistance, mainActivity, true,
                new ModelManagerListener() {

                    @Override
                    public void onSuccess(String json) {

                        if (ParseJsonUtil.isSuccess(json)) {
                            setDataCreateRequest(estimateDistance, link);
                            createServiceRequest();
                            globalValue.setEstimate_fare(ParseJsonUtil.getEstimateFare(json));
                            preferencesManager.putStringValue("countDriver", ParseJsonUtil.getCountDriver(json));
                            preferencesManager.putStringValue("estimate", globalValue.getEstimate_fare());
                            mainActivity.gotoActivity(WaitDriverConfirmActivity.class);
                            dialog.dismiss();
                        } else {
                            showToast(ParseJsonUtil.getMessage(json));
                        }
                    }

                    @Override
                    public void onError() {

                        showToast(R.string.message_have_some_error);
                    }
                });
    }

    public void setDataCreateRequest(String estimateDistance, String link) {
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LATITUDE, startLocation.latitude + "");
        preferencesManager.putStringValue(Constant.KEY_STARTLOCATION_LONGITUDE, startLocation.longitude + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LATITUDE, endLocation.latitude + "");
        preferencesManager.putStringValue(Constant.KEY_ENDLOCATION_LONGITUDE, endLocation.latitude + "");
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_START, txtFrom.getText().toString());
        preferencesManager.putStringValue(Constant.KEY_ADDRESS_TO, txtTo.getText().toString());
        preferencesManager.putStringValue(Constant.KEY_LINK, link);
        preferencesManager.putStringValue(Constant.KEY_ESTIMATE_DISTANCE, estimateDistance);
    }

    private void setUpMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    mMap.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            mMap.clear();
                            mLocation = location;
                            CameraUpdate myLoc = CameraUpdateFactory
                                    .newCameraPosition(new CameraPosition.Builder()
                                            .target(new LatLng(location.getLatitude(),
                                                    location.getLongitude())).zoom(12)
                                            .build());
                            mMap.moveCamera(myLoc);
                            mMap.setOnMyLocationChangeListener(null);
                            setUpMapOnClick();
                            mMap.setOnCameraChangeListener(getCameraChangeListener());
                        }
                    });
                }
            });
        }

        //close when fake location
        if (mMap != null)
            mMap.setOnMyLocationChangeListener(this);
    }

    // ===================== SETUP AUTO COMPLETE ADDRESS =====================
    public void setStartMarker() {
        if (startLocation != null) {
            preferencesManager.putStringValue("STARTLATITUDE", startLocation.latitude + "");
            preferencesManager.putStringValue("STARTLONGITUDE", startLocation.longitude + "");
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
            showDirection();
        }
    }

    public void setStartMarkerAgain() {
        if (startLocation != null) {
            if (mMarkerStartLocation != null) {
                mMarkerStartLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_position_a);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerStartLocation = mMap.addMarker(new MarkerOptions().position(
                    startLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

    public void setEndMarker() {
        if (endLocation != null) {
            preferencesManager.putStringValue("ENDLATITUDE", endLocation.latitude + "");
            preferencesManager.putStringValue("ENDLONGITUDE", endLocation.longitude + "");
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
            showDirection();
        }
    }

    public void setEndMarkerAgain() {
        if (endLocation != null) {
            if (mMarkerEndLocation != null) {
                mMarkerEndLocation.remove();
            }
            iconMarker = BitmapFactory.decodeResource(
                    mainActivity.getResources(), R.drawable.ic_position_b);
            iconMarker = Bitmap.createScaledBitmap(iconMarker,
                    iconMarker.getWidth(), iconMarker.getHeight(),
                    false);
            mMarkerEndLocation = mMap.addMarker(new MarkerOptions().position(
                    endLocation).icon(
                    BitmapDescriptorFactory.fromBitmap(iconMarker)));
        }
    }

    public void setUpMapOnClick() {
        // Click on map to get latitude and longitude.
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc) {
                // Hiding the keyboard when tab on map.
//                new MapsUtil.GetAddressByLatLng(new IMaps() {
//                    @Override
//                    public void processFinished(Object obj) {
//                        String address = (String) obj;
//                        if (!address.isEmpty()) {
//                            // Set marker's title
//                            if (txtFromIsSelected) {
//                                txtFrom.setText(address);
//                            } else {
//                                if (txtToIsSelected) {
//                                    txtTo.setText(address);
//                                }
//                            }
//                        }
//                    }
//                }).execute(loc);
                if (txtFromIsSelected) {
                    txtFrom.setText(getCompleteAddressString(loc.latitude, loc.longitude));
                } else {
                    if (txtToIsSelected) {
                        txtTo.setText(getCompleteAddressString(loc.latitude, loc.longitude));
                    }
                }
                closeKeyboard();
                selectFromMap = true;
                if (txtFromIsSelected) {
                    if (circle != null) {
                        circle.remove();
//                        mMap.clear();
                    }
                    startLocation = loc;
                    setStartMarker();
                    circle = mMap.addCircle(new CircleOptions()
                            .center(startLocation)
                            .radius(3000)
                            .strokeWidth(0.5f)
                            .strokeColor(Color.rgb(0, 136, 255))
                            .fillColor(Color.argb(20, 0, 136, 255)));
                    txtFrom.setDropDownHeight(0);
                } else {
                    if (txtToIsSelected) {
                        endLocation = loc;
                        setEndMarker();
                        txtTo.setDropDownHeight(0);
                    }
                }
                // Get address by latlng async

            }
        });
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                if (returnedAddress.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        if (i < returnedAddress.getMaxAddressLineIndex() - 1) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(", ");
                        } else {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("");
                        }

                    }
                } else {
                    strReturnedAddress.append(returnedAddress.getAddressLine(0));
                }
                strAdd = strReturnedAddress.toString();
                Log.e("CURENTLOCATION", "" + strReturnedAddress.toString());
            } else {
                Log.e("CURENTLOCATION", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CURENTLOCATION", "Canont get Address!");
        }
        return strAdd;
    }

    public void setMarker(ArrayList<Marker> listMarkers) {
        ArrayList<Marker> listMarkerDatas = new ArrayList<>();
        listMarkerDatas.addAll(listMarkers);
        listMarkers.clear();
        for (int i = 0; i < listMarkerDatas.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(listMarkerDatas.get(i).getPosition()));
            switch (link) {
                case "I":
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                    break;
                case "II":
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                    break;
                case "III":
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                    break;
                case "IV":
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                    break;
                case "V":
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver_men));
                    break;
            }
            listMarkers.add(marker);
        }
    }

    public void setupAutoComplete(View view) {
        if (mThreadHandler == null) {
            mHandlerThread = new HandlerThread(TAG,
                    android.os.Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();

            // Initialize the Handler
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        ArrayList<String> results = mAdapter.resultList;

                        if (results != null && results.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            mAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            };
        }
        txtFrom = (AutoCompleteTextView) view.findViewById(R.id.txtFrom);
        txtFrom.setAdapter(new PlacesAutoCompleteAdapter(self,
                R.layout.item_auto_place));
        txtFrom.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtFromIsSelected = true;
                } else {
                    txtFromIsSelected = false;
                }
            }
        });
        txtFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                closeKeyboard();
// Get data associated with the specified position
// in the list (AdapterView)
                final String description = (String) parent
                        .getItemAtPosition(position);

                // Move camera to new address.
                new MapsUtil.GetLatLngByAddress(new IMaps() {

                    @Override
                    public void processFinished(Object obj) {
                        try {
                            // Move camera smoothly
                            LatLng latLng = (LatLng) obj;
                            if (((LatLng) obj).latitude != 0.0 && ((LatLng) obj).longitude != 0.0) {
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(latLng, 12));

                                // Add marker
                                startLocation = latLng;
                                setStartMarker();
                            } else {
                                Toast.makeText(getActivity(), "Can not find location, please try again", Toast.LENGTH_SHORT).show();
                            }

                            // Set marker's title
                            // String address = description.replace("%20", " ");
                            // mMarker.setTitle(address);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).execute(description);
            }
        });

        txtFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                final String value = s.toString();
                if (value.length() > 0) {
                    // Remove all callbacks and messages
                    mThreadHandler.removeCallbacksAndMessages(null);

                    // Now add a new one
                    mThreadHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mAdapter == null) {
                                mAdapter = new PlacesAutoCompleteAdapter(self,
                                        R.layout.item_auto_place);
                            }
                            // Background thread
                            mAdapter.resultList = mAdapter.mPlaceAPI
                                    .autocomplete(value);

                            // Footer
                            if (mAdapter.resultList.size() > 0) {
                                mAdapter.resultList.add("footer");
                            }

                            // Post to Main Thread
                            mThreadHandler.sendEmptyMessage(1);
                        }
                    }, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectFromMap) {
                    selectFromMap = false;
                } else {
                    txtFrom.setDropDownHeight(LayoutParams.WRAP_CONTENT);
                }
            }
        });

        txtTo = (AutoCompleteTextView) view.findViewById(R.id.txtTo);
        txtTo.setAdapter(new PlacesAutoCompleteAdapter(self,
                R.layout.item_auto_place));
        txtTo.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    txtToIsSelected = true;
                } else {
                    txtToIsSelected = false;
                }
            }
        });
        txtTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                closeKeyboard();
                // Get data associated with the specified position
                // in the list (AdapterView)
                final String description = (String) parent
                        .getItemAtPosition(position);

                // Move camera to new address.
                new MapsUtil.GetLatLngByAddress(new IMaps() {

                    @Override
                    public void processFinished(Object obj) {
                        try {
                            // Move camera smoothly
                            LatLng latLng = (LatLng) obj;
                            if (((LatLng) obj).latitude != 0.0 && ((LatLng) obj).longitude != 0.0) {
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(latLng, 11));

                                // Add marker
                                endLocation = latLng;
                                setEndMarker();
                            } else {
                                Toast.makeText(getActivity(), "Can not find location, please try again", Toast.LENGTH_SHORT).show();
                            }
                            // Set marker's title
                            // String address = description.replace("%20", " ");
                            // mMarker.setTitle(address);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).execute(description);
            }
        });

        txtTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                final String value = s.toString();
                if (value.length() > 0) {
                    // Remove all callbacks and messages
                    mThreadHandler.removeCallbacksAndMessages(null);

                    // Now add a new one
                    mThreadHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mAdapter == null) {
                                mAdapter = new PlacesAutoCompleteAdapter(self,
                                        R.layout.item_auto_place);
                            }
                            // Background thread
                            mAdapter.resultList = mAdapter.mPlaceAPI
                                    .autocomplete(value);

                            // Footer
                            if (mAdapter.resultList.size() > 0) {
                                mAdapter.resultList.add("footer");
                            }

                            // Post to Main Thread
                            mThreadHandler.sendEmptyMessage(1);
                        }
                    }, 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectFromMap) {
                    selectFromMap = false;
                } else {
                    txtTo.setDropDownHeight(LayoutParams.WRAP_CONTENT);
                }
            }
        });
    }

//    public void goToMyLocation() {
//        final GPSTracker tracker = new GPSTracker(mainActivity);
//
//        if (tracker.canGetLocation() == false) {
//            tracker.showSettingsAlert();
//            showToast("Wait for location service");
//        } else {
//            LatLng currentLatLng = new LatLng(tracker.getLatitude(), tracker.getLongitude());
//            CameraUpdate myLoc = CameraUpdateFactory
//                    .newCameraPosition(new CameraPosition.Builder()
//                            .target(currentLatLng).zoom(13).build());
//            mMap.moveCamera(myLoc);
////            refreshData();
//        }
//    }

// ===================== DOMAIN =====================

    private void closeKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (txtFrom.hasFocus()) {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(txtFrom.getWindowToken(), 0);
            }

            if (txtTo.hasFocus()) {
                imm.hideSoftInputFromWindow(txtTo.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(txtTo.getWindowToken(), 0);
            }
        } catch (Exception ex) {

        }
    }

    public void back() {
        clear();
        btnBack.setVisibility(View.GONE);
    }

    public void clear() {
        if (mMap != null)
            mMap.clear();
        startLocation = null;
        endLocation = null;
        txtFrom.setText("");
        txtTo.setText("");
    }
}
