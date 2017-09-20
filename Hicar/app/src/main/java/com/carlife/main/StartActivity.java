package com.carlife.main;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.utility.CustomDialog;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.ExitManager;
import com.carlife.utility.NetHelper;
import com.carlife.utility.UpdateHelper;
import com.carlife.utility.Utili;

public class StartActivity extends Activity {
	boolean isFirstIn = false;
	private static final int GO_MAIN = 1000;
	private static final int GO_GUIDE = 1001;
	private static final int GO_UPDATE = 1002;
	private static final int DOWN_ERROR = 1003;

	private FinalHttp fh = new FinalHttp();
	private String appUrl = "";
	private int versionCode = 0;
	private ProgressDialog pd;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		context = this;
		if (NetHelper.isNetworkAvailable(this)) {
			versionCode = UpdateHelper.getVerCode(this);
			CheckUpdate();// 检查新版本
		} else {
			// 没网络
			Utili.ToastInfo(context, "没有可用的网络");
			finish();
		}
	}

	// 检查升级
	@SuppressLint("DefaultLocale")
	private void CheckUpdate() {
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();

		params.put("sign", strTemp);

		fh.post(Const.API_SERVICES_ADDRESS + "/GetNewEdition", params,
				new AjaxCallBack<Object>() {
					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						super.onFailure(t, errorNo, strMsg);
						init();
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
						removeDialog(0);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = Utili.GetJson("" + t);
						try {

							JSONObject obj = new JSONObject(jsonMessage);
							int newVersionCode = Integer.parseInt(obj
									.getString("VersionCode"));
							appUrl = obj.getString("AppUrl");

							if (newVersionCode > versionCode) {
								Message msg = new Message();
								msg.what = GO_UPDATE;
								mHandler.sendMessage(msg);
							} else {
								init();
							}

						} catch (JSONException e) {
							System.out.print(e);
							init();
						}
					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

	// 初始化
	private void init() {

		SharedPreferences preferences = getSharedPreferences(Const.spFirstPref,
				MODE_PRIVATE);
		// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
		isFirstIn = preferences.getBoolean(Const.isFirstIn, true);

		// 判断程序与第几次运行，如果是第一次运行则跳转到引导界面，否则跳转到主界面
		if (!isFirstIn) {
			// 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
			mHandler.sendEmptyMessageDelayed(GO_MAIN, Const.startDelay);
		} else {
			mHandler.sendEmptyMessageDelayed(GO_GUIDE, Const.startDelay);
		}

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_MAIN:
				goMain();
				break;
			case GO_GUIDE:
				goGuide();
				break;
			case GO_UPDATE:
				goUpdate();
				break;
			case DOWN_ERROR:
				Toast.makeText(StartActivity.this, "下载新版本失败",
						Toast.LENGTH_SHORT).show();
				init();
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 跳转至主页面
	private void goMain() {
		Intent intent = new Intent(StartActivity.this, MainActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}

	// 跳转到引导页
	private void goGuide() {
		Intent intent = new Intent(StartActivity.this, GuideActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}

	// 跳转到升级
	private void goUpdate() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(R.string.is_need_update);
		builder.setTitle("提示");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				downLoadApk();
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						init();
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	// 下载APK
	protected void downLoadApk() {
		pd = new ProgressDialog(this);
		pd.setCanceledOnTouchOutside(false);
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
					mHandler.sendMessage(msg);
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

}
