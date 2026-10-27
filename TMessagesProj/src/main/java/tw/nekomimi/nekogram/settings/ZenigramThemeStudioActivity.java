package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tw.nekomimi.nekogram.NekoConfig;

public class ZenigramThemeStudioActivity extends BaseNekoSettingsActivity {

    private final int dynamicMonetRow = rowId++;
    private final int accentColorRow = rowId++;
    private final int outBubbleColorRow = rowId++;
    private final int interfaceDensityRow = rowId++;
    private final int animationSpeedRow = rowId++;
    private final int resetDefaultsRow = rowId++;

    public static final int[] ACCENT_COLORS = new int[]{
            0,          // Default (follows theme)
            0xFFC4A98F, // Pixel Sand / Desert (Default soft warm Pixel)
            0xFF1B6EF3, // Pixel Blue
            0xFF3B693A, // Pixel Sage
            0xFF6750A4, // Pixel Lavender
            0xFF8C4F3B, // Pixel Peach
            0xFF924443, // Pixel Coral
            0xFF7E5700, // Pixel Amber
            0xFF2E6B5E, // Pixel Mint
            0xFF45464F, // Pixel Slate
            0xFFE28C72, // Pixel Terracotta
            0xFFB4C5E4  // Pixel Fog Blue
    };

    public static final String[] ACCENT_NAMES = new String[]{
            "Default",
            "Pixel Sand",
            "Pixel Blue",
            "Pixel Sage",
            "Pixel Lavender",
            "Pixel Peach",
            "Pixel Coral",
            "Pixel Amber",
            "Pixel Mint",
            "Pixel Slate",
            "Pixel Terracotta",
            "Pixel Fog Blue"
    };

    public static final int[] BUBBLE_COLORS = new int[]{
            0,          // Default (follows theme)
            0xFF2C6BC4, // Pixel Blue
            0xFF46654C, // Pixel Sage
            0xFF2E6B5E, // Pixel Mint
            0xFF61558E, // Pixel Lavender
            0xFF525E75, // Pixel Iris
            0xFF8C4F3B, // Pixel Peach
            0xFF924443, // Pixel Coral
            0xFF775A22, // Pixel Sand
            0xFF45464F, // Pixel Slate
            0xFF55624C  // Pixel Olive
    };

    public static String getBubbleColorName(int color) {
        String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
        boolean ru = "ru".equals(lang) || "uk".equals(lang) || "be".equals(lang);
        if (color == 0) return ru ? "По умолчанию" : "Default";
        if (color == 0xFF2C6BC4) return ru ? "Небесно-голубой" : "Pixel Blue";
        if (color == 0xFF46654C) return ru ? "Шалфей" : "Pixel Sage";
        if (color == 0xFF2E6B5E) return ru ? "Мятный" : "Pixel Mint";
        if (color == 0xFF61558E) return ru ? "Лаванда" : "Pixel Lavender";
        if (color == 0xFF525E75) return ru ? "Ирис" : "Pixel Iris";
        if (color == 0xFF8C4F3B) return ru ? "Персик" : "Pixel Peach";
        if (color == 0xFF924443) return ru ? "Коралл" : "Pixel Coral";
        if (color == 0xFF775A22) return ru ? "Песочный" : "Pixel Sand";
        if (color == 0xFF45464F) return ru ? "Графит" : "Pixel Slate";
        if (color == 0xFF55624C) return ru ? "Олива" : "Pixel Olive";
        return ru ? "Пользовательский" : "Custom";
    }

    public static String getAccentName(int index) {
        String lang = LocaleController.getInstance().getCurrentLocale().getLanguage();
        if ("ru".equals(lang) || "uk".equals(lang) || "be".equals(lang)) {
            return switch (index) {
                case 0 -> "По умолчанию";
                case 1 -> "Песочный Pixel";
                case 2 -> "Синий Pixel";
                case 3 -> "Шалфей";
                case 4 -> "Лаванда";
                case 5 -> "Персик";
                case 6 -> "Коралл";
                case 7 -> "Янтарь";
                case 8 -> "Мята";
                case 9 -> "Графит";
                case 10 -> "Терракота";
                case 11 -> "Туманный синий";
                default -> "По умолчанию";
            };
        }
        if (index >= 0 && index < ACCENT_NAMES.length) {
            return ACCENT_NAMES[index];
        }
        return ACCENT_NAMES[0];
    }

    public static void applyThemeMode(int mode) {
        NekoConfig.setThemeMode(mode);
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        SharedPreferences themeConfig = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Context.MODE_PRIVATE);

        if (mode == NekoConfig.THEME_MODE_SYSTEM) {
            Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_SYSTEM;
            preferences.edit().putInt("selectedAutoNightType", Theme.AUTO_NIGHT_TYPE_SYSTEM).apply();
            Theme.saveAutoNightThemeConfig();
            Theme.checkAutoNightThemeConditions(true);
        } else if (mode == NekoConfig.THEME_MODE_LIGHT) {
            Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
            preferences.edit().putInt("selectedAutoNightType", Theme.AUTO_NIGHT_TYPE_NONE).apply();

            Theme.ThemeInfo lightTheme = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && NekoConfig.dynamicMonet) {
                lightTheme = Theme.getTheme("Monet Light");
            }
            if (lightTheme == null) {
                lightTheme = Theme.getTheme("Blue");
            }
            if (lightTheme == null) {
                lightTheme = Theme.getTheme("Day");
            }
            if (lightTheme != null) {
                preferences.edit().putString("theme", lightTheme.getKey()).apply();
                themeConfig.edit().putString("lastDayTheme", lightTheme.getKey()).apply();
                Theme.setCurrentDayTheme(lightTheme);
                Theme.applyTheme(lightTheme, false);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, lightTheme, false, null, -1);
            }
        } else { // THEME_MODE_DARK
            Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
            preferences.edit().putInt("selectedAutoNightType", Theme.AUTO_NIGHT_TYPE_NONE).apply();

            Theme.ThemeInfo darkTheme = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && NekoConfig.dynamicMonet) {
                darkTheme = Theme.getTheme("Monet Dark");
            }
            if (darkTheme == null) {
                darkTheme = Theme.getTheme("Dark Blue");
            }
            if (darkTheme == null) {
                darkTheme = Theme.getTheme("Night");
            }
            if (darkTheme != null) {
                preferences.edit()
                        .putString("theme", darkTheme.getKey())
                        .putString("nighttheme", darkTheme.getKey())
                        .apply();
                themeConfig.edit()
                        .putString("lastDarkTheme", darkTheme.getKey())
                        .apply();
                Theme.setCurrentNightTheme(darkTheme);
                Theme.applyTheme(darkTheme, true);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, darkTheme, true, null, -1);
            }
        }
    }

    public static void checkAndApplyThemeStartup() {
        try {
            int mode = NekoConfig.themeMode;
            if (mode == NekoConfig.THEME_MODE_DARK) {
                if (!Theme.getActiveTheme().isDark()) {
                    applyThemeMode(NekoConfig.THEME_MODE_DARK);
                }
            } else if (mode == NekoConfig.THEME_MODE_LIGHT) {
                if (Theme.getActiveTheme().isDark()) {
                    applyThemeMode(NekoConfig.THEME_MODE_LIGHT);
                }
            } else if (mode == NekoConfig.THEME_MODE_SYSTEM) {
                if (Theme.selectedAutoNightType != Theme.AUTO_NIGHT_TYPE_SYSTEM) {
                    Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_SYSTEM;
                    MessagesController.getGlobalMainSettings().edit().putInt("selectedAutoNightType", Theme.AUTO_NIGHT_TYPE_SYSTEM).apply();
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.ZenigramThemeStudio);
    }

    private String getInterfaceDensityName() {
        return switch (NekoConfig.interfaceDensity) {
            case NekoConfig.DENSITY_COMPACT -> LocaleController.getString(R.string.ThemeStudioDensityCompact);
            case NekoConfig.DENSITY_SPACIOUS -> LocaleController.getString(R.string.ThemeStudioDensitySpacious);
            default -> LocaleController.getString(R.string.ThemeStudioDensityDefault);
        };
    }

    private String getAnimationSpeedName() {
        if (NekoConfig.animationSpeedScale == 0.0f) {
            return LocaleController.getString(R.string.ThemeStudioAnimationSpeedOff);
        } else if (NekoConfig.animationSpeedScale <= 0.6f) {
            return LocaleController.getString(R.string.ThemeStudioAnimationSpeedFast);
        } else if (NekoConfig.animationSpeedScale >= 1.4f) {
            return LocaleController.getString(R.string.ThemeStudioAnimationSpeedRelaxed);
        }
        return LocaleController.getString(R.string.ThemeStudioAnimationSpeedNormal);
    }

    private String getAccentColorName() {
        for (int i = 0; i < ACCENT_COLORS.length; i++) {
            if (ACCENT_COLORS[i] == NekoConfig.customAccentColor) {
                return getAccentName(i);
            }
        }
        return getAccentName(0);
    }

    private void applyLiveThemeToCurrentScreen() {
        if (actionBar != null) {
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector), false);
            actionBar.setTitleColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        }
        if (fragmentView != null) {
            fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        }
        if (listView != null) {
            listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            if (listView.adapter != null) {
                listView.adapter.update(true);
            }
            listView.invalidate();
        }
        if (bubbleRadiusSliderCell != null) {
            bubbleRadiusSliderCell.updateColors();
        }
        if (typographySelectorCell != null) {
            typographySelectorCell.updateState();
        }
        if (parentLayout != null) {
            parentLayout.rebuildAllFragmentViews(false, false);
        }
    }

    private BubbleRadiusSliderCell bubbleRadiusSliderCell;
    private TypographySelectorCell typographySelectorCell;

    private class BubbleRadiusSliderCell extends FrameLayout {
        private final TextView titleView;
        private final TextView valueBadge;
        private final PixelWavySeekBarView seekBar;
        private final int minRadius = 0;
        private final int maxRadius = 28;

        public BubbleRadiusSliderCell(Context context) {
            super(context);
            setWillNotDraw(false);
            setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(12), AndroidUtilities.dp(18), AndroidUtilities.dp(12));

            LinearLayout headerLayout = new LinearLayout(context);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setGravity(Gravity.CENTER_VERTICAL);

            titleView = new TextView(context);
            titleView.setText("Скругление углов сообщений");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            headerLayout.addView(titleView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f));

            int currentRadius = NekoConfig.bubbleRadiusIn >= 0 ? NekoConfig.bubbleRadiusIn : (SharedConfig.bubbleRadius <= 0 ? 16 : SharedConfig.bubbleRadius);

            valueBadge = new TextView(context);
            valueBadge.setText(currentRadius + " dp");
            valueBadge.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            valueBadge.setTypeface(AndroidUtilities.bold());
            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            valueBadge.setTextColor(accent != 0 ? accent : 0xFFC4A98F);
            valueBadge.setBackground(null);
            valueBadge.setPadding(0, 0, 0, 0);
            headerLayout.addView(valueBadge, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            addView(headerLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 0, 0, 0));

            seekBar = new PixelWavySeekBarView(context);
            seekBar.setProgress((currentRadius - minRadius) / (float) (maxRadius - minRadius));
            seekBar.setOnProgressChangeListener(progress -> {
                int radius = Math.round(minRadius + (maxRadius - minRadius) * progress);
                valueBadge.setText(radius + " dp");
                NekoConfig.setBubbleRadiusIn(radius);
                NekoConfig.setBubbleRadiusOut(radius);
                SharedConfig.bubbleRadius = radius;
                MessagesController.getGlobalMainSettings().edit().putInt("bubbleRadius", radius).apply();
                Theme.invalidateChatOutBubble();
                Theme.refreshThemeColors();
                if (parentLayout != null) {
                    parentLayout.rebuildAllFragmentViews(false, false);
                }
            });
            addView(seekBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.TOP, 0, 30, 0, 0));
        }

        public void updateColors() {
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            valueBadge.setTextColor(accent != 0 ? accent : 0xFFC4A98F);
            valueBadge.setBackground(null);
            seekBar.updateColors();
            seekBar.invalidate();
        }
    }

    private class PixelWavySeekBarView extends View {
        private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint unselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint innerThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path wavePath = new Path();

        private float progress = 0.5f;
        private boolean isDragging = false;
        private OnProgressChangeListener listener;
        private int lastStep = -1;

        public interface OnProgressChangeListener {
            void onProgress(float progress);
        }

        public PixelWavySeekBarView(Context context) {
            super(context);
            wavePaint.setStyle(Paint.Style.STROKE);
            wavePaint.setStrokeWidth(AndroidUtilities.dp(4));
            wavePaint.setStrokeCap(Paint.Cap.ROUND);
            wavePaint.setStrokeJoin(Paint.Join.ROUND);

            unselectedPaint.setStyle(Paint.Style.STROKE);
            unselectedPaint.setStrokeWidth(AndroidUtilities.dp(4));
            unselectedPaint.setStrokeCap(Paint.Cap.ROUND);

            thumbPaint.setStyle(Paint.Style.FILL);
            innerThumbPaint.setStyle(Paint.Style.FILL);
            innerThumbPaint.setColor(0xFFFFFFFF);
            updateColors();
        }

        public void setOnProgressChangeListener(OnProgressChangeListener listener) {
            this.listener = listener;
        }

        public void setProgress(float p) {
            this.progress = Math.max(0f, Math.min(1f, p));
            invalidate();
        }

        public void updateColors() {
            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            if (accent == 0) accent = 0xFFC4A98F;
            wavePaint.setColor(accent);
            thumbPaint.setColor(accent);
            unselectedPaint.setColor(Theme.multAlpha(accent, 0.25f));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            if (width <= 0) return;

            float thumbRadius = AndroidUtilities.dp(9);
            float trackLeft = thumbRadius + AndroidUtilities.dp(4);
            float trackRight = width - thumbRadius - AndroidUtilities.dp(4);
            float trackWidth = trackRight - trackLeft;
            float centerY = height / 2.0f;
            float thumbX = trackLeft + trackWidth * progress;

            if (thumbX < trackRight) {
                canvas.drawLine(thumbX, centerY, trackRight, centerY, unselectedPaint);
            }

            wavePath.reset();
            wavePath.moveTo(trackLeft, centerY);
            float waveLength = AndroidUtilities.dp(18);
            float waveAmplitude = AndroidUtilities.dp(3.5f);

            if (thumbX > trackLeft) {
                float step = AndroidUtilities.dp(2);
                for (float x = trackLeft; x <= thumbX; x += step) {
                    float phase = (x - trackLeft) / waveLength * (float) (Math.PI * 2);
                    float taper = Math.min(1.0f, Math.min((x - trackLeft) / AndroidUtilities.dp(8), (thumbX - x) / AndroidUtilities.dp(8)));
                    if (taper < 0) taper = 0;
                    float y = centerY + (float) Math.sin(phase) * (waveAmplitude * taper);
                    wavePath.lineTo(x, y);
                }
                wavePath.lineTo(thumbX, centerY);
                canvas.drawPath(wavePath, wavePaint);
            }

            canvas.drawCircle(thumbX, centerY, thumbRadius, thumbPaint);
            canvas.drawCircle(thumbX, centerY, AndroidUtilities.dp(3), innerThumbPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float thumbRadius = AndroidUtilities.dp(9);
            float trackLeft = thumbRadius + AndroidUtilities.dp(4);
            float trackRight = getWidth() - thumbRadius - AndroidUtilities.dp(4);
            float trackWidth = trackRight - trackLeft;
            if (trackWidth <= 0) return false;

            float x = event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDragging = true;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    updateTouchProgress(x, trackLeft, trackWidth);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        updateTouchProgress(x, trackLeft, trackWidth);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (isDragging) {
                        isDragging = false;
                        updateTouchProgress(x, trackLeft, trackWidth);
                        return true;
                    }
                    break;
            }
            return super.onTouchEvent(event);
        }

        private void updateTouchProgress(float x, float trackLeft, float trackWidth) {
            float newProgress = Math.max(0.0f, Math.min(1.0f, (x - trackLeft) / trackWidth));
            this.progress = newProgress;
            int step = Math.round(newProgress * 28);
            if (step != lastStep) {
                lastStep = step;
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
            if (listener != null) {
                listener.onProgress(newProgress);
            }
            invalidate();
        }
    }

    private class TypographySelectorCell extends LinearLayout {
        private final TextView titleView;
        private final LinearLayout fontPreviewCard;
        private final TextView previewTitleText;
        private final TextView previewNavText;
        private final LinearLayout fontButtonsLayout;
        private final TextView defaultFontBtn;
        private final TextView comfortaaFontBtn;

        private final LinearLayout scopeLayout;
        private final TextView scopeTitleView;
        private final TextView scopeAllBtn;
        private final TextView scopeUiOnlyBtn;

        public TypographySelectorCell(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setPadding(AndroidUtilities.dp(18), AndroidUtilities.dp(12), AndroidUtilities.dp(18), AndroidUtilities.dp(14));

            titleView = new TextView(context);
            titleView.setText("Шрифт приложения");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 10));

            // Font Live Preview Card
            fontPreviewCard = new LinearLayout(context);
            fontPreviewCard.setOrientation(VERTICAL);
            fontPreviewCard.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));
            GradientDrawable previewBg = new GradientDrawable();
            previewBg.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
            previewBg.setCornerRadius(AndroidUtilities.dp(16));
            fontPreviewCard.setBackground(previewBg);

            previewTitleText = new TextView(context);
            previewTitleText.setText("Заголовок чата • Zenigram");
            previewTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            previewTitleText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            fontPreviewCard.addView(previewTitleText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

            previewNavText = new TextView(context);
            previewNavText.setText("Чаты  •  Звонки  •  Настройки");
            previewNavText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            previewNavText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            fontPreviewCard.addView(previewNavText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            addView(fontPreviewCard, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));

            // Row 1: Font Selection Buttons side-by-side
            fontButtonsLayout = new LinearLayout(context);
            fontButtonsLayout.setOrientation(HORIZONTAL);

            defaultFontBtn = createPillButton(context, "Стандартный");
            comfortaaFontBtn = createPillButton(context, "Comfortaa");

            fontButtonsLayout.addView(defaultFontBtn, LayoutHelper.createLinear(0, 42, 1.0f, 0, 0, 6, 0));
            fontButtonsLayout.addView(comfortaaFontBtn, LayoutHelper.createLinear(0, 42, 1.0f, 6, 0, 0, 0));
            addView(fontButtonsLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

            defaultFontBtn.setOnClickListener(v -> {
                if (NekoConfig.customFont != NekoConfig.FONT_DEFAULT) {
                    NekoConfig.setCustomFont(NekoConfig.FONT_DEFAULT);
                    onTypographyUpdated();
                }
            });

            comfortaaFontBtn.setOnClickListener(v -> {
                if (NekoConfig.customFont != NekoConfig.FONT_COMFORTAA) {
                    NekoConfig.setCustomFont(NekoConfig.FONT_COMFORTAA);
                    onTypographyUpdated();
                }
            });

            // Scope Layout in 2 stacked rows (No overlap!)
            scopeLayout = new LinearLayout(context);
            scopeLayout.setOrientation(VERTICAL);

            scopeTitleView = new TextView(context);
            scopeTitleView.setText("Область применения шрифта:");
            scopeTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            scopeTitleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            scopeLayout.addView(scopeTitleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 6, 0, 8));

            // Row 2: All interface + messages
            scopeAllBtn = createPillButton(context, "Весь интерфейс и сообщения");
            scopeLayout.addView(scopeAllBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, 0, 0, 0, 8));

            // Row 3: UI elements only
            scopeUiOnlyBtn = createPillButton(context, "Только элементы интерфейса (без чата)");
            scopeLayout.addView(scopeUiOnlyBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 42, 0, 0, 0, 0));

            scopeAllBtn.setOnClickListener(v -> {
                if (NekoConfig.customFontScope != NekoConfig.FONT_SCOPE_ALL) {
                    NekoConfig.setCustomFontScope(NekoConfig.FONT_SCOPE_ALL);
                    onTypographyUpdated();
                }
            });

            scopeUiOnlyBtn.setOnClickListener(v -> {
                if (NekoConfig.customFontScope != NekoConfig.FONT_SCOPE_UI_ONLY) {
                    NekoConfig.setCustomFontScope(NekoConfig.FONT_SCOPE_UI_ONLY);
                    onTypographyUpdated();
                }
            });

            addView(scopeLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            updateState();
        }

        private TextView createPillButton(Context context, String text) {
            TextView btn = new TextView(context);
            btn.setText(text);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            btn.setGravity(Gravity.CENTER);
            return btn;
        }

        private void onTypographyUpdated() {
            AndroidUtilities.resetTypefaces();
            Theme.refreshThemeColors();
            Theme.applyChatTheme(true, false);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, Theme.getActiveTheme(), Theme.isCurrentThemeDark(), null, -1);
            applyLiveThemeToCurrentScreen();
            updateState();
        }

        public void updateState() {
            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            if (accent == 0) accent = 0xFFC4A98F;
            int textPrimary = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
            int textSecondary = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2);
            int inactiveBg = Theme.getColor(Theme.key_windowBackgroundGray);

            titleView.setTextColor(textPrimary);
            scopeTitleView.setTextColor(textSecondary);

            android.graphics.Typeface tfBold = NekoConfig.customFont == NekoConfig.FONT_COMFORTAA ?
                    AndroidUtilities.getTypeface("fonts/Comfortaa.ttf") : AndroidUtilities.bold();
            android.graphics.Typeface tfRegular = NekoConfig.customFont == NekoConfig.FONT_COMFORTAA ?
                    AndroidUtilities.getTypeface("fonts/Comfortaa.ttf") : AndroidUtilities.getTypeface("fonts/rmedium.ttf");
            if (previewTitleText != null) {
                previewTitleText.setTypeface(tfBold);
                previewTitleText.setTextColor(textPrimary);
            }
            if (previewNavText != null) {
                previewNavText.setTypeface(tfRegular);
                previewNavText.setTextColor(textSecondary);
            }

            boolean isComfortaa = NekoConfig.customFont == NekoConfig.FONT_COMFORTAA;
            styleButton(defaultFontBtn, !isComfortaa, accent, inactiveBg, textPrimary);
            styleButton(comfortaaFontBtn, isComfortaa, accent, inactiveBg, textPrimary);

            scopeLayout.setVisibility(isComfortaa ? VISIBLE : GONE);

            if (isComfortaa) {
                boolean isAll = NekoConfig.customFontScope == NekoConfig.FONT_SCOPE_ALL;
                styleButton(scopeAllBtn, isAll, accent, inactiveBg, textPrimary);
                styleButton(scopeUiOnlyBtn, !isAll, accent, inactiveBg, textPrimary);
            }
        }

        private void styleButton(TextView btn, boolean selected, int accent, int inactiveBg, int textPrimary) {
            if (selected) {
                btn.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(10), accent));
                btn.setTextColor(0xFFFFFFFF);
                btn.setTypeface(AndroidUtilities.bold());
            } else {
                btn.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(10), inactiveBg));
                btn.setTextColor(textPrimary);
                btn.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            }
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.ZenigramThemeStudio)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            items.add(UItem.asCheck(dynamicMonetRow, LocaleController.getString(R.string.ThemeStudioDynamicMonet), LocaleController.getString(R.string.ThemeStudioDynamicMonetDesc)).setChecked(NekoConfig.dynamicMonet).slug("dynamicMonet"));
        }

        items.add(TextSettingsCellFactory.of(accentColorRow, LocaleController.getString(R.string.ThemeStudioAccentColor), getAccentColorName()).slug("accentColor"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.ThemeStudioBubblesHeader)));
        items.add(TextSettingsCellFactory.of(outBubbleColorRow, LocaleController.getString(R.string.ThemeStudioOutBubbleColor), getBubbleColorName(NekoConfig.customOutBubbleColor)).slug("outBubbleColor"));
        if (bubbleRadiusSliderCell == null) {
            bubbleRadiusSliderCell = new BubbleRadiusSliderCell(getContext());
        }
        items.add(UItem.asCustom(bubbleRadiusSliderCell));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Типографика"));
        if (typographySelectorCell == null) {
            typographySelectorCell = new TypographySelectorCell(getContext());
        }
        items.add(UItem.asCustom(typographySelectorCell));
        items.add(UItem.asShadow(null));

        items.add(TextSettingsCellFactory.of(interfaceDensityRow, LocaleController.getString(R.string.ThemeStudioDensity), getInterfaceDensityName()).slug("interfaceDensity"));
        items.add(TextSettingsCellFactory.of(animationSpeedRow, LocaleController.getString(R.string.ThemeStudioAnimationSpeed), getAnimationSpeedName()).slug("animationSpeed"));
        items.add(UItem.asShadow(null));

        items.add(TextSettingsCellFactory.of(resetDefaultsRow, LocaleController.getString(R.string.ThemeStudioReset)).slug("resetDefaults").red());
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == dynamicMonetRow) {
            NekoConfig.toggleDynamicMonet();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.dynamicMonet);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                applyThemeMode(NekoConfig.themeMode);
            }
            applyLiveThemeToCurrentScreen();
        } else if (id == accentColorRow) {
            List<String> accents = new ArrayList<>();
            for (int i = 0; i < ACCENT_COLORS.length; i++) {
                accents.add(getAccentName(i));
            }
            int currentIndex = 0;
            for (int i = 0; i < ACCENT_COLORS.length; i++) {
                if (ACCENT_COLORS[i] == NekoConfig.customAccentColor) {
                    currentIndex = i;
                    break;
                }
            }
            showPopup(accents, currentIndex, item, view, index -> {
                NekoConfig.setCustomAccentColor(ACCENT_COLORS[index]);
                Theme.refreshThemeColors();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, Theme.getActiveTheme(), Theme.isCurrentThemeDark(), null, -1);
                applyLiveThemeToCurrentScreen();
            });
        } else if (id == outBubbleColorRow) {
            List<String> bubbleColors = new ArrayList<>();
            for (int color : BUBBLE_COLORS) {
                bubbleColors.add(getBubbleColorName(color));
            }
            int currentIndex = 0;
            for (int i = 0; i < BUBBLE_COLORS.length; i++) {
                if (BUBBLE_COLORS[i] == NekoConfig.customOutBubbleColor) {
                    currentIndex = i;
                    break;
                }
            }
            showPopup(bubbleColors, currentIndex, item, view, index -> {
                NekoConfig.setCustomOutBubbleColor(BUBBLE_COLORS[index]);
                Theme.invalidateChatOutBubble();
                Theme.refreshThemeColors();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, Theme.getActiveTheme(), Theme.isCurrentThemeDark(), null, -1);
                applyLiveThemeToCurrentScreen();
            });
        } else if (id == interfaceDensityRow) {
            List<String> densities = Arrays.asList(
                    LocaleController.getString(R.string.ThemeStudioDensityCompact),
                    LocaleController.getString(R.string.ThemeStudioDensityDefault),
                    LocaleController.getString(R.string.ThemeStudioDensitySpacious)
            );
            showPopup(densities, NekoConfig.interfaceDensity, item, view, index -> {
                NekoConfig.setInterfaceDensity(index);
                Theme.refreshThemeColors();
                listView.adapter.notifyItemChanged(position, PARTIAL);
            });
        } else if (id == animationSpeedRow) {
            List<String> speeds = Arrays.asList(
                    LocaleController.getString(R.string.ThemeStudioAnimationSpeedOff),
                    LocaleController.getString(R.string.ThemeStudioAnimationSpeedFast),
                    LocaleController.getString(R.string.ThemeStudioAnimationSpeedNormal),
                    LocaleController.getString(R.string.ThemeStudioAnimationSpeedRelaxed)
            );
            int currentSpeedIndex = 2;
            if (NekoConfig.animationSpeedScale == 0.0f) {
                currentSpeedIndex = 0;
            } else if (NekoConfig.animationSpeedScale <= 0.6f) {
                currentSpeedIndex = 1;
            } else if (NekoConfig.animationSpeedScale >= 1.4f) {
                currentSpeedIndex = 3;
            }
            showPopup(speeds, currentSpeedIndex, item, view, index -> {
                float scale = switch (index) {
                    case 0 -> 0.0f;
                    case 1 -> 0.5f;
                    case 3 -> 1.5f;
                    default -> 1.0f;
                };
                NekoConfig.setAnimationSpeedScale(scale);
                SharedConfig.setAnimationsEnabled(scale > 0.0f);
                MessagesController.getGlobalMainSettings().edit().putBoolean("view_animations", scale > 0.0f).apply();
                listView.adapter.notifyItemChanged(position, PARTIAL);
            });
        } else if (id == resetDefaultsRow) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), resourcesProvider);
            builder.setTitle(LocaleController.getString(R.string.ThemeStudioReset));
            builder.setMessage(LocaleController.getString(R.string.ThemeStudioResetConfirm));
            builder.setPositiveButton(LocaleController.getString(R.string.Reset), (dialog, which) -> {
                NekoConfig.resetThemeStudio();
                NekoConfig.setCustomFont(NekoConfig.FONT_DEFAULT);
                NekoConfig.setCustomFontScope(NekoConfig.FONT_SCOPE_ALL);
                AndroidUtilities.resetTypefaces();
                Theme.invalidateChatOutBubble();
                SharedConfig.bubbleRadius = 16;
                MessagesController.getGlobalMainSettings().edit().putInt("bubbleRadius", 16).apply();
                SharedConfig.setAnimationsEnabled(true);
                MessagesController.getGlobalMainSettings().edit().putBoolean("view_animations", true).apply();
                applyThemeMode(NekoConfig.THEME_MODE_DARK);
                Theme.refreshThemeColors();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, Theme.getActiveTheme(), Theme.isCurrentThemeDark(), null, -1);
                applyLiveThemeToCurrentScreen();
                BulletinFactory.of(this).createSimpleBulletin(R.raw.done, LocaleController.getString(R.string.ThemeStudioReset)).show();
            });
            builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
            showDialog(builder.create());
        }
    }
}
