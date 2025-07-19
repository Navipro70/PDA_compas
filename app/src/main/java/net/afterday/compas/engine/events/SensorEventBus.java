package net.afterday.compas.engine.events;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class SensorEventBus {
    private static SensorEventBus instance;
    private final Subject<Boolean> scanningState = BehaviorSubject.createDefault(true);

    private SensorEventBus() {
        instance = this;
    }

    public static SensorEventBus instance() {
        if (instance == null)
            return new SensorEventBus();
        return instance;
    }
    public void setScanningState(boolean isSuccess) {
        scanningState.onNext(isSuccess);
    }

    public Observable<Boolean> getScanningState() {
        return scanningState;
    }
}
