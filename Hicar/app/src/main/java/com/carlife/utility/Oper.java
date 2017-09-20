package com.carlife.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.http.AjaxCallBack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


public class Oper {

	private Context context;

	public Oper(Context context){
		this.context = context;
	}

	public void dispatchTouch(MotionEvent ev, View v) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {						
			if (isShouldHideInput(v, ev)) {
				hideSoftInput(v.getWindowToken());
			}
		}
	}	
	
	
	private  boolean isShouldHideInput(View v, MotionEvent event) {
		if (v != null && (v instanceof EditText)) {
			int[] l = { 0, 0 };
			v.getLocationInWindow(l);
			int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
					+ v.getWidth();
			if (event.getX() > left && event.getX() < right
					&& event.getY() > top && event.getY() < bottom) {			
				return false;
			} else {
				return true;
			}
		}		
		return false;
	}


	private void hideSoftInput(IBinder token) {
		if (token != null) {
			InputMethodManager im = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
			im.hideSoftInputFromWindow(token,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}	

}
