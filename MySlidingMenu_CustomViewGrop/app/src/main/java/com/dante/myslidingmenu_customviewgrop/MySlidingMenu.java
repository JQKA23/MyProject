package com.dante.myslidingmenu_customviewgrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Administrator on 2017/2/20.
 */
public class MySlidingMenu extends ViewGroup {
    private int downX;
    private int downY;
    private int moveX;
    private int moveY;
    private int diffentX;
    private int diffentY;
    private Scroller mScroller;//用于界面缓慢移动的模拟动画
    private boolean isLeftMenu=false;
    public MySlidingMenu(Context context) {
        super(context);
        mScroller=new Scroller(context);
    }

    public MySlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller=new Scroller(context);
    }

    public MySlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller=new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View leftMenu=getChildAt(0);
        leftMenu.measure(leftMenu.getLayoutParams().width, heightMeasureSpec);
        View rightMenu=getChildAt(1);
        rightMenu.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View leftMenu=getChildAt(0);
        leftMenu.layout(-leftMenu.getMeasuredWidth(), 0, 0, b);

        View rightMenu=getChildAt(1);
        rightMenu.layout(l, t, r, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        scrollBy(x,y);将屏幕移动x的距离
//        scrollTo(x,y);将屏幕移动到x的位置，
//        两个方法都是移动屏幕，因此在使用时应该使用-偏移量
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX= ((int) event.getX());
                break;
            case MotionEvent.ACTION_MOVE:
                moveX= ((int) event.getX());
                diffentX=moveX-downX;
//                getScrollX()获取当前x值
                int currentX=getScrollX()-diffentX;
                int leftMentWidth=getChildAt(0).getMeasuredWidth();
                if (currentX<-leftMentWidth){
                    scrollTo(-leftMentWidth,0);
                }else if (currentX>0){
                    scrollTo(0,0);
                }else {
                    downX=moveX;
                }
                break;
            case MotionEvent.ACTION_UP:
                currentX=getScrollX();
                leftMentWidth=getChildAt(0).getMeasuredWidth();
                if (currentX<-leftMentWidth/2){
                    isLeftMenu=true;
                }else {
                    isLeftMenu=false;
                }
                switchScreen();
                break;
        }
        return true;//因为父类无事件处理，返回true，自己处理事件
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX= ((int) ev.getX());
                downY= ((int) ev.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                moveX= ((int) ev.getX());
                moveY= ((int) ev.getY());

                diffentX= Math.abs(downX-moveX);
                diffentY= Math.abs(downY-moveY);
                if (diffentX>diffentY){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void switchScreen(){
        int currentX= ((int) getScaleX());
        int dx;
        if (isLeftMenu){
            dx=getChildAt(0).getMeasuredWidth()-currentX;
        }else {
            dx=0-currentX;
        }
        mScroller.startScroll(currentX,0,dx,0,Math.abs(dx)*5);
        //刷新界面
        invalidate();//-->drawChild()-->child.draw()-->computeScroll()
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){//true,正在模拟
            int currentX=mScroller.getCurrX();
            scrollTo(currentX,0);
            invalidate();//递归
        }
    }

    public boolean isLeftMenuShow(){
        return isLeftMenu;
    }

    public void showLeftMenu() {
        isLeftMenu=true;
        switchScreen();
    }

    public void closeLeftMenu() {
        isLeftMenu=false;
        switchScreen();
    }
}
