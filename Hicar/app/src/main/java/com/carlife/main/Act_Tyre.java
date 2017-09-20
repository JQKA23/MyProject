package com.carlife.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.member.MemberCardActivity;
import com.carlife.utility.CustomProgressDialog;

public class Act_Tyre extends Activity implements OnClickListener {
	private WebView wv;
	private String url = "http://bd.mailuntai.cn/interfacefromhi1018/?t_type=bd&t_source=hi1018_h5";
	private String mobile;
	private TextView tv_tocharge;
	private double money;
	private Handler mHandler = new Handler();// android调用JS网页的时候会用到
	private CustomProgressDialog cpd;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addetail);
		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		money = getIntent().getDoubleExtra("money", 0);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		tv_tocharge = (TextView) findViewById(R.id.tv_toCharge);
		tv_tocharge.setOnClickListener(this);
		tv_tocharge.setVisibility(View.VISIBLE);
		wv = (WebView) findViewById(R.id.wv);

		wv.setWebChromeClient(new WebChromeClient());
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				cpd.dismiss();
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// 调用JS中的 函数，当然也可以不传参
						String string = "{\"username\":\""
								+ mobile
								+ "\",\"mobile\":\""
								+ mobile
								+ "\",\"email\":\"\",\"from\":\"hi1018\",\"loginstatus\":\"1\",\"money\":\""
								+ money + "\"}";
						wv.loadUrl("javascript:getFromAndroid('" + string
								+ "')");
					}
				});
			}
		});
		cpd = CustomProgressDialog.createDialog(this);
		cpd.show();
		wv.loadUrl(url);
		// wv.loadUrl("javascript:getFromAndroid(" + json + ")");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.tv_toCharge:
			Intent intent = new Intent(this, MemberCardActivity.class);
			startActivity(intent);
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
			wv.goBack(); // goBack()表示返回WebView的上一页面
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
