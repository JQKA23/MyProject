package com.carlife.member;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GiftCardActivity extends Activity implements OnClickListener {

	private Button btn_back, btn_recharge, btn_water;
	private final static int Load_fail = 101;
	private final static int Load_success = 102;
	private CustomProgressDialog cpd;
	private TextView txt_cardNo, txt_cardAmount;
	private String cardNo = "";
	private String amount = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.giftcard);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_water = (Button) findViewById(R.id.btn_water);
		btn_water.setOnClickListener(this);

		btn_recharge = (Button) findViewById(R.id.btn_recharge);
		btn_recharge.setOnClickListener(this);
		txt_cardNo = (TextView) findViewById(R.id.txt_cardNo);
		txt_cardAmount = (TextView) findViewById(R.id.txt_cardAmount);

	}

	@Override
	protected void onResume() {
		Intent it = getIntent();
		Bundle bd = it.getExtras();
		cardNo = bd.getString("cardNo");
		amount = bd.getString("amount");
		txt_cardNo.setText(cardNo);
		txt_cardAmount.setText(amount);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_recharge: {
			Intent it = new Intent(GiftCardActivity.this, ChargeActivity.class);
			Bundle bd = new Bundle();		
			bd.putString("cardNo", cardNo);
			it.putExtras(bd);
			startActivity(it);
		}
			break;
		case R.id.btn_water: {
			Intent it = new Intent(GiftCardActivity.this,
					WaterListActivity.class);
			Bundle bd = new Bundle();
			bd.putString("cardNo", cardNo);
			bd.putInt("cardType", 2);// 1为会员卡,2礼品卡
			it.putExtras(bd);
			startActivity(it);
		}
			break;
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {
			case Load_fail:
				Toast.makeText(GiftCardActivity.this, "获取礼品卡信息失败",
						Toast.LENGTH_SHORT).show();
				finish();
				break;
			}
		}
	};

}
