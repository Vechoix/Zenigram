package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class ActivityWindowEmptyBackgroundDrawable extends Drawable {
    private ColorFilter mColorFilter;
    private int mAlpha = 255;
    private Drawable iconDrawable;

    public ActivityWindowEmptyBackgroundDrawable() {
        try {
            if (ApplicationLoader.applicationContext != null) {
                iconDrawable = ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.mipmap.ic_launcher);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int bgColor = Theme.getColor(Theme.key_windowBackgroundWhite);
        canvas.drawColor(bgColor);

        if (iconDrawable == null && ApplicationLoader.applicationContext != null) {
            try {
                iconDrawable = ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.mipmap.ic_launcher);
            } catch (Throwable ignored) {}
        }

        if (iconDrawable != null) {
            android.graphics.Rect b = getBounds();
            int cx = b.centerX();
            int cy = b.centerY();
            int size = AndroidUtilities.dp(84);
            iconDrawable.setBounds(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
            iconDrawable.setAlpha(mAlpha);
            if (mColorFilter != null) {
                iconDrawable.setColorFilter(mColorFilter);
            }
            iconDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        if (iconDrawable != null) {
            iconDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        if (iconDrawable != null) {
            iconDrawable.setColorFilter(colorFilter);
        }
    }

    @Nullable
    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
