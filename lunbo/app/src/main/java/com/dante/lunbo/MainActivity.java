package com.dante.lunbo;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager vp;
    private TextView tvName;
    private LinearLayout llPoint;

    private List<ImageView>list;
    private String[] names;
    private int previousPosition;
    private boolean isLoop;

    private void assignViews() {
        vp = (ViewPager) findViewById(R.id.vp);
        tvName = (TextView) findViewById(R.id.tv_name);
        llPoint = (LinearLayout) findViewById(R.id.ll_point);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();
        vp.setAdapter(new MyPagerAdapter());

        previousPosition=0;
        llPoint.getChildAt(0).setEnabled(true);
        tvName.setText(names[0]);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int newPosition=position%list.size();
                llPoint.getChildAt(newPosition).setEnabled(true);
                llPoint.getChildAt(previousPosition).setEnabled(false);
                previousPosition=newPosition;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        int item=Integer.MAX_VALUE/2-(Integer.MAX_VALUE/2)%list.size();
        vp.setCurrentItem(item);

        //自动循环
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLoop){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vp.setCurrentItem(vp.getCurrentItem()+1);
                        }
                    });
                    SystemClock.sleep(3000);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLoop=false;
    }

    private void initData(){
        int []arrPoint={R.mipmap.a,R.mipmap.b,R.mipmap.c,R.mipmap.d,R.mipmap.e};
        names=new String[]{"aaaaaaaa","bbbbbbbbbbbb","ccccccccccc","dddddddddd","eeeeeeeeee"};
        list=new ArrayList<ImageView>();
        ImageView iv;
        View view;
        LinearLayout.LayoutParams params;
        for (int i = 0; i < arrPoint.length; i++) {
            iv=new ImageView(this);
            iv.setBackgroundResource(arrPoint[i]);
            list.add(iv);

            view=new View(this);
            view.setTag(i);
            view.setBackgroundResource(R.drawable.selecter_pointstate);
            params=new LinearLayout.LayoutParams(15,15);
            if (i!=0){
                params.leftMargin=10;
            }
            view.setLayoutParams(params);
            view.setEnabled(false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vp.setCurrentItem(((int) v.getTag()));
                }
            });
            llPoint.addView(view);
        }
    }
    class  MyPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {

            return Integer.MAX_VALUE;//防止滑动到头后无法继续滑动
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv=list.get(position%list.size());
            container.addView(iv);
            return iv;
        }
    }
}
