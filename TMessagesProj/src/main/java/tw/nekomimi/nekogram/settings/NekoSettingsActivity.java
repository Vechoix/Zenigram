package tw.nekomimi.nekogram.settings;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.SettingsSearchCell;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FragmentFloatingButton;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.ProfileActivity.SearchAdapter.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;
import tw.nekomimi.nekogram.accessibility.AccessibilitySettingsActivity;
import tw.nekomimi.nekogram.helpers.CloudSettingsHelper;
import tw.nekomimi.nekogram.helpers.PasscodeHelper;
import tw.nekomimi.nekogram.helpers.remote.ConfigHelper;

public class NekoSettingsActivity extends BaseNekoSettingsActivity implements FactorAnimator.Target {

    private static final int ANIMATOR_ID_SEARCH_PAGE_VISIBLE = 0;

    private final BoolAnimator animatorSearchPageVisible = new BoolAnimator(ANIMATOR_ID_SEARCH_PAGE_VISIBLE,
            this, CubicBezierInterpolator.EASE_OUT_QUINT, 350);

    private final List<ConfigHelper.News> newsList = new ArrayList<>();

    private final int themeStudioRow = rowId++;
    private final int ghostSettingsRow = rowId++;
    private final int updatesRow = rowId++;
    private final int gestureLabRow = rowId++;
    private final int adaptivePerformanceRow = rowId++;
    private final int diagnosticsRow = rowId++;

    private final int generalRow = rowId++;
    private final int appearanceRow = rowId++;
    private final int chatRow = rowId++;
    private final int passcodeRow = rowId++;
    private final int experimentRow = rowId++;
    private final int accessibilityRow = rowId++;

    private final int channelRow = rowId++;
    private final int websiteRow = rowId++;
    private final int sourceCodeRow = rowId++;
    private final int translationRow = rowId++;
    private final int donateRow = rowId++;

    private final int sponsorRow = 100;

    private final ArrayList<SearchResult> searchArray = createSearchArray();
    private final ArrayList<CharSequence> resultNames = new ArrayList<>();
    private final ArrayList<SearchResult> searchResults = new ArrayList<>();
    private boolean searchWas;
    private Runnable searchRunnable;
    private String lastSearchString;

    private ZenigramHeaderCell headerCell;

    @Override
    public View createView(Context context) {
        if (parentLayout != null && parentLayout.isRightLayout()) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_close);
        }
        headerCell = new ZenigramHeaderCell(context, resourcesProvider);
        headerCell.setOnVersionClickListener(() -> presentFragment(new ZenigramUpdatesActivity()));

        var fragmentView = super.createView(context);

        var menu = actionBar.createMenu();
        createSearchItem(menu, new ActionBarMenuItem.ActionBarMenuItemSearchListener() {

            @Override
            public void onSearchCollapse() {
                animatorSearchPageVisible.setValue(false, true);
                updateActionBarVisible();
                listView.adapter.update(true);
            }

            @Override
            public void onSearchExpand() {
                animatorSearchPageVisible.setValue(true, true);
                updateActionBarVisible();
                search("");
                listView.adapter.update(true);
            }

            @Override
            public void onTextChanged(EditText editText) {
                search(editText.getText().toString());
            }
        });

        return fragmentView;
    }

    @Override
    protected boolean needActionBarPadding() {
        return true;
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (isSearchFieldVisible()) {
            fillSearchItems(items);
            return;
        }

        items.add(UItem.asCustomShadow(headerCell, LayoutHelper.WRAP_CONTENT));

        items.add(UItem.asHeader(LocaleController.getString(R.string.ZenigramPowerFeatures)));
        items.add(UItem.asButtonSubtext(themeStudioRow, R.drawable.msg_theme, "Внешний вид", "Шрифты, цвета, скругления").slug("themeStudio"));
        items.add(UItem.asButtonSubtext(ghostSettingsRow, R.drawable.msg_secret, "Режим призрака", "Чтение, скриншоты, скрытность").slug("ghostSettings"));
        items.add(UItem.asButtonSubtext(updatesRow, R.drawable.msg_download, "Обновление Zenigram", "Проверка новых версий и ченджлог").slug("updates"));
        items.add(UItem.asButtonSubtext(gestureLabRow, R.drawable.msg_discussion, "Жесты", "Свайпы и быстрые действия").slug("gestureLab"));
        items.add(UItem.asButtonSubtext(adaptivePerformanceRow, R.drawable.msg_fave, "Оптимизация", "Плавность, анимации и эффекты").slug("adaptivePerformance"));
        items.add(UItem.asButtonSubtext(diagnosticsRow, R.drawable.msg_secret, "Диагностика", "Частота кадров, память и кэш").slug("diagnostics"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asButton(generalRow, R.drawable.msg_media, LocaleController.getString(R.string.General)).slug("general"));
        items.add(UItem.asButton(appearanceRow, R.drawable.msg_theme, LocaleController.getString(R.string.ChangeChannelNameColor2)).slug("appearance"));
        items.add(UItem.asButton(chatRow, R.drawable.msg_discussion, LocaleController.getString(R.string.Chat)).slug("chat"));
        if (!PasscodeHelper.isSettingsHidden()) {
            items.add(UItem.asButton(passcodeRow, R.drawable.msg_secret, LocaleController.getString(R.string.PasscodeNeko)).slug("passcode"));
        }
        items.add(UItem.asButton(experimentRow, R.drawable.msg_fave, LocaleController.getString(R.string.NotificationsOther)).slug("experiment"));
        AccessibilityManager am = (AccessibilityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null && am.isTouchExplorationEnabled()) {
            items.add(UItem.asButton(accessibilityRow, LocaleController.getString(R.string.AccessibilitySettings)).slug("accessibility"));
        }
        items.add(UItem.asShadow(null));


    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        if (item.instanceOf(SettingsSearchCell.Factory.class)) {
            if (item.object instanceof SearchResult r) {
                r.open(null);
            }
            return;
        }
        var id = item.id;
        if (id == themeStudioRow) {
            presentFragment(new ZenigramThemeStudioActivity());
        } else if (id == ghostSettingsRow) {
            presentFragment(new ZenigramGhostSettingsActivity());
        } else if (id == updatesRow) {
            presentFragment(new ZenigramUpdatesActivity());
        } else if (id == gestureLabRow) {
            presentFragment(new ZenigramGestureLabActivity());
        } else if (id == adaptivePerformanceRow) {
            presentFragment(new ZenigramPerformanceActivity());
        } else if (id == diagnosticsRow) {
            presentFragment(new ZenigramDiagnosticsActivity());
        } else if (id == chatRow) {
            presentFragment(new NekoChatSettingsActivity());
        } else if (id == generalRow) {
            presentFragment(new NekoGeneralSettingsActivity());
        } else if (id == appearanceRow) {
            presentFragment(new NekoAppearanceSettingsActivity());
        } else if (id == passcodeRow) {
            presentFragment(new NekoPasscodeSettingsActivity());
        } else if (id == experimentRow) {
            presentFragment(new NekoExperimentalSettingsActivity());
        } else if (id == accessibilityRow) {
            presentFragment(new AccessibilitySettingsActivity());
        } else if (id == channelRow) {
            getMessagesController().openByUserName(LocaleController.getString(R.string.OfficialChannelUsername), this, 1);
        } else if (id == donateRow) {
            presentFragment(new NekoDonateActivity());
        } else if (id >= sponsorRow) {
            var news = newsList.get(id - sponsorRow);
            Browser.openUrl(getParentActivity(), news.url);
        }
    }

    @Override
    protected boolean onItemLongClick(UItem item, View view, int position, float x, float y) {
        var id = item.id;
        if (id >= sponsorRow) {
            var news = newsList.get(id - sponsorRow);
            if (news.id != null) {
                ItemOptions.makeOptions(this, view)
                        .setScrimViewBackground(listView.getClipBackground(view))
                        .add(R.drawable.msg_cancel, LocaleController.getString(R.string.Hide), () -> {
                            ConfigHelper.removeNews(news.id);
                            listView.adapter.update(true);
                        })
                        .setMinWidth(190)
                        .show();
                return true;
            }
        }
        return super.onItemLongClick(item, view, position, x, y);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.NekoSettings);
    }

    @Override
    protected String getKey() {
        return "";
    }

    @Override
    public void onFactorChanged(int id, float factor, float fraction, FactorAnimator callee) {
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return !animatorSearchPageVisible.getValue();
    }

    private static BaseNekoSettingsActivity createFragment(int icon) {
        if (icon == R.drawable.msg_media) {
            return new NekoGeneralSettingsActivity();
        } else if (icon == R.drawable.msg_theme) {
            return new NekoAppearanceSettingsActivity();
        } else if (icon == R.drawable.msg_discussion) {
            return new NekoChatSettingsActivity();
        } else if (icon == R.drawable.msg_fave) {
            return new NekoExperimentalSettingsActivity();
        }
        return new NekoSettingsActivity();
    }

    private ArrayList<SearchResult> createSearchArray() {
        var searchResultList = new ArrayList<SearchResult>();
        var icons = new int[]{
                R.drawable.msg_media,
                R.drawable.msg_theme,
                R.drawable.msg_discussion,
                R.drawable.msg_fave,
        };
        for (var i = 0; i < icons.length; i++) {
            var icon = icons[i];
            var fragment = createFragment(icon);
            var items = new ArrayList<UItem>();
            fragment.fillItems(items, null);
            var fragmentTitle = fragment.getActionBarTitle();
            String headerText = null;
            for (var item : items) {
                if (item.viewType == UniversalAdapter.VIEW_TYPE_HEADER) {
                    headerText = item.text.toString();
                    continue;
                } else if (item.viewType == UniversalAdapter.VIEW_TYPE_SHADOW) {
                    headerText = null;
                    continue;
                }
                if (TextUtils.isEmpty(item.slug)) continue;
                searchResultList.add(new SearchResult(i * 1000 + item.id, item.text.toString(), item.subtext != null ? item.subtext.toString() : null, fragmentTitle, fragmentTitle.equals(headerText) ? null : headerText, icon, () -> {
                    var fragment1 = createFragment(icon);
                    presentFragment(fragment1);
                    fragment1.scrollToRow(item.slug, null);
                }));
            }
            searchResultList.add(new SearchResult(10000 + i, fragmentTitle, icon, () -> presentFragment(fragment)));
        }
        searchResultList.add(new SearchResult(8000, LocaleController.getString(R.string.EmojiUseDefault), null, LocaleController.getString(R.string.Chat), LocaleController.getString(R.string.EmojiSets), R.drawable.msg_theme, () -> {
            var fragment = new NekoEmojiSettingsActivity();
            presentFragment(fragment);
            fragment.scrollToRow("useSystemEmoji", null);
        }));

        searchResultList.add(new SearchResult(8100, LocaleController.getString(R.string.ZenigramThemeStudio), LocaleController.getString(R.string.ZenigramThemeStudioDescription), R.drawable.msg_theme, () -> presentFragment(new ZenigramThemeStudioActivity())));
        searchResultList.add(new SearchResult(8104, "Режим призрака", "Скрытый режим, чтение, скриншоты, задержка сообщений", R.drawable.msg_secret, () -> presentFragment(new ZenigramGhostSettingsActivity())));
        searchResultList.add(new SearchResult(8101, LocaleController.getString(R.string.ZenigramGestureLab), LocaleController.getString(R.string.ZenigramGestureLabDescription), R.drawable.msg_discussion, () -> presentFragment(new ZenigramGestureLabActivity())));
        searchResultList.add(new SearchResult(8102, LocaleController.getString(R.string.AdaptivePerformanceSettings), LocaleController.getString(R.string.AdaptivePerformanceDesc), R.drawable.msg_fave, () -> presentFragment(new ZenigramPerformanceActivity())));
        searchResultList.add(new SearchResult(8103, LocaleController.getString(R.string.ZenigramDiagnostics), LocaleController.getString(R.string.ZenigramDiagnosticsDesc), R.drawable.msg_secret, () -> presentFragment(new ZenigramDiagnosticsActivity())));

        return searchResultList;
    }

    private void fillSearchItems(ArrayList<UItem> items) {
        if (searchWas) {
            for (int i = 0; i < searchResults.size(); i++) {
                items.add(SettingsSearchCell.Factory.of(resultNames.get(i), searchResults.get(i)));
            }
            if (!searchResults.isEmpty()) items.add(UItem.asShadow(null));
        }
    }

    private void search(String text) {
        lastSearchString = text;
        if (searchRunnable != null) {
            Utilities.searchQueue.cancelRunnable(searchRunnable);
            searchRunnable = null;
        }
        if (TextUtils.isEmpty(text)) {
            searchWas = false;
            searchResults.clear();
            resultNames.clear();
            listView.adapter.update(true);
            return;
        }
        Utilities.searchQueue.postRunnable(searchRunnable = () -> {
            var results = new ArrayList<SearchResult>();
            var names = new ArrayList<CharSequence>();
            var lowerQuery = text.trim().toLowerCase();
            for (var result : searchArray) {
                var title = result.searchTitle != null ? result.searchTitle.toLowerCase() : "";
                var row = result.rowName != null ? result.rowName.toLowerCase() : "";
                var p1 = (result.path != null && result.path.length > 0 && result.path[0] != null) ? result.path[0].toLowerCase() : "";
                var p2 = (result.path != null && result.path.length > 1 && result.path[1] != null) ? result.path[1].toLowerCase() : "";

                int index = title.indexOf(lowerQuery);
                if (index < 0 && (row.contains(lowerQuery) || p1.contains(lowerQuery) || p2.contains(lowerQuery))) {
                    index = 0;
                }
                if (index < 0) continue;
                var ssb = new SpannableStringBuilder(result.searchTitle);
                if (title.contains(lowerQuery)) {
                    int matchIdx = title.indexOf(lowerQuery);
                    ssb.setSpan(new ForegroundColorSpan(getThemedColor(Theme.key_windowBackgroundWhiteBlueText4)), matchIdx, Math.min(matchIdx + lowerQuery.length(), ssb.length()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                results.add(result);
                names.add(ssb);
            }

            AndroidUtilities.runOnUIThread(() -> {
                if (!text.equals(lastSearchString)) {
                    return;
                }
                searchWas = true;
                searchResults.clear();
                resultNames.clear();
                searchResults.addAll(results);
                resultNames.addAll(names);
                listView.adapter.update(true);
            });
        }, 300);
    }
}
