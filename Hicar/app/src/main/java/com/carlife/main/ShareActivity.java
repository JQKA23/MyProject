package com.carlife.main;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Share;
import com.carlife.utility.Utili;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class ShareActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private LinearLayout ll_share;

	private Context context;
	private String mobile;
	private Share share;
	private IWXAPI wxapi;
	private static final int THUMB_SIZE = 150;
	private CustomProgressDialog cpd;
	private TextView tv_tuiJianCode;// 用户Id
	private String tuiJianCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharepage);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		ll_share = (LinearLayout) findViewById(R.id.ll_share);
		ll_share.setOnClickListener(this);
		context = this;
		wxapi = WXAPIFactory.createWXAPI(this, Const.wxAPPID);
		wxapi.registerApp(Const.wxAPPID);
		tv_tuiJianCode = (TextView) findViewById(R.id.tv_tuijianCode);

		Intent intent = getIntent();
		mobile = intent.getStringExtra("mobile");

		GetRecommendCode();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.ll_share:
			share();
			break;
		}
	}

	private void share() {
		share = new Share(context, itemsOnClick);
		share.showAtLocation(findViewById(R.id.main), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0);
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
		webpage.webpageUrl = "http://m.1018.com.cn/h";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		String t = "下载Hi车APP,输入推荐码" + tuiJianCode + "您将获得免费惊喜!";
		msg.title = t;
		msg.description = "汽车代驾、救援、车务一站式服务就找Hi车，下载APP，尽享低价、优质、便捷的汽车服务。";
		try {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.code);
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

	private void GetRecommendCode() {
		if (mobile.length() != 11 || !mobile.startsWith("1")) {
			Utili.ToastInfo(context, "请输入正确的手机号码");
		} else {
			if (cpd == null || !cpd.isShowing()) {
				cpd = CustomProgressDialog.createDialog(this);
				cpd.show();
			}

			AjaxParams params = new AjaxParams();
			params.put(Const.APPKEY, Const.APPKEY_STR);
			params.put("bindMobile", mobile);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Const.APPKEY, Const.APPKEY_STR);
			map.put("bindMobile", mobile);
			String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
					EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
			strTemp = EncodeUtility.md5(strTemp).toLowerCase();
			params.put("sign", strTemp);
			FinalHttp fh = new FinalHttp();
			fh.post(Const.API_SERVICES_ADDRESS + "/GetRecommendCode", params,
					new AjaxCallBack<Object>() {

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							Utili.ToastInfo(context, "获取推荐码失败，请稍后重试");
							cpd.dismiss();
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
								tuiJianCode = obj.getString("RecommendCode");
								tv_tuiJianCode.setText(tuiJianCode);
								cpd.dismiss();
							} catch (JSONException e) {
								System.out.print(e);
								Utili.ToastInfo(context, "获取推荐码失败，请稍后重试");
								cpd.dismiss();
							}
						}

						@Override
						public AjaxCallBack<Object> progress(boolean progress,
								int rate) {
							return super.progress(progress, rate);
						}

					});
		}
	}
}
