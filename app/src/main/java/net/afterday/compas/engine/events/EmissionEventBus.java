package net.afterday.compas.engine.events;


import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * Created by spaka on 5/23/2018.
 */

public class EmissionEventBus {
    public static final int WARN_BEFORE = 4 * 60;
    private static final Subject<Boolean> emissionActive = BehaviorSubject.createDefault(false);
    private static final Subject<Integer> emissionWarning = PublishSubject.create();
    private static final Subject<Integer> fakeEmissions = PublishSubject.create();
    private static EmissionEventBus instance;
    private Disposable waitingForEmission;

    public static EmissionEventBus instance() {
        if (instance == null) {
            instance = new EmissionEventBus();
        }
        return instance;
    }

    public void setEmissionActive(boolean emissionActive) {
        EmissionEventBus.emissionActive.onNext(emissionActive);
    }

    public void emissionWillStart(int startsAfter) {
        EmissionEventBus.emissionWarning.onNext(startsAfter);
    }

    public void fakeEmission() {
        EmissionEventBus.fakeEmissions.onNext(1);
    }

    public Observable<Boolean> getEmissionStateStream() {
        return EmissionEventBus.emissionActive;
    }

    public Observable<Integer> getEmissionWarnings() {
        return EmissionEventBus.emissionWarning;
    }

    public Observable<Integer> getFakeEmissions() {
        return EmissionEventBus.fakeEmissions;
    }
}
