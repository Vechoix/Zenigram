package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class ZenigramIntroLogoView extends View {

    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint planePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path planePath = new Path();
    private final Path shadePath = new Path();
    private final RectF bounds = new RectF();

    public ZenigramIntroLogoView(Context context) {
        super(context);
        basePaint.setStyle(Paint.Style.FILL);
        planePaint.setStyle(Paint.Style.FILL);
        shadePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - AndroidUtilities.dp(6);

        int accent = Theme.getColor(Theme.key_chats_actionBackground);
        if (accent == 0) accent = 0xFFC4A98F; // Soft Pixel Sand

        basePaint.setColor(Theme.blendOver(accent, 0x33000000));
        planePaint.setColor(0xFFFFFFFF);
        shadePaint.setColor(0xCCFFFFFF);

        // Soft outer glowing ring
        canvas.drawCircle(cx, cy, radius, basePaint);

        // Modern exteraGram styled rounded delta airplane
        canvas.save();
        canvas.translate(cx - AndroidUtilities.dp(2), cy);
        float scale = radius / AndroidUtilities.dp(34);
        canvas.scale(scale, scale);

        // Plane main body
        planePath.reset();
        planePath.moveTo(AndroidUtilities.dp(16), -AndroidUtilities.dp(14));
        planePath.lineTo(-AndroidUtilities.dp(15), -AndroidUtilities.dp(2));
        planePath.lineTo(-AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        planePath.lineTo(AndroidUtilities.dp(16), -AndroidUtilities.dp(14));
        planePath.close();
        canvas.drawPath(planePath, planePaint);

        // Plane folded wing / bottom fin
        shadePath.reset();
        shadePath.moveTo(AndroidUtilities.dp(16), -AndroidUtilities.dp(14));
        shadePath.lineTo(-AndroidUtilities.dp(4), AndroidUtilities.dp(4));
        shadePath.lineTo(-AndroidUtilities.dp(2), AndroidUtilities.dp(14));
        shadePath.lineTo(AndroidUtilities.dp(4), AndroidUtilities.dp(7));
        shadePath.lineTo(AndroidUtilities.dp(16), -AndroidUtilities.dp(14));
        shadePath.close();
        canvas.drawPath(shadePath, shadePaint);

        canvas.restore();
    }
}
