package net.afterday.compas.engine.events;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * Created by spaka on 6/12/2018.
 */

public class CodeInputEventBus {
    private static final Subject<String> codeScans = PublishSubject.create();
    private static final Subject<Boolean> codeAccepts = PublishSubject.create();
    private static final Subject<String> acceptedCodes = PublishSubject.create();

    public static Observable<String> getCodeScans() {
        return codeScans;
    }

    public static Observable<Boolean> getCodeAccepts() {
        return codeAccepts;
    }

    public static Observable<String> getAcceptedCodes() {
        return acceptedCodes;
    }

    public static void codeScanned(String code) {
        codeScans.onNext(code);
    }

    public static void codeAccepted(boolean accepted) {
        codeAccepts.onNext(accepted);
    }

    public static void notifyCodeAccepted(String code) {
        acceptedCodes.onNext(code);
    }
}