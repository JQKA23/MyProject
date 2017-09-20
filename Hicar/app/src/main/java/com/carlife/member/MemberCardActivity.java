package com.carlife.member;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.BindMobileActivity;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomDialog.Builder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MemberCardActivity extends Activity implements OnClickListener {

	private TextView txt_cardAmount;
	private Button btn_back, btn_charge, btn_water;

	private CustomProgressDialog cpd;
	private String mobile = "";
	private String cardNo = "";

	private final static int Load_cardinfo_fail = 0;
	private final static int Load_cardinfo_success = 1;
	
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.membercard);
		context=this;
		txt_cardAmount = (TextView) findViewById(R.id.txt_cardAmount);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_charge = (Button) findViewById(R.id.btn_charge);
		btn_charge.setOnClickListener(this);
		btn_water = (Button) findViewById(R.id.btn_water);
		btn_water.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_charge:
			Intent it = new Intent(context,
					ChargeActivity.class);
			Bundle bd = new Bundle();
			bd.putString("cardNo", cardNo);
			it.putExtras(bd);
			startActivity(it);
			break;
		case R.id.btn_water:
			it = new Intent(context, WaterListActivity.class);
			bd = new Bundle();
			bd.putString("cardNo", cardNo);
			bd.putInt("cardType", 1);// 1为会员卡
			it.putExtras(bd);
			startActivity(it);
			break;
		}
	}

	@Override
	protected void onResume() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

		getMemberCard();

		super.onResume();
	}

	private void getMemberCard() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetMemberCard", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(Load_cardinfo_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);

						String jsonMessage = Utili.GetJson("" + t);

						try {
							// 将字符串转换成jsonObject对象
							JSONObject obj = new JSONObject(jsonMessage);

							txt_cardAmount.setText(obj.getString("Amount"));
							cardNo = obj.getString("CardNo");
							handler.sendEmptyMessage(Load_cardinfo_success);

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(Load_cardinfo_fail);
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
			case Load_cardinfo_fail:
				Utili.ToastInfo(context, "获取会员卡信息失败，请稍后重试");
				break;
			case Load_cardinfo_success:
				break;

			default:
				break;
			}
		};
	};

}
