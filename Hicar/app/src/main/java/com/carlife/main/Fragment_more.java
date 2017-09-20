package com.carlife.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.carlife.R;
import com.carlife.global.Const;

public class Fragment_more extends Fragment implements OnClickListener {
	private Context context;
	private String mobile = "";
	private LinearLayout ll_share, ll_set, ll_feedback, ll_hotline;

	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		context = activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_more, container, false);
		ll_feedback = (LinearLayout) view.findViewById(R.id.ll_feedback);
		ll_feedback.setOnClickListener(this);
		ll_hotline = (LinearLayout) view.findViewById(R.id.ll_hotline);
		ll_hotline.setOnClickListener(this);
		ll_set = (LinearLayout) view.findViewById(R.id.ll_set);
		ll_set.setOnClickListener(this);
		ll_share = (LinearLayout) view.findViewById(R.id.ll_share);
		ll_share.setOnClickListener(this);
		return view;
	}

	@Override
	public void onResume() {
		SharedPreferences sp = context.getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Intent it;
		switch (v.getId()) {
		case R.id.ll_share:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, ShareActivity.class);
				it.putExtra("mobile", mobile);
			}
			startActivity(it);
			break;
		case R.id.ll_set:
			it = new Intent(context, MoreActivity.class);
			startActivity(it);
			break;
		case R.id.ll_feedback:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, FeedbackActivity.class);
			}
			startActivity(it);
			break;
		case R.id.ll_hotline:
			it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ getString(R.string.hotlineno)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		}
	}
}
