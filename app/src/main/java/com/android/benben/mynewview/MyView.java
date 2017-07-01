package com.android.benben.mynewview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.text.DecimalFormat;

/**
 * Time      2017/6/29 14:53 .
 * Author   : LiYuanXiong.
 * Content  :
 */

public class MyView extends View {
    private static final String TAG = "lyx";
    private Paint mPaint, mPaint1; //扇形画笔
    private int background1, background2;//扇形画笔的颜色

    private Paint mTextPaint;//画文字的笔
    private String text = "";//文本
    private int textColor;//文字的颜色
    private int textSize;//字体的大小

    private Paint mWirePaint;//画线的笔
    private int wireColor;//线的颜色
    private int wireWidth;//线的宽度

    private int proportionColor;// 百分比颜色
    private int proportionSize;//百分比大小

    private int radius = 1000;

    private float max;
    private float min;

    private int mHeight, mWidth;//宽高
    private int centerX, centerY;//中心坐标

    int x = 0;//弧度1的弧度
    int y = 0;//弧度2的弧度

    private Rect mBound;

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getNature(context, attrs, defStyleAttr);

        init();
    }

    private void getNature(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MyView, defStyleAttr, 0);
        int n = array.getIndexCount();//获取自定义属性的量
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.MyView_text://文本
                    text = array.getString(attr);
                    break;
                case R.styleable.MyView_textColor://文本颜色
                    textColor = array.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.MyView_textSize://文本大小
                    // 默认设置为16sp，TypeValue也可以把sp转化为px
                    textSize = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.MyView_background1://背景颜色1
                    background1 = array.getColor(attr, Color.BLUE);
                    break;
                case R.styleable.MyView_background2://背景颜色2
                    background2 = array.getColor(attr, Color.RED);
                    break;

                case R.styleable.MyView_proportionSize://百分比大小
                    proportionSize =
                            array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.MyView_proportionColor://百分比颜色
                    proportionColor = array.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.MyView_wireColor://线的颜色
                    wireColor = array.getColor(attr, Color.WHITE);
                    break;
                case R.styleable.MyView_wireWidth://先的宽度
                    wireWidth = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.MyView_radius://半径
                    radius = array.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.MyView_max://最大值
                    max = array.getFloat(attr, 1);
                    break;
                case R.styleable.MyView_min://最小值
                    min = array.getFloat(attr, 1);
                    break;
            }
        }
        array.recycle();
    }

    private void init() {

        /*弧度1*/
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(background1);
        mPaint.setStrokeCap(Paint.Cap.ROUND);


        /*弧度2*/
        mPaint1 = new Paint();
        mPaint1.setAntiAlias(true);
        mPaint1.setColor(background2);
        mPaint1.setStrokeCap(Paint.Cap.ROUND);

        /*文字*/
        mTextPaint = new Paint();
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);

        /*线*/
        mWirePaint = new Paint();
        mWirePaint.setStrokeWidth(wireWidth);
        mWirePaint.setColor(wireColor);
        mWirePaint.setAntiAlias(true);

        mBound = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(heightMeasureSpec);

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                mHeight = heightSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                mHeight = 100;
                break;
        }

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                mWidth = widthSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                mWidth = 100;
                break;
        }
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = (getRight() - getLeft()) / 2;
        centerY = (getBottom() - getTop()) / 2;

        int min = mHeight > mWidth ? mWidth : mHeight;
        if (radius > min / 2) {
            radius = (int) ((min - getPaddingTop() - getPaddingBottom()) / 3.5);
        }



        /*画圆*/
        canvas.save();
        drawCircle(canvas);
        canvas.restore();

        /*画字*/
        canvas.save();
        drawText(canvas);
        canvas.restore();

        /*画线*/
        canvas.save();
        drawLine(canvas);
        canvas.restore();


        /*画百分比*/
        canvas.save();
        drawProportion(canvas);
        canvas.restore();

    }


    /**
     * 画圆
     *
     * @param canvas 画板
     */
    private void drawCircle(Canvas canvas) {
        RectF rect = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        canvas.drawArc(rect, -90, x, true, mPaint);

        if (x <= 360 * min / max) {
            x += 3.6;
            postInvalidateDelayed(10);
            y = x;
        } else {
            canvas.drawArc(rect, x - 90, y - x, true, mPaint1);
            if (y <= 360) {
                y += 3.6;
                postInvalidateDelayed(10);
            }
        }
    }

    /**
     * 画线和线上文字
     *
     * @param canvas 画板
     */
    private void drawLine(Canvas canvas) {


        int i1X = centerX + (mBound.width() / 4);
        int i1Y = centerY + (radius / 2) - mBound.height();
        int i2X = centerX + (mBound.width() / 4) - radius;
        int i2Y = centerY + (radius / 2) - mBound.height() - (radius / 2);
        int i3X = centerX + (mBound.width() / 4) - radius * 7 / 4;
        int i3Y = i2Y;
        mWirePaint.setColor(Color.LTGRAY);
        mWirePaint.setTextSize(textSize);
        canvas.drawCircle(i1X, i1Y, 6,
                mWirePaint);//点
        canvas.drawLine(i1X, i1Y,
                i2X, i2Y,
                mWirePaint);//斜线
        canvas.drawLine(i2X, i2Y,
                i3X, i3Y,
                mWirePaint);//横线
        canvas.drawText("金额：" + min + "万",
                i3X, i3Y - mBound.height() / 2,
                mWirePaint);// 文字


        int i4X = centerX - mBound.width() * 3 / 4;
        int i4Y = centerY + ((radius + mBound.height()) / 2) - 3;

        canvas.drawCircle(i4X, i4Y, 6, mWirePaint);//点
        canvas.drawLine(i4X, i4Y, i3X, i4Y, mWirePaint);//横线
        canvas.drawText("金额：" + max + "万", i3X, i4Y - mBound.height() / 2, mWirePaint);
    }

    /**
     * 画字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(textSize);
        mTextPaint.getTextBounds(text, 0, text.length(), mBound);
        canvas.drawText(text, centerX - (mBound.width() / 2), centerY + ((radius + mBound.height()) / 2), mTextPaint);

    }


    /**
     * 百分比
     *
     * @param canvas 画板
     */
    private void drawProportion(Canvas canvas) {

        String p = new DecimalFormat(".00").format(min * 100 / max);//构造方法的字符格式这里如果小数不足2位,会以0补足.

        String text = String.valueOf(p + "%");
        mTextPaint.setColor(proportionColor);
        mTextPaint.setTextSize(proportionSize);
        mTextPaint.getTextBounds(text, 0, text.length(), mBound);
        mTextPaint.setColor(Color.WHITE);
        canvas.drawText(text, centerX - (mBound.width()) / 2, centerY + (mBound.height()) / 2, mTextPaint);
    }

    public void setSize(float max, float min) {
        this.max = max;
        this.min = min;
        invalidate();
    }

}
