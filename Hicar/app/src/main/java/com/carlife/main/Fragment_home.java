package com.carlife.main;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.carlife.R;
import com.carlife.carwash.CarWashActivity;
import com.carlife.chewu.ChewuActivity;
import com.carlife.daijia.DaijiaActivity;
import com.carlife.daijia.DaijiaCurrentOrderListActivity;
import com.carlife.global.Const;
import com.carlife.insurance.InsuranceMainActivity;
import com.carlife.main.MyInterface.RefreshMainUIListener;
import com.carlife.member.MyAccountActivity;
import com.carlife.rescue.RescueActivity;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;
import com.carlife.weizhang.WeizhangActivity;

public class Fragment_home extends Fragment implements OnClickListener,
		OnGetGeoCoderResultListener {
	private TextView tv_recharge, tv_orderTop, tv_hotLine, tv_weather,
			tv_daijia, tv_rescue, tv_wash, tv_chewu, tv_weizhang, tv_chexian,
			tv_tyre,
			tv_account/* , tv_homePage, tv_order, tv_mine, tv_more */;
	private ImageView img_ads;
	private LocationClient mLocClient = null;
	private BDLocationListener myListener = new MyLocationListener();
	private String latitude = "";
	private String longitude = "";
	private GeoCoder mSearch = null;
	private String mobile = "";
	private Context context;
	private RefreshMainUIListener myInterface;

	@Override
	@Deprecated
	public void onAttach(Activity activity) {
		context = activity;
		super.onAttach(activity);
		myInterface = (RefreshMainUIListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		initView(view);
		return view;
	}

	@Override
	public void onResume() {
		// 获取绑定手机号
		SharedPreferences sp = context.getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		Intent it;
		switch (v.getId()) {
		case R.id.img_ads:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, AdDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putString("url", "http://m.1018.com.cn/Activity/Zyrs"
						+ "?mobile=" + mobile);
				it.putExtras(bd);
			}
			startActivity(it);
			break;
		case R.id.tv_tyre:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				getAllCardsAmount();
			}
			break;

		case R.id.tv_daijia:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				getCurrentOrders();
			}

			break;
		case R.id.tv_rescue:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, RescueActivity.class);
			}

			startActivity(it);
			break;
		case R.id.tv_chewu:// 车务办理
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, ChewuActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_weizhang:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, WeizhangActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_hotLine:
			it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ getString(R.string.hotlineno)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		case R.id.tv_recharge:
		case R.id.tv_account:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, MyAccountActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_chexian:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, InsuranceMainActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_wash:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
			} else {
				it = new Intent(context, CarWashActivity.class);
			}
			startActivity(it);
			break;
		case R.id.tv_orderTop:
			if (mobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				myInterface.setFragment(2);
			}
			break;

		}
	}

	private void initView(View view) {
		tv_recharge = (TextView) view.findViewById(R.id.tv_recharge);
		tv_recharge.setOnClickListener(this);
		tv_orderTop = (TextView) view.findViewById(R.id.tv_orderTop);
		tv_orderTop.setOnClickListener(this);
		tv_hotLine = (TextView) view.findViewById(R.id.tv_hotLine);
		tv_hotLine.setOnClickListener(this);
		tv_weather = (TextView) view.findViewById(R.id.tv_weather);
		tv_daijia = (TextView) view.findViewById(R.id.tv_daijia);
		tv_daijia.setOnClickListener(this);
		tv_rescue = (TextView) view.findViewById(R.id.tv_rescue);
		tv_rescue.setOnClickListener(this);
		tv_wash = (TextView) view.findViewById(R.id.tv_wash);
		tv_wash.setOnClickListener(this);
		tv_chewu = (TextView) view.findViewById(R.id.tv_chewu);
		tv_chewu.setOnClickListener(this);
		tv_weizhang = (TextView) view.findViewById(R.id.tv_weizhang);
		tv_weizhang.setOnClickListener(this);
		tv_chexian = (TextView) view.findViewById(R.id.tv_chexian);
		tv_chexian.setOnClickListener(this);
		tv_tyre = (TextView) view.findViewById(R.id.tv_tyre);
		tv_tyre.setOnClickListener(this);
		tv_account = (TextView) view.findViewById(R.id.tv_account);
		tv_account.setOnClickListener(this);
		img_ads = (ImageView) view.findViewById(R.id.img_ads);
		img_ads.setOnClickListener(this);
		initBaiduMap();

	}

	@SuppressLint("DefaultLocale")
	private void getCurrentOrders() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);

		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetCurrentOrders", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						Utili.ToastInfo(context, "网络异常");
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
							JSONArray jsonArray = new JSONArray(jsonMessage);
							Intent it;
							if (jsonArray.length() == 0) {
								it = new Intent(context, DaijiaActivity.class);

							} else {
								it = new Intent(context,
										DaijiaCurrentOrderListActivity.class);
								it.putExtra("url", "/GetCurrentOrders");
							}
							startActivity(it);
						} catch (JSONException e) {
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

	private void initBaiduMap() {
		mLocClient = new LocationClient(context.getApplicationContext()); // 声明LocationClient类
		mLocClient.registerLocationListener(myListener); // 注册监听函数

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

	// 百度地图
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.i("定位启动", "ee");
			if (location != null) {
				stopLocation();
				latitude = location.getLatitude() + "";
				longitude = location.getLongitude() + "";
				LatLng point = new LatLng((Float.valueOf(latitude)),
						(Float.valueOf(longitude)));
				mSearch = GeoCoder.newInstance();
				mSearch.setOnGetGeoCodeResultListener(Fragment_home.this);
				mSearch.reverseGeoCode(new ReverseGeoCodeOption()
						.location(point));
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
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
				Utili.ToastInfo(context, "定位不成功");
			} else {
				String city = result.getAddressDetail().city.replace("市", "");
				getCarAndWeather(city);
			}
		} catch (Exception ex) {
			Utili.ToastInfo(context, "定位不成功");
			ex.printStackTrace();
		}
	}

	@SuppressLint("DefaultLocale")
	private void getCarAndWeather(final String city) {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("city", city);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("city", city);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/GetCarAndWeather", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
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
								tv_weather.setText(Utili.getCurrentDate() + " "
										+ city + "   "
										+ obj.getString("DriveInfo") + "  "
										+ obj.getString("Weather"));
							}

						} catch (JSONException e) {

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

	// 获取客户卡总余额
	@SuppressLint("DefaultLocale")
	private void getAllCardsAmount() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/GetAllCardsAmount", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						Utili.ToastInfo(context, "网络异常,请稍后再试");
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
								double sum = obj.getDouble("Sum");
								Intent it = new Intent(context, Act_Tyre.class);
								it.putExtra("money", sum);
								startActivity(it);
								// if (sum > 0) {
								// } else {
								// popCharge();
								// }
							}

						} catch (JSONException e) {

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

}
