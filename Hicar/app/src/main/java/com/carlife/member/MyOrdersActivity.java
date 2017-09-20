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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.carlife.model.ChewuOrder;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.RescueOrder;
import com.carlife.rescue.RescueOrderDetailActivity;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class MyOrdersActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 0;
	private final static int getOrder_success = 1;
	private final static int getOrderRescue_fail = 10;
	private final static int getOrderRescue_success = 11;
	private final static int getOrderChewu_fail = 100;
	private final static int getOrderChewu_success = 110;

	private List<DaijiaOrder> list_daijia;
	private List<RescueOrder> list_rescue;
	private List<ChewuOrder> list_chewu;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private ListView lv_order;
	private Context context;

	private Button btn_daijia, btn_rescue, btn_chewu;
	private View view_daijia, view_rescue, view_chewu;
	private String mobile = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorders);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		lv_order = (ListView) findViewById(R.id.lv_order);

		btn_daijia = (Button) findViewById(R.id.btn_daijia);
		btn_daijia.setOnClickListener(this);
		btn_rescue = (Button) findViewById(R.id.btn_rescue);
		btn_rescue.setOnClickListener(this);
		btn_chewu = (Button) findViewById(R.id.btn_chewu);
		btn_chewu.setOnClickListener(this);

		view_daijia = (View) findViewById(R.id.view_daijia);
		view_rescue = (View) findViewById(R.id.view_rescue);
		view_chewu = (View) findViewById(R.id.view_chewu);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

		getOrderList();

	}

	/*
	 * 代驾订单
	 */
	private void getOrderList() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetOrderList", params,
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
							list_daijia = new ArrayList<DaijiaOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								DaijiaOrder order = new DaijiaOrder();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								order.setId(obj.getString("Id"));
								order.setDeparturePlaceReal(obj
										.getString("DeparturePlaceReal"));
								order.setDestinationReal(obj
										.getString("DestinationReal"));
								order.setDriverId(obj.getString("DriverId"));
								order.setOrdertime(obj.getString("AddTime"));
								order.setOrderAmountReal(obj
										.getString("OrderAmountReal"));
								order.setOrderStatus(Utili
										.getOrderStatus(Integer.parseInt(obj
												.getString("OrderStatus"))));
								list_daijia.add(order);
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

	private void loadOrder() {
		prepareData();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.daijiaorderitem, new String[] { "starttime", "state",
						"start", "end" }, new int[] { R.id.txtOrderStartTime,
						R.id.txtState, R.id.txt_start, R.id.txt_end });

		lv_order.setAdapter(listAdapter);
		lv_order.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DaijiaOrder order = list_daijia.get(position);
				Intent i = new Intent(context, DaijiaOrderDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putSerializable("daijiaOrder", order);
				i.putExtras(bd);
				startActivity(i);

			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list_daijia.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("starttime", list_daijia.get(i).getOrdertime());
			item.put("state", list_daijia.get(i).getOrderStatus());
			item.put("start", list_daijia.get(i).getDeparturePlaceReal());
			item.put("end", list_daijia.get(i).getDestinationReal());
			data.add(item);
		}
	}

	/*
	 * 救援订单
	 */
	private void getOrderRescueList() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetRescueOrders", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(getOrderRescue_fail);
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
							list_rescue = new ArrayList<RescueOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								RescueOrder order = new RescueOrder();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								order.setId(obj.getString("Id"));
								order.setRescueType(obj.getString("RescueType"));
								order.setPrice(obj.getString("Price"));
								order.setAddTime(obj.getString("AddTime"));
								order.setDriverId(obj.getString("DriverId"));
								list_rescue.add(order);
							}
							handler.sendEmptyMessage(getOrderRescue_success);
						} catch (JSONException e) {
							handler.sendEmptyMessage(getOrderRescue_fail);
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

	private void loadOrderRescue() {
		prepareDataResuce();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.rescueorderitem, new String[] { "starttime", "type" },
				new int[] { R.id.txt_time, R.id.txt_type });

		lv_order.setAdapter(listAdapter);
		lv_order.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				RescueOrder order = list_rescue.get(position);
				Intent i = new Intent(context, RescueOrderDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putSerializable("rescueOrder", order);
				i.putExtras(bd);
				startActivity(i);

			}
		});
	}

	private void prepareDataResuce() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list_rescue.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("starttime", list_rescue.get(i).getAddTime());
			item.put("type", list_rescue.get(i).getRescueType());
			data.add(item);
		}
	}

	/*
	 * 车务订单
	 */
	private void getOrderChewuList() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetChewuOrders", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(getOrderChewu_fail);
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
							list_chewu = new ArrayList<ChewuOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								ChewuOrder order = new ChewuOrder();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								order.setId(obj.getInt("ID"));
								order.setBusinessType(obj
										.getString("BusinessType"));
								order.setAddTime(obj.getString("AddTime"));
								list_chewu.add(order);
							}
							handler.sendEmptyMessage(getOrderChewu_success);
						} catch (JSONException e) {
							handler.sendEmptyMessage(getOrderChewu_fail);
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

	private void loadOrderChewu() {
		prepareChewuData();
		SimpleAdapter listAdapter = new SimpleAdapter(context, data,
				R.layout.rescueorderitem, new String[] { "starttime", "type" },
				new int[] { R.id.txt_time, R.id.txt_type });

		lv_order.setAdapter(listAdapter);
		lv_order.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// ChewuOrder order = orderList.get(position);
				// Intent i = new
				// Intent(context,RescueOrderDetailActivity.class);
				// Bundle bd = new Bundle();
				// bd.putSerializable("chewuOrder", order);
				// i.putExtras(bd);
				// startActivity(i);

			}
		});
	}

	private void prepareChewuData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list_chewu.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("starttime", list_chewu.get(i).getAddTime());
			item.put("type", list_chewu.get(i).getBusinessType());
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
				Utili.ToastInfo(MyOrdersActivity.this, "没有代驾订单");
				break;
			case getOrder_success:
				loadOrder();
				break;
			case getOrderRescue_fail:
				Utili.ToastInfo(MyOrdersActivity.this, "没有救援订单");
				break;
			case getOrderRescue_success:
				loadOrderRescue();
				break;
			case getOrderChewu_success:
				loadOrderChewu();
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
		case R.id.btn_daijia:
			getOrderList();
			btn_daijia.setTextColor(getResources().getColor(R.color.orange));
			btn_rescue.setTextColor(getResources()
					.getColor(R.color.light_black));
			btn_chewu
					.setTextColor(getResources().getColor(R.color.light_black));
			view_daijia.setVisibility(View.VISIBLE);
			view_rescue.setVisibility(View.GONE);
			view_chewu.setVisibility(View.GONE);
			break;
		case R.id.btn_rescue:
			getOrderRescueList();
			btn_daijia.setTextColor(getResources()
					.getColor(R.color.light_black));
			btn_rescue.setTextColor(getResources().getColor(R.color.orange));
			btn_chewu
					.setTextColor(getResources().getColor(R.color.light_black));
			view_daijia.setVisibility(View.GONE);
			view_rescue.setVisibility(View.VISIBLE);
			view_chewu.setVisibility(View.GONE);
			break;
		case R.id.btn_chewu:
			getOrderChewuList();
			btn_daijia.setTextColor(getResources()
					.getColor(R.color.light_black));
			btn_rescue.setTextColor(getResources()
					.getColor(R.color.light_black));
			btn_chewu.setTextColor(getResources().getColor(R.color.orange));
			view_daijia.setVisibility(View.GONE);
			view_rescue.setVisibility(View.GONE);
			view_chewu.setVisibility(View.VISIBLE);
			break;
		}

	}
}
