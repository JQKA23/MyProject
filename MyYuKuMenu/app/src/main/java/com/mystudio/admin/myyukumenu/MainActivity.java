package com.mystudio.admin.myyukumenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import utils.AnimationUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private RelativeLayout rlLevel1;
    private ImageButton ibHome;
    private RelativeLayout rlLevel2;
    private ImageButton ibMenu;
    private RelativeLayout rlLevel3;

    private boolean isDisplayLevel3=true;
    private boolean isDisplayLevel2=true;
    private boolean isDisplayLevel1=true;

    private void assignViews() {
        rlLevel1 = (RelativeLayout) findViewById(R.id.rl_level1);
        ibHome = (ImageButton) findViewById(R.id.ib_home);
        rlLevel2 = (RelativeLayout) findViewById(R.id.rl_level2);
        ibMenu = (ImageButton) findViewById(R.id.ib_menu);
        rlLevel3 = (RelativeLayout) findViewById(R.id.rl_level3);
        ibHome.setOnClickListener(this);
        ibMenu.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_home:
                //如果动画正在执行中，不再执行以下动画
                if (AnimationUtils.animationingCount != 0) {
                    return;
                }
                if (isDisplayLevel2){
                    if (isDisplayLevel3){
                        AnimationUtils.startRotateOutAnimation(rlLevel3,0);
                        isDisplayLevel3=false;
                    }
                    AnimationUtils.startRotateOutAnimation(rlLevel2,200);
                }else {
                    AnimationUtils.startRotateInAnimation(rlLevel2,0);
                }
                isDisplayLevel2=!isDisplayLevel2;
                break;
            case R.id.ib_menu:
                //如果动画正在执行中，不再执行以下动画
                if (AnimationUtils.animationingCount != 0) {
                    return;
                }
                if (isDisplayLevel3){
                    AnimationUtils.startRotateOutAnimation(rlLevel3,0);
                }else {
                    AnimationUtils.startRotateInAnimation(rlLevel3,0);
                }
                isDisplayLevel3=!isDisplayLevel3;
                break;
        }
    }
}
