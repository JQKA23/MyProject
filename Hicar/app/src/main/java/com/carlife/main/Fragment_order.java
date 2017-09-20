package com.carlife.main;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.carlife.R;
import com.carlife.global.Const;

public class Fragment_order extends Fragment implements OnClickListener {
	private Context context;
	private RelativeLayout rl_daijia, rl_rescue, rl_wash, rl_chewu;
	private String mobile;

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
		View view = inflater.inflate(R.layout.fragment_order, container, false);
		rl_daijia = (RelativeLayout) view.findViewById(R.id.rl_daijia);
		rl_daijia.setOnClickListener(this);
		rl_rescue = (RelativeLayout) view.findViewById(R.id.rl_rescue);
		rl_rescue.setOnClickListener(this);
		rl_wash = (RelativeLayout) view.findViewById(R.id.rl_wash);
		rl_wash.setOnClickListener(this);
		rl_chewu = (RelativeLayout) view.findViewById(R.id.rl_chewu);
		rl_chewu.setOnClickListener(this);
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
		Intent it = null;
		if (mobile.equals("")) {
			it = new Intent(context, BindMobileActivity.class);
		} else {
			it = new Intent(context, Act_CommonOrderList.class);
		}
		switch (v.getId()) {
		case R.id.rl_daijia:
			it.putExtra("url", "/GetOrderList");
			break;
		case R.id.rl_rescue:
			it.putExtra("url", "/GetRescueOrders");
			break;
		case R.id.rl_wash:
			it.putExtra("url", "/GetCarWashOrders");
			break;
		case R.id.rl_chewu:

			it.putExtra("url", "/GetChewuOrders");
			break;
		}
		startActivity(it);
	}
}
