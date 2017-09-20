package com.carlife.daijia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.carlife.R;
import com.carlife.global.Const;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChangeOrderMobileActivity extends Activity implements
		OnClickListener {

	private Button btn_back, btn_import,btn_save;
	private static final int Pick_Contact = 1;
	private EditText et_mobile;
	private String username,usernumber; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeordermobile);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		btn_import = (Button) findViewById(R.id.btn_import);
		btn_import.setOnClickListener(this);
		btn_save=(Button)findViewById(R.id.btn_save);
		btn_save.setOnClickListener(this);
		et_mobile=(EditText)findViewById(R.id.et_mobile);
		SharedPreferences sp = getSharedPreferences(Const.spBindMobile,	Context.MODE_PRIVATE);
		usernumber=sp.getString(Const.BindMobile, "");
		et_mobile.setText(usernumber);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			this.finish();
			break;
		case R.id.btn_import:
			startActivityForResult(new Intent(  
	                 Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI), 0);
			break;
		case R.id.btn_save:						
			SharedPreferences sp = getSharedPreferences(Const.spCallMobile,MODE_PRIVATE);
			sp.edit().putString(Const.CallMobile, et_mobile.getText().toString()).commit();
            finish(); 			
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (resultCode == Activity.RESULT_OK) { 
	//ContentProvider展示数据类似一个单个数据库表
	//ContentResolver实例带的方法可实现找到指定的ContentProvider并获取到ContentProvider的数据
	            ContentResolver reContentResolverol = getContentResolver();  
	            //URI,每个ContentProvider定义一个唯一的公开的URI,用于指定到它的数据集
	            Uri contactData = data.getData();
	            //查询就是输入URI等参数,其中URI是必须的,其他是可选的,如果系统能找到URI对应的ContentProvider将返回一个Cursor对象.
	            Cursor cursor = managedQuery(contactData, null, null, null, null);  
	            cursor.moveToFirst(); 
	            //获得DATA表中的名字
	            username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));  
	            //条件为联系人ID
	            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));  
	            // 获得DATA表中的电话号码，条件为联系人ID,因为手机号码可能会有多个
	            Cursor phone = reContentResolverol.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,   
	                     null,   
	                     ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,   
	                     null,   
	                     null);  
	             while (phone.moveToNext()) {  
	                 usernumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));  
	                 et_mobile.setText(usernumber);  
	             }  
	  
	        }  
	}

}
