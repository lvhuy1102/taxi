package com.hcpt.taxinear.activities;

import android.os.Bundle;
import android.widget.ListView;

import com.hcpt.taxinear.BaseActivity;
import com.hcpt.taxinear.R;
import com.hcpt.taxinear.adapters.PaymentHistoryAdapter;
import com.hcpt.taxinear.config.PreferencesManager;
import com.hcpt.taxinear.modelmanager.ModelManager;
import com.hcpt.taxinear.modelmanager.ModelManagerListener;
import com.hcpt.taxinear.modelmanager.ParseJsonUtil;
import com.hcpt.taxinear.object.ItemTransactionHistory;
import com.hcpt.taxinear.widget.pulltorefresh.PullToRefreshBase;
import com.hcpt.taxinear.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.hcpt.taxinear.widget.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;

public class PaymentHistoryActivity extends BaseActivity {

	PaymentHistoryAdapter paymentHistory;
	PullToRefreshListView pullPaymentHistory;
	ListView lvPaymentHistory;
	private int currentPage = 1;

	ArrayList<ItemTransactionHistory> listTransaction;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_payment_history);
		pullPaymentHistory = (PullToRefreshListView) findViewById(R.id.lsvHistory);
		pullPaymentHistory.setShowIndicator(false);
		listTransaction = new ArrayList<ItemTransactionHistory>();
		initAndSetHeader(R.string.title_trans_history);
		initUI();
		initUIInThis();
		getTransactionHistory();

	}

	public void initUIInThis() {
		pullPaymentHistory = (PullToRefreshListView) findViewById(R.id.lsvHistory);
		paymentHistory = new PaymentHistoryAdapter(self, listTransaction);
		pullPaymentHistory.setAdapter(paymentHistory);

		paymentHistory = new PaymentHistoryAdapter(self, listTransaction);
		lvPaymentHistory = pullPaymentHistory.getRefreshableView();
		lvPaymentHistory.setAdapter(paymentHistory);
		paymentHistory.notifyDataSetChanged();
		pullPaymentHistory
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {
					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						currentPage = 1;
						listTransaction.clear();
						getTransactionHistory();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						getTransactionHistory();
					}
				});
	}

	public void getTransactionHistory() {
		ModelManager.showTransactionHistory(PreferencesManager
				.getInstance(self).getToken(), currentPage + "", self, true,
				new ModelManagerListener() {
					@Override
					public void onSuccess(String json) {
						if (ParseJsonUtil.isSuccess(json)) {
							if (!ParseJsonUtil.parseTransactionHistory(json)
									.isEmpty()) {
								listTransaction.addAll(ParseJsonUtil
										.parseTransactionHistory(json));
								paymentHistory.setArrViews(listTransaction);
								paymentHistory.notifyDataSetChanged();
								pullPaymentHistory.onRefreshComplete();
								currentPage++;
							} else {
								pullPaymentHistory.onRefreshComplete();
								showToastMessage(getResources().getString(
										R.string.message_have_no_more_data));
							}
						} else {
							showToastMessage(ParseJsonUtil.getMessage(json));
							pullPaymentHistory.onRefreshComplete();
						}
					}

					@Override
					public void onError() {
						showToastMessage(getResources().getString(
								R.string.message_have_some_error));
						pullPaymentHistory.onRefreshComplete();
					}
				});

	}
}
