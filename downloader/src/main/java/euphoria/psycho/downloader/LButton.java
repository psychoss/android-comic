package euphoria.psycho.downloader;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by Administrator on 2015/1/12.
 */
public class LButton extends Button implements View.OnTouchListener {
    private static final String TAG = "LButton";
    private static final int dButtonColor = 0XFF2ECC71;
    private static final int dShadowColor = 0XFF3493C8;
    private static final float dShadowHeight = 4.0F;
    private static final float dCornerRadius = 8.0F;
    private Drawable pressedDrawable_;
    private Drawable unPressedDrawable_;
    private boolean isShadowShow_;
    private int buttonColor_;
    private float shadowHeight_;
    private int shadowColor_;
    private float cornerRadius_;
    private int paddingLeft_;
    private int paddingRight_;
    private int paddingTop_;
    private int paddingBottom_;

    private boolean isShadowShowDefined_ = false;


    public LButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);

    }

    public LButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);

    }

    private void initialize(Context context, AttributeSet attributeSet) {

        final TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LButton);
        if (typedArray != null) {
            for (int i = 0; i < typedArray.getIndexCount(); i++) {
                final int index = typedArray.getIndex(i);
                if (index == R.styleable.LButton_bttounColor) {
                    buttonColor_ = typedArray.getColor(index, dButtonColor);
                    Utilities.pushLogToError(getContext(), TAG, "buttonColor_", buttonColor_);
                } else if (index == R.styleable.LButton_isShadow) {

                    isShadowShow_ = typedArray.getBoolean(index, true);
                    Utilities.pushLogToError(getContext(), TAG, "isShadowShow_",isShadowShow_ );
                } else if (index == R.styleable.LButton_shadowHeight) {
                    shadowHeight_ = typedArray.getDimension(index, applyDimensionDip(getContext(), dShadowHeight));
                    Utilities.pushLogToError(getContext(), TAG, "shadowHeight_ ", shadowHeight_ );
                } else if (index == R.styleable.LButton_shadowColor) {
                    shadowColor_ = R.styleable.LButton_shadowColor;
                } else if (index == R.styleable.LButton_cornerRadius) {
                    cornerRadius_ = typedArray.getDimension(index, applyDimensionDip(getContext(), dCornerRadius));

                }

            }
            typedArray.recycle();
        }

        int[] attrs = new int[]{
                android.R.attr.paddingLeft,
                android.R.attr.paddingRight,
                android.R.attr.paddingTop,
                android.R.attr.paddingBottom
        };
        final TypedArray typedArrayOS = context.obtainStyledAttributes(attributeSet, attrs);
        if (typedArrayOS != null) {
            paddingLeft_ = (int) typedArrayOS.getDimension(0, 0);
            paddingRight_ = (int) typedArrayOS.getDimension(1, 0);
            paddingTop_ = (int) typedArrayOS.getDimension(2, 0);
            paddingBottom_ = (int) typedArrayOS.getDimension(3, 0);

            typedArrayOS.recycle();

        }


        setOnTouchListener(this);
    }


    private void refresh() {
        final int alpha = Color.alpha(buttonColor_);
        final float[] hsv = new float[3];
        Color.colorToHSV(buttonColor_, hsv);
        hsv[2] *= 0.8F;
        if (!isShadowShowDefined_) {
            shadowColor_ = Color.HSVToColor(alpha, hsv);
        }
        if (isShadowShow_) {
            pressedDrawable_ = createLayerDrawable((int) cornerRadius_, Color.TRANSPARENT, buttonColor_);
            unPressedDrawable_ = createLayerDrawable((int) cornerRadius_, buttonColor_, shadowColor_);
        } else {
            shadowHeight_ = 0;
            pressedDrawable_ = createLayerDrawable((int) cornerRadius_, shadowColor_, Color.TRANSPARENT);
            unPressedDrawable_ = createLayerDrawable((int) cornerRadius_, buttonColor_, Color.TRANSPARENT);
        }
        updateBackground(unPressedDrawable_);
        setPadding(paddingLeft_, paddingTop_ + (int) shadowHeight_, paddingRight_, paddingBottom_ + (int) shadowHeight_);

    }

    private void updateBackground(Drawable drawable) {
        if (drawable == null) return;
        if (Build.VERSION.SDK_INT >= 16) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    private LayerDrawable createLayerDrawable(int radius, int topColor, int bottomColor) {
        final float[] outerRadius = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        final RoundRectShape topRect = new RoundRectShape(outerRadius, null, null);
        final ShapeDrawable topShape = new ShapeDrawable(topRect);
        topShape.getPaint().setColor(topColor);
        final RoundRectShape bottomRect = new RoundRectShape(outerRadius, null, null);
        final ShapeDrawable bottomShape = new ShapeDrawable(bottomRect);
        bottomShape.getPaint().setColor(bottomColor);
        final Drawable[] drawables = new Drawable[]{bottomShape, topShape};
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        if (isShadowShow_ && topColor != Color.TRANSPARENT) {
            /**
             * Specifies the insets in pixels for the drawable at the specified index.
             *
             * @param index the index of the drawable to adjust
             * @param l number of pixels to add to the left bound
             * @param t number of pixels to add to the top bound
             * @param r number of pixels to subtract from the right bound
             * @param b number of pixels to subtract from the bottom bound
             */
            layerDrawable.setLayerInset(0, 0, 0, 0, 0);
        } else {
            layerDrawable.setLayerInset(0, 0, (int) shadowHeight_, 0, 0);

        }
        layerDrawable.setLayerInset(1, 0, 0, 0, (int) shadowHeight_);
        return layerDrawable;


    }

    public void setButtonColor_(int buttonColor) {
        this.buttonColor_ = buttonColor;
        refresh();
    }

    public void setShadowHeight_(int shadowHeight) {
        this.shadowHeight_ = shadowHeight;
        refresh();
    }

    public void setShadowShow_(boolean isShadowShow_) {
        this.isShadowShow_ = isShadowShow_;
        refresh();
    }

    public void setCornerRadius_(int cornerRadius_) {
        this.cornerRadius_ = cornerRadius_;
        refresh();
    }

    public void setShadowColor_(int shadowColor) {
        this.shadowColor_ = shadowColor;
        isShadowShowDefined_ = true;
        refresh();

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                updateBackground(pressedDrawable_);
                setPadding(paddingLeft_, paddingTop_, paddingRight_, paddingBottom_);
                break;
            case MotionEvent.ACTION_MOVE:
                final Rect rect = new Rect();
                view.getLocalVisibleRect(rect);
                if (!rect.contains((int) event.getX(), (int) event.getY() + (int) (3 * shadowHeight_)) &&
                        !rect.contains((int) event.getX(), (int) event.getY() - (int) (3 * shadowHeight_))) {
                    updateBackground(unPressedDrawable_);
                    setPadding(paddingLeft_, paddingTop_ + (int) shadowHeight_, paddingRight_, paddingBottom_ + (int) shadowHeight_);

                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                updateBackground(unPressedDrawable_);
                setPadding(paddingLeft_, paddingTop_ + (int) shadowHeight_, paddingRight_, paddingBottom_ + (int) shadowHeight_);
                break;
        }
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        refresh();
    }

    public static float applyDimensionDip(Context context, float f) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, displayMetrics);

    }
}
