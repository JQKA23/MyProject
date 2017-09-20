package com.carlife.rescue;

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

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.Car;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class RescueBackActivity extends Activity implements OnClickListener {

	private Button btn_back, btn_add_top, btn_add;
	private Context context;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 10;
	private final static int getOrder_success = 11;

	private String mobile = "";
	private List<RescueOrder> orderList;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private ListView lv;

	private WebView wv;

	private LinearLayout ll_public;
     private String lon,lat,addr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescueback);
		context = this;
		ll_public = (LinearLayout) findViewById(R.id.ll_public);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_add_top = (Button) findViewById(R.id.btn_add_top);
		btn_add_top.setOnClickListener(this);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);
		lv = (ListView) findViewById(R.id.lv);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		wv = (WebView) findViewById(R.id.wv);
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		lon=bd.getString("lon");
		lat=bd.getString("lat");
		addr=bd.getString("addr");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_add_top:
		case R.id.btn_add:
			Intent it = new Intent(context, AddRescueBackActivity.class);
			Bundle bd=new Bundle();
			bd.putString("lon",lon);
			bd.putString("lat", lat);
			bd.putString("addr", addr);
			it.putExtras(bd);
			startActivity(it);
			break;
		}
	}

	@Override
	protected void onResume() {
		getCurrentOrders();
		super.onResume();
	}

	private void getCurrentOrders() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetCurrentRescueBackOrders",
				params, new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(getOrder_fail);
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
							orderList = new ArrayList<RescueOrder>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								RescueOrder order = new RescueOrder();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								order.setId(obj.getString("Id"));
								order.setPrice(obj.getString("PriceReal"));
								order.setPricePlan(obj.getString("PricePlan"));
								order.setAddTime(obj.getString("AddTime"));
								order.setDriverId(obj.getString("DriverId"));
								order.setOrderType(obj.getString("OrderType"));
								order.setOrderNo(obj.getString("OrderNo"));
								order.setStartPlace(obj.getString("StartPlace"));
								order.setEndPlace(obj.getString("EndPlace"));
								order.setKmReal(obj.getString("KmReal"));
								order.setCarType(obj.getString("CarType"));
								order.setCarNo(obj.getString("CarNo"));
								order.setStartTime(obj.getString("StartTime"));
				                order.setIsPay(obj.getInt("IsPay"));
								orderList.add(order);
							}
							handler.sendEmptyMessage(getOrder_success);
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(getOrder_fail);
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
				R.layout.rescuebackorderitem, new String[] { "orderno",
						"addtime", "ispay","start","end" }, new int[] { R.id.txt_orderNo,
						R.id.txt_time, R.id.txt_ispay,R.id.txt_start, R.id.txt_end});

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				RescueOrder order = orderList.get(position);
				Intent i = new Intent(context,
						RescueBackOrderDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putSerializable("rescueOrder", order);
				i.putExtras(bd);
				startActivity(i);
			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < orderList.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("addtime", orderList.get(i).getAddTime());
			item.put("orderno", orderList.get(i).getOrderNo());
			item.put("start", "从 "+ orderList.get(i).getStartPlace());
			item.put("end", "到 "+orderList.get(i).getEndPlace());
			if(orderList.get(i).getDriverId().equals("0")){
				item.put("ispay", "等待接单");
			}
			else{
			   item.put("ispay", orderList.get(i).getIsPay()==1?"已付款":"未支付");
			}
			data.add(item);
		}
		if (orderList.size() > 0) {
			lv.setVisibility(View.VISIBLE);
			btn_add_top.setVisibility(View.VISIBLE);
			ll_public.setVisibility(View.GONE);
		} else {
			lv.setVisibility(View.GONE);
			ll_public.setVisibility(View.VISIBLE);
			btn_add_top.setVisibility(View.GONE);
			String url = "http://1018.com.cn/app/RescueReturnDeclare";
			try {
				wv.getSettings().setJavaScriptEnabled(true);
				wv.loadUrl(url);
			} catch (Exception ex) {

			}
			
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			case getOrder_success:
				loadOrder();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

}
