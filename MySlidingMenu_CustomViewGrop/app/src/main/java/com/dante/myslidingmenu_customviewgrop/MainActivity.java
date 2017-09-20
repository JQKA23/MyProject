package com.dante.myslidingmenu_customviewgrop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private MySlidingMenu mySlidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mySlidingMenu= ((MySlidingMenu) findViewById(R.id.mySlidingMenu));
    }

    public void toggleLeftMenu(View view){
        if (mySlidingMenu.isLeftMenuShow()){
            mySlidingMenu.closeLeftMenu();
        }else{
            mySlidingMenu.showLeftMenu();
        }
    }

    public void tabClick(View view){
        TextView tv= ((TextView) view);
        Toast.makeText(this,tv.getText(), Toast.LENGTH_SHORT);
    }
}
