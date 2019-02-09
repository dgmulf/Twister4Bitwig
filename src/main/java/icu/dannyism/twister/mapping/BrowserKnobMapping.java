package icu.dannyism.twister.mapping;

import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.PopupBrowser;
import icu.dannyism.bitwig.mappable.MappableBrowserColumn;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;
import icu.dannyism.controller.Button;
import icu.dannyism.twister.MidiFighterTwister;

public class BrowserKnobMapping {

    private final MidiFighterTwister.Knob knob;
    private final PopupBrowser browser;
    private final Device device;
    private final MappableBrowserColumnSelector columnSelector;
    private final KnobDownTask knobDownTask = new KnobDownTask();
    private boolean columnSelectionChanged;

    public BrowserKnobMapping(
            MidiFighterTwister.Knob knob,
            PopupBrowser browser,
            Device device,
            MappableBrowserColumn[] columns)
    {
        this.knob = knob;
        this.browser = browser;
        this.device = device;
        columnSelector = new MappableBrowserColumnSelector(columns);

        browser.exists().markInterested();
        knob.mapTo(columnSelector.getSelectedColumn());
        Button knobButton = knob.getButton();
        knobButton.onDown(knobDownTask);
        knobButton.onUp(new KnobUpTask());
    }

    private class KnobDownTask implements Runnable {

        private boolean lastCallOpenedBrowser = false;

        @Override
        public void run() {
            if (!browser.exists().get()) {
                device.replaceDeviceInsertionPoint().browse();
                columnSelector.selectResultsColumn();
                lastCallOpenedBrowser = true;
            } else
                lastCallOpenedBrowser = false;
            knob.mapTo(columnSelector);
            columnSelectionChanged = false;
        }

        boolean lastCallOpenedBrowser() { return lastCallOpenedBrowser; }

    }

    private class KnobUpTask implements Runnable {

        @Override
        public void run() {
            knob.mapTo(columnSelector.getSelectedColumn());
            if (!knobDownTask.lastCallOpenedBrowser() && !columnSelectionChanged)
                browser.commit();
        }

    }

    private class MappableBrowserColumnSelector extends AbstractDiscreteMappableParameter {

        private final MappableBrowserColumn[] columns;
        private int resultsColumnIndex = -1;
        private int selectedColumnIndex;

        MappableBrowserColumnSelector(MappableBrowserColumn[] columns) {
            this.columns = columns;
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].isResultsColumn()) {
                    resultsColumnIndex = i;
                    break;
                }
            }
            selectResultsColumn();
        }

        @Override
        public void increment(int delta) {
            int newIndex = selectedColumnIndex + delta;
            newIndex = Math.max(0, newIndex);
            newIndex = Math.min(columns.length - 1, newIndex);
            select(newIndex);
            columnSelectionChanged = true;
            flushValue();
        }

        void selectResultsColumn() {
            select(resultsColumnIndex);
        }

        private void select(int index) {
            selectedColumnIndex = index;
            value = (double) index / (double) (columns.length - 1);
        }

        MappableBrowserColumn getSelectedColumn() {
            return columns[selectedColumnIndex];
        }

    }

}
