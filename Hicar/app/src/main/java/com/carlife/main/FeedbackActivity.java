package com.carlife.main;




import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.UpdateHelper;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackActivity extends Activity implements OnClickListener {
	
	private Button btn_back,btn_submit;
	private EditText et_content;
	private CustomProgressDialog cpd;
	private String content="";
	private String mobile="";
	
	private final static int feedback_fail=0;
	private final static int feedback_success=1;
	
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		et_content=(EditText)findViewById(R.id.et_content);
		btn_submit=(Button)findViewById(R.id.btn_submit);
		btn_submit.setOnClickListener(this);	
		
		context=this;
		
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				MODE_PRIVATE);		
		mobile=sp.getString(Const.BindMobile, "");
		if(mobile.equals("")){
			Intent it=new Intent(context,BindMobileActivity.class);
			startActivity(it);
			finish();
		}
		
		
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
            this.finish();
			break;
		case R.id.btn_submit:
			popWindow();
			break;
		}
	
	}
	
	private void popWindow() {
		content=et_content.getText().toString().trim();
		if(content.length()<=0){
			Utili.ToastInfo(context, "请填写内容");
			return;
		}
		SharedPreferences sp = getSharedPreferences(Const.spFeedback,MODE_PRIVATE);
		String  feedback = sp.getString(Const.feedback, "");
		if(feedback.equals(content)){
			Toast.makeText(this, "您已经提交过了", Toast.LENGTH_SHORT).show();
			return;
		}
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确认提交吗?");
		builder.setTitle("提示");
		builder.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();							
						postData();
						
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
	
	
	private void postData(){
		if (cpd == null || !cpd.isShowing()){
			cpd = CustomProgressDialog.createDialog(this);	
			cpd.show();}
	
		String version="android"+UpdateHelper.getVerName(this);
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);		
		params.put("mobile", mobile);
		params.put("content", content);
		params.put("versionNo", version);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);   
		map.put("mobile", mobile);
		map.put("content", content);
		map.put("versionNo", version);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);

		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/FeedBack", params,
				new AjaxCallBack<Object>() {
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {					
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(feedback_fail);
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
							if(code==0){
							    handler.sendEmptyMessage(feedback_success);
							}else{
								handler.sendEmptyMessage(feedback_fail);
							}
						} catch (JSONException e) {
							handler.sendEmptyMessage(feedback_fail);
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
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd!=null&&cpd.isShowing()) {
				cpd.dismiss();
			}		
			Bundle b = msg.getData();
			switch (msg.what) {			
			
			case feedback_fail:
				Toast.makeText(FeedbackActivity.this, "系统错误，请稍后重试",
						Toast.LENGTH_SHORT).show();
				break;	
			case feedback_success:
				SharedPreferences sp = getSharedPreferences(Const.spFeedback,MODE_PRIVATE);
				sp.edit().putString(Const.feedback, content).commit();
				finish();
				break;
			}

		}
	};
	
}
