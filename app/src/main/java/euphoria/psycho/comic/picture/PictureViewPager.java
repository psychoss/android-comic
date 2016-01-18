package euphoria.psycho.comic.picture;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2015/1/14.
 */
public class PictureViewPager extends ViewPager{
    public static enum InterceptType {
        NONE, LEFT, RIGHT, BOTH
    }


    public static interface OnInterceptTouchListener {

        public InterceptType onTouchIntercept(float origX, float origY);
    }

    private static final int INVALID_POINTER = -1;

    private float lastMotionX_;
    private int activePointerId_;

    private float activatedX_;

    private float activatedY_;
    private OnInterceptTouchListener listener_;

    public PictureViewPager(Context context) {
        super(context);
        initialize();
    }

    public PictureViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setPageTransformer(true, new PageTransformer() {
            @Override
            public void transformPage(View page, float position) {

                if (position < 0 || position >= 1.f) {
                    page.setTranslationX(0);
                    page.setAlpha(1.f);
                    page.setScaleX(1);
                    page.setScaleY(1);
                } else {
                    page.setTranslationX(-position * page.getWidth());
                    page.setAlpha(Math.max(0, 1.f - position));
                    final float scale = Math.max(0, 1.f - position * 0.3f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final InterceptType intercept = (listener_ != null)
                ? listener_.onTouchIntercept(activatedX_, activatedY_)
                : InterceptType.NONE;
        final boolean ignoreScrollLeft =
                (intercept == InterceptType.BOTH || intercept == InterceptType.LEFT);
        final boolean ignoreScrollRight =
                (intercept == InterceptType.BOTH || intercept == InterceptType.RIGHT);

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            activePointerId_ = INVALID_POINTER;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (ignoreScrollLeft || ignoreScrollRight) {
                    final int activePointerId = activePointerId_;
                    if (activePointerId == INVALID_POINTER) {
                        break;
                    }

                    final int pointerIndex =
                            MotionEventCompat.findPointerIndex(ev, activePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);

                    if (ignoreScrollLeft && ignoreScrollRight) {
                        lastMotionX_ = x;
                        return false;
                    } else if (ignoreScrollLeft && (x > lastMotionX_)) {
                        lastMotionX_ = x;
                        return false;
                    } else if (ignoreScrollRight && (x < lastMotionX_)) {
                        lastMotionX_ = x;
                        return false;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                lastMotionX_ = ev.getX();
                activatedX_ = ev.getRawX();
                activatedY_ = ev.getRawY();
                activePointerId_ = MotionEventCompat.getPointerId(ev, 0);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == activePointerId_) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastMotionX_ = MotionEventCompat.getX(ev, newPointerIndex);
                    activePointerId_ = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }

        return super.onInterceptTouchEvent(ev);
    }


    public void setOnInterceptTouchListener(OnInterceptTouchListener l) {
        listener_ = l;
    }
}
