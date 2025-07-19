package net.afterday.compas;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import net.afterday.compas.core.gameState.Frame;
import net.afterday.compas.core.inventory.items.Events.ItemAdded;
import net.afterday.compas.core.player.Player;
import net.afterday.compas.core.serialization.Serializer;
import net.afterday.compas.db.DataBase;
import net.afterday.compas.devices.DeviceProvider;
import net.afterday.compas.devices.DeviceProviderImpl;
import net.afterday.compas.engine.Engine;
import net.afterday.compas.engine.events.ItemEventsBus;
import net.afterday.compas.logging.LogLine;
import net.afterday.compas.logging.Logger;
import net.afterday.compas.persistency.PersistencyProviderImpl;
import net.afterday.compas.sensors.Battery.Battery;
import net.afterday.compas.sensors.Battery.BatteryStatus;
import net.afterday.compas.sensors.SensorsProviderImpl;
import net.afterday.compas.serialization.SharedPrefsSerializer;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by spaka on 6/3/2018.
 */

public class LocalMainService extends Service {
    private static final String TAG = "LocalMainService";
    private static final String CHANNEL_ID = "PDA_Compas";
    public static final int MAIN_SERVICE = 1;
    private static LocalMainService instance;
    private IBinder binder = new MainBinder();
    private boolean running = false;
    private Engine engine;
    private Observable<Frame> framesStream;
    private Observable<BatteryStatus> batteryStatusStream;
    private Battery battery;
    private Serializer serializer;
    private Logger logger;
    private DataBase dataBase;
    private NotificationManager notificationManager;
    private PowerManager.WakeLock wakeLock;
    private static final int ONGOING_NOTIFICATION_ID = 1;

    public static LocalMainService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "PDACompass::BackgroundServiceLock");
                wakeLock.setReferenceCounted(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing WakeLock: " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!running) {
            Log.e(TAG, "Starting service in foreground");
            try {
                if (wakeLock != null && !wakeLock.isHeld()) {
                    wakeLock.acquire(10*60*1000L); // 10 minutes timeout
                }
            } catch (Exception e) {
                Log.e(TAG, "Error acquiring WakeLock: " + e.getMessage());
            }
            startForeground();
            initGame();
        }
        running = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing WakeLock: " + e.getMessage());
        }
        closeGame();
        Log.d(TAG, "Service destroyed");
        Intent restartServiceIntent = new Intent(getApplicationContext(), LocalMainService.class);
        startService(restartServiceIntent);
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        boolean unbinded = super.onUnbind(intent);
//        Intent i = new Intent(getApplicationContext(), HiddenActivity.class);
//        //i.setClassName("net.afterday.compas", "net.afterday.compas.HiddenActivity");
//        startActivity(i);
//        return unbinded;
        return super.onUnbind(intent);
    }
    public boolean isNotificationAlreadyShown(int notificationId) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications;
            notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == notificationId) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    public void sendControlNotification() {
        if (!isNotificationAlreadyShown(MAIN_SERVICE))
            notificationManager.notify(MAIN_SERVICE, getNotification());
    }
    private Notification getNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT); // был FLAG_UPDATE_CURRENT
        RemoteViews collapsed = new RemoteViews(getPackageName(), R.layout.notification);
        Intent intentAction = new Intent(this, ActionsReceiver.class);
        intentAction.putExtra("ServiceControlls", "STOP");
        PendingIntent stopServiceIntent = PendingIntent.getBroadcast(this, 1, intentAction, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT); // был FLAG_UPDATE_CURRENT
        collapsed.setOnClickPendingIntent(R.id.open, pIntent);
        collapsed.setOnClickPendingIntent(R.id.stop, stopServiceIntent);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContent(collapsed)
                .setSmallIcon(R.mipmap.ic_launcher)
		.setSilent(true)
                .setOngoing(true)
                .setOnlyAlertOnce(false)
                .build();
    }

    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "PDA Compass Service",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Keeps PDA Compass running in background");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PDA Compass Active")
                .setContentText("Tracking anomalies and artifacts...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int foregroundType = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION | 
                               ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
            startForeground(MAIN_SERVICE, notification, foregroundType);
        } else {
            startForeground(MAIN_SERVICE, notification);
        }
    }

    private void initGame() {
        DeviceProvider deviceProvider = new DeviceProviderImpl(this);
        serializer = SharedPrefsSerializer.instance(this);
        dataBase = DataBase.instance(this);
        logger = Logger.instance(this, deviceProvider.getVibrator());
        engine = Engine.instance();
        engine.setPersistencyProvider(new PersistencyProviderImpl());
        engine.setSensorsProvider(SensorsProviderImpl.initialize(this));
        engine.setDeviceProvider(deviceProvider);
        battery = SensorsProviderImpl.instance().getBatterySensor();
        batteryStatusStream = battery.getSensorResultsStream();
        battery.start();
        framesStream = engine.getFramesStream();
        engine.start();
    }
    private void closeGame() {
        engine.end();
    }

    public Observable<Frame> getFramesStream() {
        return framesStream;
    }

    public Observable<Long> getCountDownStream() {
        return engine.getCountDownStream();
    }

    public Observable<Integer> getPlayerLevelStream() {
        return engine.getPlayerLevelStream();
    }

    public Observable<Player.STATE> getPlayerStateStream() {
        return engine.getPlayerStateStream();
    }

    public Observable<ItemAdded> getItemAddedStream() {
        return engine.getItemAddedStream();
    }

    public Observable<List<LogLine>> getLogStream() {
        return logger.getLogStream();
    }

    public Observable<BatteryStatus> getBatteryStatusStream() {
        return batteryStatusStream;
    }

    public ItemEventsBus getItemEventBus() {
        return engine.getItemEventsBus();
    }

    public class MainBinder extends Binder {
        public LocalMainService getService() {
            return LocalMainService.this;
        }
    }
}
