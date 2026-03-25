package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class InclinedPlaneView extends View {
    private float angle = 30f; // degrees
    private float mass = 1.0f;
    private float mu = 0.2f; // friction coefficient
    private float gravity = 9.8f;
    private float acceleration = 0;
    private float velocity = 0;
    private float distance = 0;
    private boolean running = false;
    private Handler handler = new Handler();

    private Paint planePaint, blockPaint, textPaint;
    private Path planePath;

    public InclinedPlaneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        planePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        planePaint.setColor(Color.LTGRAY);
        planePaint.setStrokeWidth(5f);
        planePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockPaint.setColor(Color.parseColor("#F43F5E")); // Rose 500
        blockPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);

        planePath = new Path();
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

    public void startSimulation(float angle, float mass, float mu) {
        this.angle = angle;
        this.mass = mass;
        this.mu = mu;
        this.distance = 0;
        this.velocity = 0;
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        distance = 0;
        velocity = 0;
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.1f;
        float angleRad = (float) Math.toRadians(angle);
        float gSin = (float) (gravity * Math.sin(angleRad));
        float gCos = (float) (gravity * Math.cos(angleRad));
        float frictionAcc = mu * gCos;
        
        acceleration = gSin - frictionAcc;
        if (acceleration < 0) acceleration = 0;

        velocity += acceleration * dt;
        distance += velocity * dt;

        if (distance > 500) { // End of plane
            running = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startX = 100f;
        float startY = getHeight() - 100f;
        float planeLength = 600f;
        float angleRad = (float) Math.toRadians(angle);
        
        float endX = (float) (startX + planeLength * Math.cos(angleRad));
        float endY = (float) (startY - planeLength * Math.sin(angleRad));

        // Draw Wedge
        planePath.reset();
        planePath.moveTo(startX, startY);
        planePath.lineTo(endX, startY);
        planePath.lineTo(endX, endY);
        planePath.close();
        canvas.drawPath(planePath, planePaint);

        // Draw Block
        float blockX = (float) (endX - (distance + 40) * Math.cos(angleRad));
        float blockY = (float) (endY + (distance + 40) * Math.sin(angleRad));
        
        canvas.save();
        canvas.rotate(-angle, blockX, blockY);
        canvas.drawRect(blockX - 30, blockY - 30, blockX + 30, blockY, blockPaint);
        canvas.restore();

        canvas.drawText(String.format(Locale.US, "Acceleration: %.2f m/s²", acceleration), 50, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "Velocity: %.2f m/s", velocity), 50, 150, textPaint);
        canvas.drawText(String.format(Locale.US, "Distance: %.2f m", distance/10), 50, 200, textPaint);
    }
    
    public void setAngle(float angle) { this.angle = angle; invalidate(); }
}
