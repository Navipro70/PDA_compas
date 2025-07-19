package net.afterday.compas.core.player;

import android.util.Log;

import net.afterday.compas.core.influences.Influence;
import net.afterday.compas.core.influences.InfluencesPack;
import net.afterday.compas.core.inventory.items.Item;
import net.afterday.compas.core.serialization.Serializer;
import net.afterday.compas.util.Convert;

/**
 * Created by spaka on 4/18/2018.
 */

public class ImpactsImpl implements Impacts {
    private static final String PLAYER_PROPS = "playerProps";
    private static final String TAG = "ImpactsImpl";
    private static final int MINUTE = 60 * 1000;
    private static final int HOUR = MINUTE * 60;
    private static final int BURER_COOLDOWN = 15 * 1000; //Бюрер
    private static final int CONTROLLER_COOLDOWN = 15 * 1000; //Контролёр
    private static final int MENTAL_COOLDOWN = 25 * 1000; //Ментальное излучение
    private static final int ANOMALY_COOLDOWN = 25 * 1000; //Аномалия
    private static final int EXPLOSION_COOLDOWN = 90 * 1000; // D-сеть (1,5 минуты)
    private static final int FRUITPUNCH_COOLDOWN = 2 * 1000; // Аномалия "Кисель"
    private static final int MONOLITH_COOLDOWN = 15 * 1000; //Зов Монолита
    private static final int EMISSION_COOLDOWN = 30 * 1000; //Выброс
    private static final double MISSION_DAMAGE = 0.01;
    private InfluencesPack inflPack;
    private PlayerProps oldProps;
    private PlayerPropsImpl newProps;
    private double healthModifier = 1;
    private double radModifier = 1;
    private double[] mImpacts;
    private long delta;
    private long accumulatedBurer = 0;
    private long accumulatedController = 0;
    private long accumulatedAnomaly = 0;
    private long accumulatedEXPLOSION = 0;
    private long accumulatedFruitPunch = 0;
    private long accumulatedMental = 0;
    private long accumulatedMonolith = 0;
    private long accumulatedEmission = 0;
    private Impacts.STATE state;
    private boolean wasInExplosionZone = false;
    private long lastExplosionHitTime = 0; // Добавляем поле для отслеживания времени последнего удара
    private boolean wasInBurerZone = false;
    private boolean wasInControllerZone = false;
    private boolean wasInMentalZone = false;
    private boolean wasInMonolithZone = false;
    private boolean wasInEmissionZone = false;
    private boolean wasInFruitPunchZone = false;
    private boolean wasInAnomalyZone = false;
    private long lastBurerHitTime = 0;
    private long lastControllerHitTime = 0;
    private long lastMentalHitTime = 0;
    private long lastMonolithHitTime = 0;
    private long lastEmissionHitTime = 0;
    private long lastFruitPunchHitTime = 0;
    private long lastAnomalyHitTime = 0;

    public ImpactsImpl(PlayerProps playerProps, Serializer serializer) {
        newProps = (PlayerPropsImpl) playerProps;
    }

    public void prepare(InfluencesPack inflPack, long delta) {
        this.state = null;

        if (newProps.getState().getCode() == Player.DEAD && isHealing(newProps, inflPack)) {
            newProps.setState(Player.STATE.ALIVE);
        }
        this.inflPack = inflPack;
        oldProps = newProps;
        newProps = new PlayerPropsImpl(oldProps);
        mImpacts = inflPack.getInfluences();

        this.delta = delta;
        healthModifier = 1;
        radModifier = 1;
    }

    public void healByInfluence(long delta) {
        newProps.setHealth(newProps.getHealth() + (2 * ((double) delta / MINUTE)));
        if (newProps.getHealth() >= 30) {
            newProps.setRadiation(newProps.getRadiation() - (radModifier * 3 * ((double) delta / MINUTE)));
        }
//        if(newProps.getState() != Player.STATE.ALIVE)
//        {
//            Log.e(TAG, "---------------healByInfluence5 " + Thread.currentThread().getName() + "  " + newProps);
//            newProps.setState(Player.STATE.ALIVE);
//        }
//        Log.e(TAG, "healByInfluence6 " + Thread.currentThread().getName() + "  " + newProps);
    }


    public void calculateAccumulated(long delta) {
        if (newProps.getState().getCode() == Player.DEAD) {
            return;
        }
        if (newProps.getFraction() == Player.FRACTION.MISSION) {
            newProps.setHealth(newProps.getHealth() - MISSION_DAMAGE);
            if (newProps.getHealth() <= 0d) {
                newProps.setState(Player.STATE.DEAD_EMISSION);
            }
        }
        if ((newProps.getRadiation() <= 1d) && (mImpacts[Influence.RADIATION] <= 0d) && !(state == STATE.DAMAGE)) {
            //Esant sukaupus iki 1 SV radiacijos, gyvybe atsistato savaime, sukaupta radiacija po truputi išsinulina savaime po 100% gyvybes pasiekimo (1 SV per minute greiciu).
            newProps.setHealth(newProps.getHealth() + (healthModifier * 0.05 * ((double) delta / MINUTE)));
            newProps.setRadiation(newProps.getRadiation() - (radModifier * 0.005 * ((double) delta / MINUTE)));
//            if(newProps.getHealth() == 100)
//            {
//
//            }
        } else if (newProps.getRadiation() > 1d) {
            //Esant sukaupus virš 1 SV radiacijos, gyvybe pradeda po truputi mažeti, su greičiu, atitinkančiu prikauptai radiacijai
            double radMod = getRadMod(newProps.getRadiation());
            newProps.setHealth(newProps.getHealth() - radMod * ((double) delta / MINUTE));
            if (newProps.getHealth() <= 0) {
                newProps.setState(Player.STATE.W_DEAD_RADIATION);
            }
        }
    }

    private double getHealthToSet(int inflId, long delta, double strength) {
        if (strength >= Influence.PEAK) {
            if (inflId == Influence.RADIATION) {
                return newProps.getHealth() - 10d * ((double) delta / MINUTE);
            }
            return newProps.getHealth() - 4d * ((double) delta / MINUTE);  //Nes kiti influencai dauzo kas 5 sekundes.
        }
        if (strength >= Influence.MAX) {
            return newProps.getHealth() - 2d * ((double) delta / MINUTE);
        }
        if (strength >= Influence.MED) {
            return newProps.getHealth() - 1d * ((double) delta / MINUTE);
        }
        if (strength >= Influence.MIN) {
            return newProps.getHealth() - 0.5 * ((double) delta / MINUTE);
        }
        return newProps.getHealth();
    }

    private double getRadiationToSet(long delta, double strength) {
        if (mImpacts[Influence.RADIATION] >= Influence.PEAK) {
            return newProps.getRadiation() + 1000 * ((double) delta / HOUR);
        }
        if (mImpacts[Influence.RADIATION] <= 0.1) {
            return newProps.getRadiation();
        }
        return newProps.getRadiation() + mImpacts[Influence.RADIATION] * ((double) delta / HOUR);
    }

    private boolean hit(int infl, long delta) {
        Log.e(TAG, "TryToHit: " + infl);
        long currentTime = System.currentTimeMillis();
        
        switch (infl) {
            case Influence.BURER:
                boolean isInBurerZone = inflPack.influencedBy(Influence.BURER);
                
                if (isInBurerZone) {
                    if (!wasInBurerZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(25);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_BURER);
                        }
                        newProps.setBurerHit(true);
                        wasInBurerZone = true;
                        lastBurerHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastBurerHitTime >= BURER_COOLDOWN) {
                        newProps.subtractHealth(25);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_BURER);
                        }
                        newProps.setBurerHit(true);
                        lastBurerHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setBurerHit(false);
                    wasInBurerZone = false;
                }
                return false;

            case Influence.CONTROLLER:
                boolean isInControllerZone = inflPack.influencedBy(Influence.CONTROLLER);
                
                if (isInControllerZone) {
                    if (!wasInControllerZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(45);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_CONTROLLED);
                        }
                        newProps.setControllerHit(true);
                        wasInControllerZone = true;
                        lastControllerHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastControllerHitTime >= CONTROLLER_COOLDOWN) {
                        newProps.subtractHealth(45);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_CONTROLLED);
                        }
                        newProps.setControllerHit(true);
                        lastControllerHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setControllerHit(false);
                    wasInControllerZone = false;
                }
                return false;

            case Influence.MENTAL:
                if (newProps.getFraction() == Player.FRACTION.MONOLITH) {
                    return false;
                }

                boolean isInMentalZone = inflPack.influencedBy(Influence.MENTAL);
                
                if (isInMentalZone) {
                    if (!wasInMentalZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(35);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_MENTALLED);
                        }
                        newProps.setMentalHit(true);
                        wasInMentalZone = true;
                        lastMentalHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastMentalHitTime >= MENTAL_COOLDOWN) {
                        newProps.subtractHealth(35);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_MENTALLED);
                        }
                        newProps.setMentalHit(true);
                        lastMentalHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setMentalHit(false);
                    wasInMentalZone = false;
                }
                return false;

            case Influence.MONOLITH:
                if (newProps.getFraction() == Player.FRACTION.MONOLITH) {
                    return false;
                }

                boolean isInMonolithZone = inflPack.influencedBy(Influence.MONOLITH);
                
                if (isInMonolithZone) {
                    if (!wasInMonolithZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(10);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_MENTALLED);
                        }
                        newProps.setMonolithHit(true);
                        wasInMonolithZone = true;
                        lastMonolithHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastMonolithHitTime >= MONOLITH_COOLDOWN) {
                        newProps.subtractHealth(10);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_MENTALLED);
                        }
                        newProps.setMonolithHit(true);
                        lastMonolithHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setMonolithHit(false);
                    wasInMonolithZone = false;
                }
                return false;

            case Influence.EMISSION:
                if (newProps.getFraction() == Player.FRACTION.MONOLITH) {
                    return false;
                }

                boolean isInEmissionZone = inflPack.influencedBy(Influence.EMISSION);
                
                if (isInEmissionZone) {
                    if (!wasInEmissionZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(49);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.DEAD_EMISSION);
                        }
                        newProps.setEmissionHit(true);
                        wasInEmissionZone = true;
                        lastEmissionHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastEmissionHitTime >= EMISSION_COOLDOWN) {
                        newProps.subtractHealth(49);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.DEAD_EMISSION);
                        }
                        newProps.setEmissionHit(true);
                        lastEmissionHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setEmissionHit(false);
                    wasInEmissionZone = false;
                }
                return false;

            case Influence.FRUITPUNCH:
                boolean isInFruitPunchZone = inflPack.influencedBy(Influence.FRUITPUNCH);
                
                if (isInFruitPunchZone) {
                    if (!wasInFruitPunchZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(15);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_FRUITPUNCH);
                        }
                        newProps.setFruitPunchHit(true);
                        wasInFruitPunchZone = true;
                        lastFruitPunchHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastFruitPunchHitTime >= FRUITPUNCH_COOLDOWN) {
                        newProps.subtractHealth(15);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_FRUITPUNCH);
                        }
                        newProps.setFruitPunchHit(true);
                        lastFruitPunchHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setFruitPunchHit(false);
                    wasInFruitPunchZone = false;
                }
                return false;

            case Influence.ANOMALY:
                boolean isInAnomalyZone = inflPack.influencedBy(Influence.ANOMALY);
                
                if (isInAnomalyZone) {
                    if (!wasInAnomalyZone) {
                        // Сразу наносим удар при входе в зону
                        newProps.subtractHealth(35);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_ANOMALY);
                        }
                        newProps.setAnomalyHit(true);
                        wasInAnomalyZone = true;
                        lastAnomalyHitTime = currentTime;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (currentTime - lastAnomalyHitTime >= ANOMALY_COOLDOWN) {
                        newProps.subtractHealth(35);
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_ANOMALY);
                        }
                        newProps.setAnomalyHit(true);
                        lastAnomalyHitTime = currentTime;
                        return true;
                    }
                } else {
                    newProps.setAnomalyHit(false);
                    wasInAnomalyZone = false;
                }
                return false;

            case Influence.EXPLOSION:
                // Проверяем, есть ли влияние D-сети
                boolean isInZone = inflPack.influencedBy(Influence.EXPLOSION);
                double explosionStrength = mImpacts[Influence.EXPLOSION];
                
                Log.e(TAG, "D-сеть: isInZone=" + isInZone + 
                           ", wasInExplosionZone=" + wasInExplosionZone +
                           ", accumulatedEXPLOSION=" + accumulatedEXPLOSION + 
                           ", delta=" + delta + 
                           ", strength=" + explosionStrength);

                if (isInZone) {
                    if (!wasInExplosionZone) {
                        // Сразу наносим удар при входе в зону
                        if (explosionStrength > 100d) {
                            newProps.subtractHealth(100);
                            newProps.setStrongExplosionHit(true);
                            newProps.setWeakExplosionHit(false);
                            Log.e(TAG, "D-сеть: нанесен сильный удар при входе");
                        } else if (explosionStrength > 16d) {
                            newProps.subtractHealth(35);
                            newProps.setWeakExplosionHit(true);
                            newProps.setStrongExplosionHit(false);
                            Log.e(TAG, "D-сеть: нанесен слабый удар при входе");
                        }
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_EXPLOSION);
                        }
                        wasInExplosionZone = true;
                        return true;
                    }
                    
                    // Проверяем кулдаун для последующих ударов
                    if (accumulatedEXPLOSION >= EXPLOSION_COOLDOWN) {
                        if (explosionStrength > 100d) {
                            newProps.subtractHealth(100);
                            newProps.setStrongExplosionHit(true);
                            newProps.setWeakExplosionHit(false);
                            Log.e(TAG, "D-сеть: нанесен сильный удар после кулдауна");
                        } else if (explosionStrength > 16d) {
                            newProps.subtractHealth(35);
                            newProps.setWeakExplosionHit(true);
                            newProps.setStrongExplosionHit(false);
                            Log.e(TAG, "D-сеть: нанесен слабый удар после кулдауна");
                        }
                        if (newProps.getHealth() <= 0) {
                            newProps.setState(Player.STATE.W_DEAD_EXPLOSION);
                        }
                        accumulatedEXPLOSION = 0;
                        return true;
                    }
                } else {
                    newProps.setStrongExplosionHit(false);
                    newProps.setWeakExplosionHit(false);
                    wasInExplosionZone = false;
                    Log.e(TAG, "D-сеть: игрок вышел из зоны, сброс флагов");
                }
                return false;
        }
        return false;
    }

    private Player.STATE getDeathState(int infl) {
        switch (infl) {
            case Influence.ANOMALY:
                return Player.STATE.W_DEAD_ANOMALY;
            case Influence.BURER:
                return Player.STATE.W_DEAD_BURER;
            case Influence.CONTROLLER:
                return Player.STATE.W_CONTROLLED;
            case Influence.MENTAL:
                return Player.STATE.W_MENTALLED;
            case Influence.RADIATION:
                return Player.STATE.DEAD_RADIATION;
            case Influence.EMISSION:
                return Player.STATE.DEAD_EMISSION;
            case Influence.FRUITPUNCH:
                return Player.STATE.W_DEAD_FRUITPUNCH;
            case Influence.EXPLOSION:
                return Player.STATE.W_DEAD_EXPLOSION;
        }
        return Player.STATE.ALIVE;
    }

    private void handleExplosion(long delta) {
        boolean isInZone = inflPack.influencedBy(Influence.EXPLOSION);
        double explosionStrength = mImpacts[Influence.EXPLOSION];
        long currentTime = System.currentTimeMillis();
        
        Log.e(TAG, "D-сеть: isInZone=" + isInZone + 
                   ", currentTime=" + currentTime +
                   ", lastHitTime=" + lastExplosionHitTime +
                   ", timeSinceLastHit=" + (currentTime - lastExplosionHitTime) +
                   ", strength=" + explosionStrength);

        if (isInZone) {
            // Проверяем, прошло ли достаточно времени с последнего удара
            if (currentTime - lastExplosionHitTime >= EXPLOSION_COOLDOWN) {
                // Наносим удар
                if (explosionStrength > 100d) {
                    newProps.subtractHealth(100);
                    newProps.setStrongExplosionHit(true);
                    newProps.setWeakExplosionHit(false);
                    Log.e(TAG, "D-сеть: нанесен сильный удар");
                } else if (explosionStrength > 16d) {
                    newProps.subtractHealth(35);
                    newProps.setWeakExplosionHit(true);
                    newProps.setStrongExplosionHit(false);
                    Log.e(TAG, "D-сеть: нанесен слабый удар");
                }
                if (newProps.getHealth() <= 0) {
                    newProps.setState(Player.STATE.W_DEAD_EXPLOSION);
                }
                // Обновляем время последнего удара
                lastExplosionHitTime = currentTime;
            }
        } else {
            // Если игрок вышел из зоны, сбрасываем флаги ударов
            newProps.setStrongExplosionHit(false);
            newProps.setWeakExplosionHit(false);
            Log.e(TAG, "D-сеть: игрок вышел из зоны, сброс флагов");
        }
    }

    public void calculateEnvDamage(long delta) {
        Log.e(TAG, "calculateEnvDamage " + Thread.currentThread().getName());
        if (newProps.getFraction() == Player.FRACTION.GAMEMASTER) {
            return;
        }
        
        for (int i = 0; i < mImpacts.length; i++) {
            double strength = mImpacts[i];
            if (strength <= 0d || i == Influence.HEALTH || i == Influence.ARTEFACT || 
                ((newProps.getFraction() == Player.FRACTION.MONOLITH) && (i == Influence.MENTAL || i == Influence.MONOLITH) || 
                 (newProps.getFraction() == Player.FRACTION.DARKEN) && (i == Influence.RADIATION))) {
                continue;
            }
            if (i == Influence.RADIATION) {
                newProps.setHealth(getHealthToSet(i, delta, strength));
                if (newProps.getHealth() <= 0) {
                    newProps.setState(Player.STATE.W_DEAD_RADIATION);
                    break;
                }
                newProps.setRadiation(getRadiationToSet(delta, strength));
            } else if (i == Influence.EMISSION && inflPack.isEmission() && strength >= Influence.MAX_SATELLITES && 
                       newProps.getFraction() != Player.FRACTION.MONOLITH && !hit(i, delta)) {
                newProps.setHealth(newProps.getHealth() - 2d * ((double) delta / MINUTE));
                if (newProps.getHealth() <= 0d) {
                    newProps.setState(Player.STATE.DEAD_EMISSION);
                    break;
                }
            } else if (i == Influence.EXPLOSION) {
                // Для D-сети используем отдельный метод обработки
                handleExplosion(delta);
            } else if (strength < Influence.PEAK || !hit(i, delta)) {
                double h = getHealthToSet(i, delta, strength);
                Log.e(TAG, "------------------------------------------ health: " + h + " infl: " + i + " strength: " + strength);
                newProps.setHealth(h);
                if (newProps.getHealth() <= 0d) {
                    newProps.setState(getDeathState(i));
                    break;
                }
            }
        }
        Log.e(TAG, "" + newProps.getState());
    }

    public PlayerProps getPlayerProps() {
        newProps.setImpacts(mImpacts);
        return newProps;
    }

    private double getRadMod(double rad) {
        return Convert.map(rad, 1d, 16d, 1d, 7d);
    }

    @Override
    public void artifactsImpact(double[] artifacts) {
        healthModifier = 1;
        radModifier = 1;
        if (inflPack.influencedBy(Influence.HEALTH)) {
            healthModifier = artifacts[Item.HEALTH_MODIFIER];
            mImpacts[Influence.HEALTH] *= artifacts[Item.HEALTH_MODIFIER];
            return;
        }
        Log.d(TAG, " -- " + artifacts[Item.CONTROLLER_MODIFIER]);
        radModifier = Math.max(artifacts[Item.RADIATION_MODIFIER], 1) * Math.max(artifacts[Item.RADIATION_EMMITER], 1);
        healthModifier = artifacts[Item.HEALTH_MODIFIER];
        mImpacts[Influence.HEALTH] *= artifacts[Item.HEALTH_MODIFIER];
        mImpacts[Influence.RADIATION] += artifacts[Item.RADIATION_EMMITER];
        mImpacts[Influence.RADIATION] *= artifacts[Item.RADIATION_MODIFIER];
        mImpacts[Influence.ANOMALY] *= artifacts[Item.ANOMALY_MODIFIER];
        mImpacts[Influence.BURER] *= artifacts[Item.BURER_MODIFIER];
        mImpacts[Influence.CONTROLLER] *= artifacts[Item.CONTROLLER_MODIFIER];
        mImpacts[Influence.MENTAL] *= artifacts[Item.MENTAL_MODIFIER];
        mImpacts[Influence.MONOLITH] *= artifacts[Item.MONOLITH_MODIFIER];
        mImpacts[Influence.FRUITPUNCH] *= artifacts[Item.ANOMALY_MODIFIER];
        // D-сеть (EXPLOSION) не должна получать модификатор аномалии
    }

    @Override
    public void itemImpact(Item item) {
        if (item.hasModifier(Item.ANOMALY_MODIFIER)) {

        }
    }

    private void consumeItem(Item item, long delta) {
        if (item.hasModifier(Item.RADIATION_MODIFIER)) {
            mImpacts[Influence.RADIATION] *= item.getModifier(Item.RADIATION_MODIFIER);
            radModifier *= item.getModifier(Item.RADIATION_MODIFIER);
        }
        if (item.hasModifier(Item.ANOMALY_MODIFIER)) {
            mImpacts[Influence.ANOMALY] *= item.getModifier(Item.ANOMALY_MODIFIER);
            mImpacts[Influence.FRUITPUNCH] *= item.getModifier(Item.ANOMALY_MODIFIER);
            // D-сеть (EXPLOSION) не должна получать модификатор аномалии
        }
        if (item.hasModifier(Item.MENTAL_MODIFIER)) {
            mImpacts[Influence.MENTAL] *= item.getModifier(Item.MENTAL_MODIFIER);
        }
        if (item.hasModifier(Item.MONOLITH_MODIFIER)) {
            mImpacts[Influence.MONOLITH] *= item.getModifier(Item.MONOLITH_MODIFIER);
        }
        if (item.hasModifier(Item.CONTROLLER_MODIFIER)) {
            mImpacts[Influence.CONTROLLER] *= item.getModifier(Item.CONTROLLER_MODIFIER);
        }
        if (item.hasModifier(Item.HEALTH_MODIFIER)) {
            mImpacts[Influence.HEALTH] *= item.getModifier(Item.HEALTH_MODIFIER);
            healthModifier *= item.getModifier(Item.HEALTH_MODIFIER);
        }
    }

    public void addHealthModifier(double healthModifier) {
        this.healthModifier *= healthModifier;
    }

    @Override
    public void boosterImpact(Item item) {
        consumeItem(item, delta);
        newProps.setBoosterPercents(item.getPercentsLeft());
    }

    public void deviceImpact(Item item) {
        consumeItem(item, delta);
        newProps.setDevicePercents(item.getPercentsLeft());
    }

    @Override
    public void armorImpact(Item item) {
        consumeItem(item, delta);
        newProps.setArmorPercents(item.getPercentsLeft());
    }

    @Override
    public Impacts.STATE getState() {
        if (this.state != null) {
            return this.state;
        }
        if (isHealing(newProps, inflPack)) {
            this.state = Impacts.STATE.HEALING;
            return this.state;
        }
        boolean clear = true;
        for (int i = 0; i < mImpacts.length; i++) {
            if (i == Influence.ARTEFACT) {
                continue;
            }
            if ((i == Influence.EMISSION || i == Influence.MENTAL) && newProps.getFraction() == Player.FRACTION.MONOLITH) {
                continue;
            }
            if (i == Influence.EMISSION && inflPack.isEmission() && mImpacts[i] >= Influence.MAX_SATELLITES) {
                clear = false;
                break;
            }
            if (mImpacts[i] > 0d) {
                clear = false;
                break;
            }
        }
        if (clear) {
            this.state = STATE.CLEAR;
            return this.state;
        }
        if (mImpacts[Influence.RADIATION] > 0d ||
                mImpacts[Influence.MENTAL] > 0d ||
                mImpacts[Influence.MONOLITH] > 0d ||
                mImpacts[Influence.CONTROLLER] > 0d ||
                mImpacts[Influence.ANOMALY] > 0d ||
                mImpacts[Influence.BURER] > 0d ||
                mImpacts[Influence.EXPLOSION] > 0d ||
                mImpacts[Influence.FRUITPUNCH] > 0d ||
                mImpacts[Influence.EMISSION] > Influence.MAX_SATELLITES
        ) {
            this.state = STATE.DAMAGE;
            return this.state;
        }

        return STATE.CLEAR;
    }

    private boolean isHealing(PlayerProps playerProps, InfluencesPack inflPack) {
        Player.FRACTION fraction = playerProps.getFraction();
        Player.STATE state = playerProps.getState();
        
        // Если игрок в состоянии смерти и таймер еще не истек, лечение невозможно
        if (state == Player.STATE.DEAD_BURER || 
            state == Player.STATE.DEAD_ANOMALY || 
            state == Player.STATE.DEAD_RADIATION || 
            state == Player.STATE.DEAD_CONTROLLER || 
            state == Player.STATE.DEAD_MENTAL) {
            return false;
        }
        
        return (fraction == Player.FRACTION.MONOLITH && inflPack.influencedBy(Influence.MONOLITH)) || 
               (fraction != Player.FRACTION.MONOLITH && inflPack.influencedBy(Influence.HEALTH)) || 
               (fraction == Player.FRACTION.DARKEN && inflPack.influencedBy(Influence.RADIATION)) || 
               (fraction != Player.FRACTION.DARKEN && inflPack.influencedBy(Influence.HEALTH));
    }

    private void printImpacts() {
        Log.e(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<Impacts:");
        Log.e(TAG, "HEALTH: " + mImpacts[Influence.HEALTH]);
        Log.e(TAG, "RADIATION: " + mImpacts[Influence.RADIATION]);
        Log.e(TAG, "MENTAL: " + mImpacts[Influence.MENTAL]);
        Log.e(TAG, "CONTROLLER: " + mImpacts[Influence.CONTROLLER]);
        Log.e(TAG, "ANOMALY: " + mImpacts[Influence.ANOMALY]);
        Log.e(TAG, "BURER: " + mImpacts[Influence.BURER]);
        Log.e(TAG, "ARTEFACT: " + mImpacts[Influence.ARTEFACT]);
        Log.e(TAG, "MONOLITH: " + mImpacts[Influence.MONOLITH]);
        Log.e(TAG, "D-сеть (EXPLOSION): " + mImpacts[Influence.EXPLOSION]);
        Log.e(TAG, "FRUITPUNCH: " + mImpacts[Influence.FRUITPUNCH]);
    }

    public boolean weakExplosionHit() {
        if (mImpacts != null && mImpacts.length >= Influence.EXPLOSION) {
            return mImpacts[Influence.EXPLOSION] > 16 && mImpacts[Influence.EXPLOSION] <= 100;
        }
        return false;
    }
}
