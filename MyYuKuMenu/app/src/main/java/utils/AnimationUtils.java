package utils;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2017/1/22.
 */
public class AnimationUtils {
    public static int animationingCount=0;//正在进行中的动画数量
    public static void startRotateOutAnimation(RelativeLayout layout,int offset){
        //将布局中所有子控件都设置为不可用状态，防止被点击
        for (int i = 0; i < layout.getChildCount(); i++) {
            layout.getChildAt(i).setClickable(false);
        }
        RotateAnimation animation=new RotateAnimation(0,-180, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,1);
        animation.setDuration(500);
        animation. setFillAfter(true);//设置动画执行完毕后，停留在结束状态
        animation.setStartOffset(offset);
        animation.setAnimationListener(new myAnimationListener());
        layout.startAnimation(animation);
    }
    public static void startRotateInAnimation(RelativeLayout layout,int offset){
        //将布局中所有子控件都设置为可用状态
        for (int i = 0; i < layout.getChildCount(); i++) {
            layout.getChildAt(i).setClickable(true);
        }

        RotateAnimation animation=new RotateAnimation(-180,0, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,1);
        animation.setDuration(500);
        animation. setFillAfter(true);//设置动画执行完毕后，停留在结束状态
        animation.setStartOffset(offset);//延迟时间
        animation.setAnimationListener(new myAnimationListener());
        layout.startAnimation(animation);
    }

    static class myAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {
            animationingCount++;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            animationingCount--;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
