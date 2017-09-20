package com.carlife.member;


import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.Car;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CarDetailActivity extends Activity implements OnClickListener {
	
	private Button btn_back,btn_del,btn_save;
	private EditText et_model,et_color,et_carNo,et_frameNo,et_engineNo;
	private Context context;
	private CustomProgressDialog cpd;
	private Car car;
	
	private final static int createOrder_fail = 10;
	private final static int createOrder_Success = 11;
	
	private final static int del_fail = 110;
	private final static int del_Success = 111;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cardetail);
		context=this;
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_del=(Button)findViewById(R.id.btn_del);
		btn_del.setOnClickListener(this);
		btn_save=(Button)findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);
		
		et_model = (EditText) findViewById(R.id.et_model);
		et_color=(EditText)findViewById(R.id.et_color);
		et_carNo=(EditText)findViewById(R.id.et_carNo);
		et_frameNo=(EditText)findViewById(R.id.et_frameNo);
		et_engineNo=(EditText)findViewById(R.id.et_engineNo);
		Intent it=getIntent();
		car=(Car)it.getExtras().getSerializable("car");
		et_model.setText(car.getCarModel());
		et_color.setText(car.getCarColor());
		et_carNo.setText(car.getCarNo());
		//et_frameNo.setText(car.getCarframeNo());
		et_engineNo.setText(car.getEngineNo());
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
            this.finish();
			break;
		case R.id.btn_save:
			saveCar();
			break;
		case R.id.btn_del:
			popDel();
			break;
		}	
	}
	
	private void popDel(){
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("删除操作不可恢复，请确认要删除吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						delCarInfo();
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
	
	private void delCarInfo(){
AjaxParams params = new AjaxParams();	
		
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", car.getId()+"");
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", car.getId()+"");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/DeleteCarInfo", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(del_fail);
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
								handler.sendEmptyMessage(del_Success);
							} else {
								handler.sendEmptyMessage(del_fail);
							}

						} catch (JSONException e) {
							handler.sendEmptyMessage(del_fail);
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
	
	
	
	private void saveCar(){		
		String model=et_model.getText().toString();
		if(model.equals("")){
			Utili.ToastInfo(context, "请输入车型");
			return;
		}
		String color=et_color.getText().toString();
		if(color.equals("")){
			Utili.ToastInfo(context, "请输入颜色");
			return;
		}
		String carNo=et_carNo.getText().toString();
		if(carNo.equals("")){
			Utili.ToastInfo(context, "请输入车牌号");
		    return;
		}
		String frameNo=et_frameNo.getText().toString();
		String engineNo=et_engineNo.getText().toString();
		if(engineNo.equals("")){
			Utili.ToastInfo(context, "请输入车架号或者发动机号");
		    return;
		}
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();	
		
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", car.getId()+"");
		params.put("model", model);
		params.put("color", color);
		params.put("carNo", carNo);
		params.put("frameNo", frameNo);
		params.put("engineNo", engineNo);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", car.getId()+"");
		map.put("model", model);
		map.put("color", color);
		map.put("carNo", carNo);
		map.put("frameNo", frameNo);
		map.put("engineNo", engineNo);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.configTimeout(60000);
		fh.post(Const.API_SERVICES_ADDRESS + "/UpdateCarInfo", params,
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
				finish();
				break;
			case del_fail:
				Utili.ToastInfo(context, "删除失败");
				break;
			case del_Success:
				Utili.ToastInfo(context, "删除成功");
				finish();
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
