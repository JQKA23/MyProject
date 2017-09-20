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
import com.carlife.global.Const;
import com.carlife.model.Car;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class MyCarsActivity extends Activity implements OnClickListener {

	private Button btn_back, btn_add;
	private CustomProgressDialog cpd;

	private final static int getOrder_fail = 0;
	private final static int getOrder_success = 1;

	private List<Car> list;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

	private ListView lv;
	private Context context;

	private String mobile = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mycars);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_add = (Button) findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);

		lv = (ListView) findViewById(R.id.lv);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

	}

	@Override
	protected void onResume() {
		getCars();
		super.onResume();
	}

	private void getCars() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetMyCars", params,
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
							list = new ArrayList<Car>();
							JSONArray jsonArray = new JSONArray(jsonMessage);
							for (int i = 0; i < jsonArray.length(); i++) {
								Car car = new Car();
								JSONObject obj = (JSONObject) jsonArray.opt(i);
								car.setId(obj.getInt("Id"));
								car.setCarColor(obj.getString("CarColor"));
								car.setCarframeNo(obj.getString("CarframeNo"));
								car.setCarModel(obj.getString("CarModel"));
								car.setCarNo(obj.getString("CarNo"));
								car.setEngineNo(obj.getString("EngineNo"));
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
				R.layout.caritem, new String[] { "carModel", "carColor",
						"carNo", "carEngineNo" }, new int[] { R.id.tv_carModel,
						R.id.tv_carColor, R.id.tv_carNo, R.id.tv_engineNo });

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Car car = list.get(position);
				Intent i = new Intent(context, CarDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putSerializable("car", car);
				i.putExtras(bd);
				startActivity(i);
			}
		});
	}

	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("carModel", list.get(i).getCarModel());
			item.put("carColor", list.get(i).getCarColor());
			// item.put("carframeNo", list.get(i).getCarframeNo());
			item.put("carEngineNo", list.get(i).getEngineNo());
			item.put("carNo", list.get(i).getCarNo());
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
				Utili.ToastInfo(MyCarsActivity.this, "没有代驾订单");
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
		}

	}
}
