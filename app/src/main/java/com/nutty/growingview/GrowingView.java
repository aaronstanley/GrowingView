package com.nutty.growingview;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * GrowingView
 * Created by aaron on 30/12/2016.
 */

public class GrowingView extends FrameLayout {
    GrowingViewListener listener;
    boolean hasGrown = false;
    boolean isAnimating = false;
    int startX, startY, startWidth, startHeight, duration;

    public GrowingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Start the Grow Animation
     *
     * @param left     New Position Left (X)
     * @param top      New Position Top (Y)
     * @param right    New Position Right (Width)
     * @param bottom   New Position Bottom (Height)
     * @param duration Duration time in MS
     */
    public void startGrowAnimation(int left, int top, int right, int bottom, int duration) {
        startGrowAnimation(left, top, right, bottom, duration, null);
    }

    /**
     * Start the Grow Animation
     *
     * @param left     New Position Left (X)
     * @param top      New Position Top (Y)
     * @param right    New Position Right (Width)
     * @param bottom   New Position Bottom (Height)
     * @param duration Duration time in MS
     * @param listener Animation Listener
     */
    public void startGrowAnimation(int left, int top, int right, int bottom, int duration, GrowingViewListener listener) {

        if (!hasGrown && !isAnimating) {
            this.listener = listener;
            this.startX = (int) getX();
            this.startY = (int) getY();
            this.startWidth = getWidth();
            this.startHeight = getHeight();
            this.duration = duration;

            startAnimation(new ResizeMoveAnimation(AnimationType.GROW,
                    left, top, right, bottom, duration));
        }
    }

    /**
     * Returns view to it's starting position
     */
    public void startShrinkAnimation() {
        startShrinkAnimation(null);
    }

    /**
     * Returns view to it's starting position
     */
    public void startShrinkAnimation(GrowingViewListener listener) {

        if (hasGrown && !isAnimating) {
            this.listener = listener;
            startAnimation(new ResizeMoveAnimation(AnimationType.SHRINK,
                    startX, startY, startWidth, startHeight, duration));
        }
    }

    enum AnimationType {
        GROW,
        SHRINK
    }

    public interface GrowingViewListener {
        void growingStarted();

        void growingFinished();

        void shrinkStarted();

        void shrinkFinished();
    }

    class ResizeMoveAnimation extends Animation {

        AnimationType type;
        int fromLeft, fromTop, fromRight, fromBottom;
        int toLeft, toTop, toRight, toBottom;
        int currentLeft, currentTop, currentRight, currentBottom;
        //As the Width and Height values update over time, we must wait for the listener to fire before we reposition the X and Y
        //as this occurs instantly
        OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                setX(currentLeft);
                setY(currentTop);

                if (!isAnimating) {
                    removeOnLayoutChangeListener(this);
                }

            }
        };
        //Animation Listener that can remove attached listeners
        AnimationListener animationListener = new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimating = true;
                if (listener != null) {
                    switch (type) {
                        case GROW:
                            listener.growingStarted();
                            break;
                        case SHRINK:
                            listener.shrinkStarted();
                            break;
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimating = false;
                hasGrown = !hasGrown;

                setAnimationListener(null);
                if (listener != null) {
                    switch (type) {
                        case GROW:
                            listener.growingFinished();
                            break;
                        case SHRINK:
                            listener.shrinkFinished();
                            break;
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };


        ResizeMoveAnimation(AnimationType type, int toLeft, int toTop, int toRight, int toBottom, int duration) {

            this.type = type;
            this.fromLeft = (int) getX();
            this.fromTop = (int) getY();
            this.fromRight = getWidth();
            this.fromBottom = getHeight();
            this.toLeft = toLeft;
            this.toTop = toTop;

            switch (type) {
                case GROW:
                    this.fromBottom = getHeight();
                    //Offset the width to cope with any border applied to the left
                    this.toRight = toRight - toLeft;
                    //calculate the different between height added going upwards and height added going downwards
                    this.toBottom = toBottom - fromTop;
                    break;
                case SHRINK:
                    this.toRight = toRight;
                    //calculate the different between height added going upwards and height added going downwards
                    this.fromBottom = getHeight() - toTop;
                    //set toBottom to 0, returns to initial height.
                    this.toBottom = toBottom;
                    break;
            }

            //remove margins as this affects the view growth
            if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) getLayoutParams();
                p.setMargins(0, 0, 0, 0);
            }

            //Remove Alignment rules as they affect margins
            if(getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) getLayoutParams();
                p.addRule(RelativeLayout.ALIGN_LEFT,0);
                p.addRule(RelativeLayout.ALIGN_RIGHT,0);
                p.addRule(RelativeLayout.ALIGN_TOP,0);
                p.addRule(RelativeLayout.ALIGN_BOTTOM,0);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    p.addRule(RelativeLayout.ALIGN_START,0);
                    p.addRule(RelativeLayout.ALIGN_END,0);
                }
            }

            setDuration(duration);
            addOnLayoutChangeListener(onLayoutChangeListener);
            setAnimationListener(animationListener);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            //calculate current values based on transform
            currentLeft = (int) (fromLeft + (toLeft - fromLeft) * interpolatedTime);
            currentTop = (int) (fromTop + (toTop - fromTop) * interpolatedTime);
            currentRight = (int) (fromRight + (toRight - fromRight) * interpolatedTime);
            currentBottom = (int) (fromBottom + (toBottom - fromBottom) * interpolatedTime);

            //set new width
            getLayoutParams().width = (currentRight);

            //calculate height added above
            int v = (fromTop - currentTop);
            if (type == AnimationType.GROW) {
                //height = current height added above starting point (positive value) plus height added below the starting point
                getLayoutParams().height = v + currentBottom;
            } else {
                //height = current height below the starting point plus height above (negative value)
                getLayoutParams().height = currentBottom + ((toTop - fromTop) + v);
            }

            //ask for refresh - calls OnLayoutChangeListener
            requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
