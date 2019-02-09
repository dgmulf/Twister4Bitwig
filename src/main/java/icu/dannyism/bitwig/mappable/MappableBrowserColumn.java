package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.BrowserColumn;
import com.bitwig.extension.controller.api.BrowserResultsColumn;
import com.bitwig.extension.controller.api.CursorBrowserItem;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;

public class MappableBrowserColumn extends AbstractDiscreteMappableParameter {

    private final CursorBrowserItem cursorBrowserItem;
    private final boolean isResultsColumn;
    private boolean exists = false;
    private int resultIndex = 0;
    private int resultCount = 0;

    public MappableBrowserColumn(BrowserColumn browserColumn) {
        cursorBrowserItem = (CursorBrowserItem) browserColumn.createCursorItem();
        isResultsColumn = browserColumn instanceof BrowserResultsColumn;
        browserColumn.exists().addValueObserver((boolean exists) -> this.exists = exists);
        cursorBrowserItem.createSiblingsBank(1).scrollPosition().addValueObserver((int position) -> {
            resultIndex = position;
            flushValue();
        });
        browserColumn.entryCount().addValueObserver((int count) -> {
            resultCount = isResultsColumn ? count : count + 1;
            flushValue();
        });
    }

    @Override
    public void increment(int delta) {
        if (delta > 0)
            for (int i = 0; i < delta; i++)
                cursorBrowserItem.selectNext();
        else if (delta < 0)
            for (int i = 0; i > delta; i--)
                cursorBrowserItem.selectPrevious();
    }

    public boolean isResultsColumn() { return isResultsColumn; }

    @Override
    protected void flushValue() {
        if (exists)
            value = resultCount > 1 ? (double) resultIndex / (double) (resultCount - 1) : 0.5;
        else
            value = -1.0;
        super.flushValue();
    }
}
