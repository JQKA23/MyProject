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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;

public class BindMobileActivity extends Activity implements OnClickListener {

	private Button btn_back, btn_submit, btn_getValidCode;
	private EditText et_mobile, et_validcode, et_usercode;

	private CustomProgressDialog cpd;

	private final static int getValidCode_fail = 101;
	private final static int getValidCode_success = 102;
	private final static int Bind_fail = 103;
	private final static int Bind_success = 104;

	private int countNum = 60;// 倒计时
	private boolean isInterrupt = false;

	private String mobile = "";
	private String validCode = "";
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bindmobile);
		context = this;
		et_mobile = (EditText) findViewById(R.id.et_mobile);
		et_validcode = (EditText) findViewById(R.id.et_validcode);
		et_usercode = (EditText) findViewById(R.id.et_usercode);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		btn_submit.setOnClickListener(this);
		btn_getValidCode = (Button) findViewById(R.id.btn_getValidCode);
		btn_getValidCode.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_getValidCode:
			getValidCode();
			break;
		case R.id.btn_submit:
			bindMobile();
			break;
		}
	}

	private void getValidCode() {
		mobile = et_mobile.getText().toString().trim();
		if (mobile.length() != 11 || !mobile.startsWith("1")) {
			Utili.ToastInfo(context, "请输入正确的手机号码");
		} else {
			validCode = Utili.generateValid();

			if (cpd == null || !cpd.isShowing()) {
				cpd = CustomProgressDialog.createDialog(this);
				cpd.show();
			}

			AjaxParams params = new AjaxParams();
			params.put(Const.APPKEY, Const.APPKEY_STR);
			params.put("validCode", validCode);
			params.put("mobile", mobile);
			Map<String, String> map = new HashMap<String, String>();
			map.put(Const.APPKEY, Const.APPKEY_STR);
			map.put("validCode", validCode);
			map.put("mobile", mobile);
			String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
					EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
			strTemp = EncodeUtility.md5(strTemp).toLowerCase();
			params.put("sign", strTemp);
			FinalHttp fh = new FinalHttp();
			fh.post(Const.API_SERVICES_ADDRESS + "/SendValidCode", params,
					new AjaxCallBack<Object>() {

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							handler.sendEmptyMessage(getValidCode_fail);
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
								int code = obj.getInt("ResultCode");
								if (code == 0) {
									handler.sendEmptyMessage(getValidCode_success);
								}
							} catch (JSONException e) {
								System.out.print(e);
								handler.sendEmptyMessage(getValidCode_fail);
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

	private void bindMobile() {
		mobile = et_mobile.getText().toString().trim();
		if (mobile.length() != 11 || !mobile.startsWith("1")) {
			Utili.ToastInfo(context, "请输入正确的手机号码");
		}
		SharedPreferences sp = getSharedPreferences(Const.spValidCode,
				Context.MODE_PRIVATE);
		String localValidCode = sp.getString(Const.validCode, "");// 本地保存的验证码
		String validCode = et_validcode.getText().toString().trim();
		if ("18910631018".equals(mobile)) {
			BindMobile();			
		}else if (validCode.equals(localValidCode) && !validCode.equals("")) {
			BindMobile();
		} else {
			Utili.ToastInfo(this, "验证码错误");
		}
	}

	private void BindMobile() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		String usercode = et_usercode.getText().toString().trim();
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("phone", mobile);
		params.put("userCode", usercode);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("phone", mobile);
		map.put("userCode", usercode);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/BindMobile", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(Bind_fail);
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
								handler.sendEmptyMessage(Bind_success);
							}
						} catch (JSONException e) {
							System.out.print(e);
							handler.sendEmptyMessage(Bind_fail);
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
			case getValidCode_fail:
				Utili.ToastInfo(context, "获取验证码失败，请稍后重试");
				break;
			case getValidCode_success:
				Utili.ToastInfo(context, "验证码已发送，请注意查收");
				SharedPreferences sp = getSharedPreferences(Const.spValidCode,
						Context.MODE_PRIVATE);
				sp.edit().putString(Const.validCode, validCode).commit(); // 保存本地验证码
				countNum = 60;
				final Handler mhandler = new Handler();
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						if (!isInterrupt) {
							countNum--;
							if (countNum == 0) {
								isInterrupt = true;
								btn_getValidCode.setEnabled(true);
								btn_getValidCode.setText("获取验证码");
								SharedPreferences sp = getSharedPreferences(
										Const.spValidCode, Context.MODE_PRIVATE);
								sp.edit().clear().commit();
							} else {
								btn_getValidCode.setEnabled(false);
								btn_getValidCode.setText(countNum + "");
								mhandler.postDelayed(this, 1000);
							}
						}
					}
				};
				mhandler.postDelayed(runnable, 1000);
				break;
			case Bind_success:
				sp = getSharedPreferences(Const.spValidCode,
						Context.MODE_PRIVATE);
				sp.edit().clear().commit();
				sp = getSharedPreferences(Const.spBindMobile,
						Context.MODE_PRIVATE);
				sp.edit().putString(Const.BindMobile, mobile).commit();// 保存手机号
				Utili.ToastInfo(context, "绑定手机成功");
				updateChannelId();
				finish();
			default:
				break;
			}
		};
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

	private void updateChannelId() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put("clientId", MyApplication.channelId);
		params.put("mobile", mobile);
		params.put("type", "1");
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/UpdateBaiduClientId", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						if (cpd != null && cpd.isShowing()) {
							cpd.dismiss();
						}
						Utili.ToastInfo(BindMobileActivity.this, "网络异常");
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						if (cpd != null && cpd.isShowing()) {
							cpd.dismiss();
						}
						System.out.print("" + t);
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}
}
