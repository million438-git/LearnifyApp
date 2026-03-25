package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class CollisionView extends View {
    private float m1 = 1.0f, m2 = 1.0f;
    private float v1 = 5.0f, v2 = -2.0f;
    private float x1, x2;
    private float r1 = 40f, r2 = 40f;
    private boolean running = false;
    private Handler handler = new Handler();
    private Paint p1, p2, textPaint;

    public CollisionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setColor(Color.parseColor("#F59E0B")); // Amber 500
        p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(Color.parseColor("#8B5CF6")); // Violet 500
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

    public void startSimulation(float m1, float m2, float v1, float v2) {
        this.m1 = m1; this.m2 = m2;
        this.v1 = v1; this.v2 = v2;
        this.x1 = 100;
        this.x2 = getWidth() - 100;
        if (this.x2 <= 0) this.x2 = 500; // Fallback for early calls
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        x1 = 100;
        x2 = getWidth() - 100;
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.5f;
        x1 += v1 * dt;
        x2 += v2 * dt;

        // Collision detection
        if (x1 + r1 >= x2 - r2) {
            // Elastic collision formula
            float newV1 = ((m1 - m2) * v1 + 2 * m2 * v2) / (m1 + m2);
            float newV2 = (2 * m1 * v1 + (m2 - m1) * v2) / (m1 + m2);
            v1 = newV1;
            v2 = newV2;
            // Prevent sticking
            x1 = x2 - r2 - r1 - 1;
        }

        // Boundary check
        if (x1 - r1 < 0) { x1 = r1; v1 = -v1; }
        if (x2 + r2 > getWidth()) { x2 = getWidth() - r2; v2 = -v2; }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float y = getHeight() / 2f;
        canvas.drawCircle(x1, y, r1, p1);
        canvas.drawCircle(x2, y, r2, p2);

        canvas.drawText(String.format(Locale.US, "v1: %.2f m/s", v1), 50, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "v2: %.2f m/s", v2), getWidth() - 250, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "Total Momentum: %.2f", (m1*v1 + m2*v2)), 50, getHeight() - 50, textPaint);
    }
}
