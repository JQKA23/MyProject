package com.carlife.main;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.MyInterface.RefreshMainUIListener;
import com.carlife.member.BonusActivity;
import com.carlife.member.GiftCardListActivity;
import com.carlife.member.MemberCardActivity;
import com.carlife.member.MyCarsActivity;
import com.carlife.member.MyLicenseActivity;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class Fragment_mine extends Fragment implements OnClickListener {
	private TextView tv_name, tv_mobile, tv_order, tv_car, tv_license,
			tv_balance, tv_gift, tv_redpacket;
	private LinearLayout ll_redpacket, ll_balance, ll_gift;
	private Context context;
	private String mobile = "";
	private RefreshMainUIListener listener;
	private CustomProgressDialog cpd;
	private final static int getaccount_fail = 0;
	private final static int getaccount_success = 1;

	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		context = activity;
		listener = (RefreshMainUIListener) activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		SharedPreferences sp = context.getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		tv_mobile.setText(mobile);
		getMyAccount();
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mine, container, false);
		ll_balance = (LinearLayout) view.findViewById(R.id.ll_balance);
		ll_balance.setOnClickListener(this);
		ll_gift = (LinearLayout) view.findViewById(R.id.ll_gift);
		ll_gift.setOnClickListener(this);
		ll_redpacket = (LinearLayout) view.findViewById(R.id.ll_redpacket);
		ll_redpacket.setOnClickListener(this);
		tv_order = (TextView) view.findViewById(R.id.tv_order);
		tv_order.setOnClickListener(this);
		tv_car = (TextView) view.findViewById(R.id.tv_car);
		tv_car.setOnClickListener(this);
		tv_license = (TextView) view.findViewById(R.id.tv_license);
		tv_license.setOnClickListener(this);
		tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_mobile = (TextView) view.findViewById(R.id.tv_mobile);
		tv_balance = (TextView) view.findViewById(R.id.tv_balance);
		tv_gift = (TextView) view.findViewById(R.id.tv_gift);
		tv_redpacket = (TextView) view.findViewById(R.id.tv_redpacket);
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Intent it;
		switch (v.getId()) {
		case R.id.tv_order:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				listener.setFragment(2);
			}
			break;
		case R.id.tv_car:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, MyCarsActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_license:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, MyLicenseActivity.class);
			}
			startActivity(it);
			break;
		case R.id.ll_balance:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, MemberCardActivity.class);
			}
			startActivity(it);
			break;
		case R.id.ll_gift:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, GiftCardListActivity.class);
			}
			startActivity(it);
			break;
		case R.id.ll_redpacket:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, BonusActivity.class);
			}
			startActivity(it);
			break;
		}
	}

	private void getMyAccount() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(context);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);

		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetMyAccount", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(getaccount_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);

						System.out.print("" + t);
						String jsonMessage = Utili.GetJson("" + t);

						try {
							JSONObject obj = new JSONObject(jsonMessage);
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								String amount = obj.getString("Amount");
								tv_balance.setText(amount);
								tv_gift.setText(obj.getJSONArray(
										"GiftCardCount").length()
										+ "");
								tv_redpacket.setText(obj
										.getString("BonusCount"));
							}
							handler.sendEmptyMessage(getaccount_success);

						} catch (JSONException e) {
							handler.sendEmptyMessage(getaccount_fail);
							System.out.print(e);
						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			default:
				break;
			}
		};
	};
}
