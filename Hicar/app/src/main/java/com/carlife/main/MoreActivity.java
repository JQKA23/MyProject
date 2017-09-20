package com.carlife.main;

import java.io.File;
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
import com.carlife.utility.ExitManager;
import com.carlife.utility.Share;
import com.carlife.utility.UpdateHelper;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MoreActivity extends Activity implements OnClickListener {
	
	private TextView txt_version;
	private int versionCode=0;
	private CustomProgressDialog cpd;
	private ProgressDialog pd;
	
	private final static int checkVersion_fail=0;
	private final static int checkVersion_success=1;
	private String appUrl = "";
	private final static int GO_UPDATE=2;
	private final static int DOWN_ERROR=3;

	private Share share;
	private IWXAPI wxapi;
	private static final int THUMB_SIZE = 150;
	
	private Button btn_quit,btn_back;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more);
			
		btn_quit=(Button)findViewById(R.id.btn_quit);
		btn_quit.setOnClickListener(this);
		btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);		
		txt_version=(TextView)findViewById(R.id.txt_version);
		txt_version.setText("V" + UpdateHelper.getVerName(this));		
		wxapi = WXAPIFactory.createWXAPI(this, Const.wxAPPID);
		wxapi.registerApp(Const.wxAPPID);
	}

	@Override
	public void onClick(View v) {
		Intent it;
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
//		case R.id.tr_aboutus:
//			it = new Intent(MoreActivity.this, AboutUsActivity.class);
//			startActivity(it);
//			break;		
//		case R.id.tr_update:
//			versionCode = UpdateHelper.getVerCode(this);
//			CheckUpdate();// 检查新版本
//			break;
//		case R.id.tr_share:
//			share();
//			break;			
		case R.id.btn_quit:
			SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
					MODE_PRIVATE);
			sp.edit().clear().commit();
			sp=getSharedPreferences(Const.spCallMobile,MODE_PRIVATE);
			sp.edit().clear().commit();
			Utili.ToastInfo(MoreActivity.this, "成功退出登录");
			finish();
			break;
		}
	}
	
	private void share(){
		share = new Share(MoreActivity.this, itemsOnClick);
		share.showAtLocation(
				MoreActivity.this.findViewById(R.id.main),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
	}
	
	

	// 检查升级
	private void CheckUpdate() {
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
			FinalHttp fh=new FinalHttp();					
			fh.post(Const.API_SERVICES_ADDRESS
					+ "/GetNewEdition", params, new AjaxCallBack<Object>() {
				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg) {
					handler.sendEmptyMessage(checkVersion_fail);
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
					System.out.print("" + t);
					String jsonMessage = "" + t;
					if (jsonMessage != null && !jsonMessage.equals("")) {
						jsonMessage = jsonMessage.substring(1);
						jsonMessage = jsonMessage.substring(0,
								jsonMessage.length() - 1);
						jsonMessage = jsonMessage.replace("\\", "");

						try {
							// 返回错误
							if (jsonMessage.contains("IsError")) {
								handler.sendEmptyMessage(checkVersion_fail);
							} else {								
								JSONObject obj = new JSONObject(jsonMessage);
								int newVersionCode = Integer.parseInt(obj.getString("VersionCode"));
								appUrl = obj.getString("AppUrl");

								if (newVersionCode > versionCode) {
									Message msg = new Message();
									msg.what = GO_UPDATE;
									handler.sendMessage(msg);
								} else {
									Toast.makeText(MoreActivity.this,"当前已是最新版本！", Toast.LENGTH_SHORT)
											.show();
									handler.sendEmptyMessage(200);
								}
							}

						} catch (JSONException e) {
							handler.sendEmptyMessage(checkVersion_fail);
							System.out.print(e);

						}
					}

				}

				@Override
				public AjaxCallBack<Object> progress(boolean progress, int rate) {
					return super.progress(progress, rate);
				}

			});
		

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (cpd != null && cpd.isShowing()) {
				cpd.dismiss();
			}
			switch (msg.what) {
			case GO_UPDATE:
				goUpdate();
				break;
			case DOWN_ERROR:
				Toast.makeText(MoreActivity.this, "下载新版本失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case checkVersion_fail:
				Toast.makeText(MoreActivity.this, "系统错误，请稍后重试",
						Toast.LENGTH_SHORT).show();

			}
			super.handleMessage(msg);
		}
	};

	// 跳转到升级
	private void goUpdate() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(R.string.is_need_update);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				downLoadApk();
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// 下载APK
	protected void downLoadApk() {
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		pd.show();

		new Thread() {
			@Override
			public void run() {
				try {
					File file = UpdateHelper.getFileFromServer(appUrl, pd);
					sleep(3000);
					installApk(file);
					pd.dismiss();
				} catch (Exception e) {
					Message msg = new Message();
					msg.what = DOWN_ERROR;
					handler.sendMessage(msg);
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void installApk(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
		ExitManager.getInstance().exit();
	}
	
	
	
	private OnClickListener itemsOnClick = new OnClickListener() {

		public void onClick(View v) {
			share.dismiss();
			switch (v.getId()) {
			case R.id.btn_pyq: 
				sendToWx(0);		
				break;
			case R.id.btn_wx: 
				sendToWx(1);			
				break;
			default:
				break;
			}
		}

	};
	
	private void sendToWx(int type){		
		WXWebpageObject webpage = new WXWebpageObject();
	    webpage.webpageUrl = "http://m.1018.com.cn/h";
	    WXMediaMessage msg = new WXMediaMessage(webpage);
	    msg.title = "下载1018车生活客户端,优享汽车生活;汽车代驾、救援哪家强？1018车生活。";
	    msg.description = "汽车代驾、救援哪家强？1018车生活";
	    try
	    {
	      Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.code);
	      Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
	      bmp.recycle();
	      msg.setThumbImage(thumbBmp);
	    } 
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    SendMessageToWX.Req req = new SendMessageToWX.Req();
	    req.transaction = String.valueOf(System.currentTimeMillis());
	    req.message = msg;
	    req.scene = type==0 ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
	    wxapi.sendReq(req);
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
}
