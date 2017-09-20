package com.dante.mycustomviewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dante on 2017/1/25.
 */
public class MyRefreshListView extends ListView {
    private int downY;
    private View mHeadView;
    private View mFooterView;
    private int height;
    private int mFooterViewHeight;
    private final int pullDown=0;
    private final int releaseRefresh=1;
    private final int refreshing=2;
    private int currentState=pullDown;
    private ImageView ivArrow;
    private ProgressBar pb;
    private TextView tvState;
    private TextView tvTime;
    private RotateAnimation upAnimation;
    private RotateAnimation downAnimation;
    private OnRefreshListener listener;
    private boolean isLoadingMore=false;

    public MyRefreshListView(Context context) {
        super(context);
        initHeadView();
    }

    public MyRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeadView();
    }

    public MyRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeadView();
    }
    private void initHeadView(){

        mHeadView=View.inflate(getContext(),R.layout.listviewhead_myrefresh,null);
        ivArrow = (ImageView) mHeadView.findViewById(R.id.iv_arrow);
        pb = (ProgressBar) mHeadView.findViewById(R.id.pb);
        tvState = (TextView) mHeadView.findViewById(R.id.tv_state);
        tvTime = (TextView) mHeadView.findViewById(R.id.tv_time);
        tvTime.setText("最后刷新时间:"+getCurrentTime());

//        view.getMeasuredHeight()该方法只有在当前控件已经测量过（onmeasure()）才可以得到值
//        view.getHeight()   该方法只有在当前控件已经在屏幕上显示过才可以得到值
        mHeadView.measure(0, 0);//测量控件
        height=mHeadView.getMeasuredHeight();
        mHeadView.setPadding(0, -height, 0, 0);
        this.addHeaderView(mHeadView);

        initAnimation();
    }

    private void initFooterView(){
        mFooterView=View.inflate(getContext(), R.layout.footer_mylistview, null);
        mFooterView.measure(0,0);
        mFooterViewHeight=mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0,-mFooterViewHeight,0,0);
        this.addFooterView(mFooterView);
        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isLoadingMore){
                    return;
                }
                //如果滑动状态为停止或惯性滑动
                if (scrollState==OnScrollListener.SCROLL_STATE_IDLE
                        ||scrollState==OnScrollListener.SCROLL_STATE_FLING ){
                    if (getLastVisiblePosition()==(getCount()-1)){
                        isLoadingMore=true;
                        mFooterView.setPadding(0,0,0,0);
                        setSelection(getCount());//让listview滚到最低部
                        listener.onLoadingMore();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }
    private void initAnimation() {
        upAnimation=new RotateAnimation(0,-180,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);

        downAnimation=new RotateAnimation(-180,-360,
                Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_MOVE:
                if (currentState==refreshing){
                    break;
                }
                int moveY= ((int) ev.getY());
                int diffrent=moveY-downY;
                //偏移量>0并且当前屏幕第一个条目索引为0
                if (this.getFirstVisiblePosition()==0&&diffrent>0){
                    int paddingTop=-height+diffrent;
                    mHeadView.setPadding(0,paddingTop,0,0);
                    if (paddingTop>0&&currentState!=releaseRefresh){
                        currentState=releaseRefresh;
                        refreshHeadViewState();
                    }else if (paddingTop<0&&currentState!=pullDown){
                        currentState=pullDown;
                        refreshHeadViewState();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                downY= ((int) ev.getY());
                break;
            case MotionEvent.ACTION_UP:
                if (currentState==pullDown){
                    mHeadView.setPadding(0,-height,0,0);
                }else if (currentState==releaseRefresh){
                    currentState=refreshing;
                    refreshHeadViewState();
                    mHeadView.setPadding(0, 0, 0, 0);
                    if (listener!=null){
                        listener.onPullDownRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void refreshHeadViewState() {
        switch (currentState){
            case pullDown:
                ivArrow.startAnimation(downAnimation);
                tvState.setText("下拉刷新");
                break;
            case releaseRefresh:
                ivArrow.startAnimation(upAnimation);
                tvState.setText("释放刷新");
                break;
            case refreshing:
                ivArrow.clearAnimation();//清除自身动画
                ivArrow.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
                tvState.setText("正在刷新");
                break;
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener){
        this.listener=listener;
    }
    public interface OnRefreshListener{
        public void onPullDownRefresh();
        public void onLoadingMore();
    }

    public void refreshFinished(){
        if (isLoadingMore){
            mFooterView.setPadding(0,-mFooterViewHeight,0,0);
            isLoadingMore=false;
        }else {
            mHeadView.setPadding(0,-height,0,0);
            currentState=pullDown;
            ivArrow.setVisibility(View.VISIBLE);
            tvState.setText("下拉刷新");
            pb.setVisibility(View.INVISIBLE);
            tvTime.setText("最后刷新时间:"+getCurrentTime());
        }
    }

    public String getCurrentTime(){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str=format.format(new Date());
        return str;

    }
}
