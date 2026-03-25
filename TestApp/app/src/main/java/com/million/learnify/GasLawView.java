package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GasLawView extends View {
    private float pressure = 1.0f; // atm
    private float volume = 2.0f; // L
    private float temperature = 300f; // K
    private int particleCount = 20;
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private boolean running = false;
    private android.os.Handler handler = new android.os.Handler();

    private Paint containerPaint, particlePaint, textPaint;

    public GasLawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        containerPaint.setColor(Color.BLACK);
        containerPaint.setStyle(Paint.Style.STROKE);
        containerPaint.setStrokeWidth(5f);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(Color.parseColor("#3B82F6"));
        particlePaint.setStyle(Paint.Style.FILL);

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

    public void startSimulation(float P, float V, float T) {
        this.pressure = P;
        this.volume = V;
        this.temperature = T;
        initParticles();
        running = true;
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < particleCount; i++) {
            particles.add(new Particle());
        }
    }

    private void updatePhysics() {
        float containerWidth = (getWidth() - 100) * (volume / 5.0f);
        float containerHeight = getHeight() - 200;
        float speedScale = temperature / 300f;

        for (Particle p : particles) {
            p.x += p.vx * speedScale;
            p.y += p.vy * speedScale;

            if (p.x < 50 || p.x > 50 + containerWidth) { p.vx = -p.vx; p.x = Math.max(50, Math.min(p.x, 50 + containerWidth)); }
            if (p.y < 50 || p.y > 50 + containerHeight) { p.vy = -p.vy; p.y = Math.max(50, Math.min(p.y, 50 + containerHeight)); }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float containerWidth = (getWidth() - 100) * (volume / 5.0f);
        float containerHeight = getHeight() - 200;

        canvas.drawRect(50, 50, 50 + containerWidth, 50 + containerHeight, containerPaint);

        for (Particle p : particles) {
            canvas.drawCircle(p.x, p.y, 8f, particlePaint);
        }

        canvas.drawText(String.format(Locale.US, "P: %.2f atm | V: %.2f L | T: %.0f K", pressure, volume, temperature), 50, getHeight() - 50, textPaint);
    }

    private class Particle {
        float x, y, vx, vy;
        Particle() {
            x = 100 + random.nextFloat() * 200;
            y = 100 + random.nextFloat() * 200;
            vx = (random.nextFloat() - 0.5f) * 10;
            vy = (random.nextFloat() - 0.5f) * 10;
        }
    }

    public void setVolume(float v) { this.volume = v; invalidate(); }
    public void setTemperature(float t) { this.temperature = t; invalidate(); }
}
