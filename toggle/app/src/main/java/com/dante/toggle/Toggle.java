package com.dante.toggle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/1/23.
 */
public class Toggle extends View {

    private Bitmap slideButtonBackground;//滑动块
    private Bitmap switchBackground;//背景图片
    private boolean toggleState=false;//开关状态

    private int currentX;
    private boolean isSliding;
    private OnToggleStateChangedLisener lisener;

    public Toggle(Context context) {
        super(context);

    }

    public Toggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        String namespace="http://schemas.android.com/apk/res-auto";
        setSwitchBackgroundResource(attrs.getAttributeResourceValue(
                        namespace, "switchBackground", R.mipmap.ic_launcher));
        setSlideButtonBackgroundResource(attrs.getAttributeResourceValue(
                namespace, "slideButtonBackground", R.mipmap.ic_launcher));
        toggleState=attrs.getAttributeBooleanValue(namespace,"toggleState",false);
    }

    public Toggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setSwitchBackgroundResource ( int switchBackgroundResource){
        this.switchBackground = BitmapFactory.decodeResource(getResources(),switchBackgroundResource);
    }

    public void setSlideButtonBackgroundResource(int slideButtonBackgroundResource) {
        this.slideButtonBackground = BitmapFactory.decodeResource(getResources(),slideButtonBackgroundResource);
    }


    //设置开关状态
    public void setToggleState(boolean toggleState) {
        this.toggleState = toggleState;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //该方法为空方法，需完全自己重写

        //把背景图片画到控件上
        canvas.drawBitmap(switchBackground,0,0,null);//画图片无需画笔
        //把滑动块画到控件上
        if (isSliding){
            int left=currentX-slideButtonBackground.getWidth()/2;
            int leftEdige=switchBackground.getWidth() - slideButtonBackground.getWidth();
            if (left<0){
                left=0;
            }else if (left>leftEdige){
                left=leftEdige;
            }
            canvas.drawBitmap(slideButtonBackground,left,0,null);
        }else{
            if (toggleState){
                canvas.drawBitmap(slideButtonBackground,
                        switchBackground.getWidth() - slideButtonBackground.getWidth(),0,null);
            }else {
                canvas.drawBitmap(slideButtonBackground,0,0,null);
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 该方法最调用终setMeasuredDimension（）方法

        //设置该控件的宽高为背景图片的宽高
        setMeasuredDimension(switchBackground.getWidth(),switchBackground.getHeight());
    }

    public void setOnToggleStateChangedLisener(OnToggleStateChangedLisener lisener) {
        this.lisener=lisener;
    }

    public interface OnToggleStateChangedLisener{
        public void onToggleStateChanged(boolean state);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                isSliding=true;
                currentX= ((int) event.getX());
                break;
            case MotionEvent.ACTION_DOWN:
                currentX= ((int) event.getX());
                break;
            case MotionEvent.ACTION_UP:
                isSliding=false;
                currentX= ((int) event.getX());
                int center= switchBackground.getWidth()/2;
                boolean state=currentX>center;
                if (lisener!=null&&state!=toggleState){
                    lisener.onToggleStateChanged(state);
                }
                toggleState=state;
                break;
        }
        invalidate();//此方法用于刷新当前控件,同时会触发onDraw（）的调用
        return true;
    }
}
