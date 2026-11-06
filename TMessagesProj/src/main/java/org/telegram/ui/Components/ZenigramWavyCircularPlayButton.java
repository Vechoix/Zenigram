package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.Theme;

public class ZenigramWavyCircularPlayButton extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path wavePath = new Path();
    private final Path playTrianglePath = new Path();
    private final RectF pauseBar1 = new RectF();
    private final RectF pauseBar2 = new RectF();

    private float progress = 0f;
    private int accentColor = 0;
    private boolean isPaused = true;

    public ZenigramWavyCircularPlayButton(Context context) {
        super(context);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(AndroidUtilities.dp(2.0f));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(AndroidUtilities.dp(2.0f));
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);

        iconPaint.setStyle(Paint.Style.FILL);

        setOnClickListener(v -> {
            MessageObject playing = MediaController.getInstance().getPlayingMessageObject();
            if (playing != null) {
                if (MediaController.getInstance().isMessagePaused()) {
                    MediaController.getInstance().playMessage(playing);
                    updateState(false, progress);
                } else {
                    MediaController.getInstance().pauseMessage(playing);
                    updateState(true, progress);
                }
            }
        });
    }

    public void setAccentColor(int color) {
        this.accentColor = color;
        invalidate();
    }

    public void updateState(boolean isPaused, float progress) {
        this.isPaused = isPaused;
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float baseR = AndroidUtilities.dp(11.5f);

        int color = (accentColor != 0) ? accentColor : Theme.getColor(Theme.key_inappPlayerPlayPause);

        // When playing: gentle subtle wave. When paused: smooth clean circle (matching Screenshot 5)
        float amp = (!isPaused) ? AndroidUtilities.dp(0.6f) : 0f;
        int lobes = 8;

        // Background track (full 360 loop)
        trackPaint.setColor(color);
        trackPaint.setAlpha(45);

        wavePath.reset();
        float stepDeg = 4.0f;
        boolean first = true;
        for (float angleDeg = 0; angleDeg <= 360f; angleDeg += stepDeg) {
            float angleRad = (float) Math.toRadians(angleDeg);
            float wave = (amp > 0) ? (float) Math.sin(angleRad * lobes) : 0f;
            float r = baseR + amp * wave;
            float x = cx + (float) Math.cos(angleRad) * r;
            float y = cy + (float) Math.sin(angleRad) * r;
            if (first) {
                wavePath.moveTo(x, y);
                first = false;
            } else {
                wavePath.lineTo(x, y);
            }
        }
        wavePath.close();
        canvas.drawPath(wavePath, trackPaint);

        // Progress arc (active portion)
        if (progress > 0.005f) {
            wavePaint.setColor(color);
            wavePaint.setAlpha(255);
            wavePath.reset();

            float sweepAngle = progress * 360f;
            float startAngleDeg = -90f;
            float endAngleDeg = startAngleDeg + sweepAngle;
            first = true;

            for (float angleDeg = startAngleDeg; angleDeg <= endAngleDeg; angleDeg += stepDeg) {
                float angleRad = (float) Math.toRadians(angleDeg);
                float wave = (amp > 0) ? (float) Math.sin(angleRad * lobes) : 0f;
                float r = baseR + amp * wave;
                float x = cx + (float) Math.cos(angleRad) * r;
                float y = cy + (float) Math.sin(angleRad) * r;
                if (first) {
                    wavePath.moveTo(x, y);
                    first = false;
                } else {
                    wavePath.lineTo(x, y);
                }
            }
            canvas.drawPath(wavePath, wavePaint);
        }

        // Draw Play / Pause Icon in center (Direct canvas drawing for 100% sync and zero stuck states)
        iconPaint.setColor(color);
        iconPaint.setAlpha(255);

        if (isPaused) {
            // Draw Play Triangle
            playTrianglePath.reset();
            float halfH = AndroidUtilities.dp(5.5f);
            float halfW = AndroidUtilities.dp(4.5f);
            float opticalShift = AndroidUtilities.dp(0.8f);
            playTrianglePath.moveTo(cx - halfW + opticalShift, cy - halfH);
            playTrianglePath.lineTo(cx + halfW + opticalShift, cy);
            playTrianglePath.lineTo(cx - halfW + opticalShift, cy + halfH);
            playTrianglePath.close();
            canvas.drawPath(playTrianglePath, iconPaint);
        } else {
            // Draw Two Pause Bars
            float barW = AndroidUtilities.dp(2.4f);
            float barH = AndroidUtilities.dp(10.0f);
            float gap = AndroidUtilities.dp(3.0f);
            float radius = AndroidUtilities.dp(1.2f);

            float b1Left = cx - gap / 2f - barW;
            float b1Top = cy - barH / 2f;
            pauseBar1.set(b1Left, b1Top, b1Left + barW, b1Top + barH);
            canvas.drawRoundRect(pauseBar1, radius, radius, iconPaint);

            float b2Left = cx + gap / 2f;
            float b2Top = cy - barH / 2f;
            pauseBar2.set(b2Left, b2Top, b2Left + barW, b2Top + barH);
            canvas.drawRoundRect(pauseBar2, radius, radius, iconPaint);
        }
    }
}
