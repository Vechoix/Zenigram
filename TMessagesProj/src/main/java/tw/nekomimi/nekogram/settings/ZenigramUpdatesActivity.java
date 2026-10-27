package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.helpers.ZenigramUpdateHelper;

public class ZenigramUpdatesActivity extends BaseNekoSettingsActivity {

    private final int heroCardRow = rowId++;
    private final int autoCheckRow = rowId++;

    private HeroCardView heroCardView;

    private boolean isChecking = false;
    private boolean isDownloading = false;
    private File downloadedApkFile = null;

    private String latestVersionName = ZenigramUpdateHelper.CURRENT_VERSION_NAME;
    private String latestChangelog = "";
    private String latestDownloadUrl = "";

    @Override
    protected String getActionBarTitle() {
        return "Обновление Zenigram";
    }

    @Override
    public boolean onFragmentCreate() {
        if (!TextUtils.isEmpty(NekoConfig.cachedUpdateVersionName)) {
            latestVersionName = NekoConfig.cachedUpdateVersionName;
            latestChangelog = NekoConfig.cachedUpdateChangelog;
            latestDownloadUrl = NekoConfig.cachedUpdateUrl;
        }

        File cacheDir = getParentActivity() != null ? getParentActivity().getExternalCacheDir() : null;
        if (cacheDir != null) {
            File existingApk = new File(cacheDir, "Zenigram_Update.apk");
            if (existingApk.exists() && existingApk.length() > 10000000) {
                downloadedApkFile = existingApk;
            }
        }

        // Always check GitHub on entry so changelog & updates are 100% live
        performCheck(false);

        return super.onFragmentCreate();
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (heroCardView == null) {
            heroCardView = new HeroCardView(getParentActivity());
        }
        updateHeroCardState();
        items.add(UItem.asCustom(heroCardView));

        // Changelog: strictly taken from GitHub
        if (!TextUtils.isEmpty(latestChangelog)) {
            items.add(UItem.asHeader("Что нового в версии " + latestVersionName));
            items.add(UItem.asShadow(latestChangelog));
        }

        items.add(UItem.asHeader("Параметры"));
        items.add(UItem.asCheck(autoCheckRow, "Автоматическая проверка", "Проверять наличие обновлений при запуске").setChecked(NekoConfig.autoCheckUpdates));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == autoCheckRow) {
            NekoConfig.toggleAutoCheckUpdates();
            updateList();
        }
    }

    private void updateHeroCardState() {
        if (heroCardView == null) return;
        boolean hasUpdate = NekoConfig.hasUpdateAvailable;

        String title = "Zenigram v" + ZenigramUpdateHelper.CURRENT_VERSION_NAME;
        String badge;
        int badgeColor;

        if (isChecking) {
            badge = "Проверка обновлений...";
            badgeColor = 0xFF757575;
        } else if (isDownloading) {
            badge = "Загрузка обновления v" + latestVersionName;
            badgeColor = Theme.getColor(Theme.key_featuredStickers_addButton);
        } else if (downloadedApkFile != null && downloadedApkFile.exists()) {
            badge = "✓ Версия v" + latestVersionName + " готова к установке";
            badgeColor = 0xFF2E7D32;
        } else if (hasUpdate) {
            badge = "★ Доступна новая версия: v" + latestVersionName;
            badgeColor = Theme.getColor(Theme.key_featuredStickers_addButton);
        } else {
            badge = "✓ У вас актуальная версия";
            badgeColor = 0xFF2E7D32;
        }

        String lastCheck = "Никогда";
        if (NekoConfig.lastUpdateCheckTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            lastCheck = sdf.format(new Date(NekoConfig.lastUpdateCheckTime));
        }

        heroCardView.updateData(title, badge, badgeColor, "Последняя проверка: " + lastCheck);

        // Update button inside hero card
        if (isChecking) {
            heroCardView.setButtonState("Проверка...", false, false);
            heroCardView.setProgressVisible(false);
        } else if (isDownloading) {
            heroCardView.setProgressVisible(true);
        } else if (downloadedApkFile != null && downloadedApkFile.exists()) {
            heroCardView.setProgressVisible(false);
            heroCardView.setButtonState("Установить обновление", true, true);
        } else if (hasUpdate) {
            heroCardView.setProgressVisible(false);
            heroCardView.setButtonState("Скачать и обновить (v" + latestVersionName + ")", true, false);
        } else {
            heroCardView.setProgressVisible(false);
            heroCardView.setButtonState("Проверить наличие обновлений", true, false);
        }
    }

    private void performCheck(boolean showBulletin) {
        if (isChecking) return;
        isChecking = true;
        updateHeroCardState();

        ZenigramUpdateHelper.getInstance().checkUpdate(true, (info, hasUpdate, error) -> {
            isChecking = false;
            if (info != null && !TextUtils.isEmpty(info.versionName)) {
                latestVersionName = info.versionName;
                latestChangelog = info.changelog;
                latestDownloadUrl = info.downloadUrl;
            }

            if (showBulletin) {
                if (error != null && !hasUpdate) {
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Не удалось проверить: " + error).show();
                } else if (hasUpdate) {
                    downloadedApkFile = null;
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Найдено обновление Zenigram v" + info.versionName + "!").show();
                } else {
                    BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "У вас установлена актуальная версия!").show();
                }
            }

            updateHeroCardState();
            updateList();
        });
    }

    private void startDownload() {
        if (isDownloading) return;
        if (TextUtils.isEmpty(latestDownloadUrl)) {
            performCheck(true);
            return;
        }

        isDownloading = true;
        updateHeroCardState();

        heroCardView.setProgress(0f, "Подключение...");

        ZenigramUpdateHelper.getInstance().downloadApk(latestDownloadUrl, new ZenigramUpdateHelper.DownloadCallback() {
            @Override
            public void onProgress(float progress, long downloadedBytes, long totalBytes) {
                float mbDownloaded = downloadedBytes / (1024f * 1024f);
                float mbTotal = totalBytes / (1024f * 1024f);
                int percent = (int) (progress * 100);
                String text;
                if (totalBytes > 0) {
                    text = String.format(Locale.US, "%d%% • %.1f МБ из %.1f МБ", percent, mbDownloaded, mbTotal);
                } else {
                    text = String.format(Locale.US, "Загружено: %.1f МБ", mbDownloaded);
                }
                heroCardView.setProgress(progress, text);
            }

            @Override
            public void onSuccess(File file) {
                isDownloading = false;
                downloadedApkFile = file;
                BulletinFactory.of(ZenigramUpdatesActivity.this).createSimpleBulletin(R.raw.chats_infotip, "Обновление загружено! Запуск установки...").show();
                updateHeroCardState();
                updateList();
                ZenigramUpdateHelper.installApk(getParentActivity(), file);
            }

            @Override
            public void onError(String error) {
                isDownloading = false;
                BulletinFactory.of(ZenigramUpdatesActivity.this).createSimpleBulletin(R.raw.chats_infotip, "Ошибка: " + error).show();
                updateHeroCardState();
                updateList();
            }
        });
    }

    private void onActionButtonClicked() {
        if (downloadedApkFile != null && downloadedApkFile.exists()) {
            ZenigramUpdateHelper.installApk(getParentActivity(), downloadedApkFile);
        } else if (NekoConfig.hasUpdateAvailable) {
            startDownload();
        } else {
            performCheck(true);
        }
    }

    private void updateList() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    // Unified MD3 Hero Card View containing the Status & Action Button
    private class HeroCardView extends FrameLayout {
        private final TextView titleView;
        private final TextView badgeView;
        private final TextView checkTimeView;
        private final ImageView iconView;

        private final TextView actionButton;
        private final LinearLayout progressContainer;
        private final ProgressBar progressBar;
        private final TextView progressText;
        private final TextView cancelButton;

        public HeroCardView(Context context) {
            super(context);

            LinearLayout container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(Gravity.CENTER_HORIZONTAL);
            container.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20), AndroidUtilities.dp(20));

            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            cardBg.setCornerRadius(AndroidUtilities.dp(24));
            container.setBackground(cardBg);

            // Circular Icon with theme tint
            FrameLayout iconCircle = new FrameLayout(context);
            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            if (accent == 0) accent = 0xFFC4A98F;
            circleBg.setColor(Theme.multAlpha(accent, 0.15f));
            iconCircle.setBackground(circleBg);

            iconView = new ImageView(context);
            iconView.setImageResource(R.drawable.msg_download);
            iconView.setColorFilter(accent);
            iconCircle.addView(iconView, LayoutHelper.createFrame(32, 32, Gravity.CENTER));

            container.addView(iconCircle, LayoutHelper.createLinear(64, 64, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 12));

            // App title
            titleView = new TextView(context);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleView.setTypeface(AndroidUtilities.bold());
            titleView.setGravity(Gravity.CENTER_HORIZONTAL);
            container.addView(titleView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 8));

            // Status Badge
            badgeView = new TextView(context);
            badgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            badgeView.setTextColor(Color.WHITE);
            badgeView.setTypeface(AndroidUtilities.bold());
            badgeView.setGravity(Gravity.CENTER);
            badgeView.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(6), AndroidUtilities.dp(14), AndroidUtilities.dp(6));
            container.addView(badgeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 10));

            // Last check time
            checkTimeView = new TextView(context);
            checkTimeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            checkTimeView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            checkTimeView.setGravity(Gravity.CENTER_HORIZONTAL);
            container.addView(checkTimeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 18));

            // Progress container (hidden by default)
            progressContainer = new LinearLayout(context);
            progressContainer.setOrientation(LinearLayout.VERTICAL);
            progressContainer.setVisibility(GONE);
            progressContainer.setPadding(0, 0, 0, AndroidUtilities.dp(10));

            progressText = new TextView(context);
            progressText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            progressText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            progressText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            progressContainer.addView(progressText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 6));

            progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressContainer.addView(progressBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8, 0, 0, 0, 8));

            cancelButton = new TextView(context);
            cancelButton.setText("Отменить загрузку");
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            cancelButton.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
            cancelButton.setGravity(Gravity.CENTER);
            cancelButton.setPadding(0, AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4));
            cancelButton.setOnClickListener(v -> {
                ZenigramUpdateHelper.getInstance().cancelDownload();
                isDownloading = false;
                setProgressVisible(false);
                updateHeroCardState();
            });
            progressContainer.addView(cancelButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

            container.addView(progressContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            // MD3 Action Button directly in Hero Card (seamless, no extra borders)
            actionButton = new TextView(context);
            actionButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            actionButton.setTypeface(AndroidUtilities.bold());
            actionButton.setGravity(Gravity.CENTER);
            actionButton.setOnClickListener(v -> onActionButtonClicked());

            container.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

            addView(container, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 14, 12, 14, 8));
        }

        public void updateData(String title, String badge, int badgeColor, String checkTime) {
            titleView.setText(title);
            badgeView.setText(badge);

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(badgeColor);
            badgeBg.setCornerRadius(AndroidUtilities.dp(14));
            badgeView.setBackground(badgeBg);

            checkTimeView.setText(checkTime);
        }

        public void setButtonState(String text, boolean enabled, boolean isSuccess) {
            actionButton.setText(text);
            actionButton.setEnabled(enabled);

            int accent = Theme.getColor(Theme.key_chats_actionBackground);
            if (accent == 0) accent = 0xFFC4A98F;
            int btnColor = isSuccess ? 0xFF2E7D32 : accent;
            if (!enabled) {
                btnColor = 0xFF888888;
            }

            actionButton.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(24), btnColor, Theme.blendOver(btnColor, 0x22FFFFFF)));
            actionButton.setTextColor(Color.WHITE);
        }

        public void setProgressVisible(boolean visible) {
            progressContainer.setVisibility(visible ? VISIBLE : GONE);
            actionButton.setVisibility(visible ? GONE : VISIBLE);
        }

        public void setProgress(float progress, String text) {
            progressBar.setProgress((int) (progress * 100));
            progressText.setText(text);
        }
    }
}
