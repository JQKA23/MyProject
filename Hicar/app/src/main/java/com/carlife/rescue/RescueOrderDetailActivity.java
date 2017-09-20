package com.carlife.rescue;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.Driver;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Share;
import com.carlife.utility.Utili;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class RescueOrderDetailActivity extends Activity implements
		OnClickListener {

	private ImageView ivs1, ivs2, ivs3, ivs4, ivs5, tv_share;
	private Button btn_back, btn1, btn2, btn3, btn4, btn5, btn_star;

	private TextView tv_drivername, tv_usercode, tv_ordercount, tv_star,
			txt_price, tv_tousu, tv_orderNo;

	private CustomProgressDialog cpd;
	private int starCount = 5;
	private RescueOrder order;

	private final static int comment_fail = 0;
	private final static int comment_Success = 1;
	private final static int getDriverInfo_fail = 3;
	private final static int getDriverInfo_success = 4;

	private final static int shareOrderBonus_fail = 5;
	private final static int shareOrderBonus_success = 6;

	private String sendId = "";

	private Driver driver;

	private Share share;
	private IWXAPI wxapi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescueorderdetail);
		tv_drivername = (TextView) findViewById(R.id.tv_drivername);
		tv_usercode = (TextView) findViewById(R.id.tv_usercode);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_star = (TextView) findViewById(R.id.tv_star);
		txt_price = (TextView) findViewById(R.id.txt_price);
		tv_orderNo = (TextView) findViewById(R.id.tv_orderNo);

		ivs1 = (ImageView) findViewById(R.id.ivs1);
		ivs2 = (ImageView) findViewById(R.id.ivs2);
		ivs3 = (ImageView) findViewById(R.id.ivs3);
		ivs4 = (ImageView) findViewById(R.id.ivs4);
		ivs5 = (ImageView) findViewById(R.id.ivs5);
		tv_share = (ImageView) findViewById(R.id.tv_share);
		tv_share.setOnClickListener(this);
		tv_tousu = (TextView) findViewById(R.id.tv_tousu);
		tv_tousu.setOnClickListener(this);

		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		btn3 = (Button) findViewById(R.id.btn3);
		btn4 = (Button) findViewById(R.id.btn4);
		btn5 = (Button) findViewById(R.id.btn5);
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		btn3.setOnClickListener(this);
		btn4.setOnClickListener(this);
		btn5.setOnClickListener(this);
		btn_star = (Button) findViewById(R.id.btn_star);
		btn_star.setOnClickListener(this);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		Bundle bd = getIntent().getExtras();
		order = (RescueOrder) bd.getSerializable("rescueOrder");
		txt_price.setText(order.getPrice());
		tv_orderNo.setText(order.getOrderNo());

		wxapi = WXAPIFactory.createWXAPI(this, Const.wxAPPID);
		wxapi.registerApp(Const.wxAPPID);
	}

	@Override
	protected void onResume() {
		getDriverRescueInfo();
		getOrderStar();
		super.onResume();
	}

	/*
	 * 获取司机信息
	 */
	private void getDriverRescueInfo() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getDriverId());
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getDriverId());
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/getDriverRescueInfo", params,
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

	/*
	 * 获取订单评分
	 */
	private void getOrderStar() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId());
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getId());
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetOrderRescueStar", params,
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
							int star = obj.getInt("Star");
							if (star == 0) {
								btn1.setEnabled(true);
								btn2.setEnabled(true);
								btn3.setEnabled(true);
								btn4.setEnabled(true);
								btn5.setEnabled(true);
								btn_star.setVisibility(View.VISIBLE);
							} else {
								showStar(star);
								btn1.setEnabled(false);
								btn2.setEnabled(false);
								btn3.setEnabled(false);
								btn4.setEnabled(false);
								btn5.setEnabled(false);
								btn_star.setVisibility(View.GONE);
							}
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn1:
			showStar(1);
			break;
		case R.id.btn2:
			showStar(2);
			break;
		case R.id.btn3:
			showStar(3);
			break;
		case R.id.btn4:
			showStar(4);
			break;
		case R.id.btn5:
			showStar(5);
			break;
		case R.id.btn_star:
			orderComment();
			break;
		case R.id.tv_share:
			shareOrderBonus();
			break;
		case R.id.tv_tousu:
			Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ getString(R.string.hotlineno)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		}
	}

	private void shareOrderBonus() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		String type = "2";// 1为代驾 2为救援
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId());
		params.put("type", type);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getId());
		map.put("type", type);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/ShareOrderBonus", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(shareOrderBonus_fail);
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
							if (obj.isNull("ResultCode")) {
								sendId = obj.getString("SendId");
								handler.sendEmptyMessage(shareOrderBonus_success);
							} else {
								handler.sendEmptyMessage(shareOrderBonus_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(shareOrderBonus_fail);
						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	/*
	 * 评分
	 */
	private void orderComment() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId());
		params.put("star", "" + starCount);

		Map<String, String> map = new HashMap<String, String>();
		map.put("appKey", Const.APPKEY_STR);
		map.put("id", order.getId());
		map.put("star", "" + starCount);

		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();

		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/RescueOrderComment", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(comment_fail);
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
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								handler.sendEmptyMessage(comment_Success);
							} else {
								handler.sendEmptyMessage(comment_fail);
							}

						} catch (JSONException e) {
							handler.sendEmptyMessage(comment_fail);
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

	private void showStar(int num) {
		starCount = num;
		switch (num) {
		case 1:
			btn1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn4.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn5.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			break;
		case 2:
			btn1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn4.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn5.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			break;
		case 3:
			btn1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn4.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			btn5.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			break;
		case 4:
			btn1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn4.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn5.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.star_empty));
			break;
		case 5:
			btn1.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn2.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn3.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn4.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			btn5.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			break;
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			case comment_fail:
				Utili.ToastInfo(RescueOrderDetailActivity.this, "提交失败");
				break;
			case comment_Success:
				Utili.ToastInfo(RescueOrderDetailActivity.this, "提交成功，感谢您的评价");
				finish();
				break;

			case shareOrderBonus_fail:
				Utili.ToastInfo(RescueOrderDetailActivity.this, "系统错误，请稍后重试");
				break;
			case shareOrderBonus_success:
				share = new Share(RescueOrderDetailActivity.this, itemsOnClick);
				share.showAtLocation(
						RescueOrderDetailActivity.this.findViewById(R.id.main),
						Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				break;
			default:
				break;
			}
		};
	};

	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			share.dismiss();
			switch (v.getId()) {
			case R.id.btn_pyq:
				sendToWx(0);
				break;
			case R.id.btn_wx:
				sendToWx(1);
				break;
			default:
				break;
			}
		}

	};

	private void sendToWx(int type) {
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "http://m.1018.com.cn/activity/bonus?id=" + sendId;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "汽车代驾、救援、车务一站式服务，就找Hi车。";
		msg.description = "汽车代驾、救援、车务一站式服务，就找Hi车。";
		try {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.bonusbig);
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
			bmp.recycle();
			msg.setThumbImage(thumbBmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = type == 0 ? SendMessageToWX.Req.WXSceneTimeline
				: SendMessageToWX.Req.WXSceneSession;
		wxapi.sendReq(req);
	}

}
