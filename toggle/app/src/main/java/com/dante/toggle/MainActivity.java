package com.dante.toggle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Toggle mToggle;

    private void assignViews() {
        mToggle = (Toggle) findViewById(R.id.mToggle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        //在代码中设置自定义控件属性
//        mToggle.setSwitchBackgroundResource(R.mipmap.switch_background);
//        mToggle.setSlideButtonBackgroundResource(R.mipmap.slide_button_background);
//        mToggle.setToggleState(false);
        mToggle.setOnToggleStateChangedLisener(new Toggle.OnToggleStateChangedLisener() {
            @Override
            public void onToggleStateChanged(boolean state) {
                if (state){
                    Toast.makeText(MainActivity.this,"开关打开",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"开关关闭",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
