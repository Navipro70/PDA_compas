package net.afterday.compas.sensors.Bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

public class BluetoothImpl implements Bluetooth {
    private static final String TAG = "BluetoothImpl";
    private Context context;
    private Observable<Double> resultStream = PublishSubject.create();
    private BluetoothAdapter bla;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private BluetoothAdapter.LeScanCallback callback;
    private List<String> registeredMacs = BluetoothMacAddresses.getMacs();
    private Disposable resetter;

    public BluetoothImpl(Context context) {
        this.context = context;
        BluetoothReceiver br = new BluetoothReceiver();
        bla = BluetoothAdapter.getDefaultAdapter();
        callback = new LeScanCallback();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        //context.registerReceiver(br, intentFilter);
        isRunning.set(true);
        if (bla != null)
            bla.startLeScan(callback);
    }

    @Override
    public void stop() {
        isRunning.set(false);
    }

    @Override
    public Observable<Double> getSensorResultsStream() {
        return resultStream;
    }

    private class LeScanCallback implements BluetoothAdapter.LeScanCallback {
        @SuppressLint("MissingPermission")
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            long now = System.currentTimeMillis();
            if (registeredMacs.contains(bluetoothDevice.getAddress())) {
                Log.e(TAG, "Found artifact! " + bluetoothDevice.getAddress());
                if (resetter != null && !resetter.isDisposed()) {
                    resetter.dispose();
                }
                ((Subject<Double>) resultStream).onNext((double) (i + 100));
                resetter = Observable.timer(3, TimeUnit.SECONDS).subscribe((x) -> ((Subject<Double>) resultStream).onNext(0d));
                //((Subject<BluetoothScanResult>)resultStream).onNext(new BluetoothScanResultImpl(bluetoothDevice.getAddress(), i, now));
            }
            if (!isRunning.get()) {
                bla.stopLeScan(callback);
            }
            //Log.e(TAG, "SCANNED!!!!!!" + bluetoothDevice.getAddress() + " " + bluetoothDevice.getName() + " " + i + " " + bytes);
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("BLUETOOTH RECEIVED!", "" + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
            }
            if (isRunning.get()) {
                Log.e(TAG, "START DISCOVERY");
                bla.startDiscovery();
            }
        }
    }
}