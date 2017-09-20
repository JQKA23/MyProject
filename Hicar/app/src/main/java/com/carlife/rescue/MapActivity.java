package com.carlife.rescue;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
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
import com.carlife.daijia.DaijiaMapForSearchActivity.MyLocationListener;
import com.carlife.global.Const;
import com.carlife.utility.Utili;
import com.carlife.utility.Oper;


public class MapActivity extends Activity implements OnClickListener,OnGetGeoCoderResultListener,
OnGetPoiSearchResultListener, OnGetSuggestionResultListener  {
	
	private Button btn_back;	
	private TextView txt_address,txt_address_detail;
	
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private String address="";
	private String address_detail="";
	private EditText et_address;
	
	private GeoCoder mSearch = null;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	
	private Context context;
	private String city = "";
	private String lat="";
	private String lon="";	
	
	private LinearLayout ll_address;	
	private ListView lv_address;	
	private ImageView iv_reload;
	private int isChoose=0;
	
	private LocationClient mLocClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		context=this;
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);	
		ll_address=(LinearLayout)findViewById(R.id.ll_address);
		lv_address=(ListView)findViewById(R.id.lv_address);
		et_address=(EditText)findViewById(R.id.et_address);
		iv_reload=(ImageView)findViewById(R.id.iv_reload);
		iv_reload.setOnClickListener(this);
		
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		mBaiduMap.setMapStatus(msu);
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		lat=bd.getString("lat");
		lon=bd.getString("lon");
		address=bd.getString("address");
		address_detail=bd.getString("address_detail");
		
		mLocClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocClient.registerLocationListener(myListener); // 注册监听函数
		
		LocationCenter();
		
		txt_address=(TextView)findViewById(R.id.txt_address);
		txt_address.setText(address);
		txt_address_detail=(TextView)findViewById(R.id.txt_address_detail);
		txt_address_detail.setText(address_detail);
		
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener(){
			@Override
			public void onMapStatusChange(MapStatus arg0) {	
			
			}
			
			@Override
			public void onMapStatusChangeFinish(MapStatus ms) {				
				lat=ms.target.latitude+"";
				lon=ms.target.longitude+"";	
				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(ms.target));	
			}
			
			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				
			}});
		SharedPreferences sp = getSharedPreferences(Const.City, MODE_PRIVATE);
		city = sp.getString(Const.City, "北京");

		et_address.addTextChangedListener(new TextWatcher() {
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
				lv_address.setVisibility(View.VISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

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
	protected void onResume() {		
		
		isChoose=0;
		if(mMapView==null){
			mMapView.onResume();
			}
		if (mBaiduMap != null) {
				mBaiduMap.clear();
			}
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {		
		if(mSearch!=null)
		{
			mSearch.destroy();
		}	
		if(mMapView!=null){
			mMapView.onDestroy();
		}
		super.onDestroy();
	}
	
	

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_back:
			finish();
			break;
		case R.id.iv_reload:
			initBaiduMap();
			break;
		}
		
	}

	// 以下为百度地图定位
			public class MyLocationListener implements BDLocationListener {
				@Override
				public void onReceiveLocation(BDLocation location) {
					Log.i("定位启动", "ee");
					if (location != null) {
						stopLocation();
						lat = location.getLatitude() + "";
						lon = location.getLongitude() + "";

						LatLng point = new LatLng((Float.valueOf(lat)),
								(Float.valueOf(lon)));
						mSearch.reverseGeoCode(new ReverseGeoCodeOption()
								.location(point));
					}
				}

				public void onReceivePoi(BDLocation poiLocation) {
				}
			}

			private void initBaiduMap() {
				LocationClientOption option = new LocationClientOption();
				option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
				option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
				option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
				option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
				option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
				mLocClient.setLocOption(option);
				mLocClient.start();
				if (mLocClient != null) {
					mLocClient.requestLocation();
				}
			}

			// 销毁定位
			private void stopLocation() {
				if (mLocClient != null) {
					mLocClient.stop();
				}
			}

	private void LocationCenter(){
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14f);
		mBaiduMap.setMapStatus(msu);
		LatLng point = new LatLng((Float.valueOf(lat)), (Float.valueOf(lon)));
		// 定位中心点
		msu = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.setMapStatus(msu);
	}
	
	
	

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {		
		try {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				txt_address.setText("");
				txt_address_detail.setText("");
			} else {
				address_detail=result.getAddress();
				List<PoiInfo> list= result.getPoiList();					
				if(list.size()>0){
					address=list.get(0).name;
					txt_address.setText(address);
					txt_address_detail.setText(address_detail);
					SharedPreferences sp=getSharedPreferences(Const.spLocation,	Context.MODE_PRIVATE);
					sp.edit().putString(Const.Location,lat+","+lon+","+address+","+address_detail).commit();
					ll_address.setVisibility(View.VISIBLE);
					LocationCenter();
					if(isChoose==1){
						finish();}

				}					
			}
		} catch (Exception ex) {
			
		}
	}
	
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
				isChoose=1;			}
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
			lat=result.getLocation().latitude+"";
			lon=result.getLocation().longitude+"";
			String current = lat + "@"
					+ lon + "@"
					+ result.getAddress();
			et_address.setText(result.getAddress());
			if (!addressArray.contains(result.getAddress())) {
				sp.edit()
						.putString(Const.AddressList,
								current + "|" + addressArray).commit();// 加入到地址列表
			}
			sp = getSharedPreferences(Const.spLocatedHand, MODE_PRIVATE);
			sp.edit().putString(Const.LoatedHandAddress,result.getAddress()).commit();
			sp.edit().putString(Const.latitudeByHand,result.getLocation().latitude+"").commit();
			sp.edit().putString(Const.longitudeByHand,result.getLocation().longitude+"").commit();			
			lv_address.setVisibility(View.GONE);
			
			LocationCenter();
		
			LatLng point = new LatLng((Float.valueOf(lat)), (Float.valueOf(lon)));
			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
					.location(point));
			
		}
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
