package com.carlife.daijia;


import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Share;
import com.carlife.utility.Utili;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DaijiaDriveDetailActivity extends Activity implements OnClickListener {
	
	private TextView tv_waittime,tv_starttime,tv_endtime,tv_kilo;
	private Context context;
	private CustomProgressDialog cpd;
	private String id="";
	
	private static final int  get_fail=0;
	private static final int get_success=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.daijiadrivedetail);
		
		context=this;
		
		Button btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);	
	    
		tv_waittime=(TextView)findViewById(R.id.tv_waittime);
		tv_starttime=(TextView)findViewById(R.id.tv_starttime);
		tv_kilo=(TextView)findViewById(R.id.tv_kilo);
		tv_endtime=(TextView)findViewById(R.id.tv_endtime);
		
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		id=bd.getString("id");
		
		getPaydetail();
	}
	
	private void getPaydetail(){
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", id);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", id);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetOrderDaijiaDriveDetail", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(get_fail);
						super.onFailure(t, errorNo, strMsg);

					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						System.out.print("" + t);
						String jsonMessage =Utili.GetJson("" + t);
							try {
								JSONObject obj = new JSONObject(jsonMessage);
								int code=obj.getInt("ResultCode");
								if(code==0){
									tv_starttime.setText(obj.getString("StartTime"));
									tv_endtime.setText(obj.getString("EndTime"));
									tv_waittime.setText(obj.getString("WaitTime"));
									tv_kilo.setText(obj.getString("MileageReal"));
									handler.sendEmptyMessage(get_success);
								}
								 else {
									 handler.sendEmptyMessage(get_fail);
								}
							} catch (JSONException e) {
								e.printStackTrace();
								handler.sendEmptyMessage(get_fail);
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
			case get_fail:
				Utili.ToastInfo(context, "获取数据失败，请稍后重试");
				finish();
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
            this.finish();
			break;
		}
	
	}
}
