package com.carlife.carwash;

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
import com.carlife.member.MyAccountActivity;
import com.carlife.model.RescueOrder;
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

public class CarWashOrderPayActivity extends Activity implements
		OnClickListener {
	private Button btn_billpay, btn_charge, btn_alipay, btn_wxpay;

	private CustomProgressDialog cpd;
	private Context context;
	private String price, companyId, carType;

	private static final int SDK_PAY_FLAG = 1;
	private static final int createAlipayOrder_success = 2;
	private static final int createAlipayOrder_fail = 3;

	private static final int createWxOrder_success = 4;
	private static final int createWxOrder_fail = 5;

	private static final int createCarwashOrder_fail = 10;
	private static final int createCarwashOrder_success = 11;

	private String bindMobile = "";
	private int type = 0; // 支付方式
	private String orderNo = "";
	private String outTradeNo = "";// 外部订单号

	// 微信支付
	PayReq req;
	IWXAPI msgApi;
	Map<String, String> resultunifiedorder;
	StringBuffer sb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carwashorderpay);
		context = this;
		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_billpay = (Button) findViewById(R.id.btn_billpay);
		btn_billpay.setOnClickListener(this);
		btn_charge = (Button) findViewById(R.id.btn_charge);
		btn_charge.setOnClickListener(this);
		btn_alipay = (Button) findViewById(R.id.btn_alipay);
		btn_alipay.setOnClickListener(this);
		btn_wxpay = (Button) findViewById(R.id.btn_wxpay);
		btn_wxpay.setOnClickListener(this);

		Intent it = getIntent();
		Bundle bd = it.getExtras();
		price = bd.getString("price");
		companyId = bd.getString("companyId");
		carType = bd.getString("carType");
		cpd = CustomProgressDialog.createDialog(context);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");
	}

	/*
	 * 支付宝支付
	 */

	
	private void aliPay() {
		String orderInfo = AlipayHelper.getOrderInfo(
				"洗车订单" + orderNo + "付款", "订单号：" + orderNo
						+ ",手机：" + bindMobile, price,outTradeNo,
				"http://pay.1018.com.cn/Alipay/HicarOrderCarWashReceive");
		String sign = AlipayHelper.sign(orderInfo);
		try {
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		final String payInfo = orderInfo + "&sign=\"" + sign
				+ "\"&sign_type=\"RSA\"";

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(CarWashOrderPayActivity.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payInfo);
				Message msg = new Message();
				msg.what = SDK_PAY_FLAG;
				msg.obj = result;
				handler.sendMessage(msg);
			}
		};
		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	// 支付宝支付结束

	/* 微信支付 */
	private void wxPay() {
		msgApi = WXAPIFactory.createWXAPI(this, null);
		msgApi.registerApp(Const.wxAPPID);
		GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
		getPrepayId.execute();
	}

	/**
	 * 生成签名
	 */

	private String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Const.wxKey);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", packageSign);
		return packageSign;
	}

	private class GetPrepayIdTask extends
			AsyncTask<Void, Void, Map<String, String>> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(context, "提示", "加载中...");
		}

		@Override
		protected void onPostExecute(Map<String, String> result) {
			if (dialog != null) {
				dialog.dismiss();
			}
			resultunifiedorder = result;
			Toast.makeText(CarWashOrderPayActivity.this, result+"", Toast.LENGTH_SHORT).show();
			genPayReq();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Map<String, String> doInBackground(Void... params) {
			String url = String
					.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			String entity = genProductArgs();
			byte[] buf = Util.httpPost(url, entity);
			String content = new String(buf);
			Map<String, String> xml = decodeXml(content);
			return xml;
		}
	}

	public Map<String, String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName = parser.getName();
				switch (event) {
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:

					if ("xml".equals(nodeName) == false) {
						// 实例化student对象
						xml.put(nodeName, parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion", e.toString());
		}
		return null;

	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	String nonceStr = "";

	// 生成订单参数
	private String genProductArgs() {

		StringBuffer xml = new StringBuffer();

		try {
			nonceStr = genNonceStr();
			xml.append("</xml>");
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("appid", Const.wxAPPID));
			packageParams.add(new BasicNameValuePair("body", "HicarWash"+ orderNo));
			packageParams.add(new BasicNameValuePair("mch_id", Const.wxMCH_ID));
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
			packageParams.add(new BasicNameValuePair("notify_url",
					"http://pay.1018.com.cn/WxPay/HicarForOrderCarWashReceive"));
			packageParams
					.add(new BasicNameValuePair("out_trade_no", outTradeNo));
			packageParams.add(new BasicNameValuePair("spbill_create_ip",
					"127.0.0.1"));			
			String total_fee=String.format("%.0f", Double.parseDouble(price) * 100);
			packageParams.add(new BasicNameValuePair("total_fee", total_fee));
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));
			String sign = genPackageSign(packageParams);
			packageParams.add(new BasicNameValuePair("sign", sign));
			String xmlstring = Util.toXml(packageParams);
			Log.e("xmlstring====================",xmlstring);
			return xmlstring;

		} catch (Exception e) {
			return null;
		}

	}

	private void genPayReq() {
		req = new PayReq();
		req.appId = Const.wxAPPID;
		req.partnerId = Const.wxMCH_ID;
		req.prepayId = resultunifiedorder.get("prepay_id");
		req.packageValue = "Sign=WXPay";
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(genTimeStamp());
		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = Util.genAppSign(signParams);
		sendPayReq();
	}

	private void sendPayReq() {
		msgApi.registerApp(Const.wxAPPID);
		msgApi.sendReq(req);
	}

	// 微信支付结束

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}

			switch (msg.what) {
			// 创建洗车订单成功
			case createCarwashOrder_success:
				if (!orderNo.equals("")) {
					outTradeNo = AlipayHelper.getOutTradeNo(orderNo);
					createAliInfo(); // 往aliinfo表插入一条数据
				} else {
					Utili.ToastInfo(context, "系统繁忙,请稍后重试");
				}
				break;
			case createCarwashOrder_fail:
				Utili.ToastInfo(context, "系统繁忙,请稍后重试");
				break;
			case createAlipayOrder_fail:
				Utili.ToastInfo(context, "系统繁忙，请稍后重试");
				break;
			case createAlipayOrder_success:
                if(type==1){
                	//快钱
                	 String m= String.format("%.0f", Double.parseDouble(price) * 100);
                	 Uri uri = Uri
        			 .parse("http://pay.1018.com.cn/Bill/HicarForCarWash?orderNo="
        			 + orderNo + "&outTradeNo="+outTradeNo+"&m=" +m);
        			 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        			 startActivity(intent);
                }
                else if(type==2){
                	//支付宝
                	aliPay();
                }
                else if(type==3){
                	wxPay();
                }
				
				break;

			case SDK_PAY_FLAG: {
				PayResult payResult = new PayResult((String) msg.obj);
				// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
				String resultInfo = payResult.getResult();
				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					Utili.ToastInfo(context, "支付成功");
					finish();
				} else {
					// 判断resultStatus 为非“9000”则代表可能支付失败
					// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Utili.ToastInfo(context, "支付结果确认中");
						finish();
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Utili.ToastInfo(context, "支付失败");
					}
				}
				break;
			}

			// 微信支付：
			case createWxOrder_fail:
				Utili.ToastInfo(context, "系统繁忙，请稍后重试");
				break;
			case createWxOrder_success:
				wxPay();
				break;
			default:
				break;
			}
		};
	};

	/*
	 * 创建洗车订单
	 */
	private void createCarWashOrder() {
		if (!cpd.isShowing()) {
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("price", price);
		params.put("companyId", companyId);
		params.put("mobile", bindMobile);
		params.put("carType", carType);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("price", price);
		map.put("companyId", companyId);
		map.put("mobile", bindMobile);
		map.put("carType", carType);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);

		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);

		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateOrderCarWashForPay",
				params, new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(createCarwashOrder_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = Utili.GetJson(t + "");
						try {
							JSONObject o = new JSONObject(jsonMessage);
							int code = Integer.parseInt(o
									.getString("ResultCode"));
							if (code == 0) {
								orderNo = o.getString("OrderNo");
								handler.sendEmptyMessage(createCarwashOrder_success);
							} else {
								handler.sendEmptyMessage(createCarwashOrder_fail);
							}

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(createCarwashOrder_fail);
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
	 * 往aliInfo表插入一条数据
	 */
	private void createAliInfo() {
		if (!cpd.isShowing()) {
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("outTradeNo", outTradeNo);
		params.put("amount", price);
		params.put("orderNo", orderNo);
		params.put("payType", type + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("outTradeNo", outTradeNo);
		map.put("amount", price);
		map.put("orderNo", orderNo);
		map.put("payType", type + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);

		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);

		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateAlipayInfoForCarWash",
				params, new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(createAlipayOrder_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = Utili.GetJson(t + "");
						try {
							JSONObject o = new JSONObject(jsonMessage);
							int code = Integer.parseInt(o
									.getString("ResultCode"));
							if (code == 0) {
								handler.sendEmptyMessage(createAlipayOrder_success);
							} else {
								handler.sendEmptyMessage(createAlipayOrder_fail);
							}

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(createAlipayOrder_fail);
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
			this.finish();
			break;
		
		case R.id.btn_billpay:
			type = 1;//快钱
			createCarWashOrder();			
			break;
		case R.id.btn_alipay: // 支付宝支付
			type = 2;
			createCarWashOrder();
			break;
		case R.id.btn_wxpay: // 微信支付
			type = 3;
			createCarWashOrder();
			break;
		case R.id.btn_charge:
			Intent it = new Intent(context, MyAccountActivity.class);
			startActivity(it);
			break;
		}
	}

}
