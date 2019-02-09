package icu.dannyism.twister;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.NoteInput;
import icu.dannyism.bitwig.GUIColor;
import icu.dannyism.controller.Button;
import icu.dannyism.controller.EndlessRotary;
import icu.dannyism.controller.mappable.ContinuousMappableParameter;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MidiFighterTwister {

    public final EndlessRotary.Settings encoderSettings = new EndlessRotary.Settings();

    private final MidiOut outputPort;
    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);
    private final Knob[][] knobs = new Knob[4][16];
    private final Button[][] sideButtons = new Button[4][6];
    private int activeBank = -1;
    private int indicatorStep = -1;

    public MidiFighterTwister(MidiIn inputPort, MidiOut outputPort) {

        inputPort.setMidiCallback((ShortMidiMessageReceivedCallback) this::parseInput);
        inputPort.createNoteInput("Sequencer", "87????", "97????", "B7????");
        NoteInput userControlInput = inputPort.createNoteInput("User Controls", "B03???", "B13???", "B43???");
        userControlInput.setShouldConsumeEvents(false);

        this.outputPort = outputPort;

        this.scheduledExecutor.setRemoveOnCancelPolicy(true);

        for (int bank = 0; bank < 4; bank++) {
            for (int index = 0; index < 16; index++) {
                int cc = (bank * 16) + index;
                switch (cc) {
                    case 0:
                    case 3:
                    case 4:
                    case 5:
                    case 7:
                    case 32:
                    case 35:
                    case 36:
                    case 37:
                    case 38:
                        knobs[bank][index] = new Knob(cc, IndicatorStyle.DOT);
                        break;
                    default:
                        knobs[bank][index] = new Knob(cc, IndicatorStyle.BLENDED_BAR);
                        break;
                }
            }

            for (int index = 0; index < 6; index++)
                this.sideButtons[bank][index] = new Button();
        }

    }

    public Knob getKnob(int bank, int index) {
        return this.knobs[bank][index];
    }

    public Button getSideButton(int bank, int index) {
        return this.sideButtons[bank][index];
    }

    private void parseInput(ShortMidiMessage input) {
        if (input.isControlChange()) {

            int channel = input.getChannel();
            int cc = input.getData1();
            int value = input.getData2();

            // Knob twist (banks 1-3)
            if (channel == 0 && cc >= 0 && cc < 48) {
                int bank = cc / 16;
                int index = cc % 16;
                if (value == 65)
                    this.knobs[bank][index].clockwise();
                else if (value == 63)
                    this.knobs[bank][index].counterClockwise();
            }

            // Knob push (banks 1-3)
            if (channel == 1 && cc >= 0 && cc < 48) {
                int bank = cc / 16;
                int index = cc % 16;
                if (value == 127)
                    this.knobs[bank][index].getButton().down();
                else if (value == 0)
                    this.knobs[bank][index].getButton().up();
            }

            // Side button push
            if (channel == 3 && cc >= 8 && cc < 32) {
                int bank = (cc - 8) / 6;
                int index = (cc - 8) % 6;
                if (value == 127)
                    this.sideButtons[bank][index].down();
                else if (value == 0)
                    this.sideButtons[bank][index].up();
            }

            // Bank change
            if (channel == 3 && cc >= 0 && cc < 4) {
                if (value == 127)
                    registerBankChange(cc);
            }

        }
    }

    public void selectBank(int index) {
        outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE + 3, index, 127);
        registerBankChange(index);
    }

    public void exit() {
        for (int bank = 0; bank < 4; bank++)
            for (int index = 0; index < 16; index++)
                this.knobs[bank][index].exit();
    }

    private void registerBankChange(int index) {
        if (activeBank != index) {
            if (activeBank >= 0)
                for (Knob knob : knobs[activeBank])
                    knob.hasPhysicalKnob(false);
            for (Knob knob : knobs[index])
                knob.hasPhysicalKnob(true);
            activeBank = index;
        }
    }

    public class Knob extends EndlessRotary {

        private static final int RESOLUTION = 69;
        private static final long INDICATION_TIMEOUT = 1000;

        private final int cc;
        private final IndicatorStyle indicatorStyle;
        private final Button button;
        private final Set<Consumer<Boolean>> hasPhysicalKnobObservers = new HashSet<>();
        private ScheduledFuture<?> futureClearIndication;
        private final ClearIndicatorTask clearIndicatorTask = new ClearIndicatorTask();

        Knob(int cc, IndicatorStyle indicatorStyle) {
            super(RESOLUTION, encoderSettings, scheduledExecutor);
            this.cc = cc;
            this.indicatorStyle = indicatorStyle;
            this.button = new KnobButton();
        }

        public Button getButton() {
            return this.button;
        }

        private void updateIndicator() {
            int indicatorData = 0;

            if (indicatorStyle == IndicatorStyle.DOT) {
                if (indicatorStep >= 0)
                    indicatorData = indicatorStep * 12 + 1;
            } else if (position >= 0.0) {
                indicatorData = Math.min(127, (int) (position * 128.0));
            }

            setIndicator(indicatorData);
        }

        private void setIndicator(int data) {
            outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE, this.cc, data);
        }

        public void setColor(double hue, double saturation) {
            int data;
            if (saturation > 0.0) {
                double adjustedHue = (2.0 / 3.0) - hue;
                if (adjustedHue < 0.0)
                    adjustedHue += 1.0;
                data = Math.min(125, (int) (adjustedHue * 125.0) + 1);
            } else {
                data = 0; // Greys use default inactive color
            }
            setColor(data);
        }

        public void setColor(float red, float green, float blue) {
            float[] HSB = Color.RGBtoHSB(
                    Math.min(255, (int) (red * 256.0)),
                    Math.min(255, (int) (green * 256.0)),
                    Math.min(255, (int) (blue * 256.0)),
                    null
            );
            setColor(HSB[0], HSB[1]);
        }

        public void setColor(int data) {
            outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE + 1, this.cc, data);
        }

        public void setColor(GUIColor color) {
            int data;
            switch (color) {
                case REMOTE_CONTROL_1:
                    data = 87;
                    break;
                case REMOTE_CONTROL_2:
                    data = 72;
                    break;
                case REMOTE_CONTROL_3:
                    data = 62;
                    break;
                case REMOTE_CONTROL_4:
                    data = 53;
                    break;
                case REMOTE_CONTROL_5:
                    data = 39;
                    break;
                case REMOTE_CONTROL_6:
                    data = 1;
                    break;
                case REMOTE_CONTROL_7:
                    data = 113;
                    break;
                case REMOTE_CONTROL_8:
                    data = 97;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported Bitwig GUI color");
            }
            outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE + 1, this.cc, data);
        }

        public void setAnimationState(AnimationState state) {
            outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE + 2, this.cc, state.data);
        }

        // If this encoder object's bank is selected, notify the observer
        public void onHasPhysicalKnob(Consumer<Boolean> observer) {
            hasPhysicalKnobObservers.add(observer);
        }

        @Override
        public void mapTo(ContinuousMappableParameter continuousParameter) {
            super.mapTo(continuousParameter);

            if (futureClearIndication != null) {
                futureClearIndication.cancel(false);
                futureClearIndication = null;
            }
            if (!parameterFeedback) {
                position = 0.0;
                indicatorStep = -1;
                updateIndicator();
            }
        }

        @Override
        public void mapTo(DiscreteMappableParameter discreteParameter) {
            super.mapTo(discreteParameter);

            if (futureClearIndication != null) {
                futureClearIndication.cancel(false);
                futureClearIndication = null;
            }
            if (!parameterFeedback) {
                position = -1.0;
                indicatorStep = -1;
                updateIndicator();
            }
        }

        @Override
        public void exit() {
            super.exit();
            setIndicator(0); // Dim indicator LED
            outputPort.sendMidi(ShortMidiMessage.CONTROL_CHANGE + 1, this.cc, 0); // Dim color segment LED
        }

        @Override
        protected void setImmediately(double position) {
            this.position = position;
            if (indicatorStyle == IndicatorStyle.DOT)
                indicatorStep = Math.min(10, (int) (position * 11.0));
            updateIndicator();
        }

        @Override
        protected void incrementDiscreteParameter(int delta) {
            super.incrementDiscreteParameter(delta);

            if (!parameterFeedback) {
                if (futureClearIndication != null)
                    futureClearIndication.cancel(false);
                else
                    indicatorStep = 5;

                indicatorStep += delta;
                while (indicatorStep > 10)
                    indicatorStep -= 11;
                while (indicatorStep < 0)
                    indicatorStep += 11;

                updateIndicator();

                futureClearIndication = scheduledExecutor.schedule(
                        clearIndicatorTask,
                        INDICATION_TIMEOUT,
                        TimeUnit.MILLISECONDS
                );
            }
        }

        void hasPhysicalKnob(boolean hasPhysicalKnob) {
            for (Consumer<Boolean> observer : hasPhysicalKnobObservers)
                observer.accept(hasPhysicalKnob);
        }

        private class KnobButton extends Button {
            @Override
            public void down() {
                super.down();
                microDetent();
            }

            @Override
            public void up() {
                super.up();
                microDetent();
            }
        }

        private class ClearIndicatorTask implements Runnable {
            @Override
            public void run() {
                indicatorStep = -1;
                updateIndicator();
                futureClearIndication = null;
            }
        }

    }

}
