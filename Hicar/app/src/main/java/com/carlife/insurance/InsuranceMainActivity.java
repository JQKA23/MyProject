package com.carlife.insurance;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import com.alipay.sdk.app.PayTask;
import com.carlife.R;
import com.carlife.alipay.AlipayHelper;
import com.carlife.alipay.PayResult;
import com.carlife.global.Const;
import com.carlife.main.BindMobileActivity;
import com.carlife.member.MyAccountActivity;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;

import com.carlife.utility.Utili;
import com.carlife.wxapi.MD5;
import com.carlife.wxapi.Util;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class InsuranceMainActivity extends Activity implements OnClickListener {
	private Button btn_yongan, btn_back;
	private CustomProgressDialog cpd;
	private Context context;
	private String bindMobile = "";
	private int source = 0;

	private final static int CreateOrder_fail = 0;
	private final static int CreateOrder_Success = 1;

	private String orderNo = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.insurancemain);
		context = this;
		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_yongan = (Button) findViewById(R.id.btn_yongan);
		btn_yongan.setOnClickListener(this);
		cpd = CustomProgressDialog.createDialog(context);
		
	}

	
	
	
	@Override
	protected void onResume() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");
		super.onResume();
	}




	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_yongan:
			if(bindMobile.equals("")){
				Intent it=new Intent(context,BindMobileActivity.class);
				startActivity(it);
			}else{
			source = 0;
			popCreateOrder();}
			break;
		}
	}

	private void popCreateOrder() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确定去上车险？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				createOrder();
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void createOrder() {
		if (!cpd.isShowing()) {
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("source", source + "");
		params.put("bindMobile", bindMobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("source", source + "");
		map.put("bindMobile", bindMobile);

		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateOrderInsurance", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(CreateOrder_fail);
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
								orderNo = obj.getString("OrderNo");
							}
							handler.sendEmptyMessage(CreateOrder_Success);

						} catch (JSONException e) {
							handler.sendEmptyMessage(CreateOrder_fail);
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
			case CreateOrder_fail:
				Utili.ToastInfo(context, "系统繁忙，请稍后重试");
				break;
			case CreateOrder_Success:
				if (!orderNo.equals("")) {
					if (source == 0) {
						Uri uri = Uri
								.parse("https://m.iyobee.com/ic/yaic/?partner=1018&uid="
										+ orderNo);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}
				break;
			default:
				break;
			}
		};
	};

}
