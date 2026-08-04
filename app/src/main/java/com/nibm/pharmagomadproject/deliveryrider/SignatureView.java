package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private boolean hasSignature = false;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(8f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
        canvas.drawPath(path, paint);
    }

    private float lastX, lastY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                lastX = x;
                lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
                lastX = x;
                lastY = y;
                hasSignature = true;
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(lastX, lastY);
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void clear() {
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
            hasSignature = false;
            invalidate();
        }
    }

    public boolean hasSignature() {
        return hasSignature;
    }

    public Bitmap getSignatureBitmap() {
        if (!hasSignature) return null;
        Bitmap mergedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mergedCanvas = new Canvas(mergedBitmap);
        mergedCanvas.drawColor(Color.WHITE); // White background
        mergedCanvas.drawBitmap(bitmap, 0, 0, null);
        return mergedBitmap;
    }
}
