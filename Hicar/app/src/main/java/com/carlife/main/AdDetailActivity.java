package com.carlife.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;

import com.carlife.R;
import com.carlife.utility.webViewClient;

public class AdDetailActivity extends Activity implements OnClickListener {

	private WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addetail);
		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		wv = (WebView) findViewById(R.id.wv);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebChromeClient(new WebChromeClient());
		wv.setWebViewClient(new webViewClient());
		Intent it = getIntent();
		Bundle bd = it.getExtras();
		String url = bd.getString("url");
		wv.loadUrl(url);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		}

	}
}
