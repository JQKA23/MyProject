package com.carlife.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.carlife.R;
import com.carlife.baidupush.Utils;
import com.carlife.global.Const;
import com.carlife.main.MyInterface.RefreshMainUIListener;

public class MainActivity extends Activity implements OnClickListener,
		RefreshMainUIListener {
	private TextView tv_homePage, tv_order, tv_mine, tv_more;
	private Fragment fragment;
	private FragmentManager fraManager;
	private FragmentTransaction transaction;
	private String mobile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 以下是百度推送初始化
		Utils.logStringCache = Utils.getLogText(getApplicationContext());
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, "G4wKSIeGar0olIoH7vZjpHF2");
		Resources resource = this.getResources();
		String pkgName = this.getPackageName();
		CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
				resource.getIdentifier("notification_custom_builder", "layout",
						pkgName), resource.getIdentifier("notification_icon",
						"id", pkgName), resource.getIdentifier(
						"notification_title", "id", pkgName),
				resource.getIdentifier("notification_text", "id", pkgName));
		cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
		cBuilder.setNotificationDefaults(Notification.DEFAULT_VIBRATE);
		cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
		cBuilder.setLayoutDrawable(resource.getIdentifier(
				"simple_notification_icon", "drawable", pkgName));
		cBuilder.setNotificationSound(Uri.withAppendedPath(
				Audio.Media.INTERNAL_CONTENT_URI, "6").toString());
		// 推送高级设置，通知栏样式设置为下面的ID
		PushManager.setNotificationBuilder(this, 1, cBuilder);
		// 百度推送初始化完成
		initView();
		setFragment(1);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_homePage:
			setFragment(1);
			break;
		case R.id.tv_order:
			setFragment(2);
			break;
		case R.id.tv_mine:
			setFragment(3);
			break;
		case R.id.tv_more:
			setFragment(4);
			break;
		}
	}

	private void initView() {
		tv_homePage = (TextView) findViewById(R.id.tv_homePage);
		tv_homePage.setOnClickListener(this);
		tv_order = (TextView) findViewById(R.id.tv_order);
		tv_order.setOnClickListener(this);
		tv_mine = (TextView) findViewById(R.id.tv_mine);
		tv_mine.setOnClickListener(this);
		tv_more = (TextView) findViewById(R.id.tv_more);
		tv_more.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	private void changeTextDrawableToBlue(TextView tv, int type) {
		Drawable top = null;
		if (type == 1) {
			top = getResources().getDrawable(R.drawable.home_blue);
		} else if (type == 2) {
			top = getResources().getDrawable(R.drawable.order_blue);
		} else if (type == 3) {
			top = getResources().getDrawable(R.drawable.mine_blue);
		} else {
			top = getResources().getDrawable(R.drawable.more_blue);
		}
		top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
		tv.setCompoundDrawables(null, top, null, null);
		tv.setTextColor(getResources().getColor(R.color.homeblue));
		tv.setEnabled(false);
	}

	@SuppressWarnings("deprecation")
	private void changeTextDrawableToGray(TextView tv, int type) {
		Drawable top = null;
		if (type == 1) {
			top = getResources().getDrawable(R.drawable.home_gray);
		} else if (type == 2) {
			top = getResources().getDrawable(R.drawable.order_gray);
		} else if (type == 3) {
			top = getResources().getDrawable(R.drawable.mine_gray);
		} else {
			top = getResources().getDrawable(R.drawable.more_gray);
		}
		top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
		tv.setCompoundDrawables(null, top, null, null);
		tv.setTextColor(getResources().getColor(R.color.light_black));
		tv.setEnabled(true);
	}

	@Override
	public void setFragment(int button) {
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,
				Context.MODE_PRIVATE);
		mobile = sp.getString(Const.BindMobile, "");
		fraManager = getFragmentManager();
		transaction = fraManager.beginTransaction();
		if (button == 1) {
			fragment = new Fragment_home();
			transaction.replace(R.id.fragment, fragment);
			changeTextDrawableToBlue(tv_homePage, 1);
			changeTextDrawableToGray(tv_order, 2);
			changeTextDrawableToGray(tv_mine, 3);
			changeTextDrawableToGray(tv_more, 4);
			transaction.commitAllowingStateLoss();
		} else if (button == 2) {
			fragment = new Fragment_order();
			transaction.replace(R.id.fragment, fragment);
			changeTextDrawableToBlue(tv_order, 2);
			changeTextDrawableToGray(tv_homePage, 1);
			changeTextDrawableToGray(tv_mine, 3);
			changeTextDrawableToGray(tv_more, 4);
			transaction.commitAllowingStateLoss();
		} else if (button == 3) {
			if (mobile.equals("")) {
				Intent it = new Intent();
				it.setClass(this, BindMobileActivity.class);
				startActivity(it);
			} else {
				fragment = new Fragment_mine();
				transaction.replace(R.id.fragment, fragment);
				changeTextDrawableToBlue(tv_mine, 3);
				changeTextDrawableToGray(tv_homePage, 1);
				changeTextDrawableToGray(tv_order, 2);
				changeTextDrawableToGray(tv_more, 4);
				transaction.commitAllowingStateLoss();
			}
		} else if (button == 4) {
			fragment = new Fragment_more();
			transaction.replace(R.id.fragment, fragment);
			changeTextDrawableToBlue(tv_more, 4);
			changeTextDrawableToGray(tv_homePage, 1);
			changeTextDrawableToGray(tv_order, 2);
			changeTextDrawableToGray(tv_mine, 3);
			transaction.commitAllowingStateLoss();
		}
	}

}
