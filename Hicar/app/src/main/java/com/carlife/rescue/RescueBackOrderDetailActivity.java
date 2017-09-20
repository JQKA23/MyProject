package com.carlife.rescue;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.carlife.R;
import com.carlife.R.drawable;
import com.carlife.R.id;
import com.carlife.R.layout;
import com.carlife.global.Const;
import com.carlife.member.MyAccountActivity;
import com.carlife.model.DaijiaOrder;
import com.carlife.model.Driver;
import com.carlife.model.RescueOrder;
import com.carlife.utility.Utili;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.Share;
import com.carlife.utility.CustomDialog.Builder;
import com.carlife.utility.CustomProgressDialog;
import com.carlife.utility.EncodeUtility;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RescueBackOrderDetailActivity extends Activity implements
		OnClickListener {

	private ImageView ivs1, ivs2, ivs3, ivs4, ivs5;
	private Button btn_back, btn_contact,btn_pay;

	private TextView tv_drivername, tv_usercode, tv_ordercount, tv_star,
			tv_cancel, tv_orderNo, tv_addtime, tv_startTime, tv_start, tv_end,
			tv_cartype, tv_carNo, tv_kiloprice, tv_state;

	private CustomProgressDialog cpd;
	private RescueOrder order;

	private String mobile = "";
	private Context context;

	private final static int cancelorder_fail = 0;
	private final static int cancelorder_Success = 1;
	private final static int payorder_fail = 110;
	private final static int payorder_Success = 111;

	private LinearLayout ll_driver;

	private final static int getbonus_error = 10;
	private final static int getbonus_success = 11;
	private int bonusId = 0;
	private int payType = 0;
	private String cardNo = "";
	private String cardInfo = "";
	private String bindPhone="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rescuebackorderdetail);
		ll_driver = (LinearLayout) findViewById(R.id.ll_driver);
		tv_drivername = (TextView) findViewById(R.id.tv_drivername);
		tv_usercode = (TextView) findViewById(R.id.tv_usercode);
		tv_ordercount = (TextView) findViewById(R.id.tv_ordercount);
		tv_star = (TextView) findViewById(R.id.tv_star);
		ivs1 = (ImageView) findViewById(R.id.ivs1);
		ivs2 = (ImageView) findViewById(R.id.ivs2);
		ivs3 = (ImageView) findViewById(R.id.ivs3);
		ivs4 = (ImageView) findViewById(R.id.ivs4);
		ivs5 = (ImageView) findViewById(R.id.ivs5);
		tv_orderNo = (TextView) findViewById(R.id.tv_orderNo);
		tv_addtime = (TextView) findViewById(R.id.tv_addtime);
		tv_startTime = (TextView) findViewById(R.id.tv_startTime);
		tv_start = (TextView) findViewById(R.id.tv_start);
		tv_end = (TextView) findViewById(R.id.tv_end);
		tv_cartype = (TextView) findViewById(R.id.tv_cartype);
		tv_carNo = (TextView) findViewById(R.id.tv_carNo);
		tv_kiloprice = (TextView) findViewById(R.id.tv_kiloprice);
		tv_state = (TextView) findViewById(R.id.tv_state);
		//tv_state.setOnClickListener(this);
		context = this;

		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_cancel.setOnClickListener(this);

		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);

		btn_contact = (Button) findViewById(R.id.btn_contact);
		btn_contact.setOnClickListener(this);
		
		btn_pay=(Button)findViewById(R.id.btn_pay);
		btn_pay.setOnClickListener(this);
		
		Bundle bd = getIntent().getExtras();
		order = (RescueOrder) bd.getSerializable("rescueOrder");
		tv_orderNo.setText(order.getOrderNo());
		tv_addtime.setText(order.getAddTime());
		tv_startTime.setText(order.getStartTime());
		tv_start.setText(order.getStartPlace());
		tv_end.setText(order.getEndPlace());
		tv_cartype.setText(order.getCarType());
		tv_carNo.setText(order.getCarNo());
		tv_kiloprice.setText(order.getKmReal() + "公里 " + order.getPricePlan()
				+ "元");

		tv_state.setEnabled(false);		
		
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		bindPhone = sp.getString(Const.BindMobile, "");		
	}

	@Override
	protected void onResume() {
		getOrderInfo();			
		super.onResume();
	}
	
	private void showDriver(){
		if (order.getDriverId().equals("0")) {
			ll_driver.setVisibility(View.GONE);
		} else {
			ll_driver.setVisibility(View.VISIBLE);
			getDriverRescueInfo();
		}
		
		if (order.getDriverId().equals("0")) {
			btn_pay.setVisibility(View.GONE);
			tv_state.setText("等待接单");
			tv_cancel.setVisibility(View.VISIBLE);
		} else {
			if (order.getIsPay() == 1) {
				btn_pay.setVisibility(View.GONE);
				tv_state.setText("已付款");
				tv_cancel.setVisibility(View.GONE);
			} else {
				tv_state.setText("未付款");				
				btn_pay.setVisibility(View.VISIBLE);
				tv_cancel.setVisibility(View.VISIBLE);
			}
		}	
	}
	
	private void getOrderInfo(){
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId()+"");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getId()+"");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetRescueBackOrderDetail", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
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
							if(code==0){
								order.setDriverId(obj.getString("DriverId"));
								order.setIsPay(obj.getInt("IsPay"));
								if(obj.getInt("OrderStatus")==5){
									finish();
								}
								showDriver();
							}
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	/*
	 * 获取司机信息
	 */
	private void getDriverRescueInfo() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getDriverId());
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getDriverId());
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/getDriverRescueInfo", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
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
							tv_drivername.setText(obj.getString("Name"));
							tv_usercode.setText(obj.getString("UserCode"));
							tv_star.setText(obj.getString("Star"));
							initDriverStar(obj.getString("Star"));
							tv_ordercount.setText(obj.getString("OrderCount")
									+ "单");
							mobile = obj.getString("Mobile");
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}
				});
	}

	private void initDriverStar(String star) {
		double s = Double.parseDouble(star) * 10;
		if (s >= 50) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
		} else if (s > 40) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 30) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 20) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else if (s > 10) {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhighlight));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		} else {
			ivs1.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs2.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs3.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs4.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
			ivs5.setImageDrawable(getResources().getDrawable(
					R.drawable.starhalf));
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.tv_cancel:
			Intent it=new Intent(context,RescueOrderCancelActivity.class);
			Bundle bd=new Bundle();
			bd.putString("id", order.getId());
			it.putExtras(bd);
			startActivity(it);
			//popCancelOrder();
			break;
		case R.id.btn_contact:
			it = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ mobile));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			break;
		
		case R.id.btn_pay:
			GetBonusAndSettlementType();
			break;
		}
	}

	private void GetBonusAndSettlementType() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(this);
			cpd.show();
		}

		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", bindPhone);
		params.put("price", order.getPricePlan() + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", bindPhone);
		map.put("price", order.getPricePlan() + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetBonusAndSettlementType",
				params, new AjaxCallBack<Object>() {

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
							int code = obj.getInt("ResultCode");
							if (code == 0) {
								cardInfo="";
								payType = obj.getInt("PayType");
								if (payType != 0) {
									cardNo = obj.getString("CardNo");
									if (payType == 2) {
										cardInfo += "您可用钱包余额支付";
									} else if (payType == 3) {
										cardInfo += "您可用礼品卡支付";
									}
									bonusId = obj.getInt("BonusId");
									if (bonusId != 0) {
										cardInfo += "，可用红包抵扣"
												+ obj.getString("BonusAmount")
												+ "元";
									}
									handler.sendEmptyMessage(getbonus_success);
								} else {
									handler.sendEmptyMessage(getbonus_error);
									//cardInfo = "金额不足，请先对钱包余额或礼品卡进行充值 ";
									jumpToPay();									
								}

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
	
	private void jumpToPay(){
		Intent it=new Intent(context,RescueOrderPayActivity.class);
		Bundle bd=new Bundle();
		bd.putSerializable("order", order);
		it.putExtras(bd);
		startActivity(it);
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
		fh.post(Const.API_SERVICES_ADDRESS + "/CancelRescueOrder", params,
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

	private void popPayOrder() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(cardInfo);
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				payOrder();
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

	private void payOrder() {
		if (cpd == null || !cpd.isShowing()) {
			cpd = CustomProgressDialog.createDialog(context);
			cpd.show();
		}
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("id", order.getId() + "");
		params.put("bonusId", bonusId + "");
		params.put("cardNo", cardNo);
		params.put("payType", payType + "");
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("id", order.getId() + "");
		map.put("bonusId", bonusId + "");
		map.put("cardNo", cardNo);
		map.put("payType", payType + "");
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/PayRescueBackOrder", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						handler.sendEmptyMessage(payorder_fail);
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
								handler.sendEmptyMessage(payorder_Success);
							} else {
								handler.sendEmptyMessage(payorder_fail);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							handler.sendEmptyMessage(payorder_fail);
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
			case getbonus_success:
				popPayOrder();
				break;
			case payorder_Success:
				Utili.ToastInfo(context, "支付成功");
				finish();
				break;
			case payorder_fail:
				Utili.ToastInfo(context, "支付失败，请稍后重试");
				break;
			default:
				break;
			}
		};
	};

}
