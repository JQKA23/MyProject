package com.carlife.daijia;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class DaijiaCurrentOrderDetailActivity extends Activity implements
		OnClickListener {

	private ImageView ivs1, ivs2, ivs3, ivs4, ivs5;
	private Button btn_back, btn_contact;
	private TextView tv_drivername, tv_usercode, tv_ordercount, tv_star,
			tv_cancel, tv_start, tv_starttime, tv_type;
	private LinearLayout ll_type, ll_start;

	private CustomProgressDialog cpd;
	private DaijiaOrder order_d;
	private RescueOrder order_r;
	private boolean isDaijia;

	private String mobile = "";
	private String driverId, orderId;
	private String url_getDriverInfo, url_cancal;
	private Context context;

	private final static int cancelorder_fail = 0;
	private final static int cancelorder_Success = 1;
	private WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiacurrentorderdetail);
		tv_drivername = (TextView) findViewById(R.id.tv_drivername);
		tv_usercode = (TextView) findViewById(R.id.tv_usercode);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_type = (TextView) findViewById(R.id.tv_type);
		tv_star = (TextView) findViewById(R.id.tv_star);
		ll_type = (LinearLayout) findViewById(R.id.ll_type);
		ll_start = (LinearLayout) findViewById(R.id.ll_start);
		ivs1 = (ImageView) findViewById(R.id.ivs1);
		ivs2 = (ImageView) findViewById(R.id.ivs2);
		ivs3 = (ImageView) findViewById(R.id.ivs3);
		ivs4 = (ImageView) findViewById(R.id.ivs4);
		ivs5 = (ImageView) findViewById(R.id.ivs5);
		tv_starttime = (TextView) findViewById(R.id.tv_starttime);
		context = this;

		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);
		tv_start = (TextView) findViewById(R.id.tv_start);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_contact = (Button) findViewById(R.id.btn_contact);
		btn_contact.setOnClickListener(this);

		Bundle bd = getIntent().getExtras();
		isDaijia = bd.getBoolean("isDaijia");
		if (isDaijia) {
			order_d = (DaijiaOrder) bd.getSerializable("Order");
			tv_start.setText(order_d.getDeparturePlaceReal());
			tv_starttime.setText(order_d.getOrdertime());
			driverId = order_d.getDriverId();
			orderId = order_d.getId();
			ll_start.setVisibility(View.VISIBLE);
			url_getDriverInfo = "/GetDriverDaijiaInfo";
			url_cancal = "/CancelOrderOption";
		} else {
			order_r = (RescueOrder) bd.getSerializable("Order");
			tv_type.setText(order_r.getRescueType());
			tv_starttime.setText(order_r.getAddTime());
			driverId = order_r.getDriverId();
			orderId = order_r.getId();
			ll_type.setVisibility(View.VISIBLE);
			url_getDriverInfo = "/getDriverRescueInfo";
			url_cancal = "/CancelRescueOrderOption";
		}

		wv = (WebView) findViewById(R.id.wv);
		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				cpd.dismiss();

			}
		});
		cpd = CustomProgressDialog.createDialog(this);
		cpd.show();
		wv.loadUrl("http://m.1018.com.cn/Activity/DriverOnMap?id=" + driverId);
	}

	@Override
	protected void onResume() {
		getDriverDaijiaInfo();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
	 * 获取司机信息
	 */
	@SuppressLint("DefaultLocale")
	private void getDriverDaijiaInfo() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", driverId);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", driverId);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + url_getDriverInfo, params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
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
							JSONObject obj = new JSONObject(jsonMessage);
							tv_drivername.setText(obj.getString("Name"));
							tv_usercode.setText(obj.getString("UserCode"));
							tv_star.setText(obj.getString("Star"));
							initDriverStar(obj.getString("Star"));
							tv_ordercount.setText(obj.getString("OrderCount")
									+ "单");
							mobile = obj.getString("Mobile");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	@SuppressWarnings("deprecation")
	private void initDriverStar(String star) {
		double s = Double.parseDouble(star) * 10;
		if (s >= 50) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
		} else if (s > 40) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 30) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 20) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 10) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_cancel:
			Intent it = new Intent(context, DaijiaOrderCancelActivity.class);
			Bundle bd = new Bundle();
			bd.putString("id", orderId);
			bd.putString("url_cancal", url_cancal);
			it.putExtras(bd);
			startActivity(it);
			// popCancelOrder();
			break;
		case R.id.btn_contact:
			it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mobile));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		}
	}

	@SuppressWarnings("unused")
	private void popCancelOrder() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确定取消订单？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				cancelOrder();
			}
		});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();

	}

	@SuppressLint("DefaultLocale")
	private void cancelOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(context);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", orderId);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", orderId);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + url_cancal, params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(cancelorder_fail);
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
							JSONObject obj = new JSONObject(jsonMessage);
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								handler.sendEmptyMessage(cancelorder_Success);
							} else {
								handler.sendEmptyMessage(cancelorder_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(cancelorder_fail);
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {
			case cancelorder_fail:
				Utili.ToastInfo(context, "取消失败");
				finish();
				break;
			case cancelorder_Success:
				Utili.ToastInfo(context, "取消成功");
				finish();
				break;
			default:
				break;
			}
		};
	};

}
