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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
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
import com.carlife.main.BindMobileActivity;
import com.carlife.model.Driver;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class DaijiaActivity extends Activity implements OnClickListener,
		OnGetGeoCoderResultListener {

	private TextView tv_address, tv_address_detail, tv_mobile, txt_count,
			tv_drivingYears, tv_ordercount;
	private Button btn_back, btn_menu, btn_price, btn_orders, btn_hotline,
			btn_mode, btn_add, btn_sub;
	private LinearLayout ll_menu;
	private ListView lv;
	private int driverCount = 1;
	private TextView tv_bid, tv_daijiao;
	private String bindMobile = "";
	private FrameLayout fl;

	private List<Driver> list;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private CustomProgressDialog cpd;
	private final static int getList_fail = 0;
	private final static int getList_success = 1;
	private final static int getDrivers = 100;
	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;

	private LocationClient mLocClient = null;
	private BDLocationListener myListener = new MyLocationListener();

	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private String lat = "39.90403";
	private String lon = "116.407525";
	private GeoCoder mSearch = null;

	private Context context;
	private String address = "";
	private String address_detail = "";
	private String consumerMobile = "";

	private Map<Driver, Marker> markerList = null;
	private int mode = 0;// 0 地图模式 1列表模式
	private int hideMenu = 0;
	private double x, y;
	private ImageView iv_reload, iv_search, ivs1, ivs2, ivs3, ivs4, ivs5;
	private ArrayList<String> addressList = new ArrayList<String>();
	private ArrayList<String> address_detailList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijia);
		btn_menu = (Button) findViewById(R.id.btn_menu);
		btn_menu.setOnClickListener(this);
		btn_mode = (Button) findViewById(R.id.btn_mode);
		btn_mode.setOnClickListener(this);

		ll_menu = (LinearLayout) findViewById(R.id.ll_menu);
		btn_price = (Button) findViewById(R.id.btn_price);
		btn_price.setOnClickListener(this);
		btn_orders = (Button) findViewById(R.id.btn_orders);
		btn_orders.setOnClickListener(this);
		btn_hotline = (Button) findViewById(R.id.btn_hotline);
		btn_hotline.setOnClickListener(this);
		tv_mobile = (TextView) findViewById(R.id.tv_mobile);
		tv_mobile.setOnClickListener(this);
		txt_count = (TextView) findViewById(R.id.txt_count);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);
		btn_sub = (Button) findViewById(R.id.btn_sub);
		btn_sub.setOnClickListener(this);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_drivingYears = (TextView) findViewById(R.id.tv_drivingYears);
		lv = (ListView) findViewById(R.id.lv);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.showZoomControls(false);
		mBaiduMap = mMapView.getMap();

		// 初始化司机覆盖物的点击listener
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(final Marker marker) {
				Iterator it = markerList.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					Marker value = (Marker) entry.getValue();
					if (value.equals(marker)) {
						Driver driver = (Driver) entry.getKey();
						View view = getDriversView(driver, R.drawable.geo_price);
						OnInfoWindowClickListener listener = null;
						listener = new OnInfoWindowClickListener() {
							public void onInfoWindowClick() {
								mBaiduMap.hideInfoWindow();
							}
						};
						LatLng ll = marker.getPosition();
						InfoWindow mInfoWindow = new InfoWindow(
								BitmapDescriptorFactory.fromView(view), ll,
								-47, listener);
						mBaiduMap.showInfoWindow(mInfoWindow);
						break;
					}
				}
				return false;
			}

		});

		// ll_address = (LinearLayout) findViewById(R.id.ll_address);
		tv_address = (TextView) findViewById(R.id.tv_address);
		tv_address.setOnClickListener(this);
		tv_address_detail = (TextView) findViewById(R.id.tv_address_detail);
		tv_address_detail.setOnClickListener(this);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		iv_reload = (ImageView) findViewById(R.id.iv_reload);
		iv_reload.setOnClickListener(this);

		iv_search = (ImageView) findViewById(R.id.iv_search);
		iv_search.setOnClickListener(this);

		tv_bid = (TextView) findViewById(R.id.tv_bid);
		tv_bid.setOnClickListener(this);
		tv_daijiao = (TextView) findViewById(R.id.tv_daijiao);
		tv_daijiao.setOnClickListener(this);

		context = this;
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mLocClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocClient.registerLocationListener(myListener); // 注册监听函数

		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
			@Override
			public void onMapStatusChange(MapStatus arg0) {

			}

			@Override
			public void onMapStatusChangeFinish(MapStatus ms) {
				// ll_address.setVisibility(View.GONE);
				// lat = ms.target.latitude + "";
				// lon = ms.target.longitude + "";
				// getDriversNearBy();
				// mSearch.reverseGeoCode(new ReverseGeoCodeOption()
				// .location(ms.target));
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
			}
		});

		SharedPreferences sp = getSharedPreferences(Const.spCallMobile,
				Context.MODE_PRIVATE);
		sp.edit().clear().commit();
		sp = getSharedPreferences(Const.spLocation, Context.MODE_PRIVATE);
		sp.edit().clear().commit();

	}

	@Override
	protected void onResume() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");
		if (mMapView == null) {
			mMapView.onResume();
		}
		if (mBaiduMap != null) {
			mBaiduMap.clear();
		}
		sp = getSharedPreferences(Const.spLocation, Context.MODE_PRIVATE);
		String addr = sp.getString(Const.Location, "");
		if (!addr.equals("")) {
			String[] addrs = addr.split("\\,");
			lat = addrs[0];
			lon = addrs[1];
			address = addrs[2];
			address_detail = addrs[3];
			tv_address.setText(address);
			tv_address_detail.setText(address_detail);
			LocationCenter();
		} else {
			initBaiduMap();
		}
		sp = getSharedPreferences(Const.spCallMobile, Context.MODE_PRIVATE);
		consumerMobile = sp.getString(Const.CallMobile, "");
		if (!consumerMobile.equals("")) {
			tv_mobile.setText(consumerMobile);
		} else {
			sp = getSharedPreferences(Const.spBindMobile, Context.MODE_PRIVATE);
			bindMobile = sp.getString(Const.BindMobile, "");
			tv_mobile.setText(bindMobile);
		}
		mode = 0;
		btn_mode.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.mode_list));
		lv.setVisibility(View.GONE);
		mMapView.setVisibility(View.VISIBLE);

		SharedPreferences sp1 = getSharedPreferences(Const.spLocation,
				Context.MODE_PRIVATE);
		String loc = sp1.getString(Const.Location, "");
		String[] arr = loc.split("\\,");
		if (arr.length == 4) {
			Log.i("-------", arr[0] + "," + arr[1] + "," + arr[2] + ","
					+ arr[3]);
			lat = arr[0];
			lon = arr[1];
			address = arr[2];
			address_detail = arr[3];
			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
					.location(new LatLng(Double.parseDouble(lat), Double
							.parseDouble(lon))));
		}

		super.onResume();
	}

	private void LocationCenter() {
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14f);
		mBaiduMap.setMapStatus(msu);
		LatLng point = new LatLng((Float.valueOf(lat)), (Float.valueOf(lon)));
		// 定位中心点
		msu = MapStatusUpdateFactory.newLatLng(point);
		mBaiduMap.setMapStatus(msu);
		getDriversNearBy();
	}

	@Override
	public void onDestroy() {
		destroyMap();
		stopLocation();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		if (cpd != null) {
			cpd.dismiss();
		}
		super.onDestroy();
	}

	private void destroyMap() {
		if (mSearch != null)
			mSearch.destroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_price:
			setMenu();
			Intent it = new Intent(context, DaijiaPriceActivity.class);
			Bundle bd = new Bundle();
			bd.putString("lat", lat);
			bd.putString("lon", lon);
			it.putExtras(bd);
			startActivity(it);
			break;
		case R.id.btn_orders:
			setMenu();
			if (bindMobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				it = new Intent(context, DaijiaCurrentOrderListActivity.class);
				it.putExtra("url", "/GetCurrentOrders");
				startActivity(it);
			}
			break;
		case R.id.btn_hotline:
			setMenu();
			it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ getString(R.string.hotlineno)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		case R.id.btn_menu:
			setMenu();
			break;
		case R.id.tv_daijiao:
			if (tv_mobile.getText().toString().length() != 11) {
				Utili.ToastInfo(context, "联系电话不合法,创建订单失败");
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("确定要下订单吗？");
				builder.setTitle("提示：");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								createOrders();
								dialog.dismiss();
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
			break;
		case R.id.tv_bid:
			mode = 1;
			btn_mode.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.mode_map));
			lv.setVisibility(View.VISIBLE);
			mMapView.setVisibility(View.GONE);
			loadList();
			break;
		case R.id.btn_mode:
			if (mode == 0) {
				mode = 1;
				btn_mode.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.mode_map));
				lv.setVisibility(View.VISIBLE);
				mMapView.setVisibility(View.GONE);
				loadList();
			} else {
				mode = 0;
				btn_mode.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.mode_list));
				lv.setVisibility(View.GONE);
				mMapView.setVisibility(View.VISIBLE);
				addMarkersToMap();
			}
			break;
		case R.id.iv_reload:
			initBaiduMap();
			break;
		case R.id.tv_address:
		case R.id.tv_address_detail:
		case R.id.iv_search:
			it = new Intent(context, DaijiaMapForSearchActivity.class);
			bd = new Bundle();

			bd.putString("lat", lat);
			bd.putString("lon", lon);
			bd.putString("address", address);
			bd.putString("address_detail", address_detail);
			bd.putStringArrayList("addressList", addressList);
			bd.putStringArrayList("address_detailList", address_detailList);
			it.putExtras(bd);
			startActivity(it);
			break;
		case R.id.btn_sub:
			if (driverCount <= 1) {
				Utili.ToastInfo(context, "最少选择1个司机哦");
			} else {
				driverCount--;
				txt_count.setText(driverCount + "");
			}
			break;
		case R.id.btn_add:
			if (driverCount >= 5) {
				Utili.ToastInfo(context, "最多选择5个司机哦");
			} else {
				driverCount++;
				txt_count.setText(driverCount + "");
			}
			break;
		case R.id.tv_mobile:
			it = new Intent(context, ChangeOrderMobileActivity.class);
			startActivity(it);
			break;
		}
	}

	private void setMenu() {
		if (hideMenu == 0) {
			ll_menu.setVisibility(View.VISIBLE);
			hideMenu = 1;
		} else {
			hideMenu = 0;
			ll_menu.setVisibility(View.GONE);
		}
	}

	private void getDriversNearBy() {
		handler.sendEmptyMessage(getDrivers);
	}

	private void getDrivers() {
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
								driver.setStar(obj.getDouble("Star"));
								driver.setDrivingYears(obj.getInt("Year"));
								driver.setOrderCount(obj.getInt("OrderCount"));
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
			switch (msg.what) {
			case getList_fail:
				Utili.ToastInfo(context, "没有获取到司机");
				break;
			case getList_success:
				addMarkersToMap();
				break;
			case getDrivers:
				// new Thread() {
				// public void run() {
				// getDrivers();
				// };
				// }.start();
				getDrivers();
				break;
			case createOrder_fail:
				Utili.ToastInfo(context, "创建订单失败");
				break;
			case createOrder_Success:
				Utili.ToastInfo(context, "创建订单成功");
				finish();
				break;
			default:
				break;
			}
		};
	};

	private void loadList() {
		if (list == null || list.size() <= 0) {
			return;
		}
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
				callDriver(driver);
			}
		});

	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("name", list.get(i).getRealName());
			item.put("distance", list.get(i).getDistance());
			item.put("lat", list.get(i).getLat());
			item.put("lon", list.get(i).getLon());
			item.put("bid", list.get(i).getBid() + "");
			data.add(item);
		}
	}

	private void addMarkersToMap() {
		mBaiduMap.clear();
		// 加小蓝点
		LatLng point = new LatLng(Double.parseDouble(lat),
				Double.parseDouble(lon));
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.bluedot);
		OverlayOptions option = new MarkerOptions().position(point)
				.icon(bitmap);
		mBaiduMap.addOverlay(option);
		// 加司机
		markerList = new HashMap<Driver, Marker>();
		if (list.size() > 0) {
			for (Driver d : list) {
				LatLng dporint = new LatLng(Double.parseDouble(d.getLat()),
						Double.parseDouble(d.getLon()));

				int currentcolor = R.drawable.drivermarker;
				OverlayOptions o = new MarkerOptions().position(dporint).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.drivermarker));
				// OverlayOptions o = new
				// MarkerOptions().position(dporint).icon(
				// BitmapDescriptorFactory
				// .fromBitmap(getViewBitmap(getDriversView(d,
				// currentcolor))));

				Marker m = (Marker) mBaiduMap.addOverlay(o);
				markerList.put(d, m);
			}
		}
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
		View view = View.inflate(this, R.layout.makerico, null);
		LinearLayout layout = (LinearLayout) view
				.findViewById(R.id.maker_bgLayout);
		layout.setBackgroundResource(currentcolor);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		tv_name.setText(driver.getRealName());
		TextView tv_km = (TextView) view.findViewById(R.id.tv_km);
		tv_km.setText(driver.getDistance() + "km");
		tv_ordercount = (TextView) view.findViewById(R.id.tv_ordercount);
		tv_ordercount.setText("代驾" + driver.getOrderCount() + "次");
		tv_drivingYears = (TextView) view.findViewById(R.id.tv_drivingYears);
		tv_drivingYears.setText("驾龄" + driver.getDrivingYears() + "年");
		ivs1 = (ImageView) view.findViewById(R.id.ivs1);
		ivs2 = (ImageView) view.findViewById(R.id.ivs2);
		ivs3 = (ImageView) view.findViewById(R.id.ivs3);
		ivs4 = (ImageView) view.findViewById(R.id.ivs4);
		ivs5 = (ImageView) view.findViewById(R.id.ivs5);
		initDriverStar(driver.getStar());
		return view;
	}

	private void callDriver(Driver driver) {
		if (bindMobile.equals("")) {
			Intent i = new Intent(context, BindMobileActivity.class);
			startActivity(i);
		} else {
			Intent i = new Intent(context, DriverActivity.class);
			Bundle bd = new Bundle();
			bd.putSerializable("driver", driver);
			bd.putString("lon", lon);
			bd.putString("lat", lat);
			bd.putString("addr", address_detail + "," + address);
			i.putExtras(bd);
			startActivity(i);
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

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		try {
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				tv_address.setText("暂未获取到您的地址");
			} else {
				SharedPreferences sp = getSharedPreferences(Const.City,
						MODE_PRIVATE);
				sp.edit().putString(Const.City, result.getAddressDetail().city)
						.commit();

				address_detail = result.getAddress();
				List<PoiInfo> list = result.getPoiList();
				if (list.size() > 0) {

					// ll_address.setVisibility(View.VISIBLE);
					// ///////////////////////

					address = list.get(0).name;
					tv_address.setText(address);
					tv_address_detail.setText(address_detail);
					sp = getSharedPreferences(Const.spLocation,
							Context.MODE_PRIVATE);
					sp.edit()
							.putString(
									Const.Location,
									lat + "," + lon + "," + address + ","
											+ address_detail).commit();
					LocationCenter();
					if (list.size() > 1) {
						addressList.clear();
						address_detailList.clear();
						for (int i = 1; i < list.size(); i++) {
							addressList.add(list.get(i).name);
							address_detailList.add(list.get(i).address);
						}
					}
				}
			}
		} catch (Exception ex) {
			tv_address.setText("暂未获取到您的地址");
		}
	}

	private void createOrders() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		String remark = "";
		consumerMobile = tv_mobile.getText().toString();
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("driverCount", driverCount + "");
		params.put("lon", lon);
		params.put("lat", lat);
		params.put("address", address_detail + address);
		params.put("callMobile", bindMobile);
		params.put("consumerMobile", consumerMobile);
		params.put("remark", remark);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("driverCount", driverCount + "");
		map.put("lon", lon);
		map.put("lat", lat);
		map.put("address", address_detail + address);
		map.put("callMobile", bindMobile);
		map.put("consumerMobile", consumerMobile);
		map.put("remark", remark);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateOrders", params,
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

	@SuppressWarnings("deprecation")
	private void initDriverStar(double star) {
		double s = star * 10;

		if (s >= 50) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
		} else if (s > 40) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 30) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 20) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 10) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = event.getX();
		y = event.getY();
		return super.onTouchEvent(event);
	}

}
