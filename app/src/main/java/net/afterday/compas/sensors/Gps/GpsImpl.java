package net.afterday.compas.sensors.Gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * Created by Justas Spakauskas on 1/28/2018.
 */

public class GpsImpl implements Gps {
    private static final String TAG = "GpsImpl";
    private Context context;
    private Handler handler;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Disposable d;
    private Subject<Integer> satelitesCount = BehaviorSubject.createDefault(0);

    public GpsImpl(Context context) {
        this.context = context;
        locationListener = new LocationListenerImpl();
    }

    private void waitForGpsScanPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
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
            d = Observable.timer(0, TimeUnit.SECONDS).take(1).observeOn(AndroidSchedulers.mainThread()).subscribe((t) -> locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener));
        }
    }

    @Override
    public void start() {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            handler = new Handler(Looper.getMainLooper());
            waitForGpsScanPermission(context);
        }
    }

    @Override
    public void stop() {
        if (d != null && !d.isDisposed()) {
            d.dispose();
        }
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }
        satelitesCount.onNext(0);
    }

    @Override
    public Observable<Integer> getSensorResultsStream() {
        return satelitesCount;
    }

    private class LocationListenerImpl implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //android.util.Log.e(TAG, "onLocationChanged: " + location);
            Bundle b = location.getExtras();
            int satellites = b.getInt("satellites");
            android.util.Log.e(TAG, "onLocationChanged: " + satellites);
            satelitesCount.onNext(satellites);
//            android.util.Log.e(TAG, "Satellites:" + b.getInt("satellites"));
//            android.util.Log.e(TAG, "----------------------------------------");
//            for (String key : b.keySet())
//            {
//                android.util.Log.e(TAG, key + " Extra: " + b.get(key));
//            }
            //location.getExtras().get
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            android.util.Log.e(TAG, "onStatusChanged: " + s + " Bundle: " + bundle);
        }

        @Override
        public void onProviderEnabled(@NonNull String s) {

        }

        @Override
        public void onProviderDisabled(@NonNull String s) {

        }
    }
}
