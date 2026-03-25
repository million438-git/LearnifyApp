package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class SpringMassView extends View {
    private float mass = 1.0f;
    private float k = 10f; // spring constant
    private float damping = 0.1f;
    private float displacement = 100f; // initial displacement
    private float velocity = 0;
    private float acceleration = 0;
    private float equilibriumY;
    private boolean running = false;
    private Handler handler = new Handler();

    private Paint springPaint, massPaint, textPaint;
    private Path springPath;

    public SpringMassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        springPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        springPaint.setColor(Color.DKGRAY);
        springPaint.setStyle(Paint.Style.STROKE);
        springPaint.setStrokeWidth(5f);

        massPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        massPaint.setColor(Color.parseColor("#10B981")); // Emerald 500
        massPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);

        springPath = new Path();
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

    public void startSimulation(float k, float mass, float damping) {
        this.k = k;
        this.mass = mass;
        this.damping = damping;
        this.displacement = 100f;
        this.velocity = 0;
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        displacement = 100f;
        velocity = 0;
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.5f;
        float forceSpring = -k * displacement;
        float forceDamping = -damping * velocity;
        float totalForce = forceSpring + forceDamping;
        
        acceleration = totalForce / mass;
        velocity += acceleration * dt;
        displacement += velocity * dt;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        equilibriumY = 200f;
        float centerX = getWidth() / 2f;
        float currentY = equilibriumY + displacement;

        // Draw ceiling
        canvas.drawLine(centerX - 100, 50, centerX + 100, 50, springPaint);

        // Draw spring (zigzag)
        springPath.reset();
        springPath.moveTo(centerX, 50);
        int segments = 15;
        float dy = (currentY - 50) / segments;
        for (int i = 1; i <= segments; i++) {
            float x = centerX + (i % 2 == 0 ? 30 : -30);
            if (i == segments) x = centerX;
            springPath.lineTo(x, 50 + i * dy);
        }
        canvas.drawPath(springPath, springPaint);

        // Draw mass
        canvas.drawRect(centerX - 40, currentY, centerX + 40, currentY + 80, massPaint);

        canvas.drawText("Displacement: " + String.format("%.2f px", displacement), 50, getHeight() - 50, textPaint);
        canvas.drawText("Velocity: " + String.format("%.2f px/s", velocity), 50, getHeight() - 100, textPaint);
    }
}
