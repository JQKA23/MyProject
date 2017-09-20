package com.carlife.adpater;

import java.util.List;

import com.carlife.R;
import com.carlife.global.Const;
import com.carlife.main.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;



public class GuideViewPagerAdapter extends PagerAdapter {

	private List<View> views;
	private Activity activity;

	public GuideViewPagerAdapter(List<View> views, Activity activity) {
		this.views = views;
		this.activity = activity;
	}

	
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(views.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	
	@Override
	public int getCount() {
		if (views != null) {
			return views.size();
		}
		return 0;
	}


	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(views.get(arg1), 0);
		if (arg1 == views.size() - 1) {
			ImageView startBtn = (ImageView) arg0
					.findViewById(R.id.iv_start);
			startBtn.setVisibility(View.VISIBLE);
			startBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
					setGuided();
					goHome();
				}

			});
		}
		return views.get(arg1);
	}

	private void goHome() {			
		Intent intent = new Intent(activity, MainActivity.class);
		activity.startActivity(intent);
		activity.finish();
	}

	private void setGuided() {
		SharedPreferences preferences = activity.getSharedPreferences(
				Const.spFirstPref, Context.MODE_PRIVATE);
		preferences.edit().putBoolean(Const.isFirstIn, false).commit();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

}
