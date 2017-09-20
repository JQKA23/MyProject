package com.carlife.chewu;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.carlife.R;
import com.carlife.daijia.DaijiaCurrentOrderListActivity;
import com.carlife.global.Const;
import com.carlife.global.ResultCode;
import com.carlife.main.BindMobileActivity;
import com.carlife.model.ChewuOrder;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.Utili;

public class ChewuActivity extends Activity implements OnClickListener {

	private TextView txt_hotline, txt_orderlist;
	private Button btn_back, btn_ycfw, btn_ghfw, btn_bgfw, btn_jsz, btn_zjbb,
			btn_other;

	private String bindMobile = "";
	private String type = "";
	private CustomProgressDialog cpd;

	private final static int createOrder_error = 0;
	private final static int createOrder_success = 1;
	private final static int createOrder_fail = 2;
	private final static int getOrder_fail = 10;
	private final static int getOrder_success = 11;
	private List<ChewuOrder> orderList;
	private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private Context context;
	private GridView gv;
	private List<Map<String, String>> list_gv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chewu);
		context = this;
		txt_orderlist = (TextView) findViewById(R.id.txt_orderlist);
		txt_orderlist.setOnClickListener(this);
		txt_hotline = (TextView) findViewById(R.id.txt_hotline);
		txt_hotline.setOnClickListener(this);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		gv = (GridView) findViewById(R.id.gv);
		getChewuBusiness();
	}

	@Override
	protected void onResume() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				MODE_PRIVATE);
		bindMobile = sp.getString(Const.BindMobile, ""); // 获取客户电话

		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.txt_hotline:
			Intent it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ getString(R.string.hotlineno)));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		case R.id.txt_orderlist:
			if (bindMobile.equals("")) {
				it = new Intent(context, BindMobileActivity.class);
				startActivity(it);
			} else {
				it = new Intent(context, DaijiaCurrentOrderListActivity.class);
				it.putExtra("url", "/GetCurrentChewuOrders");
				startActivity(it);
			}
			break;
		}
	}

	private void popCreateOrder(final int num) {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		LayoutInflater factory = LayoutInflater.from(context);
		View view = null;
		view = factory.inflate(R.layout.chewu_qita, null);
		TextView tv_hot = (TextView) view.findViewById(R.id.tv_mobile);
		tv_hot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ getString(R.string.hotlineno)));
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
			}
		});

		builder.setContentView(view);
		builder.setTitle(R.string.tip);
		builder.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						createOrder(num);
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

	private void createOrder(int num) {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", bindMobile);
		params.put("type", num + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", bindMobile);
		map.put("type", num + "");
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
							int code = Integer.parseInt(obj
									.getString("ResultCode"));
							if (code == 0) {
								handler.sendEmptyMessage(createOrder_success);
							} else {
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

	// 获取车务业务列表
	private void getChewuBusiness() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetChewuBusiness", params,
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
						if (cpd != null && cpd.isShowing()) {
							cpd.dismiss();
						}
						String jsonMessage = Utili.GetJson("" + t);

						try {
							JSONArray arr = new JSONArray(jsonMessage);
							list_gv = new ArrayList<Map<String, String>>();
							for (int i = 0; i < arr.length(); i++) {
								Map<String, String> map = new HashMap<String, String>();
								JSONObject obj = arr.getJSONObject(i);
								map.put("Id", obj.getString("Id"));
								map.put("TypeName", obj.getString("TypeName"));
								list_gv.add(map);
								initGridView(list_gv, gv);
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

	// 初始化listview
	private void initGridView(final List<Map<String, String>> list, GridView gv) {
		final SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.item_text1, new String[] { "TypeName" },
				new int[] { R.id.textView1 });
		gv.setAdapter(adapter);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int num = Integer.parseInt(list.get(position).get("Id"));
				popCreateOrder(num);
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
				Utili.ToastInfo(context, "提交订单失败");
				break;
			case createOrder_success:
				Utili.ToastInfo(context, "提交订单成功,工作人员将在第一时间与您联系");
				break;
			case createOrder_fail:
				Utili.ToastInfo(context, b.getString("msg"));
				break;
			case getOrder_success:
				break;
			default:
				break;
			}
		};
	};

}
