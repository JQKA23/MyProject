package com.dante.mycustomviewgroup;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MyRefreshListView mlv;

    private List<String> list;

    private void assignViews() {
        mlv = (MyRefreshListView) findViewById(R.id.mlv);
//        mlv.addHeaderView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();

        list=new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            list.add("数据"+i);
        }
        final MyAdapter adapter=new MyAdapter();
        mlv.setAdapter(adapter);
        mlv.setOnRefreshListener(new MyRefreshListView.OnRefreshListener() {
                                     @Override
                                     public void onPullDownRefresh() {
                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 SystemClock.sleep(3000);
                                                 list.add(0,"111111111");
                                                 runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         adapter.notifyDataSetChanged();
                                                         mlv.refreshFinished();
                                                     }
                                                 });
                                             }
                                         }).start();
                                     }

                                     @Override
                                     public void onLoadingMore() {
                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 SystemClock.sleep(3000);
                                                 list.add("2222222");
                                                 runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {
                                                         adapter.notifyDataSetChanged();
                                                         mlv.refreshFinished();
                                                     }
                                                 });
                                             }
                                         }).start();
                                     }
                                 }
        );
    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView==null){
                view=new TextView(MainActivity.this);
            }else{
                view= ((TextView) convertView);
            }
            view.setText(list.get(position));
            view.setTextColor(Color.BLUE);
            view.setTextSize(18);
            return view;
        }
    }
}
