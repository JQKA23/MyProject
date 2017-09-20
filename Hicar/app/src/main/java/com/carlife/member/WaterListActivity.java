package com.carlife.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.Water;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class WaterListActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private CustomProgressDialog cpd;

	private String cardNo = "";
	private WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waterlist);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		

		wv = (WebView) findViewById(R.id.wv);
	}

	@Override
	protected void onResume() {
		Intent it = getIntent();
		Bundle bd = it.getExtras();
		cardNo = bd.getString("cardNo");
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("http://m.1018.com.cn/Activity/CardWater?cardno="+cardNo);

		super.onResume();
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
