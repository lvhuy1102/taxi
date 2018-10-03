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
import com.hcpt.taxinear.config.GlobalValue;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.object.CityObj;
import com.hcpt.taxinear.object.ItemTripHistory;
import com.hcpt.taxinear.object.StateObj;
import com.hcpt.taxinear.object.User;
import com.hcpt.taxinear.widget.TextViewRaleway;
import com.paypal.android.sdk.S;

public class TypeCarAdapter extends BaseAdapter {

    // public ArrayList<NewsObj> arrNews;
    private LayoutInflater mInflate;
    private ArrayList<String> arrViews;
    Activity mAct;
    User user;

    // AQuery aq;

    public TypeCarAdapter(Activity activity, ArrayList<String> arrViews) {
        this.mAct = activity;
        this.arrViews = arrViews;
        this.mInflate = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrViews.size();
    }

    public ArrayList<String> getArrViews() {
        return arrViews;
    }

    public void setArrViews(ArrayList<String> arrViews) {
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

        final HolderView holder;
        if (convertView == null) {
            holder = new HolderView();
            convertView = mInflate.inflate(R.layout.item_type_car, null);

            holder.txtState = (TextViewRaleway) convertView.findViewById(R.id.txtTypeCar);

            convertView.setTag(holder);
        } else {
            holder = (HolderView) convertView.getTag();
        }
        String itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory);
        }
        return convertView;
    }

    public class HolderView {
        TextViewRaleway txtState;
    }

    public class HolderViewDrop {
        TextViewRaleway txtState;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        final HolderViewDrop holder;
        if (convertView == null) {
            holder = new HolderViewDrop();
            convertView = mInflate.inflate(R.layout.item_state_drop, null);

            holder.txtState = (TextViewRaleway) convertView.findViewById(R.id.txtStateDrop);

            convertView.setTag(holder);
        } else {
            holder = (HolderViewDrop) convertView.getTag();
        }
        String itemTripHistory = arrViews.get(position);

        if (itemTripHistory != null) {
            holder.txtState.setText(itemTripHistory);
        }
        return convertView;
    }
}
