package com.carlife.daijia;

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

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.carlife.R;
import com.carlife.R.drawable;
import com.carlife.R.id;
import com.carlife.R.layout;
import com.carlife.global.Const;
import com.carlife.model.Comment;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.Driver;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.Share;
import com.carlife.utility.CustomDialog.Builder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class DriverActivity extends Activity implements OnClickListener {

	private ImageView ivs1, ivs2, ivs3, ivs4, ivs5;
	private Button btn_back, btn_contact;

	private TextView tv_drivername, tv_usercode, tv_ordercount, tv_star,tv_distance,tv_price,tv_year;

	private CustomProgressDialog cpd;
	private Driver driver;

	private String mobile = "";
	private Context context;

	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;

	private String lat = "39.90403";
	private String lon = "116.407525";

	private String bindMobile = "";
	private String addr = "";

	private ListView lv;
	private final static int getList_fail = 0;
	private final static int getList_success = 1;
	private List<Comment> list;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiadriver);
		tv_drivername = (TextView) findViewById(R.id.tv_drivername);
		tv_usercode = (TextView) findViewById(R.id.tv_usercode);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_star = (TextView) findViewById(R.id.tv_star);
		tv_distance= (TextView) findViewById(R.id.tv_distance);
		tv_price= (TextView) findViewById(R.id.tv_price);
		tv_year= (TextView) findViewById(R.id.tv_year);
		
	
		
		ivs1 = (ImageView) findViewById(R.id.ivs1);
		ivs2 = (ImageView) findViewById(R.id.ivs2);
		ivs3 = (ImageView) findViewById(R.id.ivs3);
		ivs4 = (ImageView) findViewById(R.id.ivs4);
		ivs5 = (ImageView) findViewById(R.id.ivs5);

		context = this;

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_contact = (Button) findViewById(R.id.btn_contact);
		btn_contact.setOnClickListener(this);

		Bundle bd = getIntent().getExtras();
		driver = (Driver) bd.getSerializable("driver");
		lon = bd.getString("lon");
		lat = bd.getString("lat");
		addr = bd.getString("addr");
		tv_distance.setText(driver.getDistance()+"km");
		tv_price.setText(driver.getBid()+"元");

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");

		lv = (ListView) findViewById(R.id.lv);

	}

	@Override
	protected void onResume() {
		getDriverDaijiaInfo();
		getDriverComments();
		super.onResume();
	}

	/*
	 * 获取司机信息
	 */
	private void getDriverDaijiaInfo() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", driver.getId() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", driver.getId() + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetDriverDaijiaInfo", params,
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
						String jsonMessage = Utili.GetJson("" + t);
						try {
							JSONObject obj = new JSONObject(jsonMessage);
							tv_drivername.setText(obj.getString("Name"));
							tv_usercode.setText(obj.getString("UserCode"));
							tv_star.setText(obj.getString("Star"));
							initDriverStar(obj.getString("Star"));
							tv_ordercount.setText("代驾"
									+ obj.getString("OrderCount") + "次");
							mobile = obj.getString("Mobile");
							tv_year.setText(obj.getString("Year")+"年");
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	private void initDriverStar(String star) {
		double s = Double.parseDouble(star) * 10;
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

	/*
	 * 获取司机评价
	 */
	private void getDriverComments() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", driver.getId() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", driver.getId() + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetDaijiaDriverCommentList", params,
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
							list = new ArrayList<Comment>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								Comment c = new Comment();
								c.setAddTime(obj.getString("AddTime"));
								c.setStar(obj.getInt("Star"));
								c.setContent(obj.getString("Comment"));
								list.add(c);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_contact:
			popCallDriver(driver);
			break;
		}
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

		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("lon", lon);
		params.put("lat", lat);
		params.put("address", addr);
		params.put("callMobile", bindMobile);
		params.put("consumerMobile", bindMobile);
		params.put("driverId", driver.getId() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("lon", lon);
		map.put("lat", lat);
		map.put("address", addr);
		map.put("callMobile", bindMobile);
		map.put("consumerMobile", bindMobile);
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
				Intent it = new Intent(context, DaijiaCurrentOrderListActivity.class);
				startActivity(it);				
				break;
			case getList_success:
				loadList();
				break;
			default:
				break;
			}
		};
	};

	private void loadList() {
		prepareData();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.commentitem,
				new String[] { "star", "content", "addtime" }, new int[] {
						R.id.rb_star, R.id.txt_content, R.id.txt_time, });
		lv.setAdapter(listAdapter);		
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
				}
				return false;
			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("star", list.get(i).getStar()+"");
			item.put("content", list.get(i).getContent());
			item.put("addtime", list.get(i).getAddTime());
			data.add(item);
		}
	}
}
