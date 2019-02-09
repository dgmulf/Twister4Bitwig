package icu.dannyism.twister;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;
import icu.dannyism.bitwig.mappable.*;
import icu.dannyism.twister.mapping.*;

public class MidiFighterTwisterExtension extends ControllerExtension {

    private static final int DEVICE_BANK = 0;
    private static final int MIXER_BANK = 1;
    private static final int TRACK_BANK = 2;
    private static final int USER_BANK = 3;

    private static final double DEFAULT_COARSE_SENSITIVITY = 0.707;
    private static final double DEFAULT_FINE_SENSITIVITY = 0.177;
    
    private MidiFighterTwister twister;

    protected MidiFighterTwisterExtension(final MidiFighterTwisterExtensionDefinition definition, final ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {

        final ControllerHost host = getHost();
        final Application application = host.createApplication();
        final Transport transport = host.createTransport();
        final CursorTrack cursorTrack = host.createCursorTrack(8, 0);
        final PinnableCursorDevice cursorDevice = cursorTrack.createCursorDevice();
        final CursorRemoteControlsPage remoteControlsPage = cursorDevice.createCursorRemoteControlsPage(8);
        final PopupBrowser popupBrowser = host.createPopupBrowser();
        final TrackBank mixerTrackBank = host.createTrackBank(8, 0, 0);
        final Preferences preferences = host.getPreferences();
        final SendBank cursorTrackSends = cursorTrack.sendBank();

        final MappableCursorTrackSelector mappableCursorTrackSelector = new MappableCursorTrackSelector(cursorTrack);
        final PlayheadKnobMapping[] playheadKnobMappings = new PlayheadKnobMapping[2];
        final RemoteControlKnobMapping[] remoteControlMappings = new RemoteControlKnobMapping[8];
        final MappableTrackColor mappableCursorTrackColor = new MappableTrackColor(cursorTrack);

        this.twister = new MidiFighterTwister(host.getMidiInPort(0), host.getMidiOutPort(0));

        /* -------------------- *
         * Device mode (bank 1) *
         * -------------------- */

        final MidiFighterTwister.Knob cursorTrackKnob1              = twister.getKnob(DEVICE_BANK, 0);
        cursorTrackKnob1.mapTo(mappableCursorTrackSelector);
        cursorTrackKnob1.getButton().onDown(cursorTrack.arm()::toggle);
        cursorTrack.color().addValueObserver(cursorTrackKnob1::setColor);

        final MidiFighterTwister.Knob cursorTrackVolumeKnob1        = twister.getKnob(DEVICE_BANK, 1);
        new VolumeKnobMapping(cursorTrackVolumeKnob1, cursorTrack);
        cursorTrack.color().addValueObserver(cursorTrackVolumeKnob1::setColor);

        final MidiFighterTwister.Knob cursorTrackPanKnob1           = twister.getKnob(DEVICE_BANK, 2);
        new PanKnobMapping(cursorTrackPanKnob1, cursorTrack);
        cursorTrack.color().addValueObserver(cursorTrackPanKnob1::setColor);

        final MidiFighterTwister.Knob playheadKnob1                 = twister.getKnob(DEVICE_BANK, 3);
        playheadKnobMappings[0] = new PlayheadKnobMapping(playheadKnob1, transport);

        final MidiFighterTwister.Knob remoteControlsPageKnob        = twister.getKnob(DEVICE_BANK, 4);
        remoteControlsPageKnob.mapTo(new MappableRemoteControlsPageSelector(cursorDevice, remoteControlsPage));
        remoteControlsPageKnob.getButton().onDown(cursorDevice.isRemoteControlsSectionVisible()::toggle);

        final MidiFighterTwister.Knob cursorDeviceKnob              = twister.getKnob(DEVICE_BANK, 5);
        cursorDeviceKnob.mapTo(new MappableCursorDeviceSelector(cursorDevice));
        cursorDeviceKnob.getButton().onDown(cursorDevice.isWindowOpen()::toggle);

        final MidiFighterTwister.Knob enableDeviceKnob              = twister.getKnob(DEVICE_BANK, 6);
        enableDeviceKnob.getButton().onDown(cursorDevice.isEnabled()::toggle);

        final MidiFighterTwister.Knob browserKnob                   = twister.getKnob(DEVICE_BANK, 7);
        new BrowserKnobMapping(
                browserKnob,
                popupBrowser,
                cursorDevice,
                new MappableBrowserColumn[]{
                        new MappableBrowserColumn(popupBrowser.categoryColumn()),
                        new MappableBrowserColumn(popupBrowser.tagColumn()),
                        new MappableBrowserColumn(popupBrowser.creatorColumn()),
                        new MappableBrowserColumn(popupBrowser.resultsColumn())
                }
        );

        remoteControlsPage.setHardwareLayout(HardwareControlType.ENCODER, 4);
        for (int i = 0; i < 8; i++) {
            final MidiFighterTwister.Knob correspondingKnob         = twister.getKnob(DEVICE_BANK, i + 8);
            remoteControlMappings[i] = new RemoteControlKnobMapping(
                    correspondingKnob,
                    new MappableRemoteControl(cursorTrack, cursorDevice, remoteControlsPage, i)
            );
            correspondingKnob.onHasPhysicalKnob(remoteControlsPage.getParameter(i)::setIndication);
        }

//        twister.getSideButton(DEVICE_BANK, 2).onDown(application::undo);
//        twister.getSideButton(DEVICE_BANK, 5).onDown(application::redo);


        /* ------------------- *
         * Mixer mode (bank 2) *
         * ------------------- */

        for (int i = 0; i < 8; i++) {
            final Track correspondingTrack = mixerTrackBank.getItemAt(i);

            final int panKnobIndex = i < 4 ? i : i + 4;
            final MidiFighterTwister.Knob correspondingPanKnob      = twister.getKnob(MIXER_BANK, panKnobIndex);
            new PanKnobMapping(correspondingPanKnob, correspondingTrack);
            correspondingPanKnob.onHasPhysicalKnob(correspondingTrack.pan()::setIndication);

            final int volumeKnobIndex = i < 4 ? i + 4 : i + 8;
            final MidiFighterTwister.Knob correspondingVolumeKnob   = twister.getKnob(MIXER_BANK, volumeKnobIndex);
            new ColoredVolumeKnobMapping(correspondingVolumeKnob, correspondingTrack);
            correspondingVolumeKnob.onHasPhysicalKnob(correspondingTrack.volume()::setIndication);
        }

        twister.getSideButton(MIXER_BANK, 2).onDown(mixerTrackBank::scrollPageBackwards);
        twister.getSideButton(MIXER_BANK, 5).onDown(mixerTrackBank::scrollPageForwards);


        /* ------------------- *
         * Track mode (bank 3) *
         * ------------------- */

        final MidiFighterTwister.Knob cursorTrackKnob2              = twister.getKnob(TRACK_BANK, 0);
        cursorTrackKnob2.mapTo(mappableCursorTrackSelector);
        cursorTrackKnob2.getButton().onDown(cursorTrack.arm()::toggle);
        cursorTrack.color().addValueObserver(cursorTrackKnob2::setColor);

        final MidiFighterTwister.Knob cursorTrackVolumeKnob2        = twister.getKnob(TRACK_BANK, 1);
        new VolumeKnobMapping(cursorTrackVolumeKnob2, cursorTrack);
        cursorTrack.color().addValueObserver(cursorTrackVolumeKnob2::setColor);

        final MidiFighterTwister.Knob cursorTrackPanKnob2           = twister.getKnob(TRACK_BANK, 2);
        new PanKnobMapping(cursorTrackPanKnob2, cursorTrack);
        cursorTrack.color().addValueObserver(cursorTrackPanKnob2::setColor);

        final MidiFighterTwister.Knob playheadKnob2                 = twister.getKnob(TRACK_BANK, 3);
        playheadKnobMappings[1] = new PlayheadKnobMapping(playheadKnob2, transport);

        final MidiFighterTwister.Knob loopStartKnob                 = twister.getKnob(TRACK_BANK, 4);
        new LoopStartKnobMapping(loopStartKnob, transport);

        final MidiFighterTwister.Knob loopEndKnob                   = twister.getKnob(TRACK_BANK, 5);
        new LoopEndKnobMapping(loopEndKnob, transport);

        final MidiFighterTwister.Knob zoomKnob                      = twister.getKnob(TRACK_BANK, 6);
        zoomKnob.mapTo(new MappableZoomLevel(application));
        zoomKnob.getButton().onDown(application::zoomToFit);

        final MidiFighterTwister.Knob colorKnob                     = twister.getKnob(TRACK_BANK, 7);
        colorKnob.mapTo(mappableCursorTrackColor);
        colorKnob.getButton().onDown(() -> colorKnob.enableFineControl(true));
        colorKnob.getButton().onUp(() -> colorKnob.enableFineControl(false));
        cursorTrack.color().addValueObserver(colorKnob::setColor);

        for (int i = 0; i < 8; i++) {
            final MidiFighterTwister.Knob correspondingKnob         = twister.getKnob(TRACK_BANK, i + 8);
            MappableSendLevel mappableSendLevel = new MappableSendLevel(cursorTrack, cursorTrackSends, i);
            correspondingKnob.mapTo(mappableSendLevel);
            correspondingKnob.getButton().onDown(() -> correspondingKnob.enableFineControl(true));
            correspondingKnob.getButton().onUp(() -> correspondingKnob.enableFineControl(false));
            correspondingKnob.getButton().onDoublePress(mappableSendLevel::reset);
            cursorTrackSends.getItemAt(i).sendChannelColor().addValueObserver(correspondingKnob::setColor);
        }

        twister.getSideButton(TRACK_BANK, 2).onDown(cursorTrackSends::scrollPageBackwards);
        twister.getSideButton(TRACK_BANK, 5).onDown(cursorTrackSends::scrollPageForwards);


        /* ----------- *
         * Preferences *
         * ----------- */

        final SensitivitySetting coarseSensitivity = new SensitivitySetting(
                preferences,
                "Coarse adjustment rate",
                "Continuous Controls"
        );
        coarseSensitivity.addSensitivityCoefficientObserver((double coefficient) -> {
            twister.encoderSettings.sensitivity = coefficient * DEFAULT_COARSE_SENSITIVITY;
        });

        final SensitivitySetting fineSensitivity = new SensitivitySetting(
                preferences,
                "Fine adjustment rate",
                "Continuous Controls"
        );
        fineSensitivity.addSensitivityCoefficientObserver((double coefficient) -> {
            twister.encoderSettings.fineSensitivity = coefficient * DEFAULT_FINE_SENSITIVITY;
        });

        final SettableEnumValue continuousOutputMode = preferences.getEnumSetting(
                "Output mode",
                "Continuous Controls",
                new String[]{"Absolute", "Relative"},
                "Absolute"
        );
        continuousOutputMode.addValueObserver((String setting) -> {
            if (setting.equals("Absolute"))
                twister.encoderSettings.absoluteOutput = true;
            else if (setting.equals("Relative"))
                twister.encoderSettings.absoluteOutput = false;
        });

        final SettableEnumValue scrollRate = preferences.getEnumSetting(
                "List scroll rate",
                "Discrete Controls",
                new String[]{"Slow", "Medium", "Fast"},
                "Medium"
        );
        scrollRate.addValueObserver((String setting) -> {
            switch (setting) {
                case "Slow":
                    twister.encoderSettings.scrollResistance = 9;
                    break;
                case "Medium":
                    twister.encoderSettings.scrollResistance = 6;
                    break;
                case "Fast":
                    twister.encoderSettings.scrollResistance = 3;
                    break;
            }
        });

        final SettableEnumValue sensitivitySwitchBehavior = preferences.getEnumSetting(
                "Sensitivity switch behavior",
                "Device Controls",
                new String[]{"Hold", "Toggle"},
                "Toggle"
        );
        sensitivitySwitchBehavior.addValueObserver((String setting) -> {
            final boolean holdForFineControl = setting.equals("Hold");
            for (RemoteControlKnobMapping mapping : remoteControlMappings)
                mapping.enableHoldForFineControl(holdForFineControl);
        });

        final SettableBooleanValue autoScrollTracks = preferences.getBooleanSetting(
                "Scroll to follow cursor track",
                "Miscellaneous",
                true
        );
        autoScrollTracks.addValueObserver(mappableCursorTrackSelector::enableAutoScroll);

        final SettableBooleanValue flashOnPlay = preferences.getBooleanSetting(
                "Flash transport knob on playback",
                "Miscellaneous",
                false
        );
        flashOnPlay.addValueObserver((boolean flash) -> {
            for (PlayheadKnobMapping mapping : playheadKnobMappings)
                mapping.enableFlashOnPlay(flash);
        });


        // Bank selection to ensure we know the active bank
        twister.selectBank(DEVICE_BANK);
    }

    @Override
    public void exit() {
//        twister.exit();
    }

    @Override
    public void flush() {
    }

}
