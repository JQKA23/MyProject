package com.carlife.daijia;


import com.carlife.R;
import com.carlife.R.id;
import com.carlife.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DaijiaPriceActivity extends Activity implements OnClickListener {
	
	
	private Button btn_back;
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiaprice);
		
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		
		wv=(WebView)findViewById(R.id.wv);
		wv.getSettings().setJavaScriptEnabled(true);
		Intent it = getIntent();
		Bundle bd = it.getExtras();
		String lat = bd.getString("lat");
		String lon = bd.getString("lon");
		wv.loadUrl("http://m.1018.com.cn/Activity/DaijiaPrice?lat="+lat+"&lon="+lon);
	}

	@Override
	public void onClick(View v) {		
		switch (v.getId()) {
		case R.id.btn_back:
		    finish();
			break;		
		}
	
	}
}
