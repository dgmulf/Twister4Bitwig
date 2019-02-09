package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.Track;
import icu.dannyism.controller.mappable.AbstractContinuousMappableParameter;

import java.awt.*;

public class MappableTrackColor extends AbstractContinuousMappableParameter {

    private static final float DEFAULT_SATURATION = 0.8f;
    private static final float DEFAULT_BRIGHTNESS = 1.0f;

    private final Track track;
    private float hue;

    public MappableTrackColor(Track track) {
        this.track = track;
        track.color().addValueObserver((float red, float green, float blue) -> {
            final int redComponent = (int) (red * 255.0f);
            final int greenComponent = (int) (green * 255.0f);
            final int blueComponent = (int) (blue * 255.0f);
            float[] HSB = Color.RGBtoHSB(redComponent, greenComponent, blueComponent, null);
            hue = HSB[0];
            flushValue();
        });
        track.position().addValueObserver((int position) -> synchronizeAll());
    }

    @Override
    public void set(double value) {
        hue = (float) value;
        flushColor();
        flushValue();
    }

    @Override
    public void increment(double delta) {
        hue += delta;
        hue = Math.max(0.0f, hue);
        hue = Math.min(1.0f, hue);
        flushColor();
        flushValue();
    }

    public void reset() {
        track.color().set(0.0f, 0.0f, 0.0f, 0.0f);
        synchronizeAll();
    }

    private void flushColor() {
        Color trackColor = Color.getHSBColor(hue, DEFAULT_SATURATION, DEFAULT_BRIGHTNESS);
        float red = trackColor.getRed() / 255.0f;
        float green = trackColor.getGreen() / 255.0f;
        float blue = trackColor.getBlue() / 255.0f;
        track.color().set(red, green, blue);
    }

    @Override
    protected void flushValue() {
        value = hue;
        super.flushValue();
    }
}
