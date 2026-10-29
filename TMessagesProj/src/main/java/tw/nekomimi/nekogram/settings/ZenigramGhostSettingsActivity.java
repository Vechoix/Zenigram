package tw.nekomimi.nekogram.settings;

import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;

public class ZenigramGhostSettingsActivity extends BaseNekoSettingsActivity {

    private final int ghostReadMessagesRow = rowId++;
    private final int ghostSendTypingRow = rowId++;
    private final int ghostVoiceRow = rowId++;
    private final int ghostStoriesRow = rowId++;
    private final int allowScreenshotsRow = rowId++;
    private final int ignoreContentRestrictionRow = rowId++;

    @Override
    protected String getActionBarTitle() {
        return "Режим призрака";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Призрак"));
        items.add(UItem.asCheck(ghostReadMessagesRow, "Не читать сообщения", "Не отправлять статус о прочтении").setChecked(NekoConfig.ghostReadMessages).slug("ghostReadMessages"));
        items.add(UItem.asCheck(ghostSendTypingRow, "Скрыть статус набора", "Не показывать «печатает...»").setChecked(NekoConfig.ghostSendTyping).slug("ghostSendTyping"));
        items.add(UItem.asCheck(ghostVoiceRow, "Не слушать аудио открыто", "Голосовые и кружки остаются непрослушанными").setChecked(NekoConfig.ghostVoice).slug("ghostVoice"));
        items.add(UItem.asCheck(ghostStoriesRow, "Анонимный просмотр историй", "Не показывать вас в списке зрителей").setChecked(NekoConfig.ghostStories).slug("ghostStories"));
        items.add(UItem.asCheck(allowScreenshotsRow, "Разрешить скриншоты", "Снимает запрет на снимки и запись экрана").setChecked(NekoConfig.allowScreenshots).slug("allowScreenshots"));
        items.add(UItem.asCheck(ignoreContentRestrictionRow, "Запрет на копирование", "Разрешить копирование и пересылку из каналов").setChecked(NekoConfig.ignoreContentRestriction).slug("ignoreContentRestriction"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == ghostReadMessagesRow) {
            NekoConfig.toggleGhostReadMessages();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.ghostReadMessages);
            }
        } else if (id == ghostSendTypingRow) {
            NekoConfig.toggleGhostSendTyping();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.ghostSendTyping);
            }
        } else if (id == ghostVoiceRow) {
            NekoConfig.toggleGhostVoice();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.ghostVoice);
            }
        } else if (id == ghostStoriesRow) {
            NekoConfig.toggleGhostStories();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.ghostStories);
            }
        } else if (id == allowScreenshotsRow) {
            NekoConfig.toggleAllowScreenshots();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.allowScreenshots);
            }
        } else if (id == ignoreContentRestrictionRow) {
            NekoConfig.toggleIgnoreContentRestriction();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.ignoreContentRestriction);
            }
        }
    }
}
