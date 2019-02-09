package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.TrackBank;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;

public class MappableCursorTrackSelector extends AbstractDiscreteMappableParameter {

    private static final int SIBLINGS_BANK_CAPACITY = 128;

    private boolean autoScroll = true;
    private final CursorTrack cursorTrack;
    private final TrackBank siblingsBank;
    private int cursorIndex = -1;
    private int trackCount = 0;

    public MappableCursorTrackSelector(CursorTrack cursorTrack) {
        this.cursorTrack = cursorTrack;
        siblingsBank = cursorTrack.createSiblingsTrackBank(SIBLINGS_BANK_CAPACITY, 0, 0, true, true);
        siblingsBank.cursorIndex().markInterested();
        for (int i = 0; i < SIBLINGS_BANK_CAPACITY; i++)
            siblingsBank.getItemAt(i).isActivated().markInterested();

        cursorTrack.position().addValueObserver((int position) -> {
            cursorIndex = position;
            flushValue();
        });
        siblingsBank.channelCount().addValueObserver((int count) -> {
            trackCount = count;
            flushValue();
        });
    }

    @Override
    public void increment(int delta) {
        int newIndex = siblingsBank.cursorIndex().get();
        if (delta > 0) {
            for (int i = 0; i < delta; i++)
                newIndex = nextActiveTrackIndex(newIndex);
        } else if (delta < 0) {
            for (int i = 0; i > delta; i--)
                newIndex = previousActiveTrackIndex(newIndex);
        }
        if (newIndex >= 0) {
            siblingsBank.cursorIndex().set(newIndex);
            if (autoScroll) {
                cursorTrack.makeVisibleInArranger();
                cursorTrack.makeVisibleInMixer();
            }
        }
    }

    public void enableAutoScroll(boolean enabled) { autoScroll = enabled; }

    @Override
    protected void flushValue() {
        value = (trackCount > 1) ? (double) cursorIndex / (double) (trackCount - 1) : 0.5;
        super.flushValue();
    }

    private int nextActiveTrackIndex(int startingIndex) {
        int index = startingIndex;
        do {
            index++;
            if (index >= SIBLINGS_BANK_CAPACITY)
                return -1;
        } while (!siblingsBank.getItemAt(index).isActivated().get());
        return index;
    }

    private int previousActiveTrackIndex(int startingIndex) {
        int index = startingIndex;
        do {
            index--;
            if (index < 0)
                return -1;
        } while (!siblingsBank.getItemAt(index).isActivated().get());
        return index;
    }
}
