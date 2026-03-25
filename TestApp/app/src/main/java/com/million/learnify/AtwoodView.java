package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class AtwoodView extends View {
    private float m1 = 2.0f; // kg
    private float m2 = 1.0f; // kg
    private float gravity = 9.8f;
    private float acceleration = 0;
    private float velocity = 0;
    private float y1 = 200f; // vertical position of m1
    private float y2 = 200f; // vertical position of m2
    private boolean running = false;
    private Handler handler = new Handler();

    private Paint pulleyPaint, ropePaint, massPaint, textPaint;

    public AtwoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pulleyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pulleyPaint.setColor(Color.LTGRAY);
        pulleyPaint.setStyle(Paint.Style.FILL);

        ropePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ropePaint.setColor(Color.DKGRAY);
        ropePaint.setStrokeWidth(4f);

        massPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        massPaint.setColor(Color.parseColor("#7C3AED")); // Violet 600
        massPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
    }

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (running) {
                updatePhysics();
                invalidate();
                handler.postDelayed(this, 16);
            }
        }
    };

    public void startSimulation(float m1, float m2) {
        this.m1 = m1;
        this.m2 = m2;
        this.velocity = 0;
        this.y1 = 200f;
        this.y2 = 200f;
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        velocity = 0;
        y1 = 200f;
        y2 = 200f;
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.1f;
        // a = (m1 - m2)g / (m1 + m2)
        acceleration = (m1 - m2) * gravity / (m1 + m2);
        velocity += acceleration * dt;
        
        y1 += velocity * dt * 5; // scaled
        y2 -= velocity * dt * 5; // scaled

        if (y1 > getHeight() - 100 || y1 < 100 || y2 > getHeight() - 100 || y2 < 100) {
            running = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float pulleyRadius = 50f;
        float pulleyY = 100f;

        // Draw Pulley
        canvas.drawCircle(cx, pulleyY, pulleyRadius, pulleyPaint);
        
        // Draw Rope
        canvas.drawLine(cx - pulleyRadius, pulleyY, cx - pulleyRadius, y1, ropePaint);
        canvas.drawLine(cx + pulleyRadius, pulleyY, cx + pulleyRadius, y2, ropePaint);

        // Draw Masses
        canvas.drawRect(cx - pulleyRadius - 30, y1, cx - pulleyRadius + 30, y1 + 60, massPaint);
        canvas.drawRect(cx + pulleyRadius - 30, y2, cx + pulleyRadius + 30, y2 + 60, massPaint);

        canvas.drawText(String.format(Locale.US, "M1: %.1f kg", m1), cx - 150, y1 + 30, textPaint);
        canvas.drawText(String.format(Locale.US, "M2: %.1f kg", m2), cx + 100, y2 + 30, textPaint);
        canvas.drawText(String.format(Locale.US, "Accel: %.2f m/s²", acceleration), 50, getHeight() - 50, textPaint);
        canvas.drawText(String.format(Locale.US, "Tension: %.2f N", (2 * m1 * m2 * gravity) / (m1 + m2)), 50, getHeight() - 100, textPaint);
    }
}
