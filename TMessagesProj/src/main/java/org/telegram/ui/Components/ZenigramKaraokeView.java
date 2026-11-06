package org.telegram.ui.Components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import org.telegram.messenger.AndroidUtilities;
import tw.nekomimi.nekogram.helpers.ZenigramLrcParser;

import java.util.ArrayList;
import java.util.List;

public class ZenigramKaraokeView extends View {

    public interface OnLineClickListener {
        void onLineClick(int lineIndex, ZenigramLrcParser.LrcLine line);
    }

    private static class FormattedLine {
        ZenigramLrcParser.LrcLine lrcLine;
        int rawLineIndex;
        int subLineIndex;
        int totalSubLines;
        StaticLayout layout;
        int topY;
        int height;
    }

    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private List<ZenigramLrcParser.LrcLine> rawLines = new ArrayList<>();
    private final List<FormattedLine> formattedLines = new ArrayList<>();
    private String plainLyrics;

    private int activeLineIndex = -1;
    private float activeLineProgress = 0f;

    private float scrollY = 0f;
    private float targetScrollY = 0f;
    private ValueAnimator scrollAnimator;
    private final Scroller scroller;

    private final int paddingHorizontal = AndroidUtilities.dp(24);
    private final int lineSpacing = AndroidUtilities.dp(16);

    private OnLineClickListener onLineClickListener;
    private final GestureDetector gestureDetector;
    private boolean isUserScrolling = false;
    private long lastUserScrollTime = 0;
    private int lastMeasuredWidth = 0;

    public ZenigramKaraokeView(Context context) {
        super(context);

        scroller = new Scroller(context);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(AndroidUtilities.dp(22));
        textPaint.setTypeface(AndroidUtilities.bold());

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                // Must return true so all scroll and tap gestures are received
                if (scrollAnimator != null) {
                    scrollAnimator.cancel();
                }
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                isUserScrolling = true;
                lastUserScrollTime = SystemClock.elapsedRealtime();
                if (scrollAnimator != null) {
                    scrollAnimator.cancel();
                }
                scrollY = Math.max(0, Math.min(getMaxScroll(), scrollY + distanceY));
                invalidate();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                isUserScrolling = true;
                lastUserScrollTime = SystemClock.elapsedRealtime();
                scroller.fling(0, (int) scrollY, 0, (int) -velocityY, 0, 0, 0, (int) getMaxScroll());
                postInvalidateOnAnimation();
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (formattedLines.isEmpty() || onLineClickListener == null) {
                    return false;
                }
                float touchY = e.getY() + scrollY;
                int startY = getMeasuredHeight() / 3;
                for (int i = 0; i < formattedLines.size(); i++) {
                    FormattedLine fl = formattedLines.get(i);
                    int lineTop = startY + fl.topY;
                    int lineBottom = lineTop + fl.height;
                    if (touchY >= lineTop && touchY <= lineBottom) {
                        onLineClickListener.onLineClick(fl.rawLineIndex, fl.lrcLine);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void setOnLineClickListener(OnLineClickListener listener) {
        this.onLineClickListener = listener;
    }

    public void setLyrics(List<ZenigramLrcParser.LrcLine> lrcLines, String plainText) {
        if ("null".equalsIgnoreCase(plainText)) {
            plainText = null;
        }
        boolean isSame = (lrcLines != null && !lrcLines.isEmpty() && rawLines != null && rawLines.size() == lrcLines.size());
        if (isSame && lrcLines != null && !lrcLines.isEmpty() && rawLines != null && !rawLines.isEmpty()) {
            isSame = TextUtils.equals(rawLines.get(0).text, lrcLines.get(0).text);
        }

        this.rawLines = lrcLines != null ? lrcLines : new ArrayList<>();
        this.plainLyrics = plainText;

        rebuildLayouts();

        if (!isSame) {
            this.activeLineIndex = -1;
            this.activeLineProgress = 0f;
            this.scrollY = 0f;
            this.targetScrollY = 0f;
            if (scrollAnimator != null) {
                scrollAnimator.cancel();
            }
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        if (w != lastMeasuredWidth && w > 0) {
            lastMeasuredWidth = w;
            rebuildLayouts();
        }
    }

    private void rebuildLayouts() {
        formattedLines.clear();
        int contentWidth = getMeasuredWidth() > 0 ? getMeasuredWidth() - paddingHorizontal * 2 : AndroidUtilities.displaySize.x - paddingHorizontal * 2;
        if (contentWidth <= 0) {
            contentWidth = AndroidUtilities.dp(300);
        }

        int currentY = 0;
        if (rawLines != null && !rawLines.isEmpty()) {
            for (int r = 0; r < rawLines.size(); r++) {
                ZenigramLrcParser.LrcLine line = rawLines.get(r);
                String text = line.text != null ? line.text.trim() : "";
                if (TextUtils.isEmpty(text) || "null".equalsIgnoreCase(text)) {
                    continue;
                }

                StaticLayout temp = createStaticLayout(text, contentWidth);
                int lineCount = temp.getLineCount();

                if (lineCount <= 1) {
                    FormattedLine fl = new FormattedLine();
                    fl.lrcLine = line;
                    fl.rawLineIndex = r;
                    fl.subLineIndex = 0;
                    fl.totalSubLines = 1;
                    fl.layout = temp;
                    fl.topY = currentY;
                    fl.height = temp.getHeight() + lineSpacing;
                    currentY += fl.height;
                    formattedLines.add(fl);
                } else {
                    // Split wrapped text into distinct 1-line layouts: completely eliminates overlap and ghosting!
                    for (int l = 0; l < lineCount; l++) {
                        int start = temp.getLineStart(l);
                        int end = temp.getLineEnd(l);
                        CharSequence sub = text.subSequence(start, end).toString().trim();
                        if (TextUtils.isEmpty(sub)) continue;

                        StaticLayout single = createStaticLayout(sub, contentWidth);
                        FormattedLine fl = new FormattedLine();
                        fl.lrcLine = line;
                        fl.rawLineIndex = r;
                        fl.subLineIndex = l;
                        fl.totalSubLines = lineCount;
                        fl.layout = single;
                        fl.topY = currentY;
                        fl.height = single.getHeight() + (l == lineCount - 1 ? lineSpacing : AndroidUtilities.dp(6));
                        currentY += fl.height;
                        formattedLines.add(fl);
                    }
                }
            }
        }
    }

    private StaticLayout createStaticLayout(CharSequence text, int width) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            return StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(AndroidUtilities.dp(4), 1.0f)
                    .setIncludePad(false)
                    .build();
        } else {
            return new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, AndroidUtilities.dp(4), false);
        }
    }

    public void updateTime(long currentTimeMs) {
        if (rawLines == null || rawLines.isEmpty()) {
            return;
        }

        int newRawIndex = -1;
        float newProgress = 0f;

        for (int i = 0; i < rawLines.size(); i++) {
            ZenigramLrcParser.LrcLine line = rawLines.get(i);
            if (currentTimeMs >= line.startTimeMs && currentTimeMs < line.endTimeMs) {
                newRawIndex = i;
                newProgress = line.getProgress(currentTimeMs);
                break;
            } else if (currentTimeMs >= line.endTimeMs) {
                newRawIndex = i;
                newProgress = 1f;
            }
        }

        boolean indexChanged = (newRawIndex != activeLineIndex);
        activeLineIndex = newRawIndex;
        activeLineProgress = newProgress;

        if (indexChanged && activeLineIndex >= 0) {
            long now = SystemClock.elapsedRealtime();
            if (now - lastUserScrollTime > 3000) {
                isUserScrolling = false;
            }
            if (!isUserScrolling) {
                smoothScrollToRawLine(activeLineIndex);
            }
        }

        invalidate();
    }

    private void smoothScrollToRawLine(int rawIndex) {
        for (int i = 0; i < formattedLines.size(); i++) {
            FormattedLine fl = formattedLines.get(i);
            if (fl.rawLineIndex == rawIndex) {
                smoothScrollToFormattedLine(i);
                break;
            }
        }
    }

    private void smoothScrollToFormattedLine(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= formattedLines.size()) return;
        FormattedLine fl = formattedLines.get(lineIndex);

        int centerY = getMeasuredHeight() / 2;
        int lineCenter = getMeasuredHeight() / 3 + fl.topY + fl.height / 2;
        float newTarget = Math.max(0, Math.min(getMaxScroll(), lineCenter - centerY));

        if (Math.abs(newTarget - targetScrollY) > AndroidUtilities.dp(4)) {
            targetScrollY = newTarget;
            if (scrollAnimator != null) {
                scrollAnimator.cancel();
            }
            scrollAnimator = ValueAnimator.ofFloat(scrollY, targetScrollY);
            scrollAnimator.setDuration(450);
            scrollAnimator.setInterpolator(new DecelerateInterpolator());
            scrollAnimator.addUpdateListener(anim -> {
                scrollY = (float) anim.getAnimatedValue();
                invalidate();
            });
            scrollAnimator.start();
        }
    }

    private float getMaxScroll() {
        if (formattedLines.isEmpty()) return 0;
        FormattedLine last = formattedLines.get(formattedLines.size() - 1);
        int totalHeight = getMeasuredHeight() / 3 + last.topY + last.height + getMeasuredHeight() / 2;
        return Math.max(0, totalHeight - getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            lastUserScrollTime = SystemClock.elapsedRealtime();
        }
        return handled || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.getCurrY();
            invalidate();
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width <= 0 || height <= 0) return;

        if (!formattedLines.isEmpty()) {
            int startY = height / 3;

            for (int i = 0; i < formattedLines.size(); i++) {
                FormattedLine fl = formattedLines.get(i);
                float lineY = startY + fl.topY - scrollY;

                // Culling: skip off-screen lines
                if (lineY < -fl.height || lineY > height + fl.height) {
                    continue;
                }

                StaticLayout layout = fl.layout;
                if (layout == null) continue;

                canvas.save();
                canvas.translate(paddingHorizontal, lineY);

                if (fl.rawLineIndex == activeLineIndex) {
                    // Sequential highlighting calculation for this sub-line
                    float subProgress;
                    if (fl.totalSubLines <= 1) {
                        subProgress = activeLineProgress;
                    } else {
                        float subWindow = 1f / fl.totalSubLines;
                        float subStart = fl.subLineIndex * subWindow;
                        float subEnd = (fl.subLineIndex + 1) * subWindow;

                        if (activeLineProgress <= subStart) {
                            subProgress = 0f;
                        } else if (activeLineProgress >= subEnd) {
                            subProgress = 1f;
                        } else {
                            subProgress = (activeLineProgress - subStart) / subWindow;
                        }
                    }

                    float lineWidth = layout.getLineWidth(0);
                    if (lineWidth <= 0) lineWidth = layout.getWidth();

                    if (subProgress <= 0.001f) {
                        textPaint.setShader(null);
                        textPaint.setColor(Color.WHITE);
                        textPaint.setAlpha(0x55);
                    } else if (subProgress >= 0.999f) {
                        textPaint.setShader(null);
                        textPaint.setColor(Color.WHITE);
                        textPaint.setAlpha(255);
                    } else {
                        // Soft smooth feather transition at the singing edge
                        float sweepX = lineWidth * Math.max(0f, Math.min(1f, subProgress));
                        float feather = AndroidUtilities.dp(24);

                        float p1 = Math.max(0f, (sweepX - feather) / lineWidth);
                        float p2 = Math.min(1f, sweepX / lineWidth);

                        if (p2 > p1) {
                            LinearGradient gradient = new LinearGradient(
                                    0, 0, lineWidth, 0,
                                    new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0x55FFFFFF, 0x55FFFFFF},
                                    new float[]{0f, p1, p2, 1f},
                                    Shader.TileMode.CLAMP
                            );
                            textPaint.setShader(gradient);
                            textPaint.setAlpha(255);
                        } else {
                            textPaint.setShader(null);
                            textPaint.setColor(Color.WHITE);
                            textPaint.setAlpha(255);
                        }
                    }

                    layout.draw(canvas);
                    textPaint.setShader(null);
                } else if (fl.rawLineIndex < activeLineIndex) {
                    // Past line: clean translucent white
                    textPaint.setShader(null);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setAlpha((int) (255 * 0.45f));
                    layout.draw(canvas);
                } else {
                    // Future line: softer translucent white
                    textPaint.setShader(null);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setAlpha((int) (255 * 0.32f));
                    layout.draw(canvas);
                }

                canvas.restore();
            }
        } else if (!TextUtils.isEmpty(plainLyrics) && !"null".equalsIgnoreCase(plainLyrics.trim())) {
            // Fallback for plain unsynchronized lyrics
            String[] rawPlain = plainLyrics.split("\\r?\\n");
            int curY = AndroidUtilities.dp(40);
            int contentWidth = width - paddingHorizontal * 2;
            for (String s : rawPlain) {
                if (TextUtils.isEmpty(s) || "null".equalsIgnoreCase(s.trim())) {
                    curY += AndroidUtilities.dp(16);
                    continue;
                }
                StaticLayout layout = createStaticLayout(s.trim(), contentWidth);
                float lineY = curY - scrollY;
                if (lineY >= -layout.getHeight() && lineY <= height + layout.getHeight()) {
                    canvas.save();
                    canvas.translate(paddingHorizontal, lineY);
                    textPaint.setShader(null);
                    textPaint.setColor(Color.WHITE);
                    textPaint.setAlpha((int) (255 * 0.70f));
                    layout.draw(canvas);
                    canvas.restore();
                }
                curY += layout.getHeight() + lineSpacing;
            }
        }
    }
}
