package com.suppresswarnings.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.suppresswarnings.android.R;

import java.util.concurrent.atomic.AtomicInteger;

public class FloatingView extends View {

    public int height = 120;
    public int width = 120;
    private Paint paint;
    private Bitmap bitmap;
    static AtomicInteger index = new AtomicInteger(0);
    public FloatingView(Context context) {
        super(context);
        paint = new Paint();
        bitmap= BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.ic_launcher);
        width = bitmap.getWidth();
        height = bitmap.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(height, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap,0, 0, paint);
    }

}