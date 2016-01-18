package euphoria.psycho.downloader;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2015/1/11.
 */
public class LProgressBar extends View {
    private boolean isAnimating_ = false;
    private boolean isDrawBar_;
    private boolean isDrawNoBar_;
    private final static float dBarHeight = 1.5F;
    private final static float dNoBarHeight = 1.0F;
    private final static float dOffset = 3.0F;
    private final static float dTextSize = 12.0F;
    private final static int dNoBarColor = 0XFFE8E8E8;
    private final static int dTextColor = 0XFF56C5D5;
    private RectF barBounds_ = new RectF();
    private final static RectF noBarBounds_ = new RectF();
    private final static String BARCOLOR = "barColor";
    private final static String BARHEIGHT = "barHeight";
    private final static String INSTANCE_STATE = "saved_instance";
    private final static String MAX = "max";
    private final static String NOBARCOLOR = "noBarColor";
    private final static String NOBARHEIGHT = "noBarHeight";
    private final static String OFFSET = "offset";
    private final static String PROGRESS = "progress";
    private final static String SUFFIX = "suffix";
    private final static String TEXTCOLOR = "textColor";
    private final static String TEXTSIZE = "textSize";
    private float barHeight_;
    private float drawTextEnd_;
    private float drawTextStart_;
    private float drawTextWidth_;
    private float noBarHeight_;
    private float offset_;
    private float textSize_;
    private int barColor_;
    private int max_;
    private int noBarColor_;
    private int progress_;
    private int targetProgress_;
    private int textColor_;
    private Paint paint_;
    private Paint paintNoBar_;
    private String drawText_;
    private String suffix_ = "%";
    private TextPaint textPaint_;


    public LProgressBar(Context context, AttributeSet attrs) {

        super(context, attrs);
        initialize(context, attrs);
    }

    public LProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void calculateBounds() {
        drawText_ = String.format("%d", progress_ * 100 / max_) + suffix_;

        drawTextWidth_ = textPaint_.measureText(drawText_);
        final float halfHeight = getHeight() / 2.0f;
        final float halfBarHeight = barHeight_ / 2.0f;
        final int w = getWidth();
        final int pL = getPaddingLeft();
        final int pR = getPaddingRight();
        final float per = (w - pL - pR) / (max_ * 1.0f);
        if (progress_ == 0) {
            isDrawBar_ = false;
            drawTextStart_ = pL;
        } else {
            isDrawBar_ = true;
            barBounds_.left = pL;
            barBounds_.top = halfHeight - halfBarHeight;
            barBounds_.right = per * 1.0f * progress_ - offset_ + pL;
            barBounds_.bottom = halfHeight + halfBarHeight;
            drawTextStart_ = barBounds_.right + offset_;
        }
        drawTextEnd_ = halfHeight - (textPaint_.descent() + textPaint_.ascent()) / 2.0f;
        if (drawTextStart_ + drawTextWidth_ >= w - pR) {
            drawTextStart_ = w - pR - drawTextWidth_;
            barBounds_.right = drawTextStart_ - offset_;
        }
        final float noBarStart = drawTextStart_ + drawTextWidth_ + offset_;
        if (noBarStart >= w - pR) {
            isDrawNoBar_ = false;
        } else {
            isDrawNoBar_ = true;
            noBarBounds_.left = noBarStart;
            noBarBounds_.top = halfHeight - noBarHeight_ / 2.0f;
            noBarBounds_.bottom = halfHeight + noBarHeight_ / 2.0f;
            noBarBounds_.right = w - pR;
        }
    }


    private static ValueAnimator getAnimator(int duration, int s, int e) {
        final ValueAnimator animator = ValueAnimator.ofInt(s, e);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    private void initialize(Context context, AttributeSet attributeSet) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        /*TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LProgressBar);

        typedArray.getInteger(R.attr.progressMax,1);*/
        /*final int i=R.attr.ProgressBarbarColor;*/
       /* barColor_ = typedArray.getColor(R.attr.ProgressBarbarColor, dTextColor);*/
        barColor_ = dTextColor;
        barHeight_ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dBarHeight, displayMetrics);

       /* barHeight_ = typedArray.getDimension(R.attr.ProgressBarbarHeight, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dBarHeight, displayMetrics));
     */
       /* isDrawText_ = typedArray.getBoolean(R.attr.ProgressBarisDrawText, true);*/
        max_ = 100;
       /* noBarColor_ = typedArray.getColor(R.attr.ProgressBarnoBarColor, dNoBarColor);*/
        noBarColor_ = dNoBarColor;
        noBarHeight_ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dNoBarHeight, displayMetrics);
        offset_ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dOffset, displayMetrics);
        /*prefix_ = typedArray.getString(R.attr.ProgressBarsuffix);*/
        /*progress_ = typedArray.getInt(R.attr.ProgressBarprogress, dProgress);
        suffix_ = typedArray.getString(R.attr.ProgressBarsuffix);*/
        textColor_ = dTextColor;
       /* textColor_ = typedArray.getColor(R., dTextColor);*/
        textSize_ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dTextSize, displayMetrics);
        initializePaints();
    }

    private void initializePaints() {
        paint_ = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint_.setColor(barColor_);
        paintNoBar_ = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintNoBar_.setColor(noBarColor_);
        textPaint_ = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint_.setColor(textColor_);
        textPaint_.setTextSize(textSize_);
    }

    private int measure(int d, boolean isWidth) {
        int r = 0;
        int m = MeasureSpec.getMode(d);
        int s = MeasureSpec.getSize(d);
        int p = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingBottom() + getPaddingTop();
        if (m == MeasureSpec.EXACTLY) {
            r = s;
        } else {
            r = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            r += p;
            if (m == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    r = Math.max(r, s);
                } else {
                    r = Math.min(r, s);
                }
            }
        }
        return r;
    }

    public void setMax(int max) {
        if (max > 0)
            max_ = max;
        invalidate();
    }


    public void setProgress(final int progress) {
        final int p = progress <= max_ && progress >= 0 ? progress : 0;
        if (p > 0) {
            if (isAnimating_) {
                targetProgress_ = progress;
                return;
            }
            if (progress - progress_ > 30) {
                isAnimating_ = true;
                final ValueAnimator valueAnimator = getAnimator(1000, progress_, progress);
                valueAnimator.setTarget(this);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                        progress_ = (int) valueAnimator.getAnimatedValue();
                        invalidate();

                    }
                });
                valueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        isAnimating_ = false;
                        if (targetProgress_ > progress_)
                            setProgress(targetProgress_);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });

                valueAnimator.start();
            } else {
                progress_ = p;
                invalidate();
            }
        }
    }

    public void setSuffix(String suffix) {
        suffix_ = suffix == null ? "%" : suffix;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max((int) textSize_, Math.max((int) noBarHeight_, (int) barHeight_));
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) textSize_;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        calculateBounds();

        if (isDrawBar_)
            canvas.drawRect(barBounds_, paint_);
        if (isDrawNoBar_)
            canvas.drawRect(noBarBounds_, paintNoBar_);

        canvas.drawText(drawText_, drawTextStart_, drawTextEnd_, textPaint_);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true),
                measure(heightMeasureSpec, false));
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;

            barHeight_ = bundle.getFloat(BARHEIGHT);
            noBarHeight_ = bundle.getFloat(NOBARHEIGHT);
            offset_ = bundle.getFloat(OFFSET);
            textSize_ = bundle.getFloat(TEXTSIZE);
            barColor_ = bundle.getInt(BARCOLOR);
            max_ = bundle.getInt(MAX);
            noBarColor_ = bundle.getInt(NOBARCOLOR);
            progress_ = bundle.getInt(PROGRESS);
            textColor_ = bundle.getInt(TEXTCOLOR);

            suffix_ = bundle.getString(SUFFIX);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putFloat(BARHEIGHT, barHeight_);
        bundle.putFloat(NOBARHEIGHT, noBarHeight_);
        bundle.putFloat(OFFSET, offset_);
        bundle.putFloat(TEXTSIZE, textSize_);
        bundle.putInt(BARCOLOR, barColor_);
        bundle.putInt(MAX, max_);
        bundle.putInt(NOBARCOLOR, noBarColor_);
        bundle.putInt(PROGRESS, progress_);
        bundle.putInt(TEXTCOLOR, textColor_);
        bundle.putString(SUFFIX, suffix_);
        return bundle;
    }


}
