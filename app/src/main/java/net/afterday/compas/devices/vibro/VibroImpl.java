package net.afterday.compas.devices.vibro;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

import android.content.Context;
import android.os.Build;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.util.Pair;

import net.afterday.compas.core.influences.Influence;
import net.afterday.compas.core.player.Player;
import net.afterday.compas.core.player.PlayerProps;
import net.afterday.compas.settings.Constants;
import net.afterday.compas.settings.Settings;
import net.afterday.compas.settings.SettingsListener;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * Created by spaka on 7/2/2018.
 */

public class VibroImpl implements Vibro {
    private Vibro strategy;
    private Vibro on;
    private Vibro off;
    private Settings settings;
    private SettingsListener listener;

    public VibroImpl(Context context) {
        on = new VibroOn(context);
        off = new VibroOff();
        settings = Settings.instance(context);
        strategy = settings.getBoolSetting(Constants.VIBRATION) ? on : off;
        listener = (key, val) -> {
            switch (key) {
                case Constants.VIBRATION:
                    strategy = Boolean.parseBoolean(val) ? on : off;
                    return;
            }
        };
        settings.addSettingsListener(listener);
    }

    @Override
    public void vibrateDamage(PlayerProps playerProps) {
        strategy.vibrateDamage(playerProps);
    }

    @Override
    public void vibrateHit() {
        strategy.vibrateHit();
    }

    @Override
    public void vibrateW() {
        strategy.vibrateW();
    }

    @Override
    public void vibrateDeath() {
        strategy.vibrateDeath();
    }

    @Override
    public void vibrateMessage() {
        strategy.vibrateMessage();
    }

    @Override
    public void vibrateAlarm() {
        strategy.vibrateAlarm();
    }

    @Override
    public void vibrateTouch() {
        strategy.vibrateTouch();
    }

    private static class VibroOn implements Vibro {
        private Vibrator vibrator;
        private long lVmed = 0;
        private long lVmax = 0;
        private int vibroPriority = 0;
        private Disposable p1, p2;

        public VibroOn(Context ctx) {
            vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        }

        @Override
        public void vibrateDamage(PlayerProps playerProps) {
            if (playerProps.getState().getCode() != Player.ALIVE) {
                return;
            }
            boolean vibrated;
            if (vibroPriority == 0) {
                vibrated = _vibrateDmg(getMax(playerProps), playerProps);
                if (!vibrated) {
                    lVmed = lVmax = 0;
                }
            }

//        Observable.fromArray(new long[][]{p(10, 10), p(20, 10), p(40, 10), p(80, 10), p(160, 10)})
//                .concatMap((l) -> {vibrator.vibrate(l[0]); return Observable.timer(l[0] + l[1], TimeUnit.MILLISECONDS);}).subscribe();
        }

        @Override
        public void vibrateHit() {
            if (vibroPriority < 1) {
                vibroPriority = 1;
                vibrator.cancel();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    p1 = Observable.interval(150, TimeUnit.MILLISECONDS).take(10).subscribe((t) -> vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION)), (e) -> {
                    }, () -> vibroPriority = 0);
                }
                 else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    p1 = Observable.interval(150, TimeUnit.MILLISECONDS).take(10).subscribe((t) -> vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE)), (e) -> {
                    }, () -> vibroPriority = 0);
                }
                else {
                    p1 = Observable.interval(150, TimeUnit.MILLISECONDS).take(10).subscribe((t) -> vibrator.vibrate(50), (e) -> {
                    }, () -> vibroPriority = 0);
                }
            }
        }

        @Override
        public void vibrateW() {
            if (vibroPriority < 2) {
                vibroPriority = 2;
                if (p1 != null && !p1.isDisposed()) {
                    p1.dispose();
                }
                vibrator.cancel();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, DEFAULT_AMPLITUDE));
                }
                else {
                    vibrator.vibrate(1000);
                }
                Observable.timer(5, TimeUnit.SECONDS).subscribe((t) -> vibroPriority = 0);
            }
        }

        @Override
        public void vibrateDeath() {
            if (vibroPriority < 2) {
                vibroPriority = 2;
                if (p1 != null && !p1.isDisposed()) {
                    p1.dispose();
                }
                vibrator.cancel();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    vibrator.vibrate(VibrationEffect.createOneShot(2000, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(2000, DEFAULT_AMPLITUDE));
                }
                else {
                    vibrator.vibrate(2000);
                }
                Observable.timer(5, TimeUnit.SECONDS).subscribe((t) -> vibroPriority = 0);
            }
        }

        @Override
        public void vibrateMessage() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Observable.interval(1, TimeUnit.SECONDS).take(10).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION)));
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Observable.interval(1, TimeUnit.SECONDS).take(10).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE)));
            } else {
                Observable.interval(1, TimeUnit.SECONDS).take(10).subscribe((m) -> vibrator.vibrate(30));
            }
        }

        @Override
        public void vibrateTouch() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Observable.interval(0, TimeUnit.SECONDS).take(1).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)));
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Observable.interval(0, TimeUnit.SECONDS).take(1).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(30, DEFAULT_AMPLITUDE)));
            } else {
                Observable.interval(0, TimeUnit.SECONDS).take(1).subscribe((m) -> vibrator.vibrate(30));
            }
        }

        @Override
        public void vibrateAlarm() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Observable.interval(1, TimeUnit.SECONDS).take(20).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)));
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Observable.interval(1, TimeUnit.SECONDS).take(20).subscribe((m) -> vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE)));
            } else {
                Observable.interval(1, TimeUnit.SECONDS).take(20).subscribe((m) -> vibrator.vibrate(50));
            }
        }

        private Pair<Integer, Double> getMax(PlayerProps playerProps) {
            double[] impacts = playerProps.getImpacts();
            double max = 0d;
            int infl = 0;
            if (impacts == null) {
                return new Pair<Integer, Double>(0, 0d);
            }
            int level = playerProps.getLevel();
            for (int i = 0; i < impacts.length; i++) {
                double s = impacts[i];
                switch (i) {
                    case Influence.RADIATION:
                        if (s > max) {
                            max = s;
                            infl = i;
                        }
                        break;
                    case Influence.ANOMALY:
                    case Influence.EXPLOSION:
                    case Influence.FRUITPUNCH:
                        // Этот код обрабатывает случаи влияния аномалий, повреждающих аномалий и "фруктового пунша"
                        // Если текущее значение влияния (s) больше предыдущего максимума (max)
                        // И уровень игрока больше или равен 2,
                        // то обновляем максимальное значение и тип влияния
                        if (s > max && level >= 2) {
                            max = s;
                            infl = i;
                        }
                        break;
                    case Influence.MENTAL:
                        if (s > max && level >= 3) {
                            max = s;
                            infl = i;
                        }
                        break;
                    case Influence.CONTROLLER:
                    case Influence.BURER:
                        if (s > max && level >= 4) {
                            max = s;
                            infl = i;
                        }
                        break;
                }
            }
            return new Pair<>(infl, max);
        }

        private boolean _vibrateDmg(Pair<Integer, Double> infl, PlayerProps playerProps) {
            double strength = infl.second;
            if (strength <= 0d || infl.first == Influence.MENTAL && playerProps.getFraction() == Player.FRACTION.MONOLITH) {
                return false;
            }
            if (strength <= 0d || infl.first == Influence.RADIATION && playerProps.getFraction() == Player.FRACTION.DARKEN) {
                return false;
            }
            boolean vibrated = false;
            if (strength >= Influence.PEAK) {
                if (lVmax == 0) {
                    vibrator.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(100);
                    }
                    lVmax = System.currentTimeMillis();
                    vibrated = true;
                } else if (System.currentTimeMillis() - lVmax < 5000) {
                    vibrator.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(100);
                    }
                    lVmax = System.currentTimeMillis();
                    vibrated = true;
                }
            } else if (strength >= Influence.MAX) {
                if (lVmed == 0) {
                    vibrator.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        vibrator.vibrate(VibrationEffect.createOneShot(25, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(25, DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(25);
                    }
                    lVmed = System.currentTimeMillis();
                    vibrated = true;
                } else if (System.currentTimeMillis() - lVmed < 10000) {
                    vibrator.cancel();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        vibrator.vibrate(VibrationEffect.createOneShot(25, DEFAULT_AMPLITUDE), VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION));
                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(25, DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(25);
                    }
                    lVmed = System.currentTimeMillis();
                    vibrated = true;
                }
            }
            return vibrated;
        }

        private long[] p(long a, long b) {
            return new long[]{a, b};
        }
    }

    private static class VibroOff implements Vibro {

        @Override
        public void vibrateDamage(PlayerProps playerProps) {

        }

        @Override
        public void vibrateHit() {

        }

        @Override
        public void vibrateW() {

        }

        @Override
        public void vibrateDeath() {

        }

        @Override
        public void vibrateMessage() {

        }

        @Override
        public void vibrateAlarm() {

        }

        @Override
        public void vibrateTouch() {

        }
    }
}
