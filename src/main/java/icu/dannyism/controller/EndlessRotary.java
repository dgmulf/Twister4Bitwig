package icu.dannyism.controller;

import icu.dannyism.controller.mappable.ContinuousMappableParameter;
import icu.dannyism.controller.mappable.DiscreteMappableParameter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EndlessRotary implements SynchronizedHardwareControl {

    private final static long SYNCHRONIZE_DELAY = 500;
    private final static long DETENT_DELAY = 1000;

    private final int resolution;
    private final Settings settings;
    private final ScheduledExecutorService scheduledExecutor;

    private ContinuousMappableParameter continuousParameter;
    private DiscreteMappableParameter discreteParameter;
    protected boolean parameterFeedback = false;

    protected volatile double position = 0.0;
    private boolean enableFineControl = false;
    private int clockwiseResistance;
    private int counterClockwiseResistance;
    private int clockwiseTension;
    private int counterClockwiseTension;

    private double pendingPosition;
    private boolean updatePending = false;
    private final Runnable syncTask = new SynchronizeTask();
    private ScheduledFuture<?> futureSync;

    private final Runnable microDetentTask = new MicroDetentTask();
    private ScheduledFuture<?> futureMicroDetent;

    public EndlessRotary(int resolution, Settings settings, ScheduledExecutorService scheduledExecutor) {
        this.resolution = resolution;
        this.settings = settings;
        this.scheduledExecutor = scheduledExecutor;
        microDetent();
    }

    public synchronized void clockwise() {
        clockwiseTension++;
        counterClockwiseTension = 0;
        if (clockwiseTension == clockwiseResistance) {
            clockwiseTension = 0;
            if (continuousParameter != null) {
                clockwiseResistance = counterClockwiseResistance = 1;
                double sensitivity = enableFineControl ? settings.fineSensitivity : settings.sensitivity;
                double increment = sensitivity / (double) resolution;
                if (settings.absoluteOutput) {
                    double newPosition = position + increment;
                    if (newPosition > 1.0) {
                        newPosition = 1.0;
                        counterClockwiseResistance = 2;
                    } else if (Math.abs(newPosition - 0.5) < 0.5 * increment) { // Guaranteed stop at 50%
                        newPosition = 0.5;
                        counterClockwiseResistance = 2;
                    }
                    continuousParameter.set(newPosition);
                    if (futureSync != null)
                        futureSync.cancel(false);
                    setImmediately(newPosition);
                    futureSync = scheduledExecutor.schedule(syncTask, SYNCHRONIZE_DELAY, TimeUnit.MILLISECONDS);
                } else
                    incrementContinuousParameter(increment);
            } else if (discreteParameter != null) {
                clockwiseResistance = settings.scrollResistance;
                counterClockwiseResistance = 2;
                incrementDiscreteParameter(1);
            }
        }
        if (futureMicroDetent != null)
            futureMicroDetent.cancel(false);
        futureMicroDetent = scheduledExecutor.schedule(microDetentTask, DETENT_DELAY, TimeUnit.MILLISECONDS);
    }

    public synchronized void counterClockwise() {
        counterClockwiseTension++;
        clockwiseTension = 0;
        if (counterClockwiseTension == counterClockwiseResistance) {
            counterClockwiseTension = 0;
            if (continuousParameter != null) {
                clockwiseResistance = counterClockwiseResistance = 1;
                double sensitivity = enableFineControl ? settings.fineSensitivity : settings.sensitivity;
                double increment = sensitivity / (double) resolution;
                if (settings.absoluteOutput) {
                    double newPosition = position - increment;
                    if (newPosition < 0.0) {
                        newPosition = 0.0;
                        clockwiseResistance = 2;
                    } else if (Math.abs(newPosition - 0.5) < 0.5 * increment) { // Guaranteed stop at 50%
                        newPosition = 0.5;
                        clockwiseResistance = 2;
                    }
                    continuousParameter.set(newPosition);
                    if (futureSync != null)
                        futureSync.cancel(false);
                    setImmediately(newPosition);
                    futureSync = scheduledExecutor.schedule(syncTask, SYNCHRONIZE_DELAY, TimeUnit.MILLISECONDS);
                } else
                    incrementContinuousParameter(-increment);
            } else if (discreteParameter != null) {
                counterClockwiseResistance = settings.scrollResistance;
                clockwiseResistance = 2;
                incrementDiscreteParameter(-1);
            }
        }
        if (futureMicroDetent != null)
            futureMicroDetent.cancel(false);
        futureMicroDetent = scheduledExecutor.schedule(microDetentTask, DETENT_DELAY, TimeUnit.MILLISECONDS);
    }

    public void mapTo(ContinuousMappableParameter continuousParameter) {
        if (futureSync != null) {
            futureSync.cancel(false);
            futureSync = null;
            updatePending = false;
        }

        if (this.continuousParameter != null)
            this.continuousParameter.unsubscribe(this);
        else if (discreteParameter != null) {
            discreteParameter.unsubscribe(this);
            discreteParameter = null;
        }
        this.continuousParameter = continuousParameter;
        parameterFeedback = continuousParameter.subscribe(this);
    }

    public void mapTo(DiscreteMappableParameter discreteParameter) {
        if (futureSync != null) {
            futureSync.cancel(false);
            futureSync = null;
            updatePending = false;
        }

        if (continuousParameter != null) {
            continuousParameter.unsubscribe(this);
            continuousParameter = null;
        } else if (this.discreteParameter != null)
            this.discreteParameter.unsubscribe(this);

        this.discreteParameter = discreteParameter;
        parameterFeedback = discreteParameter.subscribe(this);
    }

    public void clearMapping() {
        if (futureSync != null) {
            futureSync.cancel(false);
            futureSync = null;
            updatePending = false;
        }

        if (continuousParameter != null)
            continuousParameter.unsubscribe(this);
        else if (this.discreteParameter != null)
            discreteParameter.unsubscribe(this);
        continuousParameter = null;
        discreteParameter = null;

        parameterFeedback = false;
    }

    public synchronized void set(double position) {
        if (futureSync == null)
            setImmediately(position);
        else {
            pendingPosition = position;
            updatePending = true;
        }
    }

    public void synchronize() {
        if (futureSync != null) {
            futureSync.cancel(false);
            syncTask.run();
        }
    }

    public void enableFineControl(boolean enabled) { enableFineControl = enabled; }

    public void toggleFineControl() { enableFineControl = !enableFineControl; }

    public void exit() {
        if (futureSync != null)
            futureSync.cancel(true);
        if (futureMicroDetent != null)
            futureMicroDetent.cancel(true);
    }

    protected void incrementContinuousParameter(double delta) {
        continuousParameter.increment(delta);
    }

    protected void incrementDiscreteParameter(int delta) {
        discreteParameter.increment(delta);
    }

    protected void setImmediately(double position) {
        this.position = position;
    }

    protected void microDetent() {
        if (futureMicroDetent != null) {
            futureMicroDetent.cancel(false);
            futureMicroDetent = null;
        }
        microDetentTask.run();
    }

    public static class Settings {
        public double sensitivity = 1.0;
        public double fineSensitivity = 0.25;
        public boolean absoluteOutput = true;
        public int scrollResistance = 5;
    }

    private class SynchronizeTask implements Runnable {
        @Override
        public void run() {
            synchronized (EndlessRotary.this) {
                if (updatePending) {
                    setImmediately(pendingPosition);
                    updatePending = false;
                }
                futureSync = null;
            }
        }
    }

    private class MicroDetentTask implements Runnable {
        @Override
        public void run() {
            synchronized(EndlessRotary.this) {
                clockwiseTension = counterClockwiseTension = 0;
                clockwiseResistance = counterClockwiseResistance = 2;
                futureMicroDetent = null;
            }
        }
    }

}
