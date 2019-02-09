package icu.dannyism.twister;

public enum AnimationState {

    RGB_NONE(0),
    RGB_TOGGLE_EVERY_EIGHTH(6);

    public final int data;

    AnimationState(int data) {
        this.data = data;
    }

}
