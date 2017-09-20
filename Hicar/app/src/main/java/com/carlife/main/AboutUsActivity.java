package com.carlife.main;


import com.carlife.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus);
		Button btn_back=(Button)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(this);
		
		TextView tv = (TextView) findViewById(R.id.content_tv);
		tv.setText(R.string.aboutus);
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
            this.finish();
			break;
		}
	
	}
}
