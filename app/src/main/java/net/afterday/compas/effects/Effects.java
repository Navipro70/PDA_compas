package net.afterday.compas.effects;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.HandlerThread;
import android.view.animation.LinearInterpolator;

import androidx.core.util.Pair;

import net.afterday.compas.LocalMainService;
import net.afterday.compas.R;
import net.afterday.compas.core.gameState.Frame;
import net.afterday.compas.core.player.Impacts;
import net.afterday.compas.core.player.Player;
import net.afterday.compas.core.player.PlayerProps;
import net.afterday.compas.devices.DeviceProvider;
import net.afterday.compas.devices.sound.Sound;
import net.afterday.compas.devices.vibro.Vibro;
import net.afterday.compas.engine.Engine;
import net.afterday.compas.engine.events.EmissionEventBus;
import net.afterday.compas.engine.events.ItemEventsBus;
import net.afterday.compas.engine.events.PlayerEventBus;
import net.afterday.compas.engine.events.SensorEventBus;
import net.afterday.compas.logging.Logger;
import net.afterday.compas.engine.events.CodeInputEventBus;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by spaka on 5/3/2018.
 */

public class Effects {
    private static final String TAG = "Effects";
    private Sound sound;
    private ValueAnimator soundAnimator;
    private Timer anomalyTimer = new Timer(true);
    private Disposable anomalySubsciption;
    private Disposable explosionSubsciption;
    private Disposable artefactSubsciption;

    // protected final Handler handler;
    private TimerTask playAnomalyClick = new TimerTask() {
        @Override
        public void run() {
            sound.playAnomalyClick();
        }
    };
    private Scheduler scheduler;
    private Disposable playerStatesSubsciption,
            playerLevelSubscription,
            impactsStatesSubscription;
    private Vibro vibro;
    private Player.STATE currentState;
    //private boolean healingPlaying = false;

    @SuppressLint("CheckResult")
    public Effects(DeviceProvider deviceProvider) {
        this.sound = deviceProvider.getSoundPlayer();
        this.vibro = deviceProvider.getVibrator();
        HandlerThread handlerThread = new HandlerThread("backgroundThread");
        handlerThread.start();
        scheduler = AndroidSchedulers.from(handlerThread.getLooper());
        LocalMainService.getInstance().getFramesStream().observeOn(scheduler).subscribe((f) -> processFrame(f));
        ItemEventsBus.instance().getItemAddedEvents().subscribe((ia) -> {
            sound.playItemScanned();
            vibro.vibrateTouch();
        });
        
        // Добавляем подписку на события успешного сканирования QR-кода
        CodeInputEventBus.getAcceptedCodes().subscribe((code) -> {
            // Проверяем, является ли код детектором артефактов
            if (code.equals("DetektorPolotsk2025")) {
                // Воспроизводим звук сканирования предмета
                sound.playItemScanned();
                // Добавляем вибрацию
                vibro.vibrateMessage();
            }
        });
        
        //Inventoriaus garsai ir vibro
        ItemEventsBus.instance().getItemUsedEvents().subscribe((ia) -> {
            sound.playInventoryUse();
            vibro.vibrateTouch();
        });
        ItemEventsBus.instance().getItemDroppedEvents().subscribe((ia) -> {
            sound.playInventoryDrop();
            vibro.vibrateTouch();
        });

        EmissionEventBus.instance().getEmissionStateStream().skip(1).withLatestFrom(PlayerEventBus.instance().getPlayerStateStream(), (e, ps) -> new Pair<Boolean, Player.STATE>(e, ps)).subscribe((s) -> {
            if (s.second.getCode() == Player.ALIVE) {
                if (s.first) {
                    sound.playEmissionStarts();
                    vibro.vibrateAlarm();
                } else {
                    sound.playEmissionEnds();
                    vibro.vibrateMessage();
                }
            }
        });
        EmissionEventBus.instance().getEmissionWarnings().withLatestFrom(PlayerEventBus.instance().getPlayerStateStream(), (e, ps) -> new Pair<Integer, Player.STATE>(e, ps)).subscribe((s) -> {
            if (s.second.getCode() == Player.ALIVE) {
                sound.playEmissionWarning();
                vibro.vibrateMessage();
            }
        });
        EmissionEventBus.instance().getFakeEmissions().withLatestFrom(PlayerEventBus.instance().getPlayerStateStream(), (e, ps) -> new Pair<Integer, Player.STATE>(e, ps)).subscribe((s) -> {
            if (s.second.getCode() == Player.ALIVE) {
                sound.playEmissionEnds();
                vibro.vibrateMessage();
            }
        });
        SensorEventBus.instance().getScanningState().distinctUntilChanged().skip(1).subscribe(state -> {
            if (state) {
                Logger.d(R.string.message_wifi_continue);
            } else {
                Logger.d(R.string.message_wifi_error);
            }
        });
        setPdaOn();
    }

    public void setPdaOn() {
        sound.playPdaOn();
        Logger.d(R.string.message_pda_on);
    }

    public void setPdaOff() {
        sound.playPdaOff();
        net.afterday.compas.logging.Logger.d(R.string.message_pda_off);
    }

    public void setPlayerStatesStream(Observable<Player.STATE> statesStream) {
        if (playerStatesSubsciption != null && !playerStatesSubsciption.isDisposed()) {
            playerStatesSubsciption.dispose();
        }
        playerStatesSubsciption = statesStream.observeOn(scheduler).subscribe((s) -> {
//            if(currentState == Player.STATE.HEALING && s != Player.STATE.HEALING)
//            {
//                sound.stopHealing();
//            }
            switch (s) {
                case ALIVE:
                    break;
                case MENTALLED:
                    sound.playZombify();
                    vibro.vibrateHit();
                    break;
                case CONTROLLED:
                    sound.playControlled();
                    vibro.vibrateHit();
                    break;
                case ABDUCTED:
                    sound.playAbducted();
                    vibro.vibrateHit();
                    break;
                case DEAD_BURER:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case DEAD_MENTAL:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case W_ABDUCTED:
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
                case W_MENTALLED:
                    sound.playMentalHit();
                    sound.playTransmutating();
                    vibro.vibrateW();
                    break;
                case W_DEAD_ANOMALY:
                    sound.playAnomalyDeath();
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
                case DEAD_ANOMALY:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case W_DEAD_EXPLOSION:
                    sound.playStrongExplosion();
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
                case DEAD_EXPLOSION:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case W_CONTROLLED:
                    sound.playController();
                    sound.playTransmutating();
                    vibro.vibrateW();
                    break;
                case W_DEAD_BURER:
                    sound.playBurer();
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
                case DEAD_CONTROLLER:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case W_DEAD_RADIATION:
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
                case DEAD_RADIATION:
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case DEAD_EMISSION:
                    sound.playEmissionHit();
                    sound.playBulbBreak();
                    sound.playDeath();
                    vibro.vibrateDeath();
                    break;
                case W_DEAD_FRUITPUNCH:
                    sound.playFruitPunchDeath();
                    sound.playWTimer();
                    vibro.vibrateW();
                    break;
            }
        });
    }

    public void setImpactsStatesStream(Observable<Impacts.STATE> impactsStatesStream) {
//        impactsStatesSubscription = impactsStatesStream.subscribe((is) -> {
//            switch (is)
//            {
//                case HEALING: sound.playHealing();
//                default: sound.stopHealing();
//            }
//        });
    }


    public void setPlayerLevelStream(Observable<Integer> playerLevelStream) {
        if (playerLevelSubscription != null && !playerLevelSubscription.isDisposed()) {
            playerLevelSubscription.dispose();
        }
        playerLevelSubscription = playerLevelStream.skip(1).subscribe((l) -> {

            if (l == 1) {
                sound.playLevelUp();
                net.afterday.compas.logging.Logger.d(R.string.message_level_1);
            }
            if (l == 2) {
                sound.playLevelUp();
                net.afterday.compas.logging.Logger.d(R.string.message_level_2);
            }
            if (l == 3) {
                sound.playLevelUp();
                net.afterday.compas.logging.Logger.d(R.string.message_level_3);
            }
            if (l == 4) {
                sound.playLevelUp();
                net.afterday.compas.logging.Logger.d(R.string.message_level_4);
            }
            if (l == 5) {
                sound.playGlassBreak();
                net.afterday.compas.logging.Logger.d(R.string.message_level_max);
            }
//            else
//            {
//                sound.playLevelUp();
//            }
        });
    }

    private void processFrame(Frame frame) {
        PlayerProps playerProps = frame.getPlayerProps();
        if (playerProps.getState().getCode() != Player.ALIVE || playerProps.getHealthImpact() > 0) {
            return;
        }
        soundAnimator = ValueAnimator.ofInt(0, 60);
        // Вибрация только если нет воздействия D-сети
        if (playerProps.getExplosionImpact() <= 0) {
            vibro.vibrateDamage(frame.getPlayerProps());
        }
        playHits(playerProps);
        playRad(playerProps);
        playController(playerProps);
        playAnomaly(playerProps);
        playExplosion(playerProps);
        playArtefact(playerProps);
        playMental(playerProps);
        playBurer(playerProps);
    }

    private void playHits(PlayerProps playerProps) {
        boolean hasHit = false;
        if (playerProps.anomalyHit()) {
            hasHit = true;
            sound.playAnomalyDeath();
            net.afterday.compas.logging.Logger.e(R.string.message_anomaly_hit);
        }
        if (playerProps.fruitPunchHit()) {
            hasHit = true;
            sound.playFruitPunch();
            net.afterday.compas.logging.Logger.e(R.string.message_anomaly_hit);
        }
        if (playerProps.strongExplosionHit()) {
            hasHit = true;
            sound.playStrongExplosion();
            net.afterday.compas.logging.Logger.e(R.string.message_strong_explosion_hit);
        } else if (playerProps.weakExplosionHit()) {
            hasHit = true;
            sound.playWeakExplosion();
            net.afterday.compas.logging.Logger.e(R.string.message_weak_explosion_hit);
        }
        if (playerProps.burerHit()) {
            hasHit = true;
            sound.playBurer();
            net.afterday.compas.logging.Logger.e(R.string.message_burer_hit);
        }
        if (playerProps.controllerHit()) {
            hasHit = true;
            sound.playController();
            net.afterday.compas.logging.Logger.e(R.string.message_controller_hit);
        }
        if (playerProps.mentalHit()) {
            hasHit = true;
            sound.playMentalHit();
            net.afterday.compas.logging.Logger.e(R.string.message_mental_hit);
        }
        if (playerProps.monolithHit()) {
            hasHit = true;
            sound.playMonolithHit();
            net.afterday.compas.logging.Logger.d(R.string.message_monolit_call);
        }
        if (playerProps.emissionHit()) {
            hasHit = true;
            sound.playEmissionHit();
            net.afterday.compas.logging.Logger.e(R.string.message_emission_hit);
        }
        // Вибрация для всех ударов
        if (hasHit && !(playerProps.mentalHit() && playerProps.getFraction() == Player.FRACTION.MONOLITH)) {
            vibro.vibrateHit();
        }
    }

    private void playRad(PlayerProps playerProps) {
        double strength = playerProps.getRadiationImpact();
        if (strength == 0) {
            return;
        }
        soundAnimator.cancel();
        //Log.d("Sound", "Play!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + Thread.currentThread().getName());
        soundAnimator.setDuration(Engine.TICK_MILLISECONDS);
        soundAnimator.setInterpolator(new LinearInterpolator());
        soundAnimator.addUpdateListener((v) -> {
            double rand = Math.random();
            double probability = Math.min(strength / 17d, 0.9d);
            if (rand <= probability) {
                sound.playRadClick();
            }
        });
        soundAnimator.start();
    }

    private void playMental(PlayerProps playerProps) {
        if (playerProps.getMentalImpact() <= 0 || playerProps.getFraction() == Player.FRACTION.MONOLITH) {
            return;
        }
        if (playerProps.getLevel() >= 3) {
            //soundAnimator.cancel();
            sound.playMental();

        }
    }

    private void playBurer(PlayerProps playerProps) {
        if (playerProps.getBurerImpact() <= 0) {
            return;
        }
        //soundAnimator.cancel();
        //sound.playBurer();
        double strength = playerProps.getBurerImpact();
        int level = playerProps.getLevel();
        if (level >= 4) {
            //soundAnimator.cancel();
            sound.playBurerPresence();
        } else {
            playUnknown(playerProps);
        }
    }

    private void playController(PlayerProps playerProps) {
        if (playerProps.getControllerImpact() <= 0) {
            return;
        }
        double strength = playerProps.getControllerImpact();
        int level = playerProps.getLevel();
        if (level >= 4) {
            //soundAnimator.cancel();
            sound.playControllerPresence();
        } else {
            playUnknown(playerProps);
        }
    }

    private void playAnomaly(PlayerProps playerProps) {
        // Метод воспроизводит звуковые эффекты для аномалий
        
        // Если воздействие аномалии отсутствует (равно 0 или меньше), выходим из метода
        if (playerProps.getAnomalyImpact() <= 0) {
            return;
        }

        // Получаем уровень игрока и силу воздействия аномалии
        int level = playerProps.getLevel();
        double strength = playerProps.getAnomalyImpact();
        
        // Если уровень игрока >= 2 и игрок жив
        if (level >= 2 && playerProps.getState().getCode() == Player.ALIVE) {
            // Отменяем предыдущую подписку на звуковые эффекты, если она существует
            if (anomalySubsciption != null && !anomalySubsciption.isDisposed()) {
                anomalySubsciption.dispose();
            }
            
            // Вычисляем интервал между звуками (минимум 100мс)
            // Чем сильнее воздействие, тем чаще звуки
            int to = Math.max(100, Engine.TICK_MILLISECONDS / (int) Math.ceil(strength));
            
            // Создаем новую подписку: воспроизводим щелчки аномалии через определенные интервалы
            // в течение одного игрового тика
            anomalySubsciption = Observable.interval(to, TimeUnit.MILLISECONDS).take(Engine.TICK_MILLISECONDS, TimeUnit.MILLISECONDS).subscribe((t) -> sound.playAnomalyClick());
            //anomalyTimer.scheduleAtFixedRate(playAnomalyClick, to, to);
        } else {
            // Если уровень недостаточный или игрок мертв, воспроизводим неопознанный звук
            playUnknown(playerProps);
        }
    }

    private void playExplosion(PlayerProps playerProps) {
        // Если воздействие D-сети отсутствует (равно 0 или меньше), выходим из метода
        if (playerProps.getExplosionImpact() <= 0) {
            return;
        }

        // Получаем уровень игрока и силу воздействия D-сети
        int level = playerProps.getLevel();
        double strength = playerProps.getExplosionImpact();
        
        // Если уровень игрока >= 2 и игрок жив
        if (level >= 1 && playerProps.getState().getCode() == Player.ALIVE) {
            // Воспроизводим звук в зависимости от силы воздействия
            if (playerProps.strongExplosionHit()) {
                sound.playStrongExplosion();
            } else if (playerProps.weakExplosionHit()) {
                sound.playWeakExplosion();
            }
        } else {
            // Если уровень недостаточный или игрок мертв, воспроизводим неопознанный звук
            playUnknown(playerProps);
        }
    }

    private void playArtefact(PlayerProps playerProps) {
        if (playerProps.getArtefactImpact() <= 0) {
            return;
        }
        double strength = playerProps.getArtefactImpact();
        // Используем флаг доступа к детектору артефактов вместо проверки уровня
        if (playerProps.hasDetectorAccess() && playerProps.getState().getCode() == Player.ALIVE) {
            if (artefactSubsciption != null && !artefactSubsciption.isDisposed()) {
                artefactSubsciption.dispose();
            }
            int to = Math.max(1000, Engine.TICK_MILLISECONDS / (int) Math.ceil(strength));
            artefactSubsciption = Observable.interval(to, TimeUnit.MILLISECONDS).take(Engine.TICK_MILLISECONDS, TimeUnit.MILLISECONDS).subscribe((t) -> {
                sound.playArtefact();
                vibro.vibrateMessage(); // Добавляем вибрацию при обнаружении артефакта
            });
        }
    }

    private void playUnknown(PlayerProps playerProps) {
//        soundAnimator.cancel();
//        sound.playAnomalyDeath();
    }


}
