package com.hcpt.taxinear.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hcpt.taxinear.BaseFragment;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.ServiceUpdateLocation;
import com.hcpt.taxinear.activities.Ac_ConfirmPayByCash;
import com.hcpt.taxinear.activities.OnlineActivity;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.network.ProgressDialog;
import com.hcpt.taxinear.service.GPSTracker;
import com.hcpt.taxinear.widget.MyLocation;
import com.hcpt.taxinear.widget.TextViewRaleway;

public class BeforeOnlineFragment extends BaseFragment implements
        OnClickListener {

    Button btnOnline;
    private TextView lblTitle;
    TextViewRaleway lbl_Online;
    private TextViewRaleway lblRequest, lblWaiting;
    private GPSTracker tracker;
    private ProgressDialog pDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_before_online,
                container, false);
        tracker = new GPSTracker(getActivity());
        initUI(view);
        initControl();
        initMenuButton(view);
        return view;
    }

    public void changeLanguage() {
        btnOnline.setText(R.string.lbl_online);
        lblTitle.setText(getResources().getString(R.string.lbl_online));
        lbl_Online.setText(R.string.lbl_guide);
        lblWaiting.setText(R.string.lblClearGps);
    }

    public void initUI(View view) {
        lblTitle = (TextView) view.findViewById(R.id.lblTitle);
        lblTitle.setText(getResources().getString(R.string.lbl_online));
        btnOnline = (Button) view.findViewById(R.id.btnOnline);
        lblRequest = (TextViewRaleway) view.findViewById(R.id.lblRequest);
        lbl_Online = (TextViewRaleway) view.findViewById(R.id.lbl_Online);
        lblWaiting = (TextViewRaleway) view.findViewById(R.id.lblWaiting);
        lblTitle.setVisibility(View.GONE);
        pDialog = new ProgressDialog(self);
        pDialog.setCanceledOnTouchOutside(false);
    }

    public void initControl() {
        btnOnline.setOnClickListener(this);
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pDialog;
        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
            pDialog = new ProgressDialog(context);
            pDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (pDialog != null) {
                pDialog.show();
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblWaiting));
                    }
                });

            } else {
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                    }
                });

            }


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        if (location != null) {
                            online(location);

                        } else {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            self.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                                }
                            });

                        }
                    }
                };
                MyLocation myLocation = new MyLocation();
                myLocation.getLocation(context, locationResult);
            } else {
                if (pDialog != null) {
                    pDialog.dismiss();
                }
                self.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lblWaiting.setText(getString(R.string.lblCanNotgetGps));
                    }
                });
            }
            super.onPostExecute(aVoid);
        }

        public void online(final Location location) {

            ModelManager.online(PreferencesManager.getInstance(context)
                    .getToken(), context, false, new ModelManagerListener() {
                @Override
                public void onSuccess(String json) {

                    if (ParseJsonUtil.isSuccess(json)) {
                        Intent intent1 = new Intent(getActivity(), ServiceUpdateLocation.class);
                        getActivity().startService(intent1);
                        preferencesManager.setDriverOnline();
                        preferencesManager.setCloseService("1");
                        preferencesManager.isFromBeforeOnline(true);
                        mainActivity.gotoActivity(OnlineActivity.class);
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
//                        updateCoordinate(location.getLatitude() + "", location.getLongitude()
//                                + "");
                    } else {
                        pDialog.dismiss();
                        showToast(ParseJsonUtil.getMessage(json));
                    }
                }

                @Override
                public void onError() {
                    pDialog.dismiss();
                    showToast(R.string.message_have_some_error);
                }
            });
        }

//        public void updateCoordinate(String lat, String lon) {
//            if (!lat.isEmpty() && !lon.isEmpty()) {
//                ModelManager.updateCoordinate(preferencesManager.getToken(), lat,
//                        lon, context, false, new ModelManagerListener() {
//
//                            @Override
//                            public void onSuccess(String json) {
//                                if (ParseJsonUtil.isSuccess(json)) {
//                                    mainActivity.gotoActivity(OnlineActivity.class);
//                                    if (pDialog != null) {
//                                        pDialog.dismiss();
//                                    }
//                                } else {
//                                    Toast.makeText(getActivity(), ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//
//                            @Override
//                            public void onError() {
//
//                            }
//                        });
//            }
//
//        }
    }

    public void getDriverConfirm() {
        ModelManager.showTripHistory(preferencesManager.getToken(), "1", getActivity(), true, new ModelManagerListener() {
            @Override
            public void onError() {
                showToast(getString(R.string.message_have_some_error));
            }

            @Override
            public void onSuccess(String json) {
                if (ParseJsonUtil.isSuccess(json)) {
                    String isWaitDriverConfirm = ParseJsonUtil.pareWaitDriverConfirm(json);
                    if (isWaitDriverConfirm.equals(Constant.TRIP_WAIT_DRIVER_NOTCONFIRM)) {
                        mainActivity.gotoActivity(Ac_ConfirmPayByCash.class);
                    }
                } else {
                    showToast(ParseJsonUtil.getMessage(json));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnOnline:
//                new DownloadTask(self).execute();
                if (tracker == null) {
                    tracker = new GPSTracker(self);
                }
                if (tracker.canGetLocation()) {
                    online(tracker.getLocation());
                } else {
                    tracker.showSettingsAlert();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lblWaiting.setText(getString(R.string.lblClearGps));
    }

//    public int getLocationMode(Context context) {
//        try {
//            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }

    public void online(final Location location) {
        pDialog.show();
        ModelManager.online(PreferencesManager.getInstance(self)
                .getToken(), self, false, new ModelManagerListener() {
            @Override
            public void onSuccess(String json) {
                pDialog.dismiss();
                if (ParseJsonUtil.isSuccess(json)) {
                    Intent intent1 = new Intent(getActivity(), ServiceUpdateLocation.class);
                    getActivity().startService(intent1);
                    preferencesManager.setDriverOnline();
                    preferencesManager.setCloseService("1");
                    preferencesManager.isFromBeforeOnline(true);
                    mainActivity.gotoActivity(OnlineActivity.class);
                    if (pDialog != null) {
                        pDialog.dismiss();
                    }
//                        updateCoordinate(location.getLatitude() + "", location.getLongitude()
//                                + "");
                } else {
                    pDialog.dismiss();
                    showToast(ParseJsonUtil.getMessage(json));
                }
            }

            @Override
            public void onError() {
                pDialog.dismiss();
                showToast(R.string.message_have_some_error);
            }
        });
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            getDriverConfirm();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    public void updateCoordinate(String lat, String lon) {
//        if (!lat.isEmpty() && !lon.isEmpty()) {
//            ModelManager.updateCoordinate(preferencesManager.getToken(), lat,
//                    lon, self, false, new ModelManagerListener() {
//
//                        @Override
//                        public void onSuccess(String json) {
//                            if (ParseJsonUtil.isSuccess(json)) {
//
//                            } else {
//                                Toast.makeText(getActivity(), ParseJsonUtil.getMessage(json), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
//        }
//
//    }
}
