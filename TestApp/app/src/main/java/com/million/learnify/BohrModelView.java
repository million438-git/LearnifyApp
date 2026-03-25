package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class BohrModelView extends View {
    private int n = 1; // Principal quantum number
    private float electronAngle = 0;
    private boolean running = true;
    private android.os.Handler handler = new android.os.Handler();

    private Paint orbitPaint, nucleusPaint, electronPaint, textPaint;

    public BohrModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        orbitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        orbitPaint.setColor(Color.LTGRAY);
        orbitPaint.setStyle(Paint.Style.STROKE);
        orbitPaint.setStrokeWidth(2f);
        orbitPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        nucleusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nucleusPaint.setColor(Color.parseColor("#EF4444")); // Red 500
        nucleusPaint.setStyle(Paint.Style.FILL);

        electronPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        electronPaint.setColor(Color.parseColor("#3B82F6")); // Blue 500
        electronPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(35f);
    }

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (running) {
                // Speed decreases with n
                electronAngle += 0.05f / n;
                invalidate();
                handler.postDelayed(this, 16);
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(updateTask);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateTask);
    }

    public void setN(int n) {
        this.n = n;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Draw Nucleus
        canvas.drawCircle(cx, cy, 30f, nucleusPaint);
        canvas.drawText("+", cx - 10, cy + 10, textPaint);

        // Draw Orbits
        for (int i = 1; i <= 4; i++) {
            float radius = i * 80f;
            canvas.drawCircle(cx, cy, radius, orbitPaint);
            canvas.drawText("n=" + i, cx + radius - 20, cy - 10, textPaint);
        }

        // Draw Electron
        float currentRadius = n * 80f;
        float ex = cx + currentRadius * (float) Math.cos(electronAngle);
        float ey = cy + currentRadius * (float) Math.sin(electronAngle);
        canvas.drawCircle(ex, ey, 15f, electronPaint);

        // Energy Calculation: E = -13.6 / n^2 eV
        double energy = -13.6 / (n * n);
        canvas.drawText(String.format(Locale.US, "Energy: %.2f eV", energy), 50, 50, textPaint);
        canvas.drawText(String.format(Locale.US, "Shell: %s", (n == 1 ? "K" : n == 2 ? "L" : n == 3 ? "M" : "N")), 50, 100, textPaint);
    }
}
