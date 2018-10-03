package com.hcpt.taxinear.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.object.ItemTripHistory;
import com.hcpt.taxinear.object.User;

public class HistoryAdapter extends BaseAdapter {

    // public ArrayList<NewsObj> arrNews;
    private LayoutInflater mInflate;
    private ArrayList<ItemTripHistory> arrViews;
    Activity mAct;
    User user;
    private String exChangeCurrency;
    private double change_rate;
    // AQuery aq;

    public HistoryAdapter(Activity activity, ArrayList<ItemTripHistory> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        exChangeCurrency = PreferencesManager.getInstance(activity).getStringValue(Constant.CURRENCY);
        change_rate = Double.parseDouble(PreferencesManager.getInstance(activity).getDataSettings().getExchange_rate());
    }

    @Override
    public int getCount() {
        return arrViews.size();
    }

    public ArrayList<ItemTripHistory> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<ItemTripHistory> arrViews) {
        this.arrViews = arrViews;
    }

    @Override
    public Object getItem(int position) {

        return arrViews.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        getDataFromGlobal();
        final HolderView holder;
        if (convertView == null) {
            holder = new HolderView();
            convertView = mInflate.inflate(R.layout.layout_item_history, null);

            holder.tripId = (TextView) convertView.findViewById(R.id.txtTripId);
            holder.timeEnd = (TextView) convertView.findViewById(R.id.txtTimeTo);
            holder.departure = (TextView) convertView.findViewById(R.id.txtPlaceGo);
            holder.destination = (TextView) convertView.findViewById(R.id.txtDestination);
            holder.totalTime = (TextView) convertView.findViewById(R.id.txtTime);
            holder.totalDistance = (TextView) convertView.findViewById(R.id.txtLength);
            holder.totalPoint = (TextView) convertView.findViewById(R.id.txtTotalPoint);
            holder.totalPoint.setSelected(true);
            holder.txtLinkTrip = (TextView) convertView.findViewById(R.id.txtLinkTrip);
            holder.txtPaymentMethod = (TextView) convertView.findViewById(R.id.txtPaymentMethod);

            convertView.setTag(holder);
        } else {
            holder = (HolderView) convertView.getTag();
        }
        ItemTripHistory itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.tripId.setText(itemTripHistory.getTripId() + "");
            holder.timeEnd.setText(itemTripHistory.getEndTime());
//			holder.fullName.setText("("+itemTripHistory.getDriverId()+")");
            holder.departure.setText(itemTripHistory.getStartLocaton());
            holder.destination.setText(itemTripHistory.getEndLocation());
            holder.totalDistance.setText(String.valueOf(itemTripHistory.getDistance()) + "Km");
            if (itemTripHistory.getPaymentMethod().equals("1")) {
                holder.txtPaymentMethod.setText(mAct.getString(R.string.pay_by_paypal));
            } else {
                holder.txtPaymentMethod.setText(mAct.getString(R.string.lbl_paybycash));
            }

            if (exChangeCurrency.equals(Constant.LKR)) {
                if ((itemTripHistory.getDriverId() + "").equals(PreferencesManager.getInstance(mAct).getUserID())) {
                    double priceReceive = Double.parseDouble(itemTripHistory.getActualReceive()) * change_rate;
                    if (itemTripHistory.getPaymentMethod().equals("2")) {
                        holder.totalPoint.setText("-" + mAct.getString(R.string.lbl_LKR) + Math.floor(priceReceive) + "");
                        holder.totalPoint.setBackgroundResource(R.color.from);
                    } else {
                        holder.totalPoint.setText("+" + mAct.getString(R.string.lbl_LKR) + Math.floor(priceReceive) + "");
                        holder.totalPoint.setBackgroundResource(R.color.blue);
                    }
                } else {
                    double priceFare = Double.parseDouble(itemTripHistory.getActualFare()) * change_rate;
                    holder.totalPoint.setText("-" + mAct.getString(R.string.lbl_LKR) + Math.floor(priceFare) + "");
                    holder.totalPoint.setBackgroundResource(R.color.from);
                }
            } else {
                if ((itemTripHistory.getDriverId() + "").equals(PreferencesManager.getInstance(mAct).getUserID())) {
                    if (itemTripHistory.getPaymentMethod().equals("2")) {
                        holder.totalPoint.setText("-" + mAct.getString(R.string.lbl_USD) + itemTripHistory.getActualReceive() + "");
                        holder.totalPoint.setBackgroundResource(R.color.from);
                    } else {
                        holder.totalPoint.setText("+" + mAct.getString(R.string.lbl_USD) + itemTripHistory.getActualReceive() + "");
                        holder.totalPoint.setBackgroundResource(R.color.blue);
                    }
                } else {
                    holder.totalPoint.setText("-" + mAct.getString(R.string.lbl_USD) + itemTripHistory.getActualFare() + "");
                    holder.totalPoint.setBackgroundResource(R.color.from);
                }
            }


            holder.totalTime.setText(itemTripHistory.getTotalTime() + " minutes");
            holder.txtLinkTrip.setText(convertLinkToString(itemTripHistory.getLink()) + "");
        }
        return convertView;
    }

    private void getDataFromGlobal() {
        user = GlobalValue.getInstance().user;
    }

    public class HolderView {
        ImageView imgNews;
        TextView tripId, fullName, timeStart, timeEnd, departure, destination,
                totalTime, totalDistance, totalPoint, txtLinkTrip, txtPaymentMethod;
    }

    public String convertLinkToString(String link) {
        switch (link) {
            case "I":
                return mAct.getString(R.string.sedan4);

            case "II":
                return mAct.getString(R.string.suv6);

            case "III":
                return mAct.getString(R.string.lux);
        }
        return link;
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
}
