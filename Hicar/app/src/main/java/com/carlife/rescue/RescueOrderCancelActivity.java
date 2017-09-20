package com.carlife.rescue;

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
import com.carlife.utility.Oper;
import com.carlife.utility.Utili;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RescueOrderCancelActivity extends Activity implements
		OnClickListener {
	private Button btn_submit;
	private ImageView iv1, iv2, iv3, iv4, iv5, iv6;
	private EditText et_other;
	private String remark = "";
	private String orderId="";
	private CustomProgressDialog cpd;
	private Context context;
	
	private final static int cancelorder_fail = 0;
	private final static int cancelorder_Success = 1;
	
	private LinearLayout ll1,ll2,ll3,ll4,ll5,ll6;
	private int cancelType=0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescueordercancel);
		Button btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		btn_submit.setOnClickListener(this);
		et_other = (EditText) findViewById(R.id.et_other);
		iv1 = (ImageView) findViewById(R.id.iv1);
		iv2 = (ImageView) findViewById(R.id.iv2);
		iv3 = (ImageView) findViewById(R.id.iv3);
		iv4 = (ImageView) findViewById(R.id.iv4);
		iv5 = (ImageView) findViewById(R.id.iv5);
		iv6 = (ImageView) findViewById(R.id.iv6);
		ll1=(LinearLayout)findViewById(R.id.ll1);
		ll2=(LinearLayout)findViewById(R.id.ll2);
		ll3=(LinearLayout)findViewById(R.id.ll3);
		ll4=(LinearLayout)findViewById(R.id.ll4);
		ll5=(LinearLayout)findViewById(R.id.ll5);
		ll6=(LinearLayout)findViewById(R.id.ll6);
		ll1.setOnClickListener(this);
		ll2.setOnClickListener(this);
		ll3.setOnClickListener(this);
		ll4.setOnClickListener(this);
		ll5.setOnClickListener(this);
		ll6.setOnClickListener(this);
		Intent it=getIntent();
		Bundle bd=it.getExtras();
		orderId=bd.getString("id");
		context=this;
	}

	private void setRemark(int type) {
		cancelType=type;
		iv1.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		iv2.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		iv3.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		iv4.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		iv5.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		iv6.setImageDrawable(getResources().getDrawable(R.drawable.nochoosen));
		switch (type) {
		case 1:
			iv1.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			remark = "找其他救援公司了";
			break;
		case 2:
			iv2.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			remark = "救援到达速度太慢";
			break;
		case 3:
			iv3.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			remark = "救援费用太贵了";
			break;
		case 4:
			iv4.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			remark = "无法提供救援服务";
			break;
		case 5:
			iv5.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			remark = "服务态度不好取消";
			break;
		case 6:
			iv6.setImageDrawable(getResources().getDrawable(R.drawable.choosen));
			
			break;
		}
	}
	
	private void cancelOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(context);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", orderId);
		params.put("remark", remark);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", orderId);
		map.put("remark", remark);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CancelRescueOrderOption", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(cancelorder_fail);
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
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								handler.sendEmptyMessage(cancelorder_Success);
							} else {
								handler.sendEmptyMessage(cancelorder_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(cancelorder_fail);
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
			case cancelorder_fail:
				Utili.ToastInfo(context, "取消失败");
				finish();
				break;
			case cancelorder_Success:
				Utili.ToastInfo(context, "取消成功");
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
		case R.id.ll1:
			setRemark(1);
			break;
		case R.id.ll2:
			setRemark(2);
			break;
		case R.id.ll3:
			setRemark(3);
			break;
		case R.id.ll4:
			setRemark(4);
			break;
		case R.id.ll5:
			setRemark(5);
			break;
		case R.id.ll6:
			setRemark(6);
			break;
		case R.id.btn_submit:
			if(cancelType==6){
				remark=et_other.getText().toString();
			}
			if(remark.equals("")){
			  Utili.ToastInfo(context, "请选择取消原因");	
			}else{
			 cancelOrder();
			}
			break;
		}

	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		View v = getCurrentFocus();
		Oper o = new Oper(this);
		o.dispatchTouch(ev, v);
		return super.dispatchTouchEvent(ev);
	}
	
}
