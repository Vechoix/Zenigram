package tw.nekomimi.nekogram.settings;

import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.SwipeGestureSettingsView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tw.nekomimi.nekogram.NekoConfig;

public class ZenigramGestureLabActivity extends BaseNekoSettingsActivity {

    private final int chatListSwipeRow = rowId++;
    private final int doubleTapInRow = rowId++;
    private final int doubleTapOutRow = rowId++;

    private final int swipeToReplyRow = rowId++;
    private final int quickForwardGestureRow = rowId++;
    private final int quickForwardRow = rowId++;
    private final int openArchiveOnPullRow = rowId++;
    private final int hideKeyboardOnScrollRow = rowId++;
    private final int disableJumpToNextRow = rowId++;

    private static final int[] SWIPE_ACTIONS = new int[]{
            SwipeGestureSettingsView.SWIPE_GESTURE_ARCHIVE,
            SwipeGestureSettingsView.SWIPE_GESTURE_MUTE,
            SwipeGestureSettingsView.SWIPE_GESTURE_DELETE,
            SwipeGestureSettingsView.SWIPE_GESTURE_PIN,
            SwipeGestureSettingsView.SWIPE_GESTURE_READ,
            SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS
    };

    private static final int[] DOUBLE_TAP_ACTIONS = new int[]{
            NekoConfig.DOUBLE_TAP_ACTION_NONE,
            NekoConfig.DOUBLE_TAP_ACTION_REACTION,
            NekoConfig.DOUBLE_TAP_ACTION_TRANSLATE,
            NekoConfig.DOUBLE_TAP_ACTION_REPLY,
            NekoConfig.DOUBLE_TAP_ACTION_SAVE,
            NekoConfig.DOUBLE_TAP_ACTION_REPEAT,
            NekoConfig.DOUBLE_TAP_ACTION_EDIT
    };

    @Override
    protected String getActionBarTitle() {
        return "Жесты";
    }

    private String getSwipeActionName(int action) {
        return switch (action) {
            case SwipeGestureSettingsView.SWIPE_GESTURE_PIN -> LocaleController.getString(R.string.SwipeSettingsPin);
            case SwipeGestureSettingsView.SWIPE_GESTURE_READ -> LocaleController.getString(R.string.SwipeSettingsRead);
            case SwipeGestureSettingsView.SWIPE_GESTURE_MUTE -> LocaleController.getString(R.string.SwipeSettingsMute);
            case SwipeGestureSettingsView.SWIPE_GESTURE_DELETE -> LocaleController.getString(R.string.SwipeSettingsDelete);
            case SwipeGestureSettingsView.SWIPE_GESTURE_FOLDERS -> LocaleController.getString(R.string.SwipeSettingsFolders);
            default -> LocaleController.getString(R.string.SwipeSettingsArchive);
        };
    }

    private String getDoubleTapActionName(int action) {
        return switch (action) {
            case NekoConfig.DOUBLE_TAP_ACTION_REACTION -> LocaleController.getString(R.string.Reactions);
            case NekoConfig.DOUBLE_TAP_ACTION_TRANSLATE -> LocaleController.getString(R.string.TranslateMessage);
            case NekoConfig.DOUBLE_TAP_ACTION_REPLY -> LocaleController.getString(R.string.Reply);
            case NekoConfig.DOUBLE_TAP_ACTION_SAVE -> LocaleController.getString(R.string.AddToSavedMessages);
            case NekoConfig.DOUBLE_TAP_ACTION_REPEAT -> LocaleController.getString(R.string.Repeat);
            case NekoConfig.DOUBLE_TAP_ACTION_EDIT -> LocaleController.getString(R.string.Edit);
            default -> LocaleController.getString(R.string.Disable);
        };
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.ChatListSwipeGesture)));
        int currentSwipe = SharedConfig.getChatSwipeAction(currentAccount);
        items.add(TextSettingsCellFactory.of(chatListSwipeRow, LocaleController.getString(R.string.GestureChatListSwipe), getSwipeActionName(currentSwipe)).slug("chatListSwipe"));
        items.add(UItem.asShadow(LocaleController.getString(R.string.GestureChatListSwipeDesc)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.GestureDoubleTap)));
        items.add(TextSettingsCellFactory.of(doubleTapInRow, LocaleController.getString(R.string.GestureDoubleTapIn), getDoubleTapActionName(NekoConfig.doubleTapInAction)).slug("doubleTapIn"));
        items.add(TextSettingsCellFactory.of(doubleTapOutRow, LocaleController.getString(R.string.GestureDoubleTapOut), getDoubleTapActionName(NekoConfig.doubleTapOutAction)).slug("doubleTapOut"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.GestureInChat)));
        items.add(UItem.asCheck(swipeToReplyRow, LocaleController.getString(R.string.GestureSwipeToReply), LocaleController.getString(R.string.GestureSwipeToReplyDesc)).setChecked(NekoConfig.swipeToReply).slug("swipeToReply"));
        items.add(UItem.asCheck(quickForwardGestureRow, LocaleController.getString(R.string.GestureQuickForward), LocaleController.getString(R.string.GestureQuickForwardDesc)).setChecked(NekoConfig.quickForwardGesture).slug("quickForwardGesture"));
        items.add(UItem.asCheck(quickForwardRow, LocaleController.getString(R.string.QuickForward)).setChecked(NekoConfig.quickForward).slug("quickForward"));
        items.add(UItem.asCheck(openArchiveOnPullRow, LocaleController.getString(R.string.OpenArchiveOnPull)).setChecked(NekoConfig.openArchiveOnPull).slug("openArchiveOnPull"));
        items.add(UItem.asCheck(hideKeyboardOnScrollRow, LocaleController.getString(R.string.HideKeyboardOnChatScroll)).setChecked(NekoConfig.hideKeyboardOnChatScroll).slug("hideKeyboardOnScroll"));
        items.add(UItem.asCheck(disableJumpToNextRow, LocaleController.getString(R.string.DisableJumpToNextChannel)).setChecked(NekoConfig.disableJumpToNextChannel).slug("disableJumpToNext"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == chatListSwipeRow) {
            List<String> names = new ArrayList<>();
            for (int action : SWIPE_ACTIONS) {
                names.add(getSwipeActionName(action));
            }
            int currentAction = SharedConfig.getChatSwipeAction(currentAccount);
            int selectedIndex = 0;
            for (int i = 0; i < SWIPE_ACTIONS.length; i++) {
                if (SWIPE_ACTIONS[i] == currentAction) {
                    selectedIndex = i;
                    break;
                }
            }
            showPopup(names, selectedIndex, item, view, index -> {
                int selectedAction = SWIPE_ACTIONS[index];
                SharedConfig.updateChatListSwipeSetting(selectedAction);
                NekoConfig.setChatListSwipeAction(selectedAction);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            });
        } else if (id == doubleTapInRow) {
            List<String> names = new ArrayList<>();
            for (int action : DOUBLE_TAP_ACTIONS) {
                names.add(getDoubleTapActionName(action));
            }
            int selectedIndex = 0;
            for (int i = 0; i < DOUBLE_TAP_ACTIONS.length; i++) {
                if (DOUBLE_TAP_ACTIONS[i] == NekoConfig.doubleTapInAction) {
                    selectedIndex = i;
                    break;
                }
            }
            showPopup(names, selectedIndex, item, view, index -> {
                NekoConfig.setDoubleTapInAction(DOUBLE_TAP_ACTIONS[index]);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            });
        } else if (id == doubleTapOutRow) {
            List<String> names = new ArrayList<>();
            for (int action : DOUBLE_TAP_ACTIONS) {
                names.add(getDoubleTapActionName(action));
            }
            int selectedIndex = 0;
            for (int i = 0; i < DOUBLE_TAP_ACTIONS.length; i++) {
                if (DOUBLE_TAP_ACTIONS[i] == NekoConfig.doubleTapOutAction) {
                    selectedIndex = i;
                    break;
                }
            }
            showPopup(names, selectedIndex, item, view, index -> {
                NekoConfig.setDoubleTapOutAction(DOUBLE_TAP_ACTIONS[index]);
                listView.adapter.notifyItemChanged(position, PARTIAL);
            });
        } else if (id == swipeToReplyRow) {
            NekoConfig.toggleSwipeToReply();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.swipeToReply);
            }
        } else if (id == quickForwardGestureRow) {
            NekoConfig.toggleQuickForwardGesture();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.quickForwardGesture);
            }
        } else if (id == quickForwardRow) {
            NekoConfig.toggleQuickForward();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.quickForward);
            }
        } else if (id == openArchiveOnPullRow) {
            NekoConfig.toggleOpenArchiveOnPull();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.openArchiveOnPull);
            }
        } else if (id == hideKeyboardOnScrollRow) {
            NekoConfig.toggleHideKeyboardOnChatScroll();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.hideKeyboardOnChatScroll);
            }
        } else if (id == disableJumpToNextRow) {
            NekoConfig.toggleDisableJumpToNextChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.disableJumpToNextChannel);
            }
        }
    }
}
