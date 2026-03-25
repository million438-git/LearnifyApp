package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class OpticsView extends View {
    private float n1 = 1.0f; // Air
    private float n2 = 1.5f; // Glass / Medium
    private float incidentAngle = 45f; // degrees
    private float focalLength = 150f;
    private float objectDistance = 250f;
    private int mode = 1; // 1: Snell's Law, 2: Lens Ray Diagram

    private Paint rayPaint, normalPaint, interfacePaint, textPaint, lensPaint;

    public OpticsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rayPaint.setColor(Color.RED);
        rayPaint.setStrokeWidth(4f);

        normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        normalPaint.setColor(Color.GRAY);
        normalPaint.setStrokeWidth(2f);
        normalPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        interfacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        interfacePaint.setColor(Color.parseColor("#94A3B8"));
        interfacePaint.setStrokeWidth(6f);

        lensPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lensPaint.setColor(Color.parseColor("#93C5FD"));
        lensPaint.setAlpha(150);
        lensPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        lensPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
    }

    public void setSnellParams(float n2, float angle) {
        this.mode = 1;
        this.n2 = n2;
        this.incidentAngle = angle;
        invalidate();
    }

    public void setLensParams(float focal, float objDist) {
        this.mode = 2;
        this.focalLength = focal;
        this.objectDistance = objDist;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == 1) drawSnell(canvas);
        else if (mode == 2) drawLens(canvas);
    }

    private void drawSnell(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        Paint p1 = new Paint(); p1.setColor(Color.parseColor("#F1F5F9"));
        canvas.drawRect(0, 0, getWidth(), cy, p1);
        Paint p2 = new Paint(); p2.setColor(Color.parseColor("#E2E8F0"));
        canvas.drawRect(0, cy, getWidth(), getHeight(), p2);

        canvas.drawLine(0, cy, getWidth(), cy, interfacePaint);
        canvas.drawLine(cx, 0, cx, getHeight(), normalPaint);

        float angleRad = (float) Math.toRadians(incidentAngle);
        float rayLen = 400f;
        float xIn = cx - (float)(rayLen * Math.sin(angleRad));
        float yIn = cy - (float)(rayLen * Math.cos(angleRad));
        canvas.drawLine(xIn, yIn, cx, cy, rayPaint);

        float sinTheta2 = (n1 * (float)Math.sin(angleRad)) / n2;
        if (sinTheta2 <= 1.0f) {
            float angle2Rad = (float) Math.asin(sinTheta2);
            float xOut = cx + (float)(rayLen * Math.sin(angle2Rad));
            float yOut = cy + (float)(rayLen * Math.cos(angle2Rad));
            canvas.drawLine(cx, cy, xOut, yOut, rayPaint);
            canvas.drawText(String.format(Locale.US, "θ2: %.1f°", Math.toDegrees(angle2Rad)), cx + 50, cy + 100, textPaint);
        } else {
            float xOut = cx + (float)(rayLen * Math.sin(angleRad));
            float yOut = cy - (float)(rayLen * Math.cos(angleRad));
            canvas.drawLine(cx, cy, xOut, yOut, rayPaint);
            canvas.drawText("Total Internal Reflection", cx + 50, cy - 100, textPaint);
        }
        canvas.drawText(String.format(Locale.US, "n1 = %.2f | n2 = %.2f", n1, n2), 50, 50, textPaint);
    }

    private void drawLens(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Optical Axis
        canvas.drawLine(0, cy, getWidth(), cy, normalPaint);

        // Draw Lens (Convex)
        Path path = new Path();
        path.moveTo(cx, cy - 150);
        path.quadTo(cx + 40, cy, cx, cy + 150);
        path.quadTo(cx - 40, cy, cx, cy - 150);
        canvas.drawPath(path, lensPaint);

        // Foci
        canvas.drawCircle(cx - focalLength, cy, 5, textPaint);
        canvas.drawCircle(cx + focalLength, cy, 5, textPaint);
        canvas.drawText("F", cx - focalLength - 10, cy + 30, textPaint);
        canvas.drawText("F'", cx + focalLength - 10, cy + 30, textPaint);

        // Object
        float objX = cx - objectDistance;
        float objH = 80f;
        canvas.drawLine(objX, cy, objX, cy - objH, interfacePaint);
        canvas.drawCircle(objX, cy - objH, 5, interfacePaint);

        // Ray 1: Parallel to axis
        canvas.drawLine(objX, cy - objH, cx, cy - objH, rayPaint);
        canvas.drawLine(cx, cy - objH, cx + focalLength * 2, cy + objH, rayPaint);

        // Ray 2: Through optical center
        canvas.drawLine(objX, cy - objH, cx + (cx - objX), cy + objH, rayPaint);

        // Image calculation: 1/f = 1/v + 1/u (u is negative)
        // 1/v = 1/f - 1/u = 1/f + 1/|u|
        float v = (focalLength * objectDistance) / (objectDistance - focalLength);
        float mag = -v / objectDistance;
        float imgH = mag * objH;

        if (v > 0) { // Real image
            float imgX = cx + v;
            canvas.drawLine(imgX, cy, imgX, cy + imgH, interfacePaint);
            canvas.drawText("Real Image", imgX - 50, cy + imgH + 40, textPaint);
        } else { // Virtual image
            canvas.drawText("Virtual Image", 50, 100, textPaint);
        }

        canvas.drawText(String.format(Locale.US, "f = %.0f | u = -%.0f | v = %.1f", focalLength, objectDistance, v), 50, 50, textPaint);
    }
}
