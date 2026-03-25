package com.million.learnify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class WaveView extends View {
    private float amplitude = 50f;
    private float frequency = 1f;
    private float phase = 0f;
    private float speed = 5f;
    private boolean running = true;
    private android.os.Handler handler = new android.os.Handler();

    private Paint wavePaint, axisPaint, textPaint;
    private Path wavePath;

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.parseColor("#8B5CF6")); // Violet 500
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(5f);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(Color.LTGRAY);
        axisPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);

        wavePath = new Path();
    }

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            if (running) {
                phase -= speed * 0.05f;
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

    public void setParams(float amp, float freq) {
        this.amplitude = amp;
        this.frequency = freq;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float midY = getHeight() / 2f;
        
        // Draw Axis
        canvas.drawLine(0, midY, getWidth(), midY, axisPaint);

        wavePath.reset();
        wavePath.moveTo(0, midY);

        for (int x = 0; x <= getWidth(); x += 5) {
            float y = midY + amplitude * (float) Math.sin(2 * Math.PI * frequency * (x / (float)getWidth()) + phase);
            wavePath.lineTo(x, y);
        }

        canvas.drawPath(wavePath, wavePaint);

        canvas.drawText(String.format(Locale.US, "Amplitude: %.0f px", amplitude), 50, 50, textPaint);
        canvas.drawText(String.format(Locale.US, "Frequency: %.1f Hz", frequency), 50, 100, textPaint);
    }
}
