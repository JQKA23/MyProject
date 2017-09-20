package com.carlife.member;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.CustomerSpinner;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class MyLicenseActivity extends Activity implements OnClickListener {

	private Button btn_back, btn_save;
	private EditText et_name, et_number, et_address, et_year, et_month, et_day,
			et_issue_year, et_issue_month, et_issue_day;
	private EditText et_start_year, et_start_month, et_start_day, et_end_year,
			et_end_month, et_end_day;
	private RadioButton rb_male, rb_female;

	private Context context;
	private CustomProgressDialog cpd;
	private Car car;

	private final static int get_fail = 0;
	private final static int get_success = 1;
	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;
	private CustomerSpinner spinner;
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private String sex = "1";
	private String type = "";
	private String mobile = "";
	private String id="0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mylicense);
		context = this;
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_save = (Button) findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);

		rb_male = (RadioButton) findViewById(R.id.rb_male);
		rb_female = (RadioButton) findViewById(R.id.rb_female);
		et_name = (EditText) findViewById(R.id.et_name);
		et_number = (EditText) findViewById(R.id.et_number);
		et_address = (EditText) findViewById(R.id.et_address);
		et_year = (EditText) findViewById(R.id.et_year);
		et_month = (EditText) findViewById(R.id.et_month);
		et_day = (EditText) findViewById(R.id.et_day);
		et_issue_year = (EditText) findViewById(R.id.et_issue_year);
		et_issue_month = (EditText) findViewById(R.id.et_issue_month);
		et_issue_day = (EditText) findViewById(R.id.et_issue_day);
		et_start_year = (EditText) findViewById(R.id.et_start_year);
		et_start_month = (EditText) findViewById(R.id.et_start_month);
		et_start_day = (EditText) findViewById(R.id.et_start_day);
		et_end_year = (EditText) findViewById(R.id.et_end_year);
		et_end_month = (EditText) findViewById(R.id.et_end_month);
		et_end_day = (EditText) findViewById(R.id.et_end_day);

		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");

		init();
		spinner = (CustomerSpinner) findViewById(R.id.spinner);
		spinner.setList(list);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int index, long arg3) {
				type = list.get(index);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	public void init() {
		list.add("A1证");
		list.add("A2证");
		list.add("B1证");
		list.add("B2证");
		list.add("C1证");
		list.add("C2证");
	}

	@Override
	protected void onResume() {
		getMyLicense();
		super.onResume();
	}

	/*
	 * 初始化
	 * */
	private void getMyLicense() {
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
		fh.post(Const.API_SERVICES_ADDRESS + "/GetMyLicense", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(get_fail);
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
							int code=obj.getInt("ResultCode");
							if(code==0){
							et_name.setText(obj.getString("Name"));							
							et_address.setText(obj.getString("Address"));
							et_number.setText(obj.getString("LicenseNumber"));
							String birthday=obj.getString("Birthday");
							initBirthday(birthday);
							sex=obj.getString("Sex");
							if(sex.equals("1")){
								rb_male.setSelected(true);
							}else{
								rb_female.setSelected(true);
							}
							type=obj.getString("LicenseType");
							initType();
							String issueDate=obj.getString("IssueDate");
							initIssueDate(issueDate);
							String startDate=obj.getString("IssueStartDate");
							initStartDate(startDate);
							String endDate=obj.getString("IssueEndDate");
							initEndDate(endDate);
							id=obj.getString("MemberId");
							}
							handler.sendEmptyMessage(get_success);

						} catch (JSONException e) {
							handler.sendEmptyMessage(get_fail);
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
	
	private void initBirthday(String birthday){
		try{
			String[] bs=birthday.split("\\-");
			et_year.setText(bs[0]);
			et_month.setText(bs[1]);
			et_day.setText(bs[2]);
		}
		catch(Exception ex){
			
		}
	}
	
	private void initType(){
		if(type.endsWith("A1证")){
			spinner.setSelection(0, true);
		}
		else if(type.equals("A2证")){
			spinner.setSelection(1, true);
		}
		else if(type.equals("B1证")){
			spinner.setSelection(2, true);
		}
		else if(type.equals("B2证")){
			spinner.setSelection(3, true);
		}
		else if(type.equals("C1证")){
			spinner.setSelection(4, true);
		}
		else if(type.equals("C2证")){
			spinner.setSelection(5, true);
		}		
	}

	private void initIssueDate(String issueDate){
		try{
			String[] bs=issueDate.split("\\-");
			et_issue_year.setText(bs[0]);
			et_issue_month.setText(bs[1]);
			et_issue_day.setText(bs[2]);
		}
		catch(Exception ex){
			
		}
	}
	
	private void initStartDate(String startDate){
		try{
			String[] bs=startDate.split("\\-");
			et_start_year.setText(bs[0]);
			et_start_month.setText(bs[1]);
			et_start_day.setText(bs[2]);
		}
		catch(Exception ex){			
		}
	}
	
	private void initEndDate(String endDate){
		try{
			String[] bs=endDate.split("\\-");
			et_end_year.setText(bs[0]);
			et_end_month.setText(bs[1]);
			et_end_day.setText(bs[2]);
		}
		catch(Exception ex){			
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_save:
			saveLicense();
			break;
		}
	}

	private void saveLicense() {
		String name = et_name.getText().toString();
		if (name.equals("")) {
			Utili.ToastInfo(context, "请输入姓名");
			return;
		}
		String number = et_number.getText().toString();
		if (number.equals("")) {
			Utili.ToastInfo(context, "请输入档案编号");
			return;
		}
		if (rb_male.isSelected()) {
			sex = "1";
		} else {
			sex = "0";
		}
		String address=et_address.getText().toString();
		if(address.equals("")){
			Utili.ToastInfo(context, "请填写住址");
			return;
		}
		
		String b_year=et_year.getText().toString();
		String b_month=et_month.getText().toString();
		String b_day=et_day.getText().toString();
		String i_year=et_issue_year.getText().toString();
		String i_month=et_issue_month.getText().toString();
		String i_day=et_issue_day.getText().toString();
		String s_year=et_start_year.getText().toString();
		String s_month=et_start_month.getText().toString();
		String s_day=et_start_day.getText().toString();
		String e_year=et_end_year.getText().toString();
		String e_month=et_end_month.getText().toString();
		String e_day=et_end_day.getText().toString();
		
		if(b_year.equals("")||b_month.equals("")||b_day.equals("")){
			Utili.ToastInfo(context, "请填写完整出生日期");
			return;
		}
		if(i_year.equals("")||i_month.equals("")||i_day.equals("")){
			Utili.ToastInfo(context, "请填写完整初次领证日期");
			return;
		}
		if(s_year.equals("")||s_month.equals("")||s_day.equals("")){
			Utili.ToastInfo(context, "请填写完整有效开始日期");
			return;
		}
		if(e_year.equals("")||e_month.equals("")||e_day.equals("")){
			Utili.ToastInfo(context, "请填写完整有效结束日期");
			return;
		}
		
		String birthday=b_year+"-"+b_month+"-"+b_day;
		String issueDate=i_year+"-"+i_month+"-"+i_day;
		String startDate=s_year+"-"+s_month+"-"+s_day;
		String endDate=e_year+"-"+e_month+"-"+e_day;
		

		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();

		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", id);
		params.put("name", name);
		params.put("licenseNumber", number);
		params.put("sex", sex);
        params.put("birthday",birthday);
        params.put("address", address);
        params.put("licenseType", type);
        params.put("issueDate", issueDate);
        params.put("issueStartDate", startDate);
        params.put("issueEndDate", endDate);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", id);
		map.put("name", name);
		map.put("licenseNumber", number);
		map.put("sex", sex);
		map.put("birthday",birthday);
		map.put("address", address);
		map.put("licenseType", type);
		map.put("issueDate", issueDate);
		map.put("issueStartDate", startDate);
		map.put("issueEndDate", endDate);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/UpdateMyLicense", params,
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
				Utili.ToastInfo(context, "保存失败");
				break;
			case createOrder_Success:
				Utili.ToastInfo(context, "保存成功");
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
