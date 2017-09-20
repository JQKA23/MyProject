package com.carlife.member;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class MyAccountActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private CustomProgressDialog cpd;
	private LinearLayout ll_membercard, ll_giftcard, ll_bonus;

	private Context context;
	private final static int getaccount_fail = 0;
	private final static int getaccount_success = 1;
	private TextView tv_amount, tv_giftcardCount, tv_bonusCount;

	private String mobile = "";
	private String amount = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myaccount);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		ll_membercard = (LinearLayout) findViewById(R.id.ll_membercard);
		ll_membercard.setOnClickListener(this);
		ll_giftcard = (LinearLayout) findViewById(R.id.ll_giftcard);
		ll_giftcard.setOnClickListener(this);

		ll_bonus = (LinearLayout) findViewById(R.id.ll_bonus);
		ll_bonus.setOnClickListener(this);
		tv_amount = (TextView) findViewById(R.id.tv_amount);
		tv_giftcardCount = (TextView) findViewById(R.id.tv_giftcardCount);
		tv_bonusCount = (TextView) findViewById(R.id.tv_bonusCount);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

	}

	@Override
	protected void onResume() {
		getMyAccount();
		super.onResume();
	}

	private void getMyAccount() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
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
								amount = obj.getString("Amount");
								tv_amount.setText(amount);
								tv_giftcardCount.setText(obj.getJSONArray(
										"GiftCardCount").length()
										+ "");
								tv_bonusCount.setText(obj
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.ll_membercard:
			Intent it = new Intent(context, MemberCardActivity.class);
			startActivity(it);
			break;
		case R.id.ll_giftcard:
			it = new Intent(context, GiftCardListActivity.class);
			startActivity(it);
			break;
		case R.id.ll_bonus:
			it = new Intent(context, BonusActivity.class);
			startActivity(it);
			break;
		}

	}
}
