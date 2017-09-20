package com.carlife.daijia;

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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class DaijiaPayDetailActivity extends Activity implements
		OnClickListener {

	private TextView tv_amount, tv_membercardpay, tv_giftcardpay, tv_cashpay,
			tv_coupon;
	private Context context;
	private CustomProgressDialog cpd;
	private String id = "";

	private static final int get_fail = 0;
	private static final int get_success = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiapaydetail);

		context = this;

		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		tv_amount = (TextView) findViewById(R.id.tv_amount);
		tv_membercardpay = (TextView) findViewById(R.id.tv_membercardpay);
		tv_giftcardpay = (TextView) findViewById(R.id.tv_giftcardpay);
		tv_cashpay = (TextView) findViewById(R.id.tv_cashpay);
		tv_coupon = (TextView) findViewById(R.id.tv_coupon);

		Intent it = getIntent();
		Bundle bd = it.getExtras();
		id = bd.getString("id");

		getPaydetail();
	}

	private void getPaydetail() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", id);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", id);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetOrderDaijiaPayDetail",
				params, new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(get_fail);
						super.onFailure(t, errorNo, strMsg);

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
								tv_amount.setText(obj.getString("OrderAmount"));
								tv_membercardpay.setText(obj
										.getString("MemberCardPay"));
								tv_giftcardpay.setText(obj
										.getString("GiftCardPay"));
								tv_cashpay.setText(obj.getString("Paymoney"));
								tv_coupon.setText(obj.getString("CouponMoney"));
								handler.sendEmptyMessage(get_success);
							} else {
								handler.sendEmptyMessage(get_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(get_fail);
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
			case get_fail:
				Utili.ToastInfo(context, "获取数据失败，请稍后重试");
				finish();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		}

	}
}
