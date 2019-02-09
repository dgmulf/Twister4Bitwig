package icu.dannyism.twister;

import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableEnumValue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleConsumer;

class SensitivitySetting {

    private static final double STEP_SIZE = 0.5;

    private final Set<DoubleConsumer> sensitivityCoefficientObservers = new HashSet<>();

    SensitivitySetting(Preferences preferences, String label, String category) {
        final SettableEnumValue settableValue = preferences.getEnumSetting(
                label,
                category,
                new String[]{
                        Option.SLOWEST.title,
                        Option.SLOWER.title,
                        Option.DEFAULT.title,
                        Option.FASTER.title,
                        Option.FASTEST.title
                },
                Option.DEFAULT.title
        );

        settableValue.addValueObserver((String title) -> {
            final Option option = Option.from(title);
            for (DoubleConsumer observer : sensitivityCoefficientObservers)
                observer.accept(Math.pow(2.0, STEP_SIZE * option.exponent));
        });
    }

    void addSensitivityCoefficientObserver(DoubleConsumer observer) {
        sensitivityCoefficientObservers.add(observer);
    }

    private enum Option {
        SLOWEST("Slowest", -2.0),
        SLOWER("Slower", -1.0),
        DEFAULT("Default", 0.0),
        FASTER("Faster", 1.0),
        FASTEST("Fastest", 2.0);

        final String title;
        final double exponent;

        Option(String title, double exponent) {
            this.title = title;
            this.exponent = exponent;
        }

        static Option from(String label) {
            for (Option option : Option.values())
                if (option.title.equals(label))
                    return option;
            throw new IllegalArgumentException("Undefined sensitivity option");
        }
    }

}
