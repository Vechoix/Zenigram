package tw.nekomimi.nekogram.settings;

import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;

public class ZenigramPerformanceActivity extends BaseNekoSettingsActivity {

    private final int adaptiveMasterRow = rowId++;
    private final int disableBlurPassesRow = rowId++;
    private final int scaleAnimationsRow = rowId++;
    private final int hqVideoMessagesRow = rowId++;

    @Override
    protected String getActionBarTitle() {
        return "Оптимизация";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Основное"));
        items.add(UItem.asCheck(adaptiveMasterRow, "Режим высокой плавности", "Отключает тяжелые фоновые эффекты для максимального FPS").setChecked(NekoConfig.adaptivePerformanceMode).slug("adaptiveMaster"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Настройки графики"));
        items.add(UItem.asCheck(disableBlurPassesRow, "Отключить размытие фона", "Убирает блюр в диалогах для мгновенной прокрутки").setChecked(NekoConfig.disableBlurPasses).slug("disableBlurPasses"));
        items.add(UItem.asCheck(scaleAnimationsRow, "Быстрые анимации", "Ускоряет анимации открытия чатов и меню").setChecked(NekoConfig.scaleAnimationsForPerformance).slug("scaleAnimations"));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Медиа"));
        items.add(UItem.asCheck(hqVideoMessagesRow, "Высокое качество кружков", "Повышенное разрешение и битрейт видеозаметок").setChecked(NekoConfig.hqVideoMessages).slug("hqVideoMessages"));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onItemClick(UItem item, View view, int position, float x, float y) {
        int id = item.id;
        if (id == adaptiveMasterRow) {
            NekoConfig.toggleAdaptivePerformanceMode();
            if (NekoConfig.adaptivePerformanceMode) {
                if (!NekoConfig.disableBlurPasses) NekoConfig.toggleDisableBlurPasses();
                if (!NekoConfig.scaleAnimationsForPerformance) NekoConfig.toggleScaleAnimationsForPerformance();
            }
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.adaptivePerformanceMode);
            }
            listView.adapter.update(true);
            if (parentLayout != null) {
                parentLayout.rebuildAllFragmentViews(false, false);
            }
        } else if (id == disableBlurPassesRow) {
            NekoConfig.toggleDisableBlurPasses();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.disableBlurPasses);
            }
            if (parentLayout != null) {
                parentLayout.rebuildAllFragmentViews(false, false);
            }
        } else if (id == scaleAnimationsRow) {
            NekoConfig.toggleScaleAnimationsForPerformance();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.scaleAnimationsForPerformance);
            }
        } else if (id == hqVideoMessagesRow) {
            NekoConfig.toggleHqVideoMessages();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(NekoConfig.hqVideoMessages);
            }
        }
    }
}
