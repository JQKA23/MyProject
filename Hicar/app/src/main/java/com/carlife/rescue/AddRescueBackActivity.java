package com.carlife.rescue;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.member.MyAccountActivity;
import com.carlife.model.Car;
import com.carlife.model.RescueOrder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;

public class AddRescueBackActivity extends Activity implements OnClickListener,OnGetGeoCoderResultListener,
OnGetPoiSearchResultListener, OnGetSuggestionResultListener {
	
	private Button btn_save,btn_back;	
	private Context context;
	private CustomProgressDialog cpd;
	private String mobile="";
	private EditText et_model,et_carNo,et_start,et_end,et_remark;
	private String startLat="";
	private String startLon="";
	private String endLat="";
	private String endLon="";
	private ListView lv_address;
	private TextView tv_time,tv_date,tv_cardinfo;
	
	private final static int createOrder_error = 0;
	private final static int createOrder_success = 1;
	
	private final static int getbonus_error = 10;
	private final static int getbonus_success = 11;
	
	private GeoCoder mSearch = null;
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	private String city = "";
	private TextView tv_info;
	
	private  int searchType=0; //0起点1终点
	private int price=0;
	private int distance=0;
	
	private boolean isAccountEnough=false;
	private int bonusId=0;
	private int payType=0;
	private String cardNo="";
	private int kmPrice=4;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addrescueback);
		context=this;
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_save=(Button)findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);
		et_model=(EditText)findViewById(R.id.et_model);
		et_carNo=(EditText)findViewById(R.id.et_carNo);
		et_start=(EditText)findViewById(R.id.et_start);
		et_end=(EditText)findViewById(R.id.et_end);
		et_remark=(EditText)findViewById(R.id.et_remark);
		lv_address=(ListView)findViewById(R.id.lv_address);
		tv_info=(TextView)findViewById(R.id.tv_info);
		
		tv_date=(TextView)findViewById(R.id.tv_date);
		tv_time=(TextView)findViewById(R.id.tv_time);		
		Date dt=new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd&HH:mm:ss");
		String sysDate = sdf.format(dt); // 当期日期
//		tv_date.setText(sysDate.split("\\&")[0]);
//		tv_time.setText(sysDate.split("\\&")[1]);
		tv_date.setOnClickListener(this);
		tv_time.setOnClickListener(this);
		
		tv_cardinfo=(TextView)findViewById(R.id.tv_cardinfo);
		tv_cardinfo.setOnClickListener(this);
		
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		startLat=bd.getString("lat");
		startLon=bd.getString("lon");
		et_start.setText(bd.getString("addr"));
		
		
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		sp = getSharedPreferences(Const.City, MODE_PRIVATE);
		city = sp.getString(Const.City, "北京");
		
		et_start.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				searchType=0;
				if (cs.length() <= 0) {
					return;
				}

				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(city));
				
				lv_address.setVisibility(View.VISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				

			}

			@Override
			public void afterTextChanged(Editable s) {
				// mSearch.geocode(new GeoCodeOption().city(city).address(et_end.getText().toString()));
			}
		});
		
		et_end.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				searchType=1;
				if (cs.length() <= 0) {
					return;
				}

				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city(city));
				
				lv_address.setVisibility(View.VISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				

			}

			@Override
			public void afterTextChanged(Editable s) {
				// mSearch.geocode(new GeoCodeOption().city(city).address(et_end.getText().toString()));
			}
		});

		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
	}

	@Override
	public void onDestroy() {
		mSearch.destroy();
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		super.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
            this.finish();
			break;		
		case R.id.btn_save:
			createRescueBackOrder();
			break;
		case R.id.tv_date:
	    	showDialog(1);
	    	break;
	    case R.id.tv_time:
	    	showDialog(2);
	    	break;
	    case R.id.tv_cardinfo:
	    	Intent it=new Intent(context,MyAccountActivity.class);
	    	startActivity(it);
	    	break;
		}	
	}
	
	private Calendar c = null;	
	@Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case 1:       
        	c = Calendar.getInstance();
            dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker dp, int year,int month, int dayOfMonth) {
                    	tv_date.setText(year+"-"+(month+1)+"-"+dayOfMonth);                        
                    }
                }, 
                c.get(Calendar.YEAR), // 传入年份
                c.get(Calendar.MONTH), // 传入月份
                c.get(Calendar.DAY_OF_MONTH) // 传入天数
            );
            break;
        case 2:
            c=Calendar.getInstance();
            dialog=new TimePickerDialog(
                this, 
                new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tv_time.setText(hourOfDay+":"+minute+":"+"00");
                    }
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),               
                false
            );
            break;
        }
        return dialog;
    }
	
	
	
	@Override
	protected void onResume() {	
		getKmPrice();
		super.onResume();
	}
	
	private void getKmPrice(){
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);		
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);	
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetRescueBackOrderKmPrice", params,
				new AjaxCallBack<Object>() {

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
							int code=obj.getInt("ResultCode");
							if (code==0) {							
								kmPrice=obj.getInt("KmPrice");
								handler.sendEmptyMessage(getbonus_success);								
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
	
	private void createRescueBackOrder(){		
		String carModel=et_model.getText().toString();
		if(carModel.equals("")){
			Utili.ToastInfo(context, "请填写车型");
			return;
		}
		String carNo=et_carNo.getText().toString();
		if(carNo.equals("")){
			Utili.ToastInfo(context, "请填写车牌号");
		    return;
		}
		if(startLat.equals("")||startLon.equals("")){
			Utili.ToastInfo(context, "没有获取到起点经纬度");
		    return;
		}
		if(endLat.equals("")||endLon.equals("")){
			Utili.ToastInfo(context, "没有获取到终点经纬度");
		    return;
		}
		String startPlace=et_start.getText().toString();
		if(startPlace.equals("")){
			Utili.ToastInfo(context, "请选择起点");
			return;
		}
		String endPlace=et_end.getText().toString();
		if(endPlace.equals("")){
			Utili.ToastInfo(context, "请选择终点");
			return;
		}
		String remark=et_remark.getText().toString();
//		if(payType==0){
//			Utili.ToastInfo(context, "余额不足，请到我的钱包中充值");
//			return;
//		}
		
		String startTime=tv_date.getText().toString()+" "+tv_time.getText().toString();
		if(tv_date.getText().toString().equals("")||tv_time.getText().toString().equals("")||startTime.length()<13){
			Utili.ToastInfo(context, "请选择日期和时间");
			return;
		}
		
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		
		
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", mobile);
		params.put("carModel", carModel);
		params.put("carNo", carNo);
		params.put("startPlace", startPlace);
        params.put("endPlace", endPlace);
        params.put("startLon", startLon);
        params.put("startLat", startLat);
        params.put("startTime", startTime);
        params.put("remark", remark);
        params.put("price", price+"");
        params.put("distance", distance+"");
//        params.put("bonusId", bonusId+"");
//        params.put("payType", payType+"");
//        params.put("amountCardNo", cardNo);       
        
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", mobile);
		map.put("carModel", carModel);
		map.put("carNo", carNo);
		map.put("startPlace", startPlace);
		map.put("endPlace", endPlace);
		map.put("startLon", startLon);
		map.put("startLat", startLat);
		map.put("startTime", startTime);
		map.put("remark", remark);
		map.put("price", price+"");
		map.put("distance", distance+"");
//		map.put("bonusId", bonusId+"");
//		map.put("payType", payType+"");
//		map.put("amountCardNo", cardNo);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CreateRescueBackOrder", params,
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
							int code=obj.getInt("ResultCode");
							if (code==0) {
								handler.sendEmptyMessage(createOrder_success);
							} else {
								handler.sendEmptyMessage(createOrder_error);
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
	
	
	
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			Bundle b = msg.getData();
			switch (msg.what) {			
			case createOrder_error:
				Utili.ToastInfo(context, "创建失败");
				break;
			case createOrder_success:
				finish();
				Utili.ToastInfo(context, "创建成功");
				break;
			case getbonus_error:
				Utili.ToastInfo(context, "系统繁忙，请稍后重试");
				finish();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			Map<String, String> item = new HashMap<String, String>();
			if (info.key != null)
				item.put("Address", info.key);
			item.put("Latitude", "");
			item.put("Longitude", "");
			data.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data,
				R.layout.addressitem, new String[] { "Address" },
				new int[] { R.id.txt_address });
		lv_address.setAdapter(adapter);
		lv_address.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				mSearch.geocode(new GeoCodeOption().city(city).address(
						data.get(position).get("Address")));				
			}
		});
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult arg0) {
		
	}

	@Override
	public void onGetPoiResult(PoiResult arg0) {
		
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			// 没取到
		} else {			
			if(searchType==0){
				startLat=result.getLocation().latitude+"";
				startLon=result.getLocation().longitude+"";
				et_start.setText(result.getAddress());				
			}
			else{
				endLat=result.getLocation().latitude+"";
				endLon=result.getLocation().longitude+"";
				et_end.setText(result.getAddress());				
			}
			if(!startLat.equals("")&&!startLon.equals("")&&!endLat.equals("")&&!endLon.equals("")){
				LatLng p1 = new LatLng(Double.parseDouble(startLat), Double.parseDouble(startLon));
				LatLng p2 = new LatLng(Double.parseDouble(endLat), Double.parseDouble(endLon));
				double dis = DistanceUtil.getDistance(p1, p2)/1000;
				distance=Integer.parseInt(String.format("%.0f", dis));
				price=distance*kmPrice;
				tv_info.setText("大约"+String.format("%.0f", dis)+"公里,"+price+"元");	
				//GetBonusAndSettlementType();
			}	
			lv_address.setVisibility(View.GONE);
		}
	}
	
	
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
				
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onGetPoiIndoorResult(PoiIndoorResult arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
