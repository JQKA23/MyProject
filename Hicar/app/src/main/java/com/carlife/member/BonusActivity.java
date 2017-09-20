package com.carlife.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.BindMobileActivity;
import com.carlife.model.Bonus;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Share;
import com.carlife.utility.Util;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BonusActivity extends Activity implements OnClickListener {

	private Button btn_back;

	private LinearLayout ll_getbonus, ll_sharebonus;
	private String mobile = "";
	private CustomProgressDialog cpd;

	private final static int getFreeBonus_fail = 0;
	private final static int getFreeBonus_success = 1;
	private final static int getFreeBonus_success_withErrorMsg = 2;

	private final static int shareFreeBonus_fail = 3;
	private final static int shareFreeBonus_success = 4;

	private String sendId = "";

	private Context context;
	private Share share;
	private IWXAPI wxapi;
	private static final int THUMB_SIZE = 150;

	private ListView lv;
	private List<Bonus> list;

	private final static int MSG_REVIEWS_OK = 100;
	private final static int MSG_REVIEWS_ERROR = 200;

	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bonus);
		context=this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		ll_getbonus = (LinearLayout) findViewById(R.id.ll_getbonus);
		ll_getbonus.setOnClickListener(this);
		ll_sharebonus = (LinearLayout) findViewById(R.id.ll_sharebonus);
		ll_sharebonus.setOnClickListener(this);
		lv = (ListView) findViewById(R.id.lv);

		wxapi = WXAPIFactory.createWXAPI(this, Const.wxAPPID);
		wxapi.registerApp(Const.wxAPPID);
	}

	@Override
	protected void onResume() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		getBonusList();
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.ll_getbonus:
			getFreeBonus();
			break;
		case R.id.ll_sharebonus:
			shareBonus();
			break;

		}
	}

	private void getFreeBonus() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetFreeBonus", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(getFreeBonus_fail);
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
								handler.sendEmptyMessage(getFreeBonus_success);
							} else {
								handler.sendEmptyMessage(getFreeBonus_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(getFreeBonus_fail);
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});

	}

	private void shareBonus() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/ShareFreeBonus", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(shareFreeBonus_fail);
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
						String jsonMessage = "" + t;
						if (jsonMessage != null && !jsonMessage.equals("")) {
							jsonMessage = jsonMessage.substring(1);
							jsonMessage = jsonMessage.substring(0,
									jsonMessage.length() - 1);
							jsonMessage = jsonMessage.replace("\\", "");
							try {
								JSONObject obj = new JSONObject(jsonMessage);
								if (obj.isNull("IsError")) {
									sendId = obj.getString("SendId");
									handler.sendEmptyMessage(shareFreeBonus_success);
								} else {
									handler.sendEmptyMessage(shareFreeBonus_fail);
								}
							} catch (JSONException e) {
								e.printStackTrace();
								handler.sendEmptyMessage(getFreeBonus_fail);
							}
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	private void getBonusList() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		String mobile = sp.getString(Const.BindMobile, "");
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetBonusList", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(MSG_REVIEWS_ERROR);
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
							list = new ArrayList<Bonus>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								Bonus water = new Bonus();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								water.setPeriod(obj.getString("Period"));
								water.setAmount(obj.getString("Amount"));
								water.setRemark(obj.getString("Remark"));
								list.add(water);
							}

							handler.sendEmptyMessage(MSG_REVIEWS_OK);

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(MSG_REVIEWS_ERROR);
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
			case getFreeBonus_success:
				Utili.ToastInfo(BonusActivity.this, "领取成功");
				break;
			case getFreeBonus_fail:
				Utili.ToastInfo(BonusActivity.this, "一周只能领取一次哦");
				break;
			case shareFreeBonus_fail:
				Utili.ToastInfo(BonusActivity.this, "");
				break;
			case shareFreeBonus_success:
				share = new Share(BonusActivity.this, itemsOnClick);
				share.showAtLocation(
						BonusActivity.this.findViewById(R.id.main),
						Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				break;
			case MSG_REVIEWS_OK:
				if (list != null && list.size() > 0) {
					prepareData();
					SimpleAdapter listAdapter = new SimpleAdapter(
							context, data, R.layout.bonusitem,
							new String[] { "Amount", "Remark","Period" },
							new int[] { R.id.txt_Amount,R.id.txt_remark, R.id.txt_addtime});
					lv.setAdapter(listAdapter);
				}
				break;
			default:
				break;
			}
		};
	};
	
	
	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("Amount", list.get(i).getAmount());			
			item.put("Remark", list.get(i).getRemark());
			item.put("Period", list.get(i).getPeriod());
			data.add(item);
		}
	}
	
	
	

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
		msg.title = "抢Hi车红包,优享汽车生活";
		msg.description = "汽车代驾、救援、车务，就找Hi车";
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
