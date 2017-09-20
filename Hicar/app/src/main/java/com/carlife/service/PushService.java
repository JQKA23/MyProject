package com.carlife.service;

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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.MainActivity;
import com.carlife.model.PushMessage;
import com.carlife.utility.EncodeUtility;
import com.carlife.utility.NetHelper;

public class PushService extends Service {

	public static final String ACTION = "com.carlife.service.PushService";

	private Context context;

	private final static int pushService = 0;
	private final static int getMessage_fail = 1;
	private final static int getMessage_success = 2;

	private List<PushMessage> list;

	@Override
	public void onCreate() {
		context = this;
	}

	@Override
	public void onStart(Intent intent, int startid) {
		SharedPreferences sp = getSharedPreferences("spPushService",
				MODE_PRIVATE);
		int isOn = sp.getInt("IsOn", 0);
		if (isOn == 1) {
			boolean isNetWorkAvailable = NetHelper.isNetworkAvailable(context);
			if (isNetWorkAvailable) {
				handler.sendEmptyMessage(pushService);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case pushService:
				getLastPushMessageIds();
				break;
			case getMessage_success:
				if (list.size() > 0) {
					SharedPreferences sp = getSharedPreferences(
							Const.spPushMessageList, MODE_PRIVATE);
					String messageId = sp.getString(Const.PushMessage, "0");
					Log.i("messageId============", messageId);
					String[] ids = messageId.split("\\,");
					int localLastId = Integer.parseInt(ids[ids.length - 1]);

					String newMessageId = "";
					for (int i = list.size() - 1; i >= 0; i--) {
						if (list.get(i).getId() > localLastId) {
							newMessageId += list.get(i).getId() + ",";
							showNotification(list.get(i));
						}
					}
					if (newMessageId.endsWith(",")) {
						newMessageId = newMessageId.substring(0,
								newMessageId.length() - 1);
						sp.edit().putString(Const.PushMessage, newMessageId)
								.commit();
					}

				}
				break;
			}
		}
	};

	private void showNotification(PushMessage message) {
		MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(),
				R.raw.crystal);
		mPlayer.start();
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(
				getApplicationContext());
		builder.setSmallIcon(R.drawable.icon)
				.setContentTitle(message.getTitle())
				.setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_VIBRATE);

		// Notification notification = new Notification(R.drawable.icon,
		// message.getTitle(), System.currentTimeMillis());
		// notification.defaults = Notification.DEFAULT_VIBRATE;
		CharSequence contentTitle = message.getTitle(); // 通知栏标题
		CharSequence contentText = message.getContent();

		// 通知栏内容
		Intent notificationIntent = new Intent(PushService.this,
				MainActivity.class); // 点击该通知后要跳转的Activity
		PendingIntent contentItent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		Notification notification = builder.setContentTitle(contentTitle).setContentText(contentText).setContentIntent(contentItent).build();

		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.ledARGB = Color.BLUE;
		notification.ledOnMS = 5000; // 闪光时间，毫秒
		notificationManager.notify(0, notification);
	}

	private void getLastPushMessageIds() {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				MODE_PRIVATE);
		String consumerMobile = sp.getString(Const.BindMobile, ""); // 获取客户电话
		AjaxParams params = new AjaxParams();
		params.put(Const.APPKEY, Const.APPKEY_STR);
		params.put("mobile", consumerMobile);

		Map<String, String> map = new HashMap<String, String>();
		map.put(Const.APPKEY, Const.APPKEY_STR);
		map.put("mobile", consumerMobile);
		String strTemp = String.format("%s%s%s", Const.APPSECRET_STR,
				EncodeUtility.JoinUtil(map), Const.APPSECRET_STR);
		strTemp = EncodeUtility.md5(strTemp).toLowerCase();
		params.put("sign", strTemp);
		FinalHttp fh = new FinalHttp();
		fh.post(Const.API_SERVICES_ADDRESS + "/GetLastPushMessages", params,
				new AjaxCallBack<Object>() {

					@Override
					public void onFailure(Throwable t, int errorNo,
							String strMsg) {
						handler.sendEmptyMessage(getMessage_fail);
						super.onFailure(t, errorNo, strMsg);
					}

					@Override
					public void onLoading(long count, long current) {
						super.onLoading(count, current);
					}

					@Override
					public void onSuccess(Object t) {
						super.onSuccess(t);
						String jsonMessage = "" + t;
						if (jsonMessage != null && !jsonMessage.equals("")) {
							jsonMessage = jsonMessage.substring(1);
							jsonMessage = jsonMessage.substring(0,
									jsonMessage.length() - 1);
							jsonMessage = jsonMessage.replace("\\", "");

							if (jsonMessage.contains("IsError")) {
								handler.sendEmptyMessage(getMessage_fail);
							} else {
								try {
									JSONArray jsonArray = new JSONArray(
											jsonMessage);
									list = new ArrayList<PushMessage>();
									for (int i = 0; i < jsonArray.length(); i++) {
										JSONObject obj = (JSONObject) jsonArray
												.opt(i);
										PushMessage m = new PushMessage();
										m.setContent(obj.getString("Content"));
										m.setId(obj.getInt("Id"));
										list.add(m);
									}
									handler.sendEmptyMessage(getMessage_success);
								} catch (JSONException e) {
									handler.sendEmptyMessage(getMessage_fail);
									e.printStackTrace();
								}

							}

						}

					}

					@Override
					public AjaxCallBack<Object> progress(boolean progress,
							int rate) {
						return super.progress(progress, rate);
					}

				});
	}

}
