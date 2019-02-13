package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.TrackBank;
import icu.dannyism.controller.mappable.AbstractDiscreteMappableParameter;

public class MappableCursorTrackSelector extends AbstractDiscreteMappableParameter {

    private static final int SIBLINGS_BANK_CAPACITY = 128;

    private boolean autoScroll = true;
    private final CursorTrack cursorTrack;
    private final TrackBank siblingsBank;
    private int cursorPosition = -1;
    private int trackCount = 0;
    private int relativeCursorIndex;
    private final boolean[] siblingIsActive = new boolean[SIBLINGS_BANK_CAPACITY];

    public MappableCursorTrackSelector(CursorTrack cursorTrack) {
        this.cursorTrack = cursorTrack;
        siblingsBank = cursorTrack.createSiblingsTrackBank(SIBLINGS_BANK_CAPACITY, 0, 0, true, true);

        siblingsBank.cursorIndex().addValueObserver((int index) -> relativeCursorIndex = index);
        for (int i = 0; i < SIBLINGS_BANK_CAPACITY; i++) {
            final int index = i;
            siblingsBank.getItemAt(i).isActivated().addValueObserver((boolean active) -> {
                siblingIsActive[index] = active;
            });
        }

        cursorTrack.position().addValueObserver((int position) -> {
            cursorPosition = position;
            flushValue();
        });
        siblingsBank.channelCount().addValueObserver((int count) -> {
            trackCount = count;
            flushValue();
        });
    }

    @Override
    public void increment(int delta) {
        int newIndex = relativeCursorIndex;
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
        value = (trackCount > 1) ? (double) cursorPosition / (double) (trackCount - 1) : 0.5;
        super.flushValue();
    }

    private int nextActiveTrackIndex(int startingIndex) {
        int index = startingIndex;
        do {
            index++;
            if (index >= SIBLINGS_BANK_CAPACITY)
                return -1;
        } while (!siblingIsActive[index]);
        return index;
    }

    private int previousActiveTrackIndex(int startingIndex) {
        int index = startingIndex;
        do {
            index--;
            if (index < 0)
                return -1;
        } while (!siblingIsActive[index]);
        return index;
    }
}
