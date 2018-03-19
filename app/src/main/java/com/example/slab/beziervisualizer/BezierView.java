package com.example.slab.beziervisualizer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by hotstuNg on 2016/7/31.
 */
public class BezierView extends View {
    private static final String TAG = "BezierView";
    private  float capRegion = 20;
    private float density;
    private float width;
    private float height;
    private PointF[] basePoints;
    private  int N = 10;
    private PointF capturedPoint = null;
    private Paint pointPaint;
    private Paint pointLabelPaint;
    private Paint bezierCurvePathPaint;
    private Paint straightPathsPaint;
    private Path bezierCurvePath;
    private Path[] straightPaths;
    private ValueAnimator moveAnimator;
    private long duration = 3000;

    public BezierView(Context context) {
        this(context, null);
    }

    public BezierView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.density = context.getResources().getDisplayMetrics().density;
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(2* density);

        pointLabelPaint = new Paint();
        pointLabelPaint.setColor(Color.BLACK);
        pointLabelPaint.setAntiAlias(true);
        pointLabelPaint.setTextSize(40);

        capRegion = 20*density;
        bezierCurvePath = new Path();
        bezierCurvePathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bezierCurvePathPaint.setColor(Color.YELLOW);
        bezierCurvePathPaint.setStrokeWidth(2*density);
        bezierCurvePathPaint.setStyle(Paint.Style.STROKE);

        straightPathsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        straightPathsPaint.setColor(Color.CYAN);
        straightPathsPaint.setStrokeWidth(1*density);
        straightPathsPaint.setStyle(Paint.Style.STROKE);

    }

    public void setLevel(int level) {
        int old = getLevel();
        this.N = level;
        if (old != getLevel()) {
            buildPoints(false);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public int getLevel() {
        return this.N;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    /**
     *
     *
     * @param from
     * @return
     */
    private PointF generateRandomPoint(PointF from) {
        if (from == null) {
            from = new PointF(width*.5f, height*.5f);
        }
        float Rw = (float) (width / 5 * ( 2 * Math.random() + 1.5f));
        float Rh = (float) (height / 5 * ( 2 * Math.random() + 1.5f));
        PointF result = new PointF();
        double rad = Math.random() * 2* Math.PI;
        result.x = (float) (from.x + Rw * Math.cos(rad));
        result.y = (float) (from.y + Rh * Math.sin(rad));
        result.x = clamp(result.x, 10*density, width - 10*density);
        result.y = clamp(result.y, 10*density, height - 10*density);
        return result;
    }

    private PointF capturePoint(float x, float y) {
        RectF rectF = new RectF();
        for (PointF point : basePoints) {
            rectF.set(point.x - capRegion, point.y - capRegion, point.x + capRegion, point.y + capRegion);
            if (rectF.contains(x, y)) {
                return point;
            }
        }
        return null;
    }

    private float clamp(float value, float min, float max) {
        float d = value - min;
        if (d < 0) {
            value -= 2*d;
        }
        d = max - value;
        if (d < 0) {
            value += 2*d;
        }
        return Math.min(max, Math.max(min, value));
    }

    /**
     *
     * @param sizeChenged 是否需要重新生成各个点的位置
     */
    private void buildPoints(boolean sizeChenged) {
        boolean newCreate = (basePoints == null);
        if (basePoints == null ) {
            basePoints = new PointF[N];
        }
        if (sizeChenged || newCreate) {
            basePoints[0] = generateRandomPoint(null);
            PointF temp = basePoints[0];
            for (int i = 1; i < basePoints.length; i++) {
                basePoints[i] = generateRandomPoint(temp);
                //theta = (float) (Math.atan2(( temp.y - array[i].y ), ( temp.x - array[i].x)));
                temp = basePoints[i];
            }
        }
        else {
            PointF[] oldArray = basePoints;
            basePoints = new PointF[N];
            int i;
            for(i = 0; i < basePoints.length && i < oldArray.length; i++) {
                basePoints[i] = oldArray[i];
            }
            for(; i < basePoints.length; i++) {
                if (i == 0) {
                    basePoints[i] = generateRandomPoint(null);
                } else {
                    basePoints[i] = generateRandomPoint(basePoints[i - 1]);
                }
            }
        }

    }

    /**
     * call when N is changed, the array of paths need rebuild
     */
    private void rewindPaths() {
        Path[] oldPaths = straightPaths;
        straightPaths = new Path[N];
        if (oldPaths == null) {
            return;
        }
        for (int i = 0; i < straightPaths.length && i < oldPaths.length; i++) {
            straightPaths[i] = oldPaths[i];
        }

    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                capturedPoint = capturePoint(x ,y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (capturedPoint != null) {
                    capturedPoint.x = clamp(x, 10 * density, width - 10 * density);
                    capturedPoint.y = clamp(y, 10 * density, height - 10 * density);
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {

                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (capturedPoint != null) {
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    showAnimate();
                }
                capturedPoint = null;
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;

        buildPoints(true);
        showAnimate();
    }

    private void showAnimate() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (moveAnimator != null && moveAnimator.isStarted()) {
            moveAnimator.removeAllUpdateListeners();
            moveAnimator.removeAllListeners();
            moveAnimator.cancel();
        }
        bezierCurvePath.reset();
        bezierCurvePath.moveTo(basePoints[0].x, basePoints[0].y);
        moveAnimator = ValueAnimator.ofFloat(0, 1);
        moveAnimator.setDuration(duration);

        final float[] deepCopyX = new float[basePoints.length];
        final float[] deepCopyY = new float[basePoints.length];
        final float[] dpX = new float[basePoints.length];
        final float[] dpY = new float[basePoints.length];
        rewindPaths();
        if (straightPaths[0] == null)
            straightPaths[0] = new Path();
        straightPaths[0].reset();
        for (int i = 0; i < basePoints.length; i++) {
            deepCopyX[i] = basePoints[i].x;
            dpX[i] = basePoints[i].x;
            deepCopyY[i] = basePoints[i].y;
            dpY[i] = basePoints[i].y;
            if (i == 0) {
                straightPaths[0].moveTo(deepCopyX[i], deepCopyY[i]);
            } else {
                straightPaths[0].lineTo(deepCopyX[i], deepCopyY[i]);
            }
        }
        final float[] ret = new float[2];

        moveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                compute(deepCopyX, deepCopyY, dpX, dpY, ret, animation.getAnimatedFraction());
                bezierCurvePath.lineTo(ret[0], ret[1]);
                ViewCompat.postInvalidateOnAnimation(BezierView.this);
            }
        });
        moveAnimator.start();
    }

    private float[] compute(float[] deepcopyX, float[] deepcopyY, float[] dpx, float[] dpy, float[] result, float fraciton) {
        System.arraycopy(deepcopyX, 0, dpx, 0, deepcopyX.length);
        System.arraycopy(deepcopyY, 0, dpy, 0, deepcopyY.length);
        for (int i = 1; i < dpx.length; i++) {
            for(int j = 0; j < dpx.length - i; j++){
                dpx[j] = dpx[j] + (dpx[j+1] - dpx[j]) * fraciton;
                dpy[j] = dpy[j] + (dpy[j+1] - dpy[j]) * fraciton;
            }
            if (straightPaths[i] == null)
                straightPaths[i] = new Path();
            straightPaths[i].reset();
            for (int j = 0; j < dpx.length - i; j++) {
                if (j == 0) {
                    straightPaths[i].moveTo(dpx[j], dpy[j]);
                } else {
                    straightPaths[i].lineTo(dpx[j], dpy[j]);
                }
            }
        }
        result[0] = dpx[0];
        result[1] = dpy[0];
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < basePoints.length; i++) {
            canvas.drawCircle(basePoints[i].x, basePoints[i].y, 5*density, pointPaint);
            canvas.drawText("" + i, basePoints[i].x+ 2*5*density, basePoints[i].y + 2*5*density, pointLabelPaint);
        }
        for (int i = 0; i < straightPaths.length; i++) {
            if (straightPaths[i] != null) {
                straightPathsPaint.setColor(getColor(i, straightPaths.length));
                canvas.drawPath(straightPaths[i], straightPathsPaint);
            }
        }
        canvas.drawPath(bezierCurvePath, bezierCurvePathPaint);

    }

    private int getColor(int clusterSize, int sizeRange) {
        final float hueRange = 220;
        final float size = Math.min(clusterSize, sizeRange);
        final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
        return Color.HSVToColor(new float[]{
                hue, 1f, .6f
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SaveState state = new SaveState(superState);
        state.n = N;
        state.duration = duration;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SaveState s = (SaveState) state;
        super.onRestoreInstanceState(s.getSuperState());
        setLevel(s.n);
        setDuration(s.duration);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (moveAnimator != null) {
            if (moveAnimator.isStarted()) {
                moveAnimator.cancel();
            }
            moveAnimator = null;
        }
        super.onDetachedFromWindow();
    }

    private static class SaveState extends BaseSavedState {
        int n;
        long duration;

        public SaveState(Parcelable superState) {
            super(superState);
        }

        public SaveState(Parcel source) {
            super(source);
            n = source.readInt();
            duration = source.readLong();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(n);
            out.writeLong(duration);
        }

        public static final Creator<SaveState> CREATOR
                = new Creator<SaveState>() {
            public SaveState createFromParcel(Parcel in) {
                return new SaveState(in);
            }

            public SaveState[] newArray(int size) {
                return new SaveState[size];
            }
        };
    }


}
