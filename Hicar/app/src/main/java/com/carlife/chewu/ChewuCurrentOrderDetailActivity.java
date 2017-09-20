package com.carlife.chewu;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.model.ChewuOrder;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class ChewuCurrentOrderDetailActivity extends Activity implements
		OnClickListener {

	private Button btn_back, btn_contact;

	private TextView tv_cancel, tv_type, tv_starttime;

	private CustomProgressDialog cpd;
	private ChewuOrder order;

	private String mobile = "";
	private Context context;

	private final static int cancelorder_fail = 0;
	private final static int cancelorder_Success = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chewucurrentorderdetail);

		tv_starttime = (TextView) findViewById(R.id.tv_starttime);
		context = this;

		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);
		tv_type = (TextView) findViewById(R.id.tv_type);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_contact = (Button) findViewById(R.id.btn_contact);
		btn_contact.setOnClickListener(this);

		Bundle bd = getIntent().getExtras();
		order = (ChewuOrder) bd.getSerializable("Order");
		tv_type.setText(order.getBusinessType());
		tv_starttime.setText(order.getAddTime());
	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_cancel:
			popCancelOrder();
			break;
		case R.id.btn_contact:
			Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ getString(R.string.hotline)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		}
	}

	private void popCancelOrder() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage("确定取消订单？");
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				cancelOrder();
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

	private void cancelOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(context);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getId() + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/CancelChewuOrder", params,
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

}
