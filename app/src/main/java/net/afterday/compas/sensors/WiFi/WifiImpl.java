package net.afterday.compas.sensors.WiFi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import net.afterday.compas.engine.events.SensorEventBus;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class WifiImpl implements WiFi {
    private static final String TAG = "WiFi sensor";

    private boolean successScan;
    private WifiManager mWifi;
    private int scanInterval;
    private Context context;
    private Handler handler;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Observable<List<ScanResult>> wifiScans = PublishSubject.create();
    private Subject<Boolean> isRunningSubj = BehaviorSubject.createDefault(false);

    @SuppressLint("CheckResult")
    private void startScanning() {
        isRunningSubj.switchMap((isRunning) -> isRunning ? Observable.interval(scanInterval, TimeUnit.MILLISECONDS) : Observable.empty()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((t) -> {
                    SensorEventBus.instance().setScanningState(mWifi.startScan());
                    @SuppressLint("MissingPermission") List<ScanResult> results = mWifi.getScanResults();
                    if (wifiScans == null) {
                        return;
                    }
                    ((Subject) wifiScans).onNext(results);
                });
    }

    public WifiImpl(Context context) {
        this.context = context;
        successScan = true;
        mWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private void waitForGpsScanPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Снова проверяем разрешение
            Log.d(TAG, "Location permission not yet granted.");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitForGpsScanPermission(context); // каждую секунду
                }
            }, 1000);
        } else {
            // Разрешение предоставлено, можно сканировать устройства
            Log.d(TAG, "Gps permission granted.");
            startScanning();
        }
    }

    public void start() {
        Log.d(TAG, "WIFI Sensor started " + Thread.currentThread().getName());
        isRunning.set(true);
        this.isRunningSubj.onNext(true);
        handler = new Handler(Looper.getMainLooper());
        scanInterval = 1500;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mWifi.isScanThrottleEnabled()) {
                scanInterval = 30250;
            }
            waitForGpsScanPermission(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            for (int i = 0; i < 5; i++) {
                if (!mWifi.startScan()) {
                    scanInterval = 30250;
                    break;
                }
            }
            if (scanInterval == 30250){
                Handler scanDelayHandler = new Handler(Looper.getMainLooper());
                scanDelayHandler.postDelayed(() -> waitForGpsScanPermission(context), 120 * 1000);
            } else {
                waitForGpsScanPermission(context);
            }
        }
        else {
            waitForGpsScanPermission(context);
        }
    }

    public void stop() {
        //Log.d(TAG, "Sensor stopped");
        isRunning.set(false);
        this.isRunningSubj.onNext(false);
    }

    @Override
    public Observable<List<ScanResult>> getSensorResultsStream() {
        return wifiScans;
    }

}
