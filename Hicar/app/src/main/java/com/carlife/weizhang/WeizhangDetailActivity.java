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
import com.carlife.global.ResultCode;
import com.carlife.member.CarDetailActivity;
import com.carlife.member.MyCarsActivity;
import com.carlife.model.Car;
import com.carlife.model.WzInfo;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

public class WeizhangDetailActivity extends Activity implements OnClickListener {

	private Button btn_back;
	private ListView lv_wzInfo;

	private CustomProgressDialog cpd;
	private static String URL = "http://www.cheshouye.com";

	private static final int SearchCityIdSuccess = 1;
	private static final int SearchCityIdFail = 2;
	private static final int SearchWzInfoSuccess = 3;
	private static final int SearchWzInfoFail = 4;
	private static final int SearchWzInfoSuccessNoInfo = 5;
	
	private final static int createOrder_error = 10;
	private final static int createOrder_success = 11;
	private final static int createOrder_fail = 12;

	private List<WzInfo> list;

	private Context context;
	private String mobile = "";
	private Car car;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weizhangdetail);
		context = this;
		
		btn_back = (Button) findViewById(R.id.btn_back);
		lv_wzInfo = (ListView) findViewById(R.id.lv_wzInfo);

		btn_back.setOnClickListener(this);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

		Intent it = getIntent();
		Bundle bd = it.getExtras();
		car = (Car) bd.getSerializable("car");

	}

	@Override
	protected void onResume() {
		searchWz();
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		}

	}

	/* 查违章 */

	private int cityId = 0;
	private String carNo = "";
	private String engineNo = "";

	private void searchWz() {
		carNo = car.getCarNo();
		engineNo = car.getEngineNo();
		if (carNo.startsWith("京"))
			cityId = 189;
		// else if(carNo.startsWith("津")) cityId=0;
		else if (carNo.startsWith("沪"))
			cityId = 280;
		else if (carNo.startsWith("渝"))
			cityId = 332;
        if(cityId!=0){
        	handler.sendEmptyMessage(SearchCityIdSuccess);
        }else{
		searchCityId();
        }
	}

	// 查询城市Id
	private void searchCityId() {
		
			try {
				if (cpd == null || !cpd.isShowing()) {
					cpd = CustomProgressDialog.createDialog(this);
					cpd.show();
				}
				AjaxParams params = new AjaxParams();
				params.put(Const.APPKEY, Const.APPKEY_STR);
				params.put("carhead", carNo);
				Map<String, String> map = new HashMap<String, String>();
				map.put(Const.APPKEY, Const.APPKEY_STR);
				map.put("carhead", carNo);
				String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
						EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
				strTemp = EncodeUtility.md5(strTemp).toLowerCase();

				params.put("sign", strTemp);
				FinalHttp fh = new FinalHttp();
				fh.post(Const.API_SERVICES_ADDRESS + "/CarHeadInfo", params,
						new AjaxCallBack<Object>() {
							@Override
							public void onFailure(Throwable t, int errorNo,
									String strMsg) {
								super.onFailure(t, errorNo, strMsg);
								handler.sendEmptyMessage(SearchCityIdFail);
							}

							@Override
							public void onLoading(long count, long current) {
								super.onLoading(count, current);
								removeDialog(0);
							}

							@Override
							public void onSuccess(Object t) {
								super.onSuccess(t);
								String jsonMessage =Utili.GetJson("" + t);
									try {
										
											JSONObject obj = new JSONObject(
													jsonMessage);
											cityId=obj.getInt("CityId");
											handler.sendEmptyMessage(SearchCityIdSuccess);
										

									} catch (JSONException e) {
										System.out.print(e);
										handler.sendEmptyMessage(SearchCityIdFail);
									}
								

							}

							@Override
							public AjaxCallBack<Object> progress(
									boolean progress, int rate) {
								return super.progress(progress, rate);
							}

						});
			} catch (Exception e) {
				System.out.print(e.getMessage());
				handler.sendEmptyMessage(SearchCityIdFail);
			}
		
	}

	// 查询违章信息
	private void searchWzInfo() {
		final String carInfo = "{hphm=" + carNo + "&engineno=" + engineNo
				+ "&city_id=" + cityId + "&car_type=02}";
		final String appId = "55";
		final String appKey = "9837cbcb68641583b0f0477e33157848";
		long timestamp = System.currentTimeMillis();
		String signStr = appId + carInfo + timestamp + appKey;
		String sign = md5(signStr);
		// String sign = EncodeUtility.md5(signStr);
		try {
			if (cpd == null || !cpd.isShowing()) {
				cpd = CustomProgressDialog.createDialog(this);
				cpd.show();
			}
			AjaxParams params = new AjaxParams();
			FinalHttp fh = new FinalHttp();
			fh.post(URL + "/api/weizhang/query_task?car_info="
					+ URLEncoder.encode(carInfo, "utf-8") + "&sign=" + sign
					+ "&timestamp=" + timestamp + "&app_id=" + appId, params,
					new AjaxCallBack<Object>() {
						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							super.onFailure(t, errorNo, strMsg);
							handler.sendEmptyMessage(SearchWzInfoFail);
						}

						@Override
						public void onLoading(long count, long current) {
							super.onLoading(count, current);
							removeDialog(0);
						}

						@Override
						public void onSuccess(Object t) {
							super.onSuccess(t);
							removeDialog(0);
							System.out.print("" + t);
							String jsonMessage = "" + t;
							if (jsonMessage != null && !jsonMessage.equals("")) {
								try {
									JSONObject obj = new JSONObject(jsonMessage);
									if (obj.getString("status").equals("2001")) {
										// 有违章记录
										JSONArray ja = new JSONArray(obj
												.getString("historys"));
										list = new ArrayList<WzInfo>();
										for (int i = 0; i < ja.length(); i++) {
											JSONObject o = (JSONObject) ja
													.opt(i);
											WzInfo m = new WzInfo();
											m.setCarNo(carNo);
											m.setEngineNo(engineNo);
											m.setFen(o.getString("fen"));
											m.setOfficer(o.getString("officer"));
											m.setOccurTime(o
													.getString("occur_date"));
											m.setOccurArea(o
													.getString("occur_area"));
											m.setCode(o.getString("code"));
											m.setInfo(o.getString("info"));
											m.setMoney(o.getString("money"));
											list.add(m);
										}
										handler.sendEmptyMessage(SearchWzInfoSuccess);
									} else {
										// 无违章信息
										handler.sendEmptyMessage(SearchWzInfoSuccessNoInfo);
									}

								} catch (JSONException e) {
									System.out.print(e);
									handler.sendEmptyMessage(SearchWzInfoFail);
								}
							} else {
								handler.sendEmptyMessage(SearchWzInfoFail);
							}

						}

						@Override
						public AjaxCallBack<Object> progress(boolean progress,
								int rate) {
							return super.progress(progress, rate);
						}

					});
		} catch (Exception e) {
			System.out.print(e.getMessage());
			handler.sendEmptyMessage(SearchWzInfoFail);
		}
	}

	// 展示违章信息
	private void showWzInfo() {
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			WzInfo order = list.get(i);
			item.put("Fen", order.getFen());
			item.put("Officer", order.getOfficer());
			item.put("OccurTime", order.getOccurTime());
			item.put("OccurArea", order.getOccurArea());
			item.put("Money", order.getMoney());
			item.put("Code", order.getCode());
			item.put("Info", order.getInfo());
			data.add(item);
		}
		SimpleAdapter listAdapter = new SimpleAdapter(
				WeizhangDetailActivity.this, data, R.layout.weizhangitem,
				new String[] { "Fen", "Officer", "OccurTime", "OccurArea",
						"Money", "Code", "Info" }, new int[] { R.id.txt_fen,
						R.id.txt_officer, R.id.txt_occurtime,
						R.id.txt_occurarea, R.id.txt_money, R.id.txt_code,
						R.id.txt_info });

		lv_wzInfo.setAdapter(listAdapter);
		lv_wzInfo.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				popCreateOrder();
			}
		});
	}
	
	private void popCreateOrder() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确认下单吗？");
		builder.setTitle(R.string.tip);
		builder.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						createOrder();
					}
				});
		builder.setNegativeButton(R.string.btn_cancel,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private String type="其他业务";
	private void createOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		params.put("type", type);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);
		map.put("type", type);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateChewuOrder", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(createOrder_error);
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
								int code = Integer.parseInt(obj.getString("ResultCode"));
								if (code == 0) {
									handler.sendEmptyMessage(createOrder_success);
								}
								 else {
									 Message msg = new Message();
										msg.what = createOrder_fail;
										Bundle b = new Bundle();
										b.putString("msg", ResultCode.GetResult(code));
										msg.setData(b);
										handler.sendMessage(msg);									 
								
								}
							} catch (JSONException e) {
								e.printStackTrace();
								handler.sendEmptyMessage(createOrder_error);
							}

						

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	
	

	// 发送违章数据
	private void postWzInfo() {
		for (int i = 0; i < list.size(); i++) {
			WzInfo o = list.get(i);
			AjaxParams params = new AjaxParams();
			params.put(Const.APPKEY, Const.APPKEY_STR);
			params.put("carNo", o.getCarNo());
			params.put("engineNo", o.getEngineNo());
			params.put("fen", o.getFen());
			params.put("officer", o.getOfficer());
			params.put("occurTime", o.getOccurTime());
			params.put("occurArea", o.getOccurArea());
			params.put("code", o.getCode());
			params.put("info", o.getInfo());
			params.put("money", o.getMoney());
			Map<String, String> map = new HashMap<String, String>();
			map.put(Const.APPKEY, Const.APPKEY_STR);
			map.put("carNo", o.getCarNo());
			map.put("engineNo", o.getEngineNo());
			map.put("fen", o.getFen());
			map.put("officer", o.getOfficer());
			map.put("occurTime", o.getOccurTime());
			map.put("occurArea", o.getOccurArea());
			map.put("code", o.getCode());
			map.put("info", o.getInfo());
			map.put("money", o.getMoney());
			String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
					EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
			strTemp = EncodeUtility.md5(strTemp).toLowerCase();

			params.put("sign", strTemp);
			FinalHttp fh = new FinalHttp();
			fh.post(Const.API_SERVICES_ADDRESS + "/AddCarWzInfo", params,
					new AjaxCallBack<Object>() {
						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							super.onFailure(t, errorNo, strMsg);
						}

						@Override
						public void onLoading(long count, long current) {
							super.onLoading(count, current);
							removeDialog(0);
						}

						@Override
						public void onSuccess(Object t) {
							super.onSuccess(t);
							removeDialog(0);

							System.out.print("" + t);
							String jsonMessage = "" + t;

						}

						@Override
						public AjaxCallBack<Object> progress(boolean progress,
								int rate) {
							return super.progress(progress, rate);
						}

					});
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {
			case SearchCityIdFail:
				Toast.makeText(WeizhangDetailActivity.this, "查询失败",
						Toast.LENGTH_SHORT).show();
				break;
			case SearchCityIdSuccess:
				searchWzInfo();
				break;
			case SearchWzInfoFail:
				Toast.makeText(WeizhangDetailActivity.this, "查询失败",
						Toast.LENGTH_SHORT).show();
				break;
			case SearchWzInfoSuccessNoInfo:
				Toast.makeText(WeizhangDetailActivity.this, "暂无违章信息",
						Toast.LENGTH_SHORT).show();
				break;
			case SearchWzInfoSuccess:
				if (list != null && list.size() > 0) {
					showWzInfo();
					postWzInfo();
				}
				break;
			case createOrder_error:
				Utili.ToastInfo(context, "提交订单失败");
				break;
			case createOrder_success:
				Utili.ToastInfo(context, "提交订单成功,工作人员将在第一时间与您联系");
				break;
			case createOrder_fail:
				Utili.ToastInfo(context, b.getString("msg"));
				break;	
			}

		}
	};

	/**
	 * title:md5加密,应与 (http://tool.chinaz.com/Tools/MD5.aspx) 上32加密结果一致
	 * 
	 * @param password
	 * @return
	 */
	private static String md5(String msg) {
		try {
			MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(msg.getBytes("UTF-8"));
			byte[] md = instance.digest();
			return byteArrayToHex(md);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

}
