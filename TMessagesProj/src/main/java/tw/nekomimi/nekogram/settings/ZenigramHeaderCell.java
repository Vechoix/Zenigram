package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.Locale;

public class ZenigramHeaderCell extends FrameLayout {

    private static final String CHANNEL_URL = "https://t.me/ZenigramClient";
    private static final String CREATOR_URL = "https://t.me/vechoix";

    private final Theme.ResourcesProvider resourcesProvider;
    private final LinearLayout cardView;
    private final FrameLayout logoContainer;
    private final BackupImageView logoView;
    private final TextView titleView;
    private final TextView versionChipView;
    private final ImageView channelButton;
    private final ImageView creatorButton;

    public ZenigramHeaderCell(@NonNull Context context) {
        this(context, null);
    }

    public ZenigramHeaderCell(@NonNull Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;

        cardView = new LinearLayout(context) {
            private final RectF rect = new RectF();
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                rect.set(AndroidUtilities.dp(0.5f), AndroidUtilities.dp(0.5f), w - AndroidUtilities.dp(0.5f), h - AndroidUtilities.dp(0.5f));
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int bgColor = Theme.getColor(Theme.key_windowBackgroundWhite, ZenigramHeaderCell.this.resourcesProvider);
                bgPaint.setColor(bgColor);
                canvas.drawRoundRect(rect, AndroidUtilities.dp(20), AndroidUtilities.dp(20), bgPaint);

                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(AndroidUtilities.dp(1));
                strokePaint.setColor(Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, ZenigramHeaderCell.this.resourcesProvider), Theme.isCurrentThemeDark() ? 0.08f : 0.06f));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(20), AndroidUtilities.dp(20), strokePaint);

                super.onDraw(canvas);
            }
        };
        cardView.setOrientation(LinearLayout.HORIZONTAL);
        cardView.setGravity(Gravity.CENTER_VERTICAL);
        cardView.setWillNotDraw(false);
        cardView.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(22), AndroidUtilities.dp(16), AndroidUtilities.dp(22));
        addView(cardView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 16, 12, 16, 12));

        logoContainer = new FrameLayout(context) {
            private final RectF rect = new RectF();
            private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                rect.set(0, 0, w, h);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int headerColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, ZenigramHeaderCell.this.resourcesProvider);
                int glowColor = Theme.multAlpha(headerColor, Theme.isCurrentThemeDark() ? 0.16f : 0.12f);
                bgPaint.setColor(glowColor);
                canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), bgPaint);
                super.onDraw(canvas);
            }
        };
        logoContainer.setWillNotDraw(false);

        logoView = new BackupImageView(context);
        logoView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.zenigram_header_logo));
        logoContainer.addView(logoView, LayoutHelper.createFrame(52, 52, Gravity.CENTER));

        cardView.addView(logoContainer, LayoutHelper.createLinear(56, 56, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_VERTICAL);

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setSingleLine();
        titleView.setText("Zenigram");
        titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        textContainer.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START));

        versionChipView = new TextView(context);
        versionChipView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
        versionChipView.setSingleLine();
        versionChipView.setText("v" + BuildConfig.VERSION_NAME);
        versionChipView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
        versionChipView.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(6), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider), 0.08f)));
        versionChipView.setPadding(AndroidUtilities.dp(6), AndroidUtilities.dp(2), AndroidUtilities.dp(6), AndroidUtilities.dp(2));
        textContainer.addView(versionChipView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.START, 0, 4, 0, 0));

        cardView.addView(textContainer, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f, Gravity.CENTER_VERTICAL, 14, 0, 8, 0));

        LinearLayout buttonsContainer = new LinearLayout(context);
        buttonsContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonsContainer.setGravity(Gravity.CENTER_VERTICAL);

        int iconColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
        int buttonBgColor = Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider), 0.06f);
        int selectorColor = Theme.getColor(Theme.key_listSelector, resourcesProvider);

        channelButton = new ImageView(context);
        channelButton.setScaleType(ImageView.ScaleType.CENTER);
        channelButton.setImageResource(R.drawable.msg_channel);
        channelButton.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
        channelButton.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(36), buttonBgColor, selectorColor));
        channelButton.setContentDescription("Official Channel @ZenigramClient");
        channelButton.setOnClickListener(v -> Browser.openUrl(getContext(), CHANNEL_URL));
        buttonsContainer.addView(channelButton, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL, 0, 0, 6, 0));

        creatorButton = new ImageView(context);
        creatorButton.setScaleType(ImageView.ScaleType.CENTER);
        creatorButton.setImageResource(R.drawable.msg_openprofile);
        creatorButton.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
        creatorButton.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(36), buttonBgColor, selectorColor));
        creatorButton.setContentDescription("Creator @vechoix");
        creatorButton.setOnClickListener(v -> Browser.openUrl(getContext(), CREATOR_URL));
        buttonsContainer.addView(creatorButton, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        cardView.addView(buttonsContainer, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
    }

    public void updateColors() {
        if (titleView != null) {
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        }
        if (versionChipView != null) {
            versionChipView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText, resourcesProvider));
            versionChipView.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(6), Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider), 0.08f)));
        }
        int iconColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon, resourcesProvider);
        int buttonBgColor = Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider), 0.06f);
        int selectorColor = Theme.getColor(Theme.key_listSelector, resourcesProvider);
        if (channelButton != null) {
            channelButton.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            channelButton.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(36), buttonBgColor, selectorColor));
        }
        if (creatorButton != null) {
            creatorButton.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            creatorButton.setBackground(Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(36), buttonBgColor, selectorColor));
        }
        if (cardView != null) {
            cardView.invalidate();
        }
        if (logoContainer != null) {
            logoContainer.invalidate();
        }
    }
}
