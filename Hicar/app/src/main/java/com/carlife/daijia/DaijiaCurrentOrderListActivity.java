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
import android.widget.TextView;

import com.carlife.R;
import com.carlife.chewu.ChewuCurrentOrderDetailActivity;
import com.carlife.global.Const;
import com.carlife.main.Act_CommonOrderList;
import com.carlife.model.ChewuOrder;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class DaijiaCurrentOrderListActivity extends Activity implements
		OnClickListener {

	private Button btn_back;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 10;
	private final static int getOrder_success = 11;

	private List<DaijiaOrder> daijiaList;
	private List<RescueOrder> rescueList;
	private List<ChewuOrder> chewuList;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private String bindMobile = "";

	private ListView lv;
	private Context context;

	private TextView tv_daijiaorder, tv_info;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiacurrentorderlist);
		context = this;

		tv_daijiaorder = (TextView) findViewById(R.id.tv_daijiaorder);
		tv_daijiaorder.setOnClickListener(this);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		tv_info = (TextView) findViewById(R.id.tv_info);

		lv = (ListView) findViewById(R.id.lv);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, "");
		Intent it = getIntent();
		url = it.getStringExtra("url");
		getCurrentOrders();

	}

	private void getCurrentOrders() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", bindMobile);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", bindMobile);

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
							rescueList = new ArrayList<RescueOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							if (url.equals("/GetCurrentRescueOrders")) {
								for (int i = 0; i < jsonArray.length(); i++) {
									RescueOrder order = new RescueOrder();
									JSONObject obj = (JSONObject) jsonArray
											.opt(i);
									order.setId(obj.getString("Id"));
									order.setRescueType(obj
											.getString("RescueType"));
									order.setAddTime(obj.getString("AddTime"));
									order.setDriverId(obj.getString("DriverId"));
									order.setOrderType(obj
											.getString("OrderType"));
									order.setPrice(obj
											.getString("TotalPriceReal"));
									order.setPricePlan(obj
											.getString("TotalPricePlan"));
									order.setOrderNo(obj.getString("OrderNo"));
									rescueList.add(order);
								}
							} else if (url.equals("/GetCurrentChewuOrders")) {
								chewuList = new ArrayList<ChewuOrder>();
								for (int i = 0; i < jsonArray.length(); i++) {
									ChewuOrder order = new ChewuOrder();
									JSONObject obj = (JSONObject) jsonArray
											.opt(i);
									order.setId(obj.getInt("ID"));
									order.setBusinessType(obj
											.getString("BusinessType"));
									order.setAddTime(obj.getString("AddTime"));
									chewuList.add(order);
								}
							} else {
								daijiaList = new ArrayList<DaijiaOrder>();
								for (int i = 0; i < jsonArray.length(); i++) {
									DaijiaOrder order = new DaijiaOrder();
									JSONObject obj = (JSONObject) jsonArray
											.opt(i);
									order.setId(obj.getString("Id"));
									order.setDeparturePlaceReal(obj
											.getString("DeparturePlacePlan"));
									order.setDriverId(obj.getString("DriverId"));
									order.setOrdertime(obj.getString("AddTime"));
									daijiaList.add(order);
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

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {

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
		SimpleAdapter listAdapter;
		if (url.equals("/GetCurrentRescueOrders")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.rescuecurrentorderitem, new String[] { "addtime",
							"type", "ordertype", "toPay" }, new int[] {
							R.id.txt_time, R.id.txt_type, R.id.txt_ordertype,
							R.id.txt_topay });
		} else if (url.equals("/GetCurrentChewuOrders")) {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.chewuorderitem,
					new String[] { "addtime", "type" }, new int[] {
							R.id.txt_time, R.id.txt_type });
		} else {
			listAdapter = new SimpleAdapter(context, data,
					R.layout.daijiacurrentorderitem, new String[] {
							"starttime", "start" }, new int[] {
							R.id.txtOrderStartTime, R.id.txt_start });
		}

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (url.equals("/GetCurrentRescueOrders")) {
					RescueOrder order = rescueList.get(position);
					Intent i = new Intent(context,
							DaijiaCurrentOrderDetailActivity.class);
					Bundle bd = new Bundle();
					bd.putSerializable("Order", order);
					bd.putBoolean("isDaijia", false);
					i.putExtras(bd);
					startActivity(i);
				} else if (url.equals("/GetCurrentChewuOrders")) {
					ChewuOrder order = chewuList.get(position);
					Intent i = new Intent(context,
							ChewuCurrentOrderDetailActivity.class);
					Bundle bd = new Bundle();
					bd.putSerializable("Order", order);
					i.putExtras(bd);
					startActivity(i);
				} else {
					DaijiaOrder order = daijiaList.get(position);
					Intent i = new Intent(context,
							DaijiaCurrentOrderDetailActivity.class);
					Bundle bd = new Bundle();
					bd.putSerializable("Order", order);
					bd.putBoolean("isDaijia", true);
					i.putExtras(bd);
					startActivity(i);
				}

			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		boolean b = false;
		if (url.equals("/GetCurrentRescueOrders")) {
			data = new ArrayList<Map<String, String>>();
			b = rescueList.size() > 0;
			for (int i = 0; i < rescueList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("addtime", rescueList.get(i).getAddTime());
				item.put("type", rescueList.get(i).getRescueType());
				item.put("ordertype", rescueList.get(i).getOrderType());
				item.put("toPay", Double.parseDouble(rescueList.get(i)
						.getPrice()) > 0 ? "待支付" : "");
				data.add(item);
			}
		} else if (url.equals("/GetCurrentChewuOrders")) {
			b = chewuList.size() > 0;
			for (int i = 0; i < chewuList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("addtime", chewuList.get(i).getAddTime());
				item.put("type", chewuList.get(i).getBusinessType());
				data.add(item);
			}
		} else {
			b = daijiaList.size() > 0;
			for (int i = 0; i < daijiaList.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("starttime", daijiaList.get(i).getOrdertime());
				item.put("start", daijiaList.get(i).getDeparturePlaceReal());
				data.add(item);
			}
		}

		if (b) {
			tv_info.setVisibility(View.GONE);
		} else {
			tv_info.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_daijiaorder:
			Intent it = new Intent(context, Act_CommonOrderList.class);
			if ("/GetCurrentRescueOrders".equals(url)) {
				it.putExtra("url", "/GetRescueOrders");
			} else if ("/GetCurrentOrders".equals(url)) {
				it.putExtra("url", "/GetOrderList");
			} else if ("/GetCurrentChewuOrders".equals(url)) {
				it.putExtra("url", "/GetChewuOrders");
			}
			startActivity(it);
			break;
		}

	}
}
