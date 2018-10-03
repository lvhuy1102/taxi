package com.hcpt.taxinear.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.config.Constant;
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.object.RequestObj;
import com.hcpt.taxinear.widget.TextViewRaleway;

import java.math.BigDecimal;
import java.util.ArrayList;

public class RequestPassengerAdapter extends BaseAdapter {
    private final AQuery aq;
    private Context context;
    private ArrayList<RequestObj> array;
    private LayoutInflater inflater;
    IListennerAdapter iListennerAdapter;
    private String exChangeCurrency;
    private double change_rate;

    public interface IListennerAdapter {
        void onClickItemAdapter(int position);
    }

    public RequestPassengerAdapter(Context context, ArrayList<RequestObj> array, IListennerAdapter iListennerAdapter) {
        this.context = context;
        this.array = array;
        aq = new AQuery(context);
        this.iListennerAdapter = iListennerAdapter;
        inflater = LayoutInflater.from(context);
        exChangeCurrency = PreferencesManager.getInstance(context).getStringValue(Constant.CURRENCY);
        change_rate = Double.parseDouble(PreferencesManager.getInstance(context).getDataSettings().getExchange_rate());
    }

    public void setArrViews(ArrayList<RequestObj> array) {
        this.array = array;
    }

    public void updateResults(ArrayList<RequestObj> results) {
        this.array = results;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_request_passenger,
                    parent, false);
            holder = new ViewHolder();

            holder.lblStartingLocation = (TextViewRaleway) convertView
                    .findViewById(R.id.lblStartingLocation);
            holder.lblEstimateFare = (TextViewRaleway) convertView
                    .findViewById(R.id.lblEstimateFare);
            holder.lblEndingLocation = (TextViewRaleway) convertView
                    .findViewById(R.id.lblEndingLocation);
            holder.ratingBar = (RatingBar) convertView
                    .findViewById(R.id.ratingBar);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvPhone = (TextView) convertView.findViewById(R.id.tvPhone);
            holder.tvSeat = (TextView) convertView.findViewById(R.id.tvSeat);
            holder.imgPassenger = (ImageView) convertView.findViewById(R.id.imgPassenger);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        RequestObj obj = array.get(position);
        Log.e("OBJ", obj.toString());
        holder.lblStartingLocation.setText(obj.getStartLocation());
        holder.lblEndingLocation.setText(obj.getEndLocation());
        Log.e("EF", obj.getEstimate_fare());
//        if (obj.getEstimate_fare() != null) {
//            Double toBeTruncated = new Double(obj.getEstimate_fare());
//            Double truncatedDouble = new BigDecimal(toBeTruncated).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//            holder.lblEstimateFare.setText(context.getString(R.string.pointEstimated) + truncatedDouble + " KM");
//        } else {
//            holder.lblEstimateFare.setText(context.getString(R.string.pointEstimated) + String.format("%.2f", Double.parseDouble(obj.getEstimate_fare())) + " KM");
//        }
        if (exChangeCurrency.equals(Constant.LKR)) {
            String price1 = obj.getEstimate_fare().split("~")[0].trim();
            String km = obj.getEstimate_fare().split("~")[1].trim();
            double price = Double.parseDouble(price1) * change_rate;
            holder.lblEstimateFare.setText(context.getString(R.string.lblCurrencyLKR) + Math.floor(price) + " ~ " + km);
        } else {
            holder.lblEstimateFare.setText(context.getString(R.string.lblCurrencyUSD) + obj.getEstimate_fare());
        }

        if (obj.getPassengerRate().isEmpty()) {
            holder.ratingBar.setRating(0);
        } else {
            holder.ratingBar.setRating(Float.parseFloat(obj
                    .getPassengerRate()) / 2);
        }
        holder.tvSeat.setText(GlobalValue.convertLinkToString(context, obj.getLink()) + "");
        holder.tvName.setText(obj.getPassengerName() + " ");
        holder.tvPhone.setText(obj.getPassengerphone());
        aq.id(holder.imgPassenger).image(obj.getPassengerImage());
        Log.e("eeeeeee", "img: " + obj.getPassengerImage());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iListennerAdapter.onClickItemAdapter(position);
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextViewRaleway lblStartingLocation;
        TextViewRaleway lblEndingLocation, lblEstimateFare;
        RatingBar ratingBar;
        TextView tvName, tvSeat, tvPhone;
        ImageView imgPassenger;
    }

    public int convertToInt(String s) {
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
