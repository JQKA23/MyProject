package com.carlife.weizhang;


import java.net.URLEncoder;
import java.security.MessageDigest;
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
import com.carlife.member.AddCarActivity;
import com.carlife.member.CarDetailActivity;
import com.carlife.member.MyCarsActivity;
import com.carlife.model.Car;
import com.carlife.model.WzInfo;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WeizhangActivity extends Activity implements OnClickListener {

	
	private Button btn_back,btn_add;
	
	private CustomProgressDialog cpd;

	private final static int getCar_fail = 10;
	private final static int getCar_success = 11;
	

	
	private ListView lv;
    private Context context;	   
    private String mobile="";

    private List<Car> listCar;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weizhang);
		context=this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_add=(Button)findViewById(R.id.btn_add);
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
						handler.sendEmptyMessage(getCar_fail);
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
								listCar = new ArrayList<Car>();
								JSONArray jsonArray = new JSONArray(jsonMessage);
								for (int i = 0; i < jsonArray.length(); i++) {
									Car car = new Car();
									JSONObject obj = (JSONObject) jsonArray
											.opt(i);
									car.setId(obj.getInt("Id"));	
									car.setCarColor(obj.getString("CarColor"));
									car.setCarframeNo(obj.getString("CarframeNo"));
									car.setCarModel(obj.getString("CarModel"));
									car.setCarNo(obj.getString("CarNo"));
									car.setEngineNo(obj.getString("EngineNo"));
									listCar.add(car);
								}
								handler.sendEmptyMessage(getCar_success);

							} catch (JSONException e) {
								handler.sendEmptyMessage(getCar_fail);
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
		SimpleAdapter listAdapter = new SimpleAdapter(
				context, data, R.layout.caritem,
				new String[] { "carModel", "carColor", "carNo","carEngineNo" },
				new int[] { R.id.tv_carModel, R.id.tv_carColor,R.id.tv_carNo,R.id.tv_engineNo });

		lv.setAdapter(listAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Car car = listCar.get(position);
				Intent i = new Intent(context,WeizhangDetailActivity.class);
				Bundle bd = new Bundle();
				bd.putSerializable("car",  car);
				i.putExtras(bd);
				startActivity(i);
			}
		});
	}
	
	private void prepareData() {
		data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < listCar.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			item.put("carModel", listCar.get(i).getCarModel());
			item.put("carColor", listCar.get(i).getCarColor());
			//item.put("carframeNo", listCar.get(i).getCarframeNo());
			item.put("carEngineNo", listCar.get(i).getEngineNo());
			item.put("carNo", listCar.get(i).getCarNo());
			data.add(item);
		}
	}

	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_add:
			Intent it=new Intent(context,AddCarActivity.class);
			startActivity(it);
			break;
		}

	}

	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd!=null&&cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			
			case getCar_success:
				loadList();
				break;			
			}

		}
	};
	
	
	
}
