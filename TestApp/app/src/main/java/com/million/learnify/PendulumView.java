package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class PendulumView extends View {
    private float length = 300f; // pixels
    private float mass = 1.0f;
    private double angle = Math.PI / 4; // 45 degrees
    private double angularVelocity = 0;
    private double angularAcceleration = 0;
    private float gravity = 9.8f;
    private float damping = 0.995f;
    private boolean running = false;
    private Handler handler = new Handler();

    private Paint linePaint, bobPaint, textPaint;

    public PendulumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(5f);

        bobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bobPaint.setColor(Color.parseColor("#EF4444")); // Red 500
        bobPaint.setStyle(Paint.Style.FILL);

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

    public void startSimulation(float length, float mass, float gravity) {
        this.length = length * 50; // Scaling
        this.mass = mass;
        this.gravity = gravity;
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        angle = Math.PI / 4;
        angularVelocity = 0;
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.5f; // Simulation time step
        angularAcceleration = - (gravity / (length / 50)) * Math.sin(angle);
        angularVelocity += angularAcceleration * dt;
        angularVelocity *= damping;
        angle += angularVelocity * dt;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float originX = getWidth() / 2f;
        float originY = 50f;

        float bobX = (float) (originX + length * Math.sin(angle));
        float bobY = (float) (originY + length * Math.cos(angle));

        canvas.drawLine(originX, originY, bobX, bobY, linePaint);
        canvas.drawCircle(bobX, bobY, 40f, bobPaint);

        canvas.drawText("Period (T) ≈ " + String.format("%.2f s", 2 * Math.PI * Math.sqrt((length/50) / gravity)), 50, getHeight() - 50, textPaint);
        canvas.drawText("Angle: " + String.format("%.1f°", Math.toDegrees(angle)), 50, getHeight() - 100, textPaint);
    }
    
    public void setLength(float length) { this.length = length * 50; invalidate(); }
}
