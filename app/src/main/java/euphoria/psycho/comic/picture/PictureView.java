package euphoria.psycho.comic.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;


/**
 * Created by Administrator on 2015/1/14.
 */
public class PictureView extends View implements OnScaleGestureListener, OnGestureListener, OnDoubleTapListener {
    public static final int TRANSLATE_NONE = 0;
    private final static float SCALE_OVERZOOM_FACTOR = 1.5f;
    public static final int TRANSLATE_X_ONLY = 1;
    public static final long SNAP_DURATION = 100L;
    private final static float DOUBLE_TAP_SCALE_FACTOR = 2.0f;
    public static final int TRANSLATE_Y_ONLY = 2;
    private final static float SNAP_THRESHOLD = 20.0f;
    public final static long ZOOM_ANIMATION_DURATION = 200L;
    public static final long SNAP_DELAY = 250L;
    public static final int TRANSLATE_BOTH = 3;
    private float downFocusX_;
    private float downFocusY_;
    private Drawable drawable_;
    private final float[] floatValues_ = new float[9];
    private GestureDetectorCompat gestureDetectorCompat_;
    private boolean isCenterScale_;
    private boolean isDoubleTapDeBounce_;
    private boolean isDoubleTapOccurred_;
    private boolean isDoubleTapToZoomEnabled_ = true;
    private boolean isDoubleTouch_;
    private boolean isHaveLayout_;
    private boolean isInitialized_;
    private boolean isQuickScaleEnabled_;
    private boolean isTransformEnabled_ = true;
    private final Matrix matrixOriginal_ = new Matrix();
    private final Matrix matrix_ = new Matrix();
    private Matrix matrixDraw_;
    private float maxInitialScaleFactor_;
    private float maxScale_;
    private float minScale_;
    private OnClickListener onClickListener_;
    private final RectF rectFTranslate_ = new RectF();
    private final RectF rectFTemporaryDestination_ = new RectF();
    private final RectF rectFTemporarySource_ = new RectF();
    private float rotation_;
    private ScaleGestureDetector scaleGestureDetector_;
    private ScaleRunnable scaleRunnable_;
    private SnapRunnable snapRunnable_;
    private int touchSlopSquare_;
    private TranslateRunnable translateRunnable_;


    public PictureView(Context context) {
        super(context);
        initialize();
    }

    public PictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void bindDrawable(Drawable drawable) {
        boolean changed = false;
        if (drawable != null && drawable != drawable_) {
            if (drawable_ != null) {
                drawable_.setCallback(null);
            }
            drawable_ = drawable;
            minScale_ = 0f;
            drawable_.setCallback(this);
            changed = true;
        }
        configureBounds(changed);
        invalidate();
    }

    public void bindPhoto(Bitmap photoBitmap) {
        boolean currentDrawableIsBitmapDrawable = drawable_ instanceof BitmapDrawable;
        boolean changed = !(currentDrawableIsBitmapDrawable);
        if (drawable_ != null && currentDrawableIsBitmapDrawable) {
            final Bitmap drawableBitmap = ((BitmapDrawable) drawable_).getBitmap();
            if (photoBitmap == drawableBitmap) {
                return;
            }
            changed = photoBitmap != null &&
                    (drawable_.getIntrinsicWidth() != photoBitmap.getWidth() ||
                            drawable_.getIntrinsicHeight() != photoBitmap.getHeight());
            minScale_ = 0f;
            drawable_ = null;
        }
        if (drawable_ == null && photoBitmap != null) {
            drawable_ = new BitmapDrawable(getResources(), photoBitmap);
        }
        configureBounds(changed);
        invalidate();
    }

    private void configureBounds(boolean changed) {
        if (drawable_ == null || !isHaveLayout_) {
            return;
        }
        final int dwidth = drawable_.getIntrinsicWidth();
        final int dheight = drawable_.getIntrinsicHeight();
        final int vwidth = getWidth();
        final int vheight = getHeight();
        final boolean fits = (dwidth < 0 || vwidth == dwidth) &&
                (dheight < 0 || vheight == dheight);
        drawable_.setBounds(0, 0, dwidth, dheight);
        if (changed || (minScale_ == 0 && drawable_ != null && isHaveLayout_)) {
            generateMatrix();
            generateScale();
        }
        if (fits || matrix_.isIdentity()) {
            matrixDraw_ = null;
        } else {
            matrixDraw_ = matrix_;
        }
    }

    private void generateMatrix() {
        final int dwidth = drawable_.getIntrinsicWidth();
        final int dheight = drawable_.getIntrinsicHeight();
        final int vwidth = getWidth();
        final int vheight = getHeight();
        final boolean fits = (dwidth < 0 || vwidth == dwidth) &&
                (dheight < 0 || vheight == dheight);
        rectFTemporarySource_.set(0, 0, dwidth, dheight);
        rectFTemporaryDestination_.set(0, 0, vwidth, vheight);
        RectF scaledDestination = new RectF(
                (vwidth / 2) - (dwidth * maxInitialScaleFactor_ / 2),
                (vheight / 2) - (dheight * maxInitialScaleFactor_ / 2),
                (vwidth / 2) + (dwidth * maxInitialScaleFactor_ / 2),
                (vheight / 2) + (dheight * maxInitialScaleFactor_ / 2));
        if (rectFTemporaryDestination_.contains(scaledDestination)) {
            matrix_.setRectToRect(rectFTemporarySource_, scaledDestination, Matrix.ScaleToFit.CENTER);
        } else {
            matrix_.setRectToRect(rectFTemporarySource_, rectFTemporaryDestination_, Matrix.ScaleToFit.CENTER);
        }
        matrixOriginal_.set(matrix_);
    }

    private void generateScale() {
        final int dwidth = drawable_.getIntrinsicWidth();
        final int dheight = drawable_.getIntrinsicHeight();
        final int vwidth = getWidth();
        final int vheight = getHeight();
        if (dwidth < vwidth && dheight < vheight) {
            minScale_ = 1.0f;
        } else {
            minScale_ = getScale();
        }
        maxScale_ = Math.max(minScale_ * 4, 4);
    }

    private float getScale() {
        matrix_.getValues(floatValues_);
        return floatValues_[Matrix.MSCALE_X];
    }

    private void initialize() {
        final Context context = getContext();
        if (!isInitialized_) {
            isInitialized_ = true;
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            final int touchSlop = configuration.getScaledTouchSlop();
            touchSlopSquare_ = touchSlop * touchSlop;
        }
        gestureDetectorCompat_ = new GestureDetectorCompat(context, this, null);
        scaleGestureDetector_ = new ScaleGestureDetector(context, this);
        isQuickScaleEnabled_ = ScaleGestureDetectorCompat.isQuickScaleEnabled(scaleGestureDetector_);
        translateRunnable_ = new TranslateRunnable(this);
        snapRunnable_ = new SnapRunnable(this);
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        isDoubleTapOccurred_ = true;
        if (!isQuickScaleEnabled_) {
            return scale(event);
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        final int action = e.getAction();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isQuickScaleEnabled_) {
                    downFocusX_ = e.getX();
                    downFocusY_ = e.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isQuickScaleEnabled_) {
                    handled = scale(e);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isQuickScaleEnabled_ && isDoubleTapOccurred_) {
                    final int deltaX = (int) (e.getX() - downFocusX_);
                    final int deltaY = (int) (e.getY() - downFocusY_);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > touchSlopSquare_) {
                        isDoubleTapOccurred_ = false;
                    }
                }
                break;
        }
        return handled;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        if (isTransformEnabled_) {
            translateRunnable_.stop();
            scaleRunnable_.stop();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawable_ != null) {
            final int s = canvas.getSaveCount();
            canvas.save();
            if (matrixDraw_ != null)
                canvas.concat(matrixDraw_);
            drawable_.draw(canvas);
            canvas.restoreToCount(s);
            rectFTranslate_.set(drawable_.getBounds());
            if (matrixDraw_ != null)
                matrixDraw_.mapRect(rectFTranslate_);
        }
    }

    @Override
    public boolean onFling(MotionEvent event, MotionEvent event2, float velocityX, float velocityY) {
        if (isTransformEnabled_ && !scaleRunnable_.isRunning_) {
            translateRunnable_.start(velocityX, velocityY);
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        isHaveLayout_ = true;
        configureBounds(changed);
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        if (isTransformEnabled_) {
            isDoubleTouch_ = false;
            float currentScale = getScale();
            float newScale = currentScale * scaleGestureDetector.getScaleFactor();
            if (isCenterScale_) {
                scale(newScale, getWidth() / 2, getHeight() / 2);
            } else {
                scale(newScale, scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
            }
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        if (isTransformEnabled_) {
            scaleRunnable_.stop();
            isDoubleTouch_ = true;
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        final float currentScale = getScale();
        final int width = getWidth();
        final int height = getHeight();
        if (currentScale > maxScale_) {
// The number of times the crop amount pulled in can fit on the screen
            final float marginFit = 1 / (1 - maxScale_ / currentScale);
// The (negative) relative maximum distance from an image edge such that when scaled
// this far from the edge, all of the image off-screen in that direction is pulled in
            final float relativeDistance = 1 - marginFit;
            float centerX = width / 2;
            float centerY = height / 2;
// This center will pull all of the margin from the lesser side, over will expose trim
            final float maxX = rectFTranslate_.left * relativeDistance;
            final float maxY = rectFTranslate_.top * relativeDistance;
// This center will pull all of the margin from the greater side, over will expose trim
            final float minX = width * marginFit + rectFTranslate_.right * relativeDistance;
            final float minY = height * marginFit + rectFTranslate_.bottom * relativeDistance;
// Adjust center according to bounds to avoid bad crop
            if (minX > maxX) {
// Border is inevitable due to small image size, so we split the crop difference
                centerX = (minX + maxX) / 2;
            } else {
                centerX = Math.min(Math.max(minX, centerX), maxX);
            }
            if (minY > maxY) {
// Border is inevitable due to small image size, so we split the crop difference
                centerY = (minY + maxY) / 2;
            } else {
                centerY = Math.min(Math.max(minY, centerY), maxY);
            }
            scaleRunnable_.start(currentScale, maxScale_, centerX, centerY);
        }
        if (isTransformEnabled_ && isDoubleTouch_) {
            isDoubleTapDeBounce_ = true;
            resetTransformations();
        }
    }

    @Override
    public boolean onScroll(MotionEvent event, MotionEvent event2, float distanceX, float distanceY) {
        if (isTransformEnabled_ && !scaleRunnable_.isRunning_) {
            translate(-distanceX, -distanceY);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        if (onClickListener_ != null && !isDoubleTouch_) {
            onClickListener_.onClick(this);
        }
        isDoubleTouch_ = false;
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scaleGestureDetector_ == null || gestureDetectorCompat_ == null) {
            return true;
        }
        scaleGestureDetector_.onTouchEvent(event);
        gestureDetectorCompat_.onTouchEvent(event);
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!translateRunnable_.running_) {
                    snap();
                }
                break;
        }
        return true;
    }

    public void resetTransformations() {
// snap transformations; we don't animate
        matrix_.set(matrixOriginal_);
// Invalidate the view because if you move off this PhotoView
// to another one and come back, you want it to draw from scratch
// in case you were zoomed in or translated (since those settings
// are not preserved and probably shouldn't be).
        invalidate();
    }

    private void scale(float newScale, float centerX, float centerY) {
        //matrix_.postRotate(-rotation_, getWidth() / 2, getHeight() / 2);
        newScale = Math.max(newScale, minScale_);
        newScale = Math.min(newScale, maxScale_ * SCALE_OVERZOOM_FACTOR);
        float currentScale = getScale();
        float factor = newScale / currentScale;
        matrix_.postScale(factor, factor, centerX, centerY);
        //matrix_.postRotate(rotation_, getWidth() / 2, getHeight() / 2);
        invalidate();
    }

    public void clear() {
        gestureDetectorCompat_ = null;
        scaleGestureDetector_ = null;
        drawable_ = null;
        scaleRunnable_.stop();
        scaleRunnable_ = null;
        translateRunnable_.stop();
        translateRunnable_ = null;
        snapRunnable_.stop();
        snapRunnable_ = null;
        setOnClickListener(null);
        onClickListener_ = null;
        isDoubleTapOccurred_ = false;
    }

    private boolean scale(MotionEvent e) {
        boolean handled = false;
        if (isDoubleTapToZoomEnabled_ && isTransformEnabled_ && isDoubleTapOccurred_) {
            if (!isDoubleTapDeBounce_) {
                float currentScale = getScale();
                float targetScale;
                float centerX, centerY;
// Zoom out if not default scale, otherwise zoom in
                if (currentScale > minScale_) {
                    targetScale = minScale_;
                    float relativeScale = targetScale / currentScale;
// Find the apparent origin for scaling that equals this scale and translate
                    centerX = (getWidth() / 2 - relativeScale * rectFTranslate_.centerX()) /
                            (1 - relativeScale);
                    centerY = (getHeight() / 2 - relativeScale * rectFTranslate_.centerY()) /
                            (1 - relativeScale);
                } else {
                    targetScale = currentScale * DOUBLE_TAP_SCALE_FACTOR;
// Ensure the target scale is within our bounds
                    targetScale = Math.max(minScale_, targetScale);
                    targetScale = Math.min(maxScale_, targetScale);
                    float relativeScale = targetScale / currentScale;
                    float widthBuffer = (getWidth() - rectFTranslate_.width()) / relativeScale;
                    float heightBuffer = (getHeight() - rectFTranslate_.height()) / relativeScale;
// Clamp the center if it would result in uneven borders
                    if (rectFTranslate_.width() <= widthBuffer * 2) {
                        centerX = rectFTranslate_.centerX();
                    } else {
                        centerX = Math.min(Math.max(rectFTranslate_.left + widthBuffer,
                                e.getX()), rectFTranslate_.right - widthBuffer);
                    }
                    if (rectFTranslate_.height() <= heightBuffer * 2) {
                        centerY = rectFTranslate_.centerY();
                    } else {
                        centerY = Math.min(Math.max(rectFTranslate_.top + heightBuffer,
                                e.getY()), rectFTranslate_.bottom - heightBuffer);
                    }
                }
                if (isCenterScale_) {
                    scaleRunnable_.start(currentScale, targetScale, getWidth() / 2, getHeight() / 2);
                } else {
                    scaleRunnable_.start(currentScale, targetScale, centerX, centerY);
                }
                handled = true;
            }
            isDoubleTapDeBounce_ = false;
        }
        isDoubleTapOccurred_ = false;
        return handled;
    }

    private void snap() {
        rectFTranslate_.set(rectFTemporarySource_);
        matrix_.mapRect(rectFTranslate_);
        final int width = getWidth();
        final int height = getHeight();
        final float maxLeft = 0.0f;
        final float maxRight = width;
        final float maxTop = 0.0f;
        final float maxBottom = height;
        final float b = rectFTranslate_.bottom;
        final float l = rectFTranslate_.left;
        final float r = rectFTranslate_.right;
        final float t = rectFTranslate_.top;
        final float translateX;
        if (r - l < maxRight - maxLeft) {
// Image is narrower than view; translate to the center of the view
            translateX = maxLeft + ((maxRight - maxLeft) - (r + l)) / 2;
        } else if (l > maxLeft) {
// Image is off right-edge of screen; bring it into view
            translateX = maxLeft - l;
        } else if (r < maxRight) {
// Image is off left-edge of screen; bring it into view
            translateX = maxRight - r;
        } else {
            translateX = 0.0f;
        }
        final float translateY;
        if (b - t < maxBottom - maxTop) {
// Image is shorter than view; translate to the bottom edge of the view
            translateY = maxTop + ((maxBottom - maxTop) - (b + t)) / 2;
        } else if (t > maxTop) {
// Image is off bottom-edge of screen; bring it into view
            translateY = maxTop - t;
        } else if (b < maxBottom) {
// Image is off top-edge of screen; bring it into view
            translateY = maxBottom - b;
        } else {
            translateY = 0.0f;
        }
        if (Math.abs(translateX) > SNAP_THRESHOLD || Math.abs(translateY) > SNAP_THRESHOLD) {
/* mSnapRunnable.start(translateX, translateY);*/
        } else {
            matrix_.postTranslate(translateX, translateY);
            invalidate();
        }
    }

    private int translate(float tx, float ty) {
        rectFTranslate_.set(rectFTemporarySource_);
        matrix_.mapRect(rectFTranslate_);
        final int width = getWidth();
        final int height = getHeight();
        final float maxLeft = 0.0f;
        final float maxRight = width;
        final float maxTop = 0.0f;
        final float maxBottom = height;
        final float b = rectFTranslate_.bottom;
        final float l = rectFTranslate_.left;
        final float r = rectFTranslate_.right;
        final float t = rectFTranslate_.top;
        final float translateX;
        final float translateY;
        if (r - l < maxRight - maxLeft) {
            translateX = maxLeft + ((maxRight - maxLeft) - (r + l)) / 2;
        } else {
            translateX = Math.max(maxRight - r, Math.min(maxLeft - l, tx));
        }
        if (b - t < maxBottom - maxTop) {
            translateY = maxTop + ((maxBottom - maxTop) - (b + t)) / 2;
        } else {
            translateY = Math.max(maxBottom - b, Math.min(maxTop - t, ty));
        }
        matrix_.postTranslate(translateX, translateY);
        invalidate();
        boolean didTranslateX = translateX == tx;
        boolean didTranslateY = translateY == ty;
        if (didTranslateX && didTranslateY) {
            return TRANSLATE_BOTH;
        } else if (didTranslateX) {
            return TRANSLATE_X_ONLY;
        } else if (didTranslateY) {
            return TRANSLATE_Y_ONLY;
        }
        return TRANSLATE_NONE;
    }

    @Override
    public boolean verifyDrawable(Drawable drawable) {
        return drawable_ == drawable || super.verifyDrawable(drawable);
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        if (drawable_ == drawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    public void setMaxInitialScaleFactor(float f) {
        maxInitialScaleFactor_ = f;
    }
    @Override
    public void setOnClickListener(OnClickListener l) {
        onClickListener_ = l;
    }

    private static class TranslateRunnable implements Runnable {

        private static final float DECELERATION_RATE = 1000f;
        private static final long NEVER = -1L;

        private final PictureView header_;

        private float velocityX_;
        private float velocityY_;

        private float decelerationX_;
        private float decelerationY_;

        private long lastRunTime_;
        private boolean running_;
        private boolean stop_;

        public TranslateRunnable(PictureView header) {
            lastRunTime_ = NEVER;
            header_ = header;
        }

        public boolean start(float velocityX, float velocityY) {
            if (running_) {
                return false;
            }
            lastRunTime_ = NEVER;
            velocityX_ = velocityX;
            velocityY_ = velocityY;

            float angle = (float) Math.atan2(velocityY_, velocityX_);
            decelerationX_ = (float) (DECELERATION_RATE * Math.cos(angle));
            decelerationY_ = (float) (DECELERATION_RATE * Math.sin(angle));

            stop_ = false;
            running_ = true;
            header_.post(this);
            return true;
        }

        public void stop() {
            running_ = false;
            stop_ = true;
        }

        @Override
        public void run() {
            if (stop_) {
                return;
            }

            long now = System.currentTimeMillis();
            float delta = (lastRunTime_ != NEVER) ? (now - lastRunTime_) / 1000f : 0f;
            final int translateResult = header_.translate(velocityX_ * delta, velocityY_ * delta);
            lastRunTime_ = now;
            float slowDownX = decelerationX_ * delta;
            if (Math.abs(velocityX_) > Math.abs(slowDownX)) {
                velocityX_ -= slowDownX;
            } else {
                velocityX_ = 0f;
            }
            float slowDownY = decelerationY_ * delta;
            if (Math.abs(velocityY_) > Math.abs(slowDownY)) {
                velocityY_ -= slowDownY;
            } else {
                velocityY_ = 0f;
            }

            if ((velocityX_ == 0f && velocityY_ == 0f)
                    || translateResult == TRANSLATE_NONE) {
                stop();
                header_.snap();
            } else if (translateResult == TRANSLATE_X_ONLY) {
                decelerationX_ = (velocityX_ > 0) ? DECELERATION_RATE : -DECELERATION_RATE;
                decelerationY_ = 0;
                velocityY_ = 0f;
            } else if (translateResult == TRANSLATE_Y_ONLY) {
                decelerationX_ = 0;
                decelerationY_ = (velocityY_ > 0) ? DECELERATION_RATE : -DECELERATION_RATE;
                velocityX_ = 0f;
            }

            if (stop_) {
                return;
            }
            header_.post(this);
        }
    }

    private static class SnapRunnable implements Runnable {

        private static final long NEVER = -1L;

        private final PictureView pictureView_;

        private float translateX_;
        private float translateY_;

        private long startRunTime_;
        public boolean isRunning_;
        private boolean isStop_;

        public SnapRunnable(PictureView pictureView) {
            startRunTime_ = NEVER;

            pictureView_ = pictureView;

        }

        public boolean start(float translateX, float translateY) {
            if (isRunning_) {
                return false;
            }
            startRunTime_ = NEVER;
            translateX_ = translateX;
            translateY_ = translateY;
            isStop_ = false;
            isRunning_ = true;
            pictureView_.postDelayed(this, SNAP_DELAY);
            return true;
        }

        public void stop() {
            isRunning_ = false;
            isStop_ = true;
        }

        @Override
        public void run() {
            if (isStop_) {
                return;
            }

            long now = System.currentTimeMillis();
            float delta = (startRunTime_ != NEVER) ? (now - startRunTime_) : 0f;

            if (startRunTime_ == NEVER) {
                startRunTime_ = now;
            }

            float transX;
            float transY;
            if (delta >= SNAP_DURATION) {
                transX = translateX_;
                transY = translateY_;
            } else {
                transX = (translateX_ / (SNAP_DURATION - delta)) * 10f;
                transY = (translateY_ / (SNAP_DURATION - delta)) * 10f;
                if (Math.abs(transX) > Math.abs(translateX_) || Float.isNaN(transX)) {
                    transX = translateX_;
                }
                if (Math.abs(transY) > Math.abs(translateY_) || Float.isNaN(transY)) {
                    transY = translateY_;
                }
            }

            pictureView_.translate(transX, transY);
            translateX_ -= transX;
            translateY_ -= transY;

            if (translateX_ == 0 && translateY_ == 0) {
                stop();
            }

            if (isStop_) {
                return;
            }
            pictureView_.post(this);
        }
    }

    private static class ScaleRunnable implements Runnable {


        private final PictureView pictureView_;
        private float centerX_;
        private float centerY_;
        public boolean isRunning_;
        private boolean isStop_;
        private boolean isZoomingIn_;
        private float startScale_;
        private long startTime_;
        private float targetScale_;
        private float velocity_;


        public ScaleRunnable(PictureView pictureView) {

            pictureView_ = pictureView;

        }

        public boolean start(float startScale, float targetScale, float centerX, float centerY) {

            if (isRunning_)
                return false;
            centerX_ = centerX;
            centerY_ = centerY;

            startScale_ = startScale;
            targetScale_ = targetScale;

            startTime_ = System.currentTimeMillis();
            isZoomingIn_ = targetScale_ > startScale_;

            velocity_ = (targetScale_ - startScale_) / ZOOM_ANIMATION_DURATION;

            isRunning_ = true;
            isStop_ = false;
            pictureView_.post(this);
            return true;
        }

        public void stop() {
            isRunning_ = false;
            isStop_ = true;
        }

        @Override
        public void run() {
            if (isStop_) {
                return;
            }
            long now = System.currentTimeMillis();
            long elapsed = now - startTime_;
            float newScale = (startScale_ + velocity_ * elapsed);
            pictureView_.scale(newScale, centerX_, centerY_);

            if (newScale == targetScale_ || (isZoomingIn_ == (newScale > targetScale_))) {
                pictureView_.scale(targetScale_, centerY_, centerY_);
                stop();
            }

            if (!isStop_) {
                pictureView_.post(this);
            }
        }
    }
}
