package com.hcpt.taxinear.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hcpt.taxinear.BaseFragment;
import com.hcpt.taxinear.R;

public class WaitDriverConfirmFragment extends BaseFragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.layout_passenger_waiting_confirmation, container,
				false);

		return view;
	}

}
