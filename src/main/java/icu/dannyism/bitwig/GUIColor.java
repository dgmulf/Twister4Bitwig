package icu.dannyism.bitwig;

public enum GUIColor {
    REMOTE_CONTROL_1,
    REMOTE_CONTROL_2,
    REMOTE_CONTROL_3,
    REMOTE_CONTROL_4,
    REMOTE_CONTROL_5,
    REMOTE_CONTROL_6,
    REMOTE_CONTROL_7,
    REMOTE_CONTROL_8;

    public static GUIColor REMOTE_CONTROL(int index) {
        switch (index) {
            case 0:
                return REMOTE_CONTROL_1;
            case 1:
                return REMOTE_CONTROL_2;
            case 2:
                return REMOTE_CONTROL_3;
            case 3:
                return REMOTE_CONTROL_4;
            case 4:
                return REMOTE_CONTROL_5;
            case 5:
                return REMOTE_CONTROL_6;
            case 6:
                return REMOTE_CONTROL_7;
            case 7:
                return REMOTE_CONTROL_8;
            default:
                return null;
        }
    }
}
