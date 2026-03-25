package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectileView extends View {
    private float angle = 45f;
    private float speed = 50f;
    private float gravity = 9.8f;
    private float airResistance = 0.01f; // Drag coefficient
    private float mass = 1.0f;
    private float time = 0f;
    private float vx, vy;
    private float px, py;
    private float startX = 100f, startY;
    private boolean running = false;
    private Handler handler = new Handler();
    
    private Paint ballPaint, trajectoryPaint, axisPaint, textPaint;
    private Path trajectoryPath;
    private List<float[]> points = new ArrayList<>();
    
    private float scale = 5f;

    public ProjectileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ballPaint.setColor(Color.parseColor("#3B82F6"));
        ballPaint.setStyle(Paint.Style.FILL);

        trajectoryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trajectoryPaint.setColor(Color.parseColor("#94A3B8"));
        trajectoryPaint.setStyle(Paint.Style.STROKE);
        trajectoryPaint.setStrokeWidth(4f);
        trajectoryPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30f);

        trajectoryPath = new Path();
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

    public void startSimulation(float angle, float speed, float gravity, float drag, float mass) {
        this.angle = angle;
        this.speed = speed;
        this.gravity = gravity;
        this.airResistance = drag;
        this.mass = mass;
        
        running = true;
        time = 0;
        px = 0;
        py = 0;
        
        // Initial velocity components
        vx = (float) (speed * Math.cos(Math.toRadians(angle)));
        vy = (float) (speed * Math.sin(Math.toRadians(angle)));
        
        points.clear();
        trajectoryPath.reset();
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    public void reset() {
        running = false;
        time = 0; px = 0; py = 0;
        points.clear();
        trajectoryPath.reset();
        invalidate();
    }

    private void updatePhysics() {
        float dt = 0.1f;
        time += dt;

        // F_drag = -k * v
        float ax = - (airResistance / mass) * vx;
        float ay = - gravity - (airResistance / mass) * vy;

        vx += ax * dt;
        vy += ay * dt;

        px += vx * dt;
        py += vy * dt;

        points.add(new float[]{px, py});
        
        if (py < -5 && time > 0.5) {
            running = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        startY = getHeight() - 150f;

        canvas.drawLine(startX, startY, getWidth() - 50, startY, axisPaint);
        canvas.drawLine(startX, startY, startX, 50, axisPaint);

        trajectoryPath.reset();
        if (!points.isEmpty()) {
            trajectoryPath.moveTo(startX, startY);
            for (float[] p : points) {
                float drawX = startX + p[0] * scale;
                float drawY = startY - p[1] * scale;
                trajectoryPath.lineTo(drawX, drawY);
            }
            canvas.drawPath(trajectoryPath, trajectoryPaint);
        }

        float ballX = startX + px * scale;
        float ballY = startY - py * scale;
        if (ballY > startY) ballY = startY;
        canvas.drawCircle(ballX, ballY, 20f, ballPaint);

        canvas.drawText(String.format(Locale.US, "Range: %.2f m", px), 150, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "Altitude: %.2f m", Math.max(0, py)), 150, 150, textPaint);
        canvas.drawText(String.format(Locale.US, "Time: %.2f s", time), 150, 200, textPaint);
    }
    
    public void setAngle(float angle) { this.angle = angle; invalidate(); }
    public void setSpeed(float speed) { this.speed = speed; invalidate(); }
}
