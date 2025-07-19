package net.afterday.compas.sensors;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by Justas Spakauskas on 1/28/2018.
 */

public interface Sensor<T> {
    void start();

    void stop();

    Observable<T> getSensorResultsStream();
}
