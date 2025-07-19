package net.afterday.compas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.afterday.compas.core.gameState.Frame;
import net.afterday.compas.core.inventory.Inventory;
import net.afterday.compas.core.inventory.items.Events.ItemAdded;
import net.afterday.compas.core.inventory.items.Item;
import net.afterday.compas.core.player.Player;
import net.afterday.compas.core.player.PlayerProps;
import net.afterday.compas.engine.events.EmissionEventBus;
import net.afterday.compas.engine.events.ItemEventsBus;
import net.afterday.compas.engine.events.PlayerEventBus;
import net.afterday.compas.fragment.BloodFragment;
import net.afterday.compas.fragment.InventoryFragment;
import net.afterday.compas.fragment.ItemInfoFragment;
import net.afterday.compas.fragment.ScannerFragment;
import net.afterday.compas.fragment.SuicideConfirmationFragment;
import net.afterday.compas.settings.Constants;
import net.afterday.compas.settings.Settings;
import net.afterday.compas.settings.SettingsListener;
import net.afterday.compas.view.Bar;
import net.afterday.compas.view.Battery;
import net.afterday.compas.view.Clock;
import net.afterday.compas.view.Compass;
import net.afterday.compas.view.CountDownTimer;
import net.afterday.compas.view.Geiger;
import net.afterday.compas.view.Healthbar;
import net.afterday.compas.view.Indicator;
import net.afterday.compas.view.LevelIndicator;
import net.afterday.compas.view.LevelProgress;
import net.afterday.compas.view.Radbar;
import net.afterday.compas.view.SmallLogListAdapter;
import net.afterday.compas.view.Tube;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;

//import net.afterday.compass.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static final int PRESS_DELAY = 500;
    private static final int PRESS_LONG_DELAY = 1000;
    private static final int PRESS_SUICIDE = 1000;
    private final String TAG = "MainActivity";
    private LocalMainService stalkerApp;
    //Views
    private ViewGroup mContentView;
    private Compass mCompass;
    private Geiger mGeiger;
    private Radbar mRadbar;
    private Healthbar mHealthbar;
    private Bar mArmorBar;
    private Bar mStaminaBar;
    private Bar mDeviceBar;
    private Clock mClock;
    private Battery mBattery;
    private Tube mTube;
    private ImageButton mQrButton;
    private RecyclerView logList;
    private Indicator mIndicator;
    private CountDownTimer countDownTimer;
    private LevelProgress levelProgress;
    private ViewGroup layout;
    private Handler handler;

    private boolean hasActiveBooster,
            hasActiveDevice,
            hasActiveArmor;
    ///////////////////////////////////////////////////////
    //Streams
    private Disposable framesSubscribtion,
            userActionsSubscribtion,
            impactsSubsciption;
    private CompositeDisposable disposables = new CompositeDisposable();
    private Observable<Frame> framesStream;
    private Observable<Long> countDownStream;
    private Observable<Integer> playerLevelStream;
    private Observable<Player.STATE> playerStateStream;
    private Observable<ItemAdded> itemAddedStream;
    private Player.STATE currentState;
    ////////////////////////////////////////////////////////
    private long qrBtnPressTime = 0;
    private long hBarPressTime = 0;
    private long tubePressTime = 0;
    private long staminaPressTime = 0;
    private long devicePressTime = 0;
    private long armorPressTime = 0;
    private long rBarPressTime = 0;
    private long lastTick = 0;
    private long duration = 0;
    private SmallLogListAdapter logAdapter;
    private boolean showArtifactsSignal = true;
    private boolean active = false;
    private SettingsListener settingsListener;
    ////////////////////////////////////////////////////////
    private Subject<Integer> orientationChanges = BehaviorSubject.create();
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            lastTick = System.currentTimeMillis();

            // Hide top bar
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            setupListeners();
            mIndicator.setVisibility(View.GONE);
            stalkerApp = ((LocalMainService.MainBinder) iBinder).getService();
            framesStream = stalkerApp.getFramesStream();
            countDownStream = stalkerApp.getCountDownStream();
            playerLevelStream = stalkerApp.getPlayerLevelStream();
            playerStateStream = stalkerApp.getPlayerStateStream();
            disposables.add(playerLevelStream.observeOn(AndroidSchedulers.mainThread()).subscribe((pl) -> {

                //((LevelIndicator)mQrButton).setLevel(pl);
                mTube.setLevel(pl);
                mGeiger.setLevel(pl);
                mIndicator.setLevel(pl);
                if (pl >= 4) {
                    mGeiger.setFingerPrint(true);
                }
                // Скрываем индикатор по умолчанию, его видимость будет определяться флагом hasDetectorAccess
                mIndicator.setVisibility(View.GONE);
                // Индикатор битого стекла можно оставить для 5 уровня как визуальный эффект
                if (pl == 5) {
                    mGeiger.setBrokenGlass(true);
                }
            }));
            disposables.add(playerStateStream.observeOn(AndroidSchedulers.mainThread()).subscribe((s) -> {
                currentState = s;
                mTube.setState(s);
                showArtifactsSignal = s.getCode() == Player.ALIVE;
                if (!showArtifactsSignal) {
                    mIndicator.setStrength(0);
                }
            }));
            disposables.add(countDownStream.observeOn(AndroidSchedulers.mainThread()).subscribe((t) -> countDownTimer.setSecondsLeft(t)));
            disposables.add(framesStream
                    //.doOnNext((i) -> {//Log.d(TAG, "ImpactsStream: " + Thread.currentThread().getName()); updateViews(null, null);})
                    .observeOn(AndroidSchedulers.mainThread())
                    //.doOnNext((i) -> //Log.d(TAG, "ImpactsStream: " + Thread.currentThread().getName()))
                    .subscribe((frame -> updateViews(frame))));
            itemAddedStream = stalkerApp.getItemAddedStream();
            levelProgress.addOnLevelChangedListener((l) -> ((LevelIndicator) mQrButton).setLevel(l));
            disposables.add(itemAddedStream.observeOn(AndroidSchedulers.mainThread()).subscribe((ia) -> levelProgress.setProgress(ia)));
            disposables.add(stalkerApp.getBatteryStatusStream().observeOn(AndroidSchedulers.mainThread()).subscribe((b) ->
            {
                mBattery.setStatus(b);
            }));
            disposables.add(framesStream.take(1)
                    //.doOnNext((i) -> {//Log.d(TAG, "ImpactsStream: " + Thread.currentThread().getName()); updateViews(null, null);})
                    .observeOn(AndroidSchedulers.mainThread())
                    //.doOnNext((i) -> //Log.d(TAG, "ImpactsStream: " + Thread.currentThread().getName()))
                    .subscribe((frame ->
                    {
                        if (frame.getPlayerProps().getLevel() == 5) {
                            levelProgress.showMax(true);
                        } else {
                            levelProgress.setProgress(frame.getPlayerProps().getLevelXp());
                        }
                        ((LevelIndicator) mQrButton).setLevel(frame.getPlayerProps().getLevel());
                    })));
            disposables.add(Observable.combineLatest(EmissionEventBus.instance().getEmissionStateStream(), PlayerEventBus.instance().getPlayerFractionStream(), Pair::new)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((s) -> {
                        mTube.setFraction(s.second);
                        if (s.second == Player.FRACTION.MONOLITH) {
                            mTube.setEmission(false);
                        } else {
                            mTube.setEmission(s.first);
                        }
                    }));
            //Orientation
            Log.d(TAG, "SERVICE CONNECTED!!!!");
            setupLog();
            active = true;
            handler = new Handler(Looper.getMainLooper());
            askForNotificationPermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "SERVICE DISCONNECTED!!!!");
        }
    };

    @SuppressLint("SourceLockedOrientationActivity")
    private void setOrientation(int o) {
        Log.e(TAG, "SET ORIENTATION: " + (o == Constants.ORIENTATION_PORTRAIT ? "PORTRAIT" : (o == Constants.ORIENTATION_LANDSCAPE ? "LANDSCAPE" : "UNKNOWN")));
        if (o == Constants.ORIENTATION_PORTRAIT && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (o == Constants.ORIENTATION_LANDSCAPE && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 900) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Toast.makeText(this, "Для продолжения необходим доступ к местоположению в фоне.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 901);
                }
            }
        }
        else if (requestCode == 800) {
            askForLocationPermissions();
        }
        else if (requestCode == 700) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted!");
                stalkerApp.sendControlNotification();
            }
            else {
                Log.d(TAG, "Notification permission not granted!");
            }
        }
    }

    private void askForNonLocationPermissions() {
        List<String> permissions = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        permissions.add(Manifest.permission.BLUETOOTH);
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.VIBRATE);
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 800);

    }

    private void askForLocationPermissions() {
        List<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 900);
    }

    private void askForNotificationPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 700);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        askForNotificationPermissions();
                    }
                }, 2000);
            }
            else {
                stalkerApp.sendControlNotification();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        disposables.add(orientationChanges.skip(1).observeOn(AndroidSchedulers.mainThread()).subscribe(this::setOrientation));
        orientationChanges.onNext(Settings.instance().getIntSetting(Constants.ORIENTATION));
        int o = Settings.instance().getIntSetting(Constants.ORIENTATION);
        setOrientation(o);
        setContentView(R.layout.activity_main);
        bindViews();
        setViewListeners();
        disposables.add(Observable.combineLatest(PlayerEventBus.instance().getPlayerFractionStream(), orientationChanges, Pair::new).observeOn(AndroidSchedulers.mainThread()).subscribe((p) -> setBackground(p.first, p.second)));
        bindService(new Intent(MainActivity.this, LocalMainService.class), serviceConnection, Context.BIND_ABOVE_CLIENT);
        askForNonLocationPermissions();
    }

    private void setupLog() {
        LinearLayoutManager logListManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        logListManager.setStackFromEnd(true);
        logList.setLayoutManager(logListManager);
        logAdapter = new SmallLogListAdapter(this, new ArrayList<>());
        logList.setAdapter(logAdapter);
        disposables.add(stalkerApp.getLogStream().subscribe((log) -> {
            logAdapter.setDataset(log);
            logListManager.scrollToPosition(log.size() - 1);
        }));
//        stalkerApp.registerLogAdapter(logAdapter);
    }


    private void updateViews(Frame frame) {
        // Этот метод обновляет пользовательский интерфейс на основе текущего состояния игрока
        
        PlayerProps pProps = frame.getPlayerProps(); // Получаем свойства игрока из кадра
        
        // Проверяем, жив ли игрок и получил ли он урон
        if (pProps.getState().getCode() == Player.ALIVE) {
            // Если игрок получил урон от различных источников - показываем эффект крови
            if (pProps.emissionHit() || pProps.anomalyHit()  || pProps.fruitPunchHit() || 
                pProps.burerHit() || pProps.controllerHit() || pProps.mentalHit() || 
                pProps.strongExplosionHit() || pProps.weakExplosionHit()) {
                showBlood();
            }
        }

        // Обновляем показания счетчика Гейгера
        if (pProps.getState().getCode() == Player.ALIVE && pProps.getHealthImpact() <= 0) {
            // Если игрок жив и не получает урон - показываем текущие значения
            mGeiger.setAnomaly((float) pProps.getAnomalyImpact());     // Влияние аномалий
            mGeiger.setMental((float) pProps.getMentalImpact());       // Психическое воздействие
            mGeiger.setMonolith((float) pProps.getMonolithImpact());   // Влияние монолита
            mGeiger.toSvh((float) pProps.getRadiationImpact(), 1000);  // Уровень радиации
        } else {
            // Если игрок мертв или получает урон - обнуляем показания
            mGeiger.setAnomaly(0f);
            mGeiger.setMental(0f);
            mGeiger.setMonolith(0f);
            mGeiger.toSvh(0f, 750);
        }

        // Обновляем параметры "трубки" - вероятно, какой-то индикатор
        mTube.setParameters(
                pProps.getRadiationImpact(),      // Воздействие радиации
                pProps.getAnomalyImpact(),        // Воздействие аномалий
                pProps.getMentalImpact(),         // Психическое воздействие
                pProps.getMonolithImpact(),       // Влияние монолита
                pProps.getControllerImpact(),     // Влияние контролера
                pProps.getBurerImpact(),          // Влияние бюрера
                pProps.getHealthImpact(),         // Влияние на здоровье
                pProps.getState()                 // Текущее состояние игрока
        );

        // Обновляем индикатор радиации
        mRadbar.setInfo(
                pProps.getHealth(),               // Текущее здоровье
                pProps.getRadiation(),            // Уровень радиации
                pProps.getHealthImpact(),         // Влияние на здоровье
                pProps.getController(),           // Влияние контролера
                pProps.hasRadiationInstant()      // Мгновенное радиационное воздействие
        );

        // Обновляем индикатор здоровья
        mHealthbar.setInfo(
                pProps.getHealth(),               // Текущее здоровье
                pProps.getHealthImpact(),         // Влияние на здоровье
                pProps.getController(),           // Влияние контролера
                pProps.hasHealthInstant()         // Мгновенное воздействие на здоровье
        );

        if (showArtifactsSignal) {
            if (frame.getPlayerProps().getHealthImpact() > 0) {
                mIndicator.setStrength(0);
            } else {
                // Проверяем флаг hasDetectorAccess для показа индикатора артефактов
                if (pProps.hasDetectorAccess()) {
                    // Показываем индикатор только если у игрока есть доступ к детектору
                    mIndicator.setVisibility(View.VISIBLE);
                    mIndicator.setStrength((float) pProps.getArtefactImpact());
                    if ((float) pProps.getArtefactImpact() > 1)
                        Log.d(TAG, "Artefact impact: " + (float) pProps.getArtefactImpact());
                } else {
                    // Скрываем индикатор, если у игрока нет доступа к детектору
                    mIndicator.setVisibility(View.GONE);
                }
            }
        }
        mStaminaBar.setPercents(pProps.getBoosterPercents());
        mDeviceBar.setPercents(pProps.getDevicePercents());
        mArmorBar.setPercents(pProps.getArmorPercents());
        hasActiveArmor = pProps.getArmorPercents() > 0;
        hasActiveBooster = pProps.getBoosterPercents() > 0;
        hasActiveDevice = pProps.getDevicePercents() > 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setViewListeners() {
        mStaminaBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    staminaPressTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - staminaPressTime > PRESS_DELAY && isAlive()) {
                        openInventory(Item.BOOSTER);
                    }
            }
            return true;
        });
        mDeviceBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    devicePressTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - devicePressTime > PRESS_DELAY && isAlive()) {
                        openInventory(Item.DEVICES);
                    }
            }
            return true;
        });
        mArmorBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    armorPressTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - armorPressTime > PRESS_DELAY && isAlive()) {
                        openInventory(Item.ARMOR);
                    }
            }
            return true;
        });
        mQrButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    qrBtnPressTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    long delay = isAlive() ? PRESS_DELAY : PRESS_LONG_DELAY;
                    if (System.currentTimeMillis() - qrBtnPressTime > delay) {
                        openScanner();
                    }
            }
            return true;
        });

        mRadbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        rBarPressTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - rBarPressTime > PRESS_DELAY && isAlive()) {
                            openInventory(Item.ALL);
                        }
                        break;

                }
                return true;
            }
        });
        mHealthbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hBarPressTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - hBarPressTime > PRESS_DELAY && isAlive()) {
                            openInventory(Item.ALL);
                        }
                        break;

                }
                return true;
            }
        });

        mTube.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tubePressTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (currentState != null && currentState.getSuicideType() != Player.SUICIDE_NOT_ALLOWED 
                            && currentState != Player.STATE.MENTALLED
                            && System.currentTimeMillis() - tubePressTime > PRESS_SUICIDE) {
                            if (currentState == Player.STATE.W_ABDUCTED) {
                                PlayerEventBus.instance().suicide();
                                return true;
                            }
                            openSuicideConfirmation();
                        }
                        break;

                }
                return true;
            }
        });

    }

    private boolean isAlive() {
        return currentState != null && currentState.getCode() == Player.ALIVE;
    }

    private void openSuicideConfirmation() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("suicide");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dia//Log.
        DialogFragment newFragment = new SuicideConfirmationFragment();
        newFragment.show(ft, "scanner");
    }

    private void showBlood() {
        if (!active) {
            return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DialogFragment bloodFragment = new BloodFragment();
        bloodFragment.show(ft, "blood");
    }

    private void bindViews() {
        mGeiger = (Geiger) findViewById(R.id.geiger);
        mCompass = (Compass) findViewById(R.id.compass);
        mRadbar = (Radbar) findViewById(R.id.radbar);
        mHealthbar = (Healthbar) findViewById(R.id.healthbar);
        mArmorBar = (Bar) findViewById(R.id.armorbar);
        mStaminaBar = (Bar) findViewById(R.id.staminabar);
        mDeviceBar = (Bar) findViewById(R.id.devicebar);
        //mClock = (Clock) findViewById(R.id.clock);
        mBattery = (Battery) findViewById(R.id.battery);
        mTube = (Tube) findViewById(R.id.tube);
        mQrButton = (ImageButton) findViewById(R.id.qrbutton);
        logList = (RecyclerView) findViewById(R.id.log_list);
        mIndicator = (Indicator) findViewById(R.id.indicator);
        countDownTimer = (CountDownTimer) findViewById(R.id.countdown);
        levelProgress = (LevelProgress) findViewById(R.id.levelProgress);
        layout = (ViewGroup) findViewById(R.id.activity_main);
        if (Settings.instance().getBoolSetting(Constants.COMPASS)) {
            mCompass.compassOn();
        } else {
            mCompass.compassOff();
        }
    }

    private void setBackground(Player.FRACTION pf, int orientation) {
        layout.setBackground(ContextCompat.getDrawable(this, getBackground(pf, orientation)));

    }

    private int getBackground(Player.FRACTION pf, int orientation) {
        switch (pf) {
            case MONOLITH:
                return orientation == Constants.ORIENTATION_LANDSCAPE ? R.drawable.background_h_monolith : R.drawable.background_v_monolith;
            case STALKER:
                return orientation == Constants.ORIENTATION_LANDSCAPE ? R.drawable.background_h_merged : R.drawable.background_v_merged;
            case GAMEMASTER:
                return orientation == Constants.ORIENTATION_LANDSCAPE ? R.drawable.background_h_gamemaster : R.drawable.background_v_gamemaster;
            case DARKEN:
                return orientation == Constants.ORIENTATION_LANDSCAPE ? R.drawable.background_h_darken : R.drawable.background_v_darken;
            case MISSION:
                return orientation == Constants.ORIENTATION_LANDSCAPE ? R.drawable.background_h_mission : R.drawable.background_v_mission;
            default:
                return -1;
        }
    }

    private void openInventory(int type) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (type == Item.ALL) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag(InventoryFragment.TAG_INVENTORY);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            Bundle b = new Bundle();
            b.putInt(InventoryFragment.TYPE, type);
            DialogFragment newFragment = new InventoryFragment();
            newFragment.setArguments(b);
            newFragment.show(ft, InventoryFragment.TAG_INVENTORY);
            return;
        }
//        if((type == Item.ARMOR && !hasActiveArmor) || (type == Item.BOOSTER && !hasActiveBooster) || (type == Item.DEVICES && hasActiveDevice))
//        {
//            Fragment prev = getFragmentManager().findFragmentByTag(InventoryFragment.TAG_CATEGORY);
//            if (prev != null) {
//                ft.remove(prev);
//            }
//            ft.addToBackStack(null);
//            Bundle b = new Bundle();
//            b.putInt(InventoryFragment.TYPE, type);
//            DialogFragment newFragment = new InventoryFragment();
//            newFragment.setArguments(b);
//            newFragment.show(ft, InventoryFragment.TAG_CATEGORY);
//            return;
//        }
        if ((type == Item.ARMOR && hasActiveArmor) || (type == Item.BOOSTER && hasActiveBooster) || (type == Item.DEVICES && hasActiveDevice)) {
            disposables.add(ItemEventsBus.instance().getUserItems().take(1).subscribe((Inventory inv) -> {
                switch (type) {
                    case Item.DEVICES:
                        openItemInfo(inv.getActiveDevice());
                        break;
                    case Item.ARMOR:
                        openItemInfo(inv.getActiveArmor());
                        break;
                    case Item.BOOSTER:
                        openItemInfo(inv.getActiveBooster());
                        break;
                }
            }));
            ItemEventsBus.instance().requestItems();
        }
    }


    private void openScanner() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("scanner");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dia//Log.
        DialogFragment newFragment = ScannerFragment.newInstance();
        newFragment.show(ft, "scanner");
    }

    private void setupListeners() {
        ItemEventsBus ieBus = LocalMainService.getInstance().getItemEventBus();
        disposables.add(ieBus.getItemAddedEvents().observeOn(AndroidSchedulers.mainThread()).subscribe(this::itemAdded));
        disposables.add(ieBus.getUnknownItemEvents().observeOn(AndroidSchedulers.mainThread()).subscribe(this::unknownItem));
        settingsListener = (key, val) -> {
            switch (key) {
                case Constants.COMPASS:
                    if (Boolean.parseBoolean(val)) {
                        mCompass.compassOn();
                    } else {
                        mCompass.compassOff();
                    }
                    break;
                case Constants.ORIENTATION:
                    this.orientationChanges.onNext(Integer.parseInt(val));
            }
        };
        Settings.instance().addSettingsListener(settingsListener);
    }

    private void itemAdded(Item item) {
        ////Log.d("Item added: " + item.getName());
    }

    private void unknownItem(String item) {
        ////Log.e("Unknown item!");
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "ON DESTROY!!!!");
        mCompass.compassOff();
        super.onDestroy();
        if (!disposables.isDisposed()) {
            disposables.dispose();
            settingsListener = null;
        }
        //mGeiger.setOnTouchListener(null);
        unbindService(serviceConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stalkerApp != null)
            stalkerApp.sendControlNotification();
        active = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.stopService(new Intent(this, LocalMainService.class));
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    private long getUsedMemory() {

        long freeSize = 0L;
        long totalSize = 0L;
        long usedSize = -1L;
        try {
            Runtime info = Runtime.getRuntime();
            freeSize = info.freeMemory();
            totalSize = info.totalMemory();
            usedSize = totalSize - freeSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usedSize;

    }

    private void openItemInfo(Item item) {
        android.util.Log.d(TAG, "openItemInfo");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("item");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dia//Log.
        ItemInfoFragment newFragment = ItemInfoFragment.newInstance(item);
        newFragment.show(ft, "item");

    }

}
