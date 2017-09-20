package com.carlife.main;

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

import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.carlife.R;
import com.carlife.daijia.DaijiaOrderDetailActivity;
import com.carlife.global.Const;
import com.carlife.model.CarWashOrder;
import com.carlife.model.ChewuOrder;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.RescueOrder;
import com.carlife.rescue.RescueOrderDetailActivity;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class Act_CommonOrderList extends Activity implements OnClickListener {
	private Button btn_back;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 0;
	private final static int getOrder_success = 1;

	private List<RescueOrder> orderList;
	private List<DaijiaOrder> daijiaList;
	private List<CarWashOrder> washList;
	private List<ChewuOrder> chewuList;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private ListView lv_order;
	private Context context;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescueorderlist);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		lv_order = (ListView) findViewById(R.id.lv_order);
		url = getIntent().getStringExtra("url");
	}

	@Override
	protected void onResume() {
		getOrderList();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@SuppressLint("DefaultLocale")
	private void getOrderList() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		String mobile = sp.getString(Const.BindMobile, "");
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
		fh.post(Const.API_SERVICES_ADDRESS + url, params,
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
							orderList = new ArrayList<RescueOrder>();
							daijiaList = new ArrayList<DaijiaOrder>();
							washList = new ArrayList<CarWashOrder>();
							chewuList = new ArrayList<ChewuOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject obj = (JSONObject) jsonArray.opt(i);

								if (url.equals("/GetOrderList")) {
									DaijiaOrder order = new DaijiaOrder();
									order.setId(obj.getString("Id"));
									order.setOrderNo(obj.getString("OrderNo"));
									order.setDeparturePlaceReal(obj
											.getString("DeparturePlaceReal"));
									order.setDestinationReal(obj
											.getString("DestinationReal"));
									order.setDriverId(obj.getString("DriverId"));
									order.setOrdertime(obj.getString("AddTime"));
									Log.i("-------", obj.getString("AddTime"));
									order.setOrderAmountReal(obj
											.getString("OrderAmountReal"));
									order.setOrderStatus(Utili.getOrderStatus(Integer
											.parseInt(obj
													.getString("OrderStatus"))));
									daijiaList.add(order);
								} else if (url.equals("/GetRescueOrders")) {
									RescueOrder order = new RescueOrder();
									order.setId(obj.getString("Id"));
									order.setOrderNo(obj.getString("OrderNo"));
									order.setRescueType(obj
											.getString("RescueType"));
									order.setPrice(obj.getString("Price"));
									order.setAddTime(obj.getString("AddTime"));
									order.setDriverId(obj.getString("DriverId"));
									orderList.add(order);
								} else if (url.equals("/GetCarWashOrders")) {
									CarWashOrder order = new CarWashOrder();
									order.setId(obj.getString("Id"));
									order.setAddTime(obj.getString("AddTime"));
									order.setPrice(obj.getString("Price"));
									order.setCompany(obj.getString("Company"));
									order.setOrderNo(obj.getString("OrderNo"));
									washList.add(order);
								} else if (url.equals("/GetChewuOrders")) {
									ChewuOrder order = new ChewuOrder();
									order.setId(obj.getInt("ID"));
									order.setBusinessType(obj
											.getString("BusinessType"));
									order.setAddTime(obj.getString("AddTime"));
									order.setOrderNo(obj.getString("OrderNo"));
									chewuList.add(order);
								}
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

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {
			case getOrder_fail:
				Utili.ToastInfo(Act_CommonOrderList.this, "没有获取到订单");
				break;
			case getOrder_success:
				loadOrder();
				break;
			default:
				break;
			}
		};
	};

	private void loadOrder() {
		prepareData();
		SimpleAdapter listAdapter = null;
		if (url.equals("/GetOrderList")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.daijiaorderitem, new String[] { "starttime",
							"state", "start", "end", "orderNo" }, new int[] {
							R.id.txtOrderStartTime, R.id.txtState,
							R.id.txt_start, R.id.txt_end, R.id.tv_orderNo });
		} else if (url.equals("/GetRescueOrders")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.rescueorderitem, new String[] { "starttime",
							"type", "orderNo" }, new int[] { R.id.txt_time,
							R.id.txt_type, R.id.tv_orderNo });
		} else if (url.equals("/GetCarWashOrders")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.carwashorderitem, new String[] { "addtime",
							"type", "orderNo", "price", "company" }, new int[] {
							R.id.tv_time, R.id.tv_type, R.id.tv_orderNo,
							R.id.tv_price, R.id.tv_company });
		} else if (url.equals("/GetChewuOrders")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.chewuorderitem, new String[] { "starttime",
							"type", "orderNo" }, new int[] { R.id.txt_time,
							R.id.txt_type, R.id.tv_orderNo });
		}

		lv_order.setAdapter(listAdapter);
		lv_order.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (url.equals("/GetOrderList")) {
					DaijiaOrder order = daijiaList.get(position);
					Intent i = new Intent(context,
							DaijiaOrderDetailActivity.class);
					Bundle bd = new Bundle();
					bd.putSerializable("daijiaOrder", order);
					i.putExtras(bd);
					startActivity(i);
				} else if (url.equals("/GetRescueOrders")) {
					RescueOrder order = orderList.get(position);

					Intent i = new Intent(context,
							RescueOrderDetailActivity.class);
					Bundle bd = new Bundle();
					bd.putSerializable("rescueOrder", order);
					i.putExtras(bd);
					startActivity(i);
				}

			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		if (url.equals("/GetOrderList")) {
			for (int i = 0; i < daijiaList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("starttime", daijiaList.get(i).getOrdertime());
				item.put("state", daijiaList.get(i).getOrderStatus());
				item.put("start", daijiaList.get(i).getDeparturePlaceReal());
				item.put("end", daijiaList.get(i).getDestinationReal());
				item.put("orderNo", daijiaList.get(i).getOrderNo());
				data.add(item);
			}
		} else if (url.equals("/GetRescueOrders")) {
			for (int i = 0; i < orderList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("starttime", orderList.get(i).getAddTime());
				item.put("type", orderList.get(i).getRescueType());
				item.put("orderNo", orderList.get(i).getOrderNo());
				data.add(item);
			}
		} else if (url.equals("/GetCarWashOrders")) {
			for (int i = 0; i < washList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("addtime", washList.get(i).getAddTime());
				item.put("type", washList.get(i).getCarType());
				item.put("orderNo", washList.get(i).getOrderNo());
				item.put("company", washList.get(i).getOrderNo());
				item.put("price", washList.get(i).getPrice());
				data.add(item);
			}
		} else if (url.equals("/GetChewuOrders")) {
			for (int i = 0; i < chewuList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("starttime", chewuList.get(i).getAddTime());
				item.put("type", chewuList.get(i).getBusinessType());
				item.put("orderNo", chewuList.get(i).getOrderNo());
				data.add(item);
			}
		}

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
