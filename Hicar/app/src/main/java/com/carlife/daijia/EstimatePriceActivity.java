package com.carlife.daijia;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomDialog.Builder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class EstimatePriceActivity extends Activity implements OnClickListener,OnGetGeoCoderResultListener,
OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
	
	private Button btn_back;
	private TextView tv_start,tv_start_address,tv_dis,tv_money;
	private EditText et_end;
	private ListView lv_address;
	private CustomProgressDialog cpd;

	private GeoCoder mSearch = null;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	
	private Context context;
	private String city = "";
	private String lat="";
	private String lon="";	
	private LinearLayout ll_dis;
	
	private boolean isXinjian=false;
	private int startPrice=38;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.estimate);
		context=this;
		tv_start=(TextView)findViewById(R.id.tv_start);		
		tv_start_address=(TextView)findViewById(R.id.tv_start_address);
		et_end=(EditText)findViewById(R.id.et_end);	
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		lv_address=(ListView)findViewById(R.id.lv_address);
		ll_dis=(LinearLayout)findViewById(R.id.ll_dis);
		tv_dis=(TextView)findViewById(R.id.tv_dis);
		tv_money=(TextView)findViewById(R.id.tv_money);
		
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		String address=bd.getString("address");
		if(address.contains("新疆")||address.contains("乌鲁木齐")){
		  isXinjian=true;	
		}
		String[] ads=address.split("\\,");
		tv_start.setText(ads[1]);
		tv_start_address.setText(ads[0]);
		lat=bd.getString("latitude");
		lon=bd.getString("longitude");
		startPrice=bd.getInt("startPrice");
		
		SharedPreferences sp = getSharedPreferences(Const.City, MODE_PRIVATE);
		city = sp.getString(Const.City, "北京");

		et_end.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}

				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(city));
				ll_dis.setVisibility(View.GONE);
				lv_address.setVisibility(View.VISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// mSearch.geocode(new GeoCodeOption().city(city).address(et_end.getText().toString()));
			}
		});

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
	}

	@Override
	public void onClick(View v) {		
		switch (v.getId()) {
		case R.id.btn_back:
		    finish();
			break;
		case R.id.btn_submit:
			
			break;
		}	
	}
	
	@Override
	public void onDestroy() {
		mSearch.destroy();
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		super.onDestroy();
	}

	
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if(cpd!=null&&cpd.isShowing()){
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {		
			
			default:
				break;
			}
		};
	};

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			Map<String, String> item = new HashMap<String, String>();
			if (info.key != null)
				item.put("Address", info.key);
			item.put("Latitude", "");
			item.put("Longitude", "");
			data.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data,
				R.layout.addressitem, new String[] { "Address" },
				new int[] { R.id.txt_address });
		lv_address.setAdapter(adapter);
		lv_address.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				mSearch.geocode(new GeoCodeOption().city(city).address(
						data.get(position).get("Address")));				
			}
		});
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		
	}

	@Override
	public void onGetPoiResult(PoiResult arg0) {
		
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			// 没取到
		} else {
			SharedPreferences sp = getSharedPreferences(Const.spAddressList,
					MODE_PRIVATE);
			String addressArray = sp.getString(Const.AddressList, "");
			String current = result.getLocation().latitude + "@"
					+ result.getLocation().longitude + "@"
					+ result.getAddress();
			et_end.setText(result.getAddress());
			if (!addressArray.contains(result.getAddress())) {
				sp.edit()
						.putString(Const.AddressList,
								current + "|" + addressArray).commit();// 加入到地址列表
			}
			sp = getSharedPreferences(Const.spLocatedHand, MODE_PRIVATE);
			sp.edit().putString(Const.LoatedHandAddress,result.getAddress()).commit();
			sp.edit().putString(Const.latitudeByHand,result.getLocation().latitude+"").commit();
			sp.edit().putString(Const.longitudeByHand,result.getLocation().longitude+"").commit();
			LatLng p1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
			LatLng p2 = new LatLng(result.getLocation().latitude, result.getLocation().longitude);
			double dis = DistanceUtil.getDistance(p1, p2)/1000;
			tv_dis.setText(String.format("%.1f", dis));
			int sum=startPrice;
			if(isXinjian){
				if(dis>5){
					dis = dis -5;
					int sections = (int) (dis % 5 == 0 ? dis / 5 : dis / 5 + 1);					
					sum += 10 * sections;
				} 				
			}
			else{
				if(dis>10){
					dis = dis -10;
					int sections = (int) (dis % 10 == 0 ? dis / 10 : dis / 10 + 1);					
					sum += 20 * sections;
				} 
			}
			tv_money.setText(sum+"");
			
			ll_dis.setVisibility(View.VISIBLE);
			lv_address.setVisibility(View.GONE);
		}
	}


	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
				
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult arg0) {
		// TODO Auto-generated method stub
		
	}
}
