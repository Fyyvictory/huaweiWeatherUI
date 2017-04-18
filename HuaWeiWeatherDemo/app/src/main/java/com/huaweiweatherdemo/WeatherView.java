package com.huaweiweatherdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by SH on 2017/3/24.
 */

public class WeatherView extends View {

    private int startAngle;// 圆弧开始角
    private int sweepAngle; // 圆弧总角数
    private int count; // 刻度的份数
    private int offSet;

    private int currentTemp; // 当前的角度
    private Bitmap bitmap;
    private int ocAngle; // 0度初始的角度
    private int fgAngle; // 总覆盖的角度
    private int minTemp, maxTemp; // 当天最低最高温度
    private Paint mArcPaint;
    private Paint mLinePaint;
    private TextPaint mTextPaint;
    private Paint mPointPaint;

    private int mWidth, mHeight, radius;

    public WeatherView(Context context) {
        this(context, null);
    }

    public WeatherView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context mContext) {
        initPaint();

        startAngle = 120;
        sweepAngle = 300;
        count = 60;

        currentTemp = 26;
        maxTemp = 28;
        minTemp = 20;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.skyfullstar);
        ocAngle = 240;
        fgAngle = 90;
        offSet = 22;
    }

    private void initPaint() {
        mArcPaint = new Paint();
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setStrokeWidth(2);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStrokeWidth(4);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(144);

        mPointPaint = new Paint();
        mPointPaint.setAntiAlias(true);
        mPointPaint.setColor(Color.WHITE);
        mPointPaint.setStrokeWidth(2);
        mPointPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wrap_len = 500;
        int width = measureDimension(wrap_len, widthMeasureSpec);
        int height = measureDimension(wrap_len, heightMeasureSpec);
        int len = Math.min(width, height);

        setMeasuredDimension(len, len);
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(defaultSize);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize; // unspecified
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();
        radius = (mWidth - getPaddingLeft() - getPaddingRight()) / 2 - 1; // 半径
        canvas.translate(mWidth / 2, mHeight / 2);  // 参照原点改变，如果还有别的，应该先save，再translate，还可以restore返回到上次save的状态

        //drawArcView(canvas); // 画圆环
        drawLine(canvas); // 画短线
        drawTextBitmapView(canvas); //画中间的温度及它下面的画
        drawTempLineView(canvas); // 画动态的温度
    }

    private void drawTempLineView(Canvas mCanvas) {
        mTextPaint.setTextSize(28);
        int startTempAngle = getStartAngle(minTemp, maxTemp);
        mCanvas.drawText(minTemp + "°", getRealCosX(startTempAngle, offSet, true), getRealSinY(startTempAngle, offSet, true), mTextPaint);
        mCanvas.drawText(maxTemp + "°", getRealCosX(startTempAngle + fgAngle, offSet, true), getRealSinY(startTempAngle + fgAngle, offSet, true), mTextPaint);

        int circleAngle = startTempAngle + (currentTemp - minTemp) * fgAngle / (maxTemp - minTemp);
        mPointPaint.setColor(getRealColor(minTemp, maxTemp));
        mCanvas.drawCircle(getRealCosX(circleAngle, 50, false), getRealSinY(circleAngle, 50, false), 7, mPointPaint);
    }

    private int getRealColor(int minTemp, int maxTemp) {
        if (maxTemp <= 0)
            return Color.parseColor("#00008b");//深海蓝
        else if (minTemp <= 0 && maxTemp > 0)
            return Color.parseColor("#4169e1"); // 黄君兰
        else if (minTemp >= 0 && minTemp < 15)
            return Color.parseColor("#40e0d0"); // 宝石绿
        else if (minTemp >= 15 && minTemp < 25)
            return Color.parseColor("#00ff00"); //酸橙绿
        else if (minTemp >= 25 && minTemp < 30)
            return Color.parseColor("#ffd700"); //金黄色
        else if (minTemp >= 30)
            return Color.parseColor("#cd5c5c"); // 印度红
        return Color.parseColor("#00ff00"); // 酸橙绿
    }

    private float getCosX(int Angle) {
        return (float) (radius * Math.cos(Angle * Math.PI / 180)) + getTextPaintOffset(mTextPaint);
    }

    private float getSinY(int Angle) {
        return (float) (radius * Math.sin(Angle * Math.PI / 180)) + getTextPaintOffset(mTextPaint);
    }

    private float getRealCosX(int Angle, int mOffSet, boolean outOff) {
        if (!outOff)
            mOffSet = -mOffSet;
        if (getCosX(Angle) < 0)
            return getCosX(Angle) - mOffSet;
        else
            return getCosX(Angle) + mOffSet;
    }

    private float getRealSinY(int Angle, int mOffSet, boolean outOff) {
        if (!outOff)
            mOffSet = -mOffSet;
        if (getSinY(Angle) < 0)
            return getSinY(Angle) - mOffSet;
        else
            return getSinY(Angle) + mOffSet;
    }

    private int getStartAngle(int mMinTemp, int mMaxTemp) {
        int startFgAngle = 0;
        if (minTemp >= maxTemp) {
            Log.e("weather", "getstartangle---fail");
            return startFgAngle;
        }
        if (minTemp <= 0)
            startFgAngle = ocAngle - (0 - minTemp) * fgAngle / (maxTemp - minTemp);
        else
            startFgAngle = ocAngle + (minTemp - 0) * fgAngle / (maxTemp - minTemp);
        if (startFgAngle <= startAngle)
            startFgAngle = startAngle + 10;
        else if ((startFgAngle + fgAngle) >= (startAngle + sweepAngle))
            startFgAngle = startAngle + sweepAngle - 20 - fgAngle;
        return startFgAngle;
    }

    private void drawTextBitmapView(Canvas mCanvas) {
        mTextPaint.setTextSize(144);
        mCanvas.drawText(currentTemp + "°", 0, 0 + getTextPaintOffset(mTextPaint), mTextPaint);
        mCanvas.drawBitmap(bitmap, 0 - bitmap.getWidth() / 2, radius - bitmap.getHeight(), null);
    }

    public void setBitmap(Bitmap mBitmap) {
        this.bitmap = mBitmap;
        invalidate();
    }

    private void drawLine(Canvas mCanvas) {
        mCanvas.save();
        float angle = sweepAngle / count; // 刻度间隔
        mCanvas.rotate(-270 + startAngle);
        for (int i = 0; i < count; i++) {
            if (i == 0 || i == count - 1) {
                mLinePaint.setStrokeWidth(1);
                mLinePaint.setColor(Color.WHITE);
                mCanvas.drawLine(0, -radius, 0, -radius + 45, mLinePaint);
            } else if (i >= getStartLineIndex(minTemp, maxTemp) && i <= getEndLineIndex(minTemp, maxTemp)) {
                mLinePaint.setStrokeWidth(4);
                mLinePaint.setColor(getRealColor(minTemp, maxTemp));
                mCanvas.drawLine(0, -radius, 0, -radius + 35, mLinePaint);
            } else {
                mLinePaint.setStrokeWidth(2);
                mLinePaint.setColor(Color.WHITE);
                mCanvas.drawLine(0, -radius, 0, -radius + 35, mLinePaint);
            }
            mCanvas.rotate(angle);
        }
        mCanvas.restore();
    }

    public float getTextPaintOffset(Paint mPaint) {
        Paint.FontMetricsInt mInt = mPaint.getFontMetricsInt();
        return -mInt.descent + (mInt.bottom - mInt.top) / 2;
    }

    private int getEndLineIndex(int minTemp, int maxTemp) {
        return (getStartAngle(minTemp, maxTemp) - startAngle) / (sweepAngle / count) + fgAngle / (sweepAngle / count);
    }

    private int getStartLineIndex(int minTemp, int maxTemp) {
        return (getStartAngle(minTemp, maxTemp) - startAngle) / (sweepAngle / count);
    }

    private void drawArcView(Canvas mCanvas) {
        RectF mRectF = new RectF(-radius, -radius, radius, radius);
        mCanvas.drawArc(mRectF, startAngle, sweepAngle, false, mArcPaint);
    }
}
