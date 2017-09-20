package com.carlife.main;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.carlife.global.Const;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {
	public static boolean isChannelIdUpdate = false;
	public static String channelId = "";

	@Override
	public void onCreate() {
		super.onCreate();
		SDKInitializer.initialize(this);
		String appId = Const.BuglyAppID;
		boolean isDebug = false; // true代表App处于调试阶段，false代表App发布阶段
		CrashReport.initCrashReport(this, appId, !isDebug); // 初始化SDK
	}

}