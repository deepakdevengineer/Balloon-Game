package com.example.myapplication;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class Balloon extends AppCompatImageView implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator mAnimator;
    private BalloonListener mListener;
    private boolean mPopped;
    private int mBalloonColor;

    public Balloon(Context context) {
        super(context);
    }

    public Balloon(Context context, int color, int rawHeight) {
        super(context);

        mListener = (BalloonListener) context;

        this.setImageResource(R.drawable.balloon);
        this.setColorFilter(color);
        this.mBalloonColor = color;

        int rawWidth = rawHeight / 2;

        int dpHeight = PixelHelper.pixelsToDp(rawHeight, context);
        int dpWidth = PixelHelper.pixelsToDp(rawWidth, context);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(dpWidth, dpHeight);
        this.setLayoutParams(params);
    }

    public void releaseBalloon(int screenHeight, int duration) {

        mAnimator = new ValueAnimator();
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(screenHeight, 0f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setTarget(this);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationStart(Animator animator) {

    }

    @Override
    public void onAnimationEnd(Animator animator) {
        if (!mPopped) {
            mListener.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationCancel(Animator animator) {
        if (!mPopped) {
            mListener.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationRepeat(Animator animator) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        setY((Float) valueAnimator.getAnimatedValue());
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!mPopped && event.getAction() == MotionEvent.ACTION_DOWN) {
            // Change the image to the burst image when the balloon is popped
            this.setImageResource(R.drawable.balloonburst);

            // Apply the balloon burst animation
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.balloon_burst_animation);
            startAnimation(animation);

            mListener.popBalloon(this, true);
            mPopped = true;
            mAnimator.cancel();
        }

        return true;
    }

    public void setPopped(boolean popped) {
        mPopped = popped;
        if(popped){
            mAnimator.cancel();
        }
    }

    public int getColor() {
        return mBalloonColor;
    }

    public interface BalloonListener {
        void popBalloon(Balloon balloon, boolean userTouch);
    }
}
