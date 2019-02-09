package icu.dannyism.bitwig.mappable;

import com.bitwig.extension.controller.api.Application;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;

public class MappableZoomLevel implements DiscreteMappableParameter {

    private final Application application;

    public MappableZoomLevel(Application application) {
        this.application = application;
    }

    @Override
    public void increment(int delta) {
        if (delta > 0)
            for (int i = 0; i < delta; i++)
                application.zoomIn();
        else if (delta < 0)
            for (int i = 0; i > delta; i--)
                application.zoomOut();
    }
}
