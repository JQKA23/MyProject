package com.carlife.carwash;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.CarWashCompany;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class CarWashDetailActivity extends Activity implements OnClickListener {

	private RatingBar rb_star;
	private Button btn_back, btn_pay;

	private TextView tv_name, tv_ordercount, tv_address, tv_pricebig,
			tv_pricebigdiscount, tv_pricesmall, tv_pricesmalldiscount, tv_tel,
			tv_distance;

	private CustomProgressDialog cpd;
	private CarWashCompany carwash;

	private ImageView iv;

	private Context context;

	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;

	private String lat = "39.90403";
	private String lon = "116.407525";
	private String companyLat = "";
	private String companyLon = "";

	private String bindMobile = "";
	private String addr = "";
	private String companyMobile = "";

	private ImageView iv_big, iv_small;
	private int carType = 0;// 小车
	private final static int getbonus_error = 100;
	private final static int getbonus_success = 111;
	private int bonusId = 0;
	private int payType = 0;
	private String cardNo = "";
	private String cardInfo = "";
	private String price = "";
	private MapView mapView__carWashDetail;
	private BaiduMap bdMap;
	private TextView tv_toNaVi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carwashdetail);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_address = (TextView) findViewById(R.id.tv_address);
		rb_star = (RatingBar) findViewById(R.id.rb_star);
		tv_pricebig = (TextView) findViewById(R.id.tv_pricebig);
		tv_pricebigdiscount = (TextView) findViewById(R.id.tv_pricebigdiscount);
		tv_pricesmall = (TextView) findViewById(R.id.tv_pricesmall);
		tv_pricesmalldiscount = (TextView) findViewById(R.id.tv_pricesmalldiscount);
		tv_tel = (TextView) findViewById(R.id.tv_tel);
		tv_tel.setOnClickListener(this);
		tv_toNaVi = (TextView) findViewById(R.id.tv_toNavi);
		tv_toNaVi.setOnClickListener(this);
		tv_distance = (TextView) findViewById(R.id.tv_distance);

		context = this;
		// 初始化百度地图
		mapView__carWashDetail = (MapView) findViewById(R.id.mapView__carWashDetail);
		mapView__carWashDetail.showZoomControls(false);
		bdMap = mapView__carWashDetail.getMap();

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_pay = (Button) findViewById(R.id.btn_pay);
		btn_pay.setOnClickListener(this);

		iv_big = (ImageView) findViewById(R.id.iv_big);
		iv_big.setOnClickListener(this);
		iv_small = (ImageView) findViewById(R.id.iv_small);
		iv_small.setOnClickListener(this);

		iv = (ImageView) findViewById(R.id.iv);
		// Bundle bd = getIntent().getExtras();
		Intent bd = getIntent();
		// 获取司机坐标
		lat = bd.getStringExtra("lat");
		lon = bd.getStringExtra("lon");
		// 获取洗车店详情
		carwash = (CarWashCompany) bd.getSerializableExtra("carwash");
		tv_name.setText(carwash.getCompanyName());
		rb_star.setRating(carwash.getStar());
		String adress = carwash.getAddress();
		if (adress.length() <= 15) {
			tv_address.setText(adress);
		} else {
			tv_address.setText(adress.substring(0, 13) + "...");
		}
		tv_ordercount.setText("洗车" + carwash.getOrderCount() + "次");
		tv_pricebig.setText("￥" + carwash.getPriceBigCar());
		tv_pricebigdiscount.setText("￥" + carwash.getPriceBigDiscount());
		tv_pricesmall.setText("￥" + carwash.getPriceSmallCar());
		tv_pricesmalldiscount.setText("￥" + carwash.getPriceSmallDiscount());
		price = carwash.getPriceSmallDiscount() + "";
		tv_distance.setText("距离" + carwash.getDistance() + "公里");
		companyMobile = carwash.getCompanyPhone();
		companyLat = carwash.getLatitude();
		companyLon = carwash.getLongitude();

		// 地图定位中心点
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
		bdMap.setMapStatus(msu);
		LatLng latLng = new LatLng(Double.parseDouble(companyLat),
				Double.parseDouble(companyLon));
		msu = MapStatusUpdateFactory.newLatLng(latLng);
		bdMap.setMapStatus(msu);
		// 添加代表洗车店的图标
		BitmapDescriptor descriptor = BitmapDescriptorFactory
				.fromResource(R.drawable.bluedot);
		OverlayOptions options = new MarkerOptions().position(latLng).icon(
				descriptor);
		bdMap.addOverlay(options);

		if (!carwash.getPicUrl().equals("")) {
			FinalBitmap fb = FinalBitmap
					.create(context.getApplicationContext());
			fb.display(iv, carwash.getPicUrl());
		}
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_tel:
			Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ companyMobile));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		case R.id.iv_small:
			carType = 0;
			iv_big.setImageDrawable(getResources().getDrawable(
					R.drawable.nochoosen));
			iv_small.setImageDrawable(getResources().getDrawable(
					R.drawable.choosen));
			price = carwash.getPriceSmallDiscount() + "";
			break;
		case R.id.iv_big:
			carType = 1;
			iv_big.setImageDrawable(getResources().getDrawable(
					R.drawable.choosen));
			iv_small.setImageDrawable(getResources().getDrawable(
					R.drawable.nochoosen));
			price = carwash.getPriceBigDiscount() + "";
			break;
		case R.id.btn_pay:
			popPay();
			break;
		case R.id.tv_toNavi:
			LatLng sLatLng = new LatLng(Double.parseDouble(lat),
					Double.parseDouble(lon));
			LatLng eLatLng = new LatLng(Double.parseDouble(companyLat),
					Double.parseDouble(companyLon));
			NaviParaOption para = new NaviParaOption().startPoint(sLatLng)
					.endPoint(eLatLng).startName("").endName("");
			try {
				BaiduMapNavigation.openBaiduMapNavi(para,
						CarWashDetailActivity.this);
			} catch (BaiduMapAppNotSupportNaviException e) {
				e.printStackTrace();
				Utili.showDialog(CarWashDetailActivity.this);
			}
			break;
		}
	}

	private void popPay() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确定结算吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				GetBonusAndSettlementType();
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

	private void GetBonusAndSettlementType() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}

		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", bindMobile);
		params.put("price", price);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", bindMobile);
		map.put("price", price);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetBonusAndSettlementType",
				params, new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(getbonus_error);
						super.onFailure(t, errorNo, strMsg);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = Utili.GetJson("" + t);

						try {
							JSONObject obj = new JSONObject(jsonMessage);
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								payType = obj.getInt("PayType");
								if (payType != 0) {
									cardNo = obj.getString("CardNo");
									if (payType == 2) {
										cardInfo += "您可用钱包余额支付";
									} else if (payType == 3) {
										cardInfo += "您可用礼品卡支付";
									}
									bonusId = obj.getInt("BonusId");
									if (bonusId != 0) {
										cardInfo += "，可用红包抵扣"
												+ obj.getString("BonusAmount")
												+ "元";
									}
									handler.sendEmptyMessage(getbonus_success);
								} else {
									handler.sendEmptyMessage(getbonus_error);
									cardInfo = "金额不足，请先对钱包余额或礼品卡进行充值 ";
									jumpToPay();
								}

							} else {
								handler.sendEmptyMessage(getbonus_error);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(getbonus_error);
						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	private void jumpToPay() {
		Intent it = new Intent(context, CarWashOrderPayActivity.class);
		Bundle bd = new Bundle();
		bd.putString("price", price);
		bd.putString("companyId", carwash.getId() + "");
		bd.putString("carType", carType + "");
		it.putExtras(bd);
		startActivity(it);
	}

	private void createOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}

		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", bindMobile);
		params.put("companyId", carwash.getId() + "");
		params.put("carType", carType + "");
		params.put("price", price);
		params.put("bonusId", bonusId + "");
		params.put("amountCardNo", cardNo);
		params.put("payType", payType + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", bindMobile);
		map.put("companyId", carwash.getId() + "");
		map.put("carType", carType + "");
		map.put("price", price);
		map.put("bonusId", bonusId + "");
		map.put("amountCardNo", cardNo);
		map.put("payType", payType + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateOrderCarWash", params,
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

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			case createOrder_fail:
				Utili.ToastInfo(context, "创建订单失败");
				break;
			case createOrder_Success:
				Utili.ToastInfo(context, "创建订单成功");
				finish();
				break;
			case getbonus_success:
				createOrder();
				break;
			default:
				break;
			}
		};
	};

}
