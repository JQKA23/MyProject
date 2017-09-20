package com.carlife.daijia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.Driver;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;

public class DriverListActivity extends Activity implements OnClickListener,
		OnGetGeoCoderResultListener {

	private Button btn_back, btn_mode;
	private ListView lv;
	private CustomProgressDialog cpd;
	private final static int getList_fail = 0;
	private final static int getList_success = 1;
	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;

	private List<Driver> list;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private Context context;
	private String lat = "39.90403";
	private String lon = "116.407525";

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private int mode = 0;// 0 列表模式 1地图模式

	private Map<Driver, Marker> markerList = null;
	private GeoCoder mSearch = null;
	private ImageView iv_location;
	private String addr = "";
	private String callMobile = "";
	private String consumerMobile = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.driverlist);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_mode = (Button) findViewById(R.id.btn_mode);
		btn_mode.setOnClickListener(this);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

		iv_location = (ImageView) findViewById(R.id.iv_location);
		context = this;
		lv = (ListView) findViewById(R.id.lv);
		Intent it = getIntent();
		Bundle bd = it.getExtras();
		lat = bd.getString("lat");
		lon = bd.getString("lon");
		callMobile = bd.getString("callMobile");
		consumerMobile = bd.getString("consumerMobile");
		addr = bd.getString("addr");

		getDriversNearBy();
		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChange(MapStatus arg0) {

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus ms) {
				lat = ms.target.latitude + "";
				lon = ms.target.longitude + "";
				getDriversNearBy();
				mSearch.reverseGeoCode(new ReverseGeoCodeOption()
						.location(ms.target));
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {

			}
		});

	}

	@Override
	protected void onResume() {
		if (mBaiduMap != null) {
			mBaiduMap.clear();
		}
		mMapView.onResume();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBaiduMap.setMapStatus(msu);
		LatLng point = new LatLng((Float.valueOf(lat)), (Float.valueOf(lon)));
		// 定位中心点
		msu = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.setMapStatus(msu);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		super.onDestroy();
	}

	private void getDriversNearBy() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("lon", lon);
		params.put("lat", lat);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("lon", lon);
		map.put("lat", lat);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetDriversNearBy", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(getList_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						System.out.print("" + t);
						String jsonMessage = Utili.GetJson("" + t);

						try {
							list = new ArrayList<Driver>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								Driver driver = new Driver();
								driver.setId(obj.getInt("Id"));
								driver.setMobile(obj.getString("Mobile"));
								driver.setLat(obj.getString("Latitude"));
								driver.setLon(obj.getString("Longitude"));
								driver.setBid(obj.getInt("Bid"));
								driver.setDistance(obj.getString("Distance"));
								driver.setRealName(obj.getString("RealName"));
								list.add(driver);
							}
							handler.sendEmptyMessage(getList_success);

						} catch (JSONException e) {
							handler.sendEmptyMessage(getList_fail);
							System.out.print(e);
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
			case getList_fail:
				Utili.ToastInfo(context, "没有获取到司机");
				break;
			case getList_success:
				loadList();
				break;
			case createOrder_fail:
				Utili.ToastInfo(context, "创建订单失败");
				break;
			case createOrder_Success:
				Utili.ToastInfo(context, "创建订单成功");
				break;
			default:
				break;
			}
		};
	};

	private void loadList() {
		prepareData();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.driveritem,
				new String[] { "name", "distance", "bid" }, new int[] {
						R.id.txt_name, R.id.txt_distance, R.id.txt_bid, });

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Driver driver = list.get(position);
				popCallDriver(driver);
			}
		});
		addMarkersToMap();
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("name", list.get(i).getRealName());
			item.put("distance", list.get(i).getDistance());
			item.put("lat", list.get(i).getLat());
			item.put("lon", list.get(i).getLon());
			item.put("bid", list.get(i).getBid()+"");
			data.add(item);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_mode:
			if (mode == 0) {
				mode = 1;
				btn_mode.setText("列表模式");
				lv.setVisibility(View.GONE);
				mMapView.setVisibility(View.VISIBLE);
				iv_location.setVisibility(View.VISIBLE);
			} else {
				mode = 0;
				btn_mode.setText("地图模式");
				lv.setVisibility(View.VISIBLE);
				mMapView.setVisibility(View.GONE);
				iv_location.setVisibility(View.GONE);
			}
			break;
		}

	}

	private void addMarkersToMap() {
		mBaiduMap.clear();
		// 加司机
		markerList = new HashMap<Driver, Marker>();
		if (list.size() > 0) {
			for (Driver d : list) {
				LatLng dporint = new LatLng(Double.parseDouble(d.getLat()),
						Double.parseDouble(d.getLon()));

				int currentcolor = R.drawable.blue;
				OverlayOptions o = new MarkerOptions().position(dporint).icon(
						BitmapDescriptorFactory
								.fromBitmap(getViewBitmap(getDriversView(d,
										currentcolor))));

				Marker m = (Marker) mBaiduMap.addOverlay(o);
				markerList.put(d, m);
			}
		}

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				Iterator it = markerList.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					Marker value = (Marker) entry.getValue();
					if (value.equals(marker)) {
						Driver driver = (Driver) entry.getKey();
						popCallDriver(driver);
						break;
					}
				}
				return false;
			}

		});
	}

	private Bitmap getViewBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	private View getDriversView(Driver driver, int currentcolor) {
		View layoutView = View.inflate(this, R.layout.map, null);
		View view = View.inflate(this, R.layout.makerico, null);
		LinearLayout layout = (LinearLayout) view
				.findViewById(R.id.maker_bgLayout);
		layout.setBackgroundResource(currentcolor);
//		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
//		tv_name.setText(driver.getRealName());
		TextView tv_bid = (TextView) view.findViewById(R.id.tv_bid);
		tv_bid.setText("￥" + driver.getBid());
//		TextView tv_distance = (TextView) view.findViewById(R.id.tv_distance);
//		tv_distance.setText("距离" + driver.getDistance() + "km");
		return view;
	}

	private void popCallDriver(final Driver driver) {
		SharedPreferences sp = getSharedPreferences(Const.spOrderCount,
				Context.MODE_PRIVATE);
		int orderCount = sp.getInt(Const.OrderCount, 0);
		if (orderCount >= 5) {
           Utili.ToastInfo(context, "一次最多叫5个司机");
		} else {
			CustomDialog.Builder builder = new CustomDialog.Builder(this);
			builder.setMessage("指定该司机代驾？");
			builder.setTitle("提示");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							createOrder(driver);
						}
					});

			builder.setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
	}

	private void createOrder(final Driver driver) {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("lon", lon);
		params.put("lat", lat);
		params.put("address", addr);
		params.put("callMobile", callMobile);
		params.put("consumerMobile", consumerMobile);
		params.put("driverId", driver.getId() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("lon", lon);
		map.put("lat", lat);
		map.put("address", addr);
		map.put("callMobile", callMobile);
		map.put("consumerMobile", consumerMobile);
		map.put("driverId", driver.getId() + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateOrder", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(createOrder_fail);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);

					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						System.out.print("" + t);
						String jsonMessage = Utili.GetJson("" + t);
						try {
							JSONObject obj = new JSONObject(jsonMessage);
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								handler.sendEmptyMessage(createOrder_Success);
							} else {
								handler.sendEmptyMessage(createOrder_fail);
							}

						} catch (JSONException e) {
							handler.sendEmptyMessage(createOrder_fail);
							System.out.print(e);
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
	public void onGetGeoCodeResult(GeoCodeResult arg0) {

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		try {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

			} else {
				String address_detail = result.getAddress();
				List<PoiInfo> list = result.getPoiList();
				if (list.size() > 0) {
					String address = list.get(0).name;
					SharedPreferences sp = getSharedPreferences(
							Const.spLocation, Context.MODE_PRIVATE);
					sp.edit()
							.putString(
									Const.Location,
									lat + "," + lon + "," + address + ","
											+ address_detail).commit();
					addr = address_detail + "," + address;
				}
			}
		} catch (Exception ex) {

		}
	}
}
