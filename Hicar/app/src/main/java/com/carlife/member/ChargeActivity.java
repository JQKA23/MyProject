package com.carlife.member;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import com.alipay.sdk.app.PayTask;
import com.carlife.R;
import com.carlife.alipay.AlipayHelper;
import com.carlife.alipay.PayResult;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
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
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ChargeActivity extends Activity implements OnClickListener {

	private String TAG="ChargeActivity";
	
	private Button btn_back;
	private ImageButton ibtn_alipay, ibtn_billpay, ibtn_wxpay;
	private String cardNo = "";
	private EditText et_amount;
	private TextView txt_cardNo, txt_amount100, txt_amount300, txt_amount500,
			txt_amount1000;
	private String amount = "0";

	private CustomProgressDialog cpd;
	private static final int SDK_PAY_FLAG = 1;
	private static final int createAlipayOrder_success = 2;
	private static final int createAlipayOrder_fail = 3;
	private static final int createWxOrder_success = 4;
	private static final int createWxOrder_fail = 5;

	private Context context;
	//微信支付
	PayReq req;
	final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
	Map<String,String> resultunifiedorder;
	String	nonceStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.charge);
		context = this;
		et_amount = (EditText) findViewById(R.id.et_amount);
		txt_cardNo = (TextView) findViewById(R.id.txt_cardNo);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		ibtn_alipay = (ImageButton) findViewById(R.id.ibtn_alipay);
		ibtn_alipay.setOnClickListener(this);
		ibtn_billpay = (ImageButton) findViewById(R.id.ibtn_billpay);
		ibtn_billpay.setOnClickListener(this);
		ibtn_wxpay = (ImageButton) findViewById(R.id.ibtn_wxpay);
		ibtn_wxpay.setOnClickListener(this);

		txt_amount100 = (TextView) findViewById(R.id.txt_amount100);
		txt_amount300 = (TextView) findViewById(R.id.txt_amount300);
		txt_amount500 = (TextView) findViewById(R.id.txt_amount500);
		txt_amount1000 = (TextView) findViewById(R.id.txt_amount1000);
		txt_amount100.setOnClickListener(this);
		txt_amount300.setOnClickListener(this);
		txt_amount500.setOnClickListener(this);
		txt_amount1000.setOnClickListener(this);

		cpd = CustomProgressDialog.createDialog(this);

		Intent it = getIntent();
		Bundle bd = it.getExtras();
		cardNo = bd.getString("cardNo");
		txt_cardNo.setText(cardNo);

		et_amount.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (et_amount.getText().toString().length() > 0) {
					setController(0, txt_amount100, false);
					setController(0, txt_amount300, false);
					setController(0, txt_amount500, false);
					setController(0, txt_amount1000, false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.txt_amount100:
			setController(0, txt_amount300, false);
			setController(0, txt_amount500, false);
			setController(0, txt_amount1000, false);
			setController(100, txt_amount100, true);
			break;
		case R.id.txt_amount300:
			setController(0, txt_amount100, false);
			setController(0, txt_amount500, false);
			setController(0, txt_amount1000, false);
			setController(300, txt_amount300, true);
			break;
		case R.id.txt_amount500:
			setController(0, txt_amount100, false);
			setController(0, txt_amount300, false);
			setController(0, txt_amount1000, false);
			setController(500, txt_amount500, true);
			break;
		case R.id.txt_amount1000:
			setController(0, txt_amount100, false);
			setController(0, txt_amount300, false);
			setController(0, txt_amount500, false);
			setController(1000, txt_amount1000, true);
			break;
		case R.id.ibtn_alipay:
			// 支付宝充值
			if (amount.equals("") || amount.equals("0")) {
				amount = et_amount.getText().toString().trim();				
			} 
			if (amount.equals("") || amount.equals("0")) {
				Utili.ToastInfo(context, "请填写充值金额");				
			} else {
				outTradeNo = AlipayHelper.getOutTradeNo(cardNo);
				createAlipayOrder();
			}
			break;
		// 微信支付
		case R.id.ibtn_wxpay:
			if (amount.equals("") || amount.equals("0")) {
				amount = et_amount.getText().toString().trim();				
			} 
			if (amount.equals("") || amount.equals("0")) {
				Utili.ToastInfo(context, "请填写充值金额");				
			} else {				
				outTradeNo = AlipayHelper.getOutTradeNo(cardNo);
				createWxOrder();
			}
			break;
		// 快钱充值
		case R.id.ibtn_billpay:
			if (amount.equals("") || amount.equals("0")) {
				amount = et_amount.getText().toString().trim();				
			} 
			if (amount.equals("") || amount.equals("0")) {
				Utili.ToastInfo(context, "请填写充值金额");				
			} else {
				kqPayOrder(amount);
			}
			break;

		default:
			break;
		}
	}

	/*
	 * 支付宝支付
	 */

	private String outTradeNo = "";// 支付宝订单号

	private void aliPay() {
		String orderInfo = AlipayHelper.getOrderInfo("卡充值" + cardNo, "卡号："
				+ cardNo, amount, outTradeNo,
				"http://pay.1018.com.cn/Alipay/HicarCardChargeReceive");
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
				PayTask alipay = new PayTask(ChargeActivity.this);
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

	private void createAlipayOrder() {
		if (!cpd.isShowing()) {
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("outTradeNo", outTradeNo);
		params.put("amount", amount);
		params.put("cardNo", cardNo);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("outTradeNo", outTradeNo);
		map.put("amount", amount);
		map.put("cardNo", cardNo);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);

		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);

		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateChargeAlipayOrder",
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

	/*
	 * 微信支付
	 */

	private void createWxOrder() {
		if (!cpd.isShowing()) {
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("outTradeNo", outTradeNo);
		params.put("amount", amount);
		params.put("cardNo", cardNo);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("outTradeNo", outTradeNo);
		map.put("amount", amount);
		map.put("cardNo", cardNo);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);

		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);

		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateChargeWxOrder", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(createWxOrder_fail);
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
								handler.sendEmptyMessage(createWxOrder_success);
							} else {
								handler.sendEmptyMessage(createWxOrder_fail);
							}

						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(createWxOrder_fail);
						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	private void wxPay() {
		nonceStr = genNonceStr();
		msgApi.registerApp(Const.wxAPPID);			
		GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
		getPrepayId.execute();
	}
	
	/**
	 生成签名
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
		String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		Log.e("orion",packageSign);
		return packageSign;
	}
	

	private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String,String>> {
		private ProgressDialog dialog;


		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(context, "提示", "加载中...");
		}

		@Override
		protected void onPostExecute(Map<String,String> result) {
			if (dialog != null) {
				dialog.dismiss();
			}		  
			resultunifiedorder=result;	
			genPayReq();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Map<String,String>  doInBackground(Void... params) {
			String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			String entity = genProductArgs();
			byte[] buf = Util.httpPost(url, entity);
			String content = new String(buf);
			Map<String,String> xml=decodeXml(content);			
			return xml;
		}
	}



	public Map<String,String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName=parser.getName();
				switch (event) {
					case XmlPullParser.START_DOCUMENT:

						break;
					case XmlPullParser.START_TAG:

						if("xml".equals(nodeName)==false){
							//实例化student对象
							xml.put(nodeName,parser.nextText());
						}
						break;
					case XmlPullParser.END_TAG:
						break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion",e.toString());
		}
		return null;

	}


	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}
	
	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}
	


    //生成订单参数
	private String genProductArgs() {		
		StringBuffer xml = new StringBuffer();
		try {					
			xml.append("</xml>");
           List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("appid", Const.wxAPPID));
			packageParams.add(new BasicNameValuePair("body", "Hicar"+cardNo));
			packageParams.add(new BasicNameValuePair("mch_id", Const.wxMCH_ID));
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
			packageParams.add(new BasicNameValuePair("notify_url", "http://pay.1018.com.cn/WxPay/HicarCardChargeReceive"));
			packageParams.add(new BasicNameValuePair("out_trade_no",outTradeNo));
			packageParams.add(new BasicNameValuePair("spbill_create_ip","127.0.0.1"));	
			packageParams.add(new BasicNameValuePair("total_fee", Integer.parseInt(amount)*100+""));
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));
			
			String sign = genPackageSign(packageParams);			
			packageParams.add(new BasicNameValuePair("sign", sign));
		    String xmlstring =Util.toXml(packageParams);

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
	// -----------微信end
	
	

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}

			switch (msg.what) {
			// 支付宝
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
			case createAlipayOrder_success:
				aliPay();
				break;
			case createAlipayOrder_fail:
				Utili.ToastInfo(context, "系统繁忙，请稍后重试");
				break;
			// 微信
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

	private void setController(int money, TextView tv, boolean isChosen) {
		amount = money + "";
		tv.setBackgroundColor(getResources().getColor(
				isChosen ? R.color.blue : R.color.white));
		tv.setTextColor((ColorStateList) getResources().getColorStateList(
				isChosen ? R.color.white : R.color.light_black));
		if (isChosen)
			et_amount.setText("");
	}

	// 快钱支付
	private void kqPayOrder(String amount) {
		Uri uri = Uri.parse("http://pay.1018.com.cn/Bill/CarLife?u=" + cardNo
				+ "&m=" + Integer.parseInt(amount) * 100);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
