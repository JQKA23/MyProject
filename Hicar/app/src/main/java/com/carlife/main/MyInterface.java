package com.carlife.main;

public class MyInterface {
	// 刷新主页面
	public interface RefreshMainUIListener {
		public void setFragment(int type);
	}

	public interface UpdateChannelIdListener {
		public void updateChannelId();
	}

}
