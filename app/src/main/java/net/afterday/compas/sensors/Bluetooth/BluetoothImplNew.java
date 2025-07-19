package net.afterday.compas.sensors.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import net.afterday.compas.logging.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

/**
 * Created by Justas Spakauskas on 4/2/2018.
 */
public class BluetoothImplNew implements Bluetooth {
    private static final String TAG = "BluetoothImplNew";
    private Context context;
    private Observable<Double> resultStream = PublishSubject.create();
    private BluetoothLeScanner bla;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private ScanCallback callback;
    private final List<String> registeredMacs = BluetoothMacAddresses.getMacs();
    private Disposable resetter;
    private Handler handler;

    @SuppressLint("NewApi")
    public BluetoothImplNew(Context context) {
        this.context = context;
        BluetoothManager blm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (blm.getAdapter() != null) {
            bla = blm.getAdapter().getBluetoothLeScanner();
            callback = new ScanCallbackImpl();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void start() {
        BluetoothManager blm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (blm.getAdapter() != null) {
            isRunning.set(true);
            handler = new Handler(Looper.getMainLooper());
            waitForBluetoothScanPermission(context);
        }
    }

    @SuppressLint("NewApi")
    private void waitForBluetoothScanPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Снова проверяем разрешение
            Log.d(TAG, "Bluetooth permission not yet granted.");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    waitForBluetoothScanPermission(context); // каждую секунду
                }
            }, 1000);
        } else {
            // Разрешение предоставлено, можно сканировать устройства
            Log.d(TAG, "Bluetooth permission granted.");
            bla.stopScan(callback);
            bla.startScan(callback);
        }
    }

    @Override
    public void stop() {
        isRunning.set(false);
    }

    @Override
    public Observable<Double> getSensorResultsStream() {
        return resultStream;
    }

    @SuppressLint("NewApi")
    private class ScanCallbackImpl extends ScanCallback {
        //@SuppressLint("MissingPermission")
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.e(TAG, "Found bluetooth device: " + result.getDevice().getName() + " " + result.getDevice().getAddress() + " " + result.getRssi());
            // Обработка результата сканирования
            if (registeredMacs.contains(result.getDevice().getAddress())) {
                Log.e(TAG, "Found bluetooth device: " + result.getDevice().getName() + " " + result.getDevice().getAddress() + " " + result.getRssi());
                Logger.d("Found bluetooth device: " + result.getDevice().getName() + " " + result.getDevice().getAddress() + " " + result.getRssi());
                if (resetter != null && !resetter.isDisposed()) {
                    resetter.dispose();
                }
                ((Subject<Double>) resultStream).onNext((double) result.getRssi() + 100);
                resetter = Observable.timer(3, TimeUnit.SECONDS).subscribe((x) -> ((Subject<Double>) resultStream).onNext(0d));
                //((Subject<BluetoothScanResult>)resultStream).onNext(new BluetoothScanResultImpl(bluetoothDevice.getAddress(), i, now));
            }
            if (!isRunning.get()) {
                bla.stopScan(callback);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            // Обработка ошибки сканирования
            Log.e(TAG, "Scan failed with error code: " + errorCode);
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @SuppressLint({"NewApi", "MissingPermission"})
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "" + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
            }
            if (isRunning.get()) {
                Log.e(TAG, "START DISCOVERY");
                bla.startScan(callback);
            }
        }
    }
}
