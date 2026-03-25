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

public class ModernPhysicsView extends View {
    private int mode = 1; // 1: Photoelectric, 2: Decay, 3: Relativity
    private float intensity = 50f;
    private float frequency = 1f;
    private float velocity = 0.5f; // fraction of c
    private float halfLife = 50f;
    private long startTime;
    private boolean running = false;
    private List<Nucleus> nuclei = new ArrayList<>();
    private Random random = new Random();
    private android.os.Handler handler = new android.os.Handler();

    private Paint particlePaint, textPaint, bgPaint;

    public ModernPhysicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(35f);
        bgPaint = new Paint();
    }

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (running) {
                invalidate();
                handler.postDelayed(this, 32);
            }
        }
    };

    public void startSimulation(int mode) {
        this.mode = mode;
        this.running = true;
        this.startTime = System.currentTimeMillis();
        if (mode == 2) { // Decay
            nuclei.clear();
            for (int i = 0; i < 100; i++) nuclei.add(new Nucleus());
        }
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == 1) drawPhotoelectric(canvas);
        else if (mode == 2) drawDecay(canvas);
        else if (mode == 3) drawRelativity(canvas);
    }

    private void drawPhotoelectric(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Draw Metal Plate
        bgPaint.setColor(Color.LTGRAY);
        canvas.drawRect(cx - 100, cy - 150, cx + 100, cy + 150, bgPaint);

        // Draw Photons (Light)
        particlePaint.setColor(Color.YELLOW);
        float time = (System.currentTimeMillis() - startTime) / 1000f;
        for (int i = 0; i < intensity/10; i++) {
            float px = (time * 200 + i * 50) % (cx - 100);
            canvas.drawCircle(px, cy + (i * 30 - 75), 10f, particlePaint);
        }

        // Draw Electrons
        if (frequency > 0.5f) { // threshold
            particlePaint.setColor(Color.BLUE);
            for (int i = 0; i < intensity/10; i++) {
                float ex = cx + 100 + ((time * 300 + i * 40) % 400);
                canvas.drawCircle(ex, cy + (i * 30 - 75), 8f, particlePaint);
            }
        }

        canvas.drawText("Photoelectric Effect", 50, 50, textPaint);
        canvas.drawText(String.format(Locale.US, "KE_max = hf - Φ ≈ %.2f eV", Math.max(0, frequency * 2 - 1)), 50, 100, textPaint);
    }

    private void drawDecay(Canvas canvas) {
        float elapsed = (System.currentTimeMillis() - startTime) / 100f;
        int activeCount = 0;
        for (Nucleus n : nuclei) {
            if (!n.decayed && random.nextFloat() < 0.005f / (halfLife/50)) {
                n.decayed = true;
            }
            if (!n.decayed) activeCount++;
            
            particlePaint.setColor(n.decayed ? Color.RED : Color.GREEN);
            canvas.drawCircle(n.x * getWidth(), n.y * getHeight(), 10f, particlePaint);
        }

        canvas.drawText("Radioactive Decay", 50, 50, textPaint);
        canvas.drawText("Remaining: " + activeCount, 50, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "N(t) = N0 * e^(-λt)"), 50, 150, textPaint);
    }

    private void drawRelativity(Canvas canvas) {
        float L0 = 400f;
        float gamma = (float) (1.0 / Math.sqrt(1 - velocity * velocity));
        float L = L0 / gamma;

        bgPaint.setColor(Color.GRAY);
        canvas.drawRect(100, 200, 100 + L0, 300, bgPaint);
        canvas.drawText("Proper Length (L0)", 100, 180, textPaint);

        bgPaint.setColor(Color.BLUE);
        canvas.drawRect(100, 400, 100 + L, 500, bgPaint);
        canvas.drawText("Contracted Length (L)", 100, 380, textPaint);

        canvas.drawText(String.format(Locale.US, "Velocity: %.2f c", velocity), 50, 50, textPaint);
        canvas.drawText(String.format(Locale.US, "γ (Lorentz Factor): %.3f", gamma), 50, 100, textPaint);
        canvas.drawText(String.format(Locale.US, "Time Dilation: Δt = γΔt0"), 50, getHeight() - 50, textPaint);
    }

    private class Nucleus {
        float x = random.nextFloat();
        float y = random.nextFloat();
        boolean decayed = false;
    }

    public void setIntensity(float i) { this.intensity = i; invalidate(); }
    public void setFrequency(float f) { this.frequency = f; invalidate(); }
    public void setVelocity(float v) { this.velocity = v; invalidate(); }
}
