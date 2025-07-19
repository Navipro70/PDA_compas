package net.afterday.compas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.afterday.compas.util.Fonts;

/**
 * Created by spaka on 5/5/2018.
 */

public class CountDownTimer extends View {
    private static final String TAG = "CountDownTimer";
    private static final int WIDGET_WIDTH = 200;
    private static final int WIDGET_HEIGHT = 120;
    private Paint paint;
    private int mWidth,
            mHeight;
    private float mScaleFactorX,
            mScaleFactorY;
    private long secondsLeft = 0;

    public CountDownTimer(Context context) {
        super(context);
        init();
    }

    public CountDownTimer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CountDownTimer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get sizes
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalMeasureSpecX = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        int finalMeasureSpecY = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpecX, finalMeasureSpecY);

        mWidth = widthSize;
        mHeight = heightSize;

        mScaleFactorX = (float) mWidth / WIDGET_WIDTH;
        mScaleFactorY = (float) mHeight / WIDGET_HEIGHT;

        paint.setTextSize(115 * mScaleFactorY);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawText("00:00", 23 * mScaleFactorX, 60 * mScaleFactorY, paint);
        if (secondsLeft < 0) {
            return;
        }
        //bringToFront();

        canvas.drawText(secondsToString(secondsLeft), getWidth() / 2, getHeight() / 2 , paint);
    }

    public void setSecondsLeft(Long timeLeft) {
        secondsLeft = timeLeft;
        invalidate();
    }

    private void init() {
        paint = Fonts.instance().getDefaultFontPaint();
    }

    private String secondsToString(long secondsLeft) {
        String mins = Long.toString(secondsLeft / 60);
        if (mins.length() < 2) {
            mins = "0" + mins;
        }
        String secs = Long.toString(secondsLeft % 60);
        if (secs.length() < 2) {
            secs = "0" + secs;
        }
        return mins + ":" + secs;
    }
}
