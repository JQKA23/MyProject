package com.carlife.carwash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.Act_CommonOrderList;
import com.carlife.main.BindMobileActivity;
import com.carlife.member.AddCarActivity;
import com.carlife.model.CarWashCompany;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class CarWashActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 0;
	private final static int getOrder_success = 1;

	private List<CarWashCompany> list;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private ListView lv;
	private Context context;

	private String mobile = "";
	private LocationClient mLocClient = null;
	private String lon = "";
	private String lat = "";
	private BDLocationListener myListener = new MyLocationListener();

	private TextView tv_order;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carwash);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		lv = (ListView) findViewById(R.id.lv);
		tv_order = (TextView) findViewById(R.id.tv_order);
		tv_order.setOnClickListener(this);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

		mLocClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
		mLocClient.registerLocationListener(myListener); // 注册监听函数

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
				getCarWashCompanys();
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void initBaiduMap() {
		Log.i("开启地图", "开启地图");
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
	protected void onResume() {
		initBaiduMap();
		super.onResume();
	}

	private void getCarWashCompanys() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("lat", lat);
		params.put("lon", lon);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("lat", lat);
		map.put("lon", lon);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetCarWashCompanys", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(getOrder_fail);
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
							list = new ArrayList<CarWashCompany>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								CarWashCompany car = new CarWashCompany();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								car.setId(obj.getInt("Id"));
								car.setAddress(obj.getString("Address"));
								car.setCompanyName(obj.getString("CompanyName"));
								car.setCompanyPhone(obj
										.getString("CompanyPhone"));
								car.setDistance(obj.getString("Distance"));
								car.setLatitude(obj.getString("Latitude"));
								car.setLongitude(obj.getString("Longitude"));
								car.setMobilePhone(obj.getString("MobilePhone"));
								car.setPriceBigCar(obj.getInt("PriceBigCar"));
								car.setPriceBigDiscount(obj
										.getInt("PriceBigDiscount"));
								car.setPriceSmallCar(obj
										.getInt("PriceSmallCar"));
								car.setPriceSmallDiscount(obj
										.getInt("PriceSmallDiscount"));
								car.setRemark(obj.getString("Remark"));
								car.setStar(obj.getInt("Star"));
								car.setOrderCount(obj.getInt("OrderCount"));
								car.setPicUrl(obj.getString("PicUrl"));
								list.add(car);
							}
							handler.sendEmptyMessage(getOrder_success);

						} catch (JSONException e) {
							handler.sendEmptyMessage(getOrder_fail);
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

	private void loadList() {
		prepareData();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.carwashitem, new String[] { "name", "address",
						"price", "discount", "mobile", "star", "distance",
						"picUrl" },
				new int[] { R.id.tv_name, R.id.tv_address, R.id.tv_pricesmall,
						R.id.tv_pricesmalldiscount, R.id.tv_mobile,
						R.id.rb_star, R.id.tv_distance, R.id.iv });

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mobile.equals("")) {
					Intent i = new Intent(context, BindMobileActivity.class);

					startActivity(i);
				} else {
					CarWashCompany car = list.get(position);
					Intent i = new Intent(context, CarWashDetailActivity.class);
					// Bundle bd = new Bundle();
					i.putExtra("carwash", car);
					// bd.putSerializable("carwash", car);
					i.putExtra("lat", lat);
					i.putExtra("lon", lon);
					startActivity(i);
				}
			}
		});
		listAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if (view.getId() == R.id.rb_star) {
					String stringval = (String) data;
					float ratingValue = Float.parseFloat(stringval);
					RatingBar ratingBar = (RatingBar) view;
					ratingBar.setRating(ratingValue);
					return true;
				} else if (view.getId() == R.id.iv) {
					String picPath = (String) data;
					ImageView iv = (ImageView) view;
					if (!picPath.equals("")) {
						FinalBitmap fb = FinalBitmap.create(context
								.getApplicationContext());
						fb.configBitmapLoadThreadSize(3);
						fb.configDiskCachePath(context.getApplicationContext()
								.getFilesDir().toString()); // 设置缓存目录；
						fb.configDiskCacheSize(1024 * 1024 * 10);// 设置缓存大小
						fb.configLoadingImage(R.drawable.ic_launcher);// 设置加载图片
						fb.display(iv, picPath);
					} else {
						iv.setImageResource(R.drawable.ic_launcher);
					}
					return true;
				}
				return false;
			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("name", list.get(i).getCompanyName());
			item.put("address", list.get(i).getAddress());
			item.put("price", "￥" + list.get(i).getPriceSmallCar());
			item.put("discount", "￥" + list.get(i).getPriceSmallDiscount());
			item.put("mobile", list.get(i).getCompanyPhone());
			item.put("star", list.get(i).getStar() + "");
			item.put("distance", "距离" + list.get(i).getDistance() + "公里");
			item.put("picUrl", list.get(i).getPicUrl());
			data.add(item);
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			case getOrder_fail:
				Utili.ToastInfo(CarWashActivity.this, "系统错误，请稍后重试");
				break;
			case getOrder_success:
				loadList();
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_add:
			Intent it = new Intent(context, AddCarActivity.class);
			startActivity(it);
			break;
		case R.id.tv_order:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				it = new Intent(context, Act_CommonOrderList.class);
				it.putExtra("url", "/GetCarWashOrders");
				startActivity(it);
			}
			break;
		}

	}
}
