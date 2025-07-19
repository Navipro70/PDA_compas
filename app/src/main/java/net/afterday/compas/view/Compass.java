package net.afterday.compas.view;

import static net.afterday.compas.settings.Constants.ORIENTATION_PORTRAIT;
import static java.lang.Math.abs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.afterday.compas.R;
import net.afterday.compas.settings.Constants;
import net.afterday.compas.settings.Settings;

/**
 * Created by Justas Spakauskas on 3/10/2018.
 */

public class Compass extends View implements SensorEventListener {
    private static final String TAG = "Compass";

    private static final int WIDGET_WIDTH = 1030;
    private static final int WIDGET_HEIGHT = 1030;
    protected final Handler mHandler = new Handler();
    //////////////////////////////////////////////////
    private final float MAX_ROATE_DEGREE = 1.0f;
    private Sensor mRotationVector;
    private Matrix matrix;
    private Bitmap compass;
    // Dimension stuff
    private int mWidth;
    private int mHeight;
    private float mScaleFactorX;
    private float mScaleFactorY;
    private float[] mLastRotation = new float[3];
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private boolean isOn = false;
    private SensorManager mSensorManager;
    private float mDirection;
    private float mTargetDirection;
    private AccelerateInterpolator mInterpolator;
    private int offset = 0;
    private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {          // Использовал ROTATION_VECTOR, так как сенсор ORIENTATION устарел и не факт что будет нормально работать
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                mLastRotation = event.values.clone();

            }
            if (mLastRotation != null) {
                SensorManager.getRotationMatrixFromVector(mR, mLastRotation);
                SensorManager.getOrientation(mR, mOrientation);
                float direction = (float) (Math.toDegrees(mOrientation[0]) * -1.0f);
                float normalizedDegree = normalizeDegree(direction);
                if (abs(normalizedDegree - mTargetDirection) > 3) // Такой подход позволяет убрать случайные повороты компаса, вызванные шумом, однако снижает его точность
                {
                    mTargetDirection = normalizedDegree;
                    //Log.d(TAG, "Normalized degree: " + normalizedDegree);
                }

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public Compass(Context context) {
        super(context);
        init();
    }

    public Compass(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Compass(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }    protected Runnable mCompassViewUpdater = new Runnable() {
        @Override
        public void run() {
            if (isOn) {
                if (mDirection != mTargetDirection) {

                    // calculate the short routine
                    float to = mTargetDirection + offset;
                    if (to - mDirection > 180) {
                        to -= 360;
                    } else if (to - mDirection < -180) {
                        to += 360;
                    }

                    // limit the max speed to MAX_ROTATE_DEGREE
                    float distance = to - mDirection;
                    if (abs(distance) > MAX_ROATE_DEGREE) {
                        distance = distance > 0 ? MAX_ROATE_DEGREE : (-1.0f * MAX_ROATE_DEGREE);
                    }

                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection
                            + ((to - mDirection) * mInterpolator.getInterpolation(abs(distance) > MAX_ROATE_DEGREE ? 0.4f : 0.3f)));
                    postInvalidate();
                }
                mHandler.postDelayed(mCompassViewUpdater, 16);
            }
        }
    };

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    public void compassOff() {
        if (!isOn) {
            return;
        }
        mHandler.removeCallbacks(mCompassViewUpdater);
        mSensorManager.unregisterListener(mOrientationSensorEventListener, mRotationVector);
        mSensorManager = null;
        mDirection = 0.0f;
        postInvalidate();
        isOn = false;
    }

    public void compassOn() {
        if (isOn) {
            return;
        }
        initResources();
        initServices();
        mSensorManager.registerListener(mOrientationSensorEventListener, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mHandler.postDelayed(mCompassViewUpdater, 50);
        isOn = true;
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
//        //Log.d(TAG, "widthSize: " + widthSize + " heightSize:" + heightSize
//                    + " finalMeasureSpecX:" + finalMeasureSpecX  + " finalMeasureSpecY:" + finalMeasureSpecY
//                    + " mWidth:" + mWidth  + " mHeight:" + mHeight
//                    + " mScaleFactorX:" + mScaleFactorX  + " finalMeasureSpecY:" + finalMeasureSpecY
//                    + " finalMeasureSpecX:" + finalMeasureSpecX  + " mScaleFactorY:" + mScaleFactorY);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        convertRect(compass.getWidth(), compass.getHeight(), 0, 0, matrix);
        canvas.drawBitmap(compass, matrix, null);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        compassOn();
    }

    private void initResources() {
        mDirection = 0.0f;
        mTargetDirection = 0.0f;
        mInterpolator = new AccelerateInterpolator();
    }

    private void initServices() {
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
    mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    private void convertRect(int bitmapWidth, int bitmapHeight, int left, int top, Matrix matrix) {
        matrix.reset();
        matrix.postRotate(mDirection, bitmapWidth / 2, bitmapHeight / 2);
        matrix.postScale(mScaleFactorX, mScaleFactorY);
        matrix.postTranslate(mScaleFactorX * left, mScaleFactorY * top);
    }

    protected void init() {
        offset = Settings.instance().getIntSetting(Constants.ORIENTATION) == ORIENTATION_PORTRAIT ? 0 : 270;
        compass = BitmapFactory.decodeResource(getResources(), R.drawable.compass);
        matrix = new Matrix();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        compassOff();
        if (!compass.isRecycled()) {
            compass.recycle();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}