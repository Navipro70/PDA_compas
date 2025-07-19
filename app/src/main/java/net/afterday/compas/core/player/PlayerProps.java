package net.afterday.compas.core.player;

import net.afterday.compas.core.serialization.Jsonable;

/**
 * Created by spaka on 4/20/2018.
 */

public interface PlayerProps extends Jsonable {
    double getHealth();

    double getRadiation();

    void setRadiation(double radiation);

    double getArtefactImpact();

    long getController();

    long getZombified();

    double getMental();

    double getRadiationImpact();

    double getHealthImpact();

    double getControllerImpact();

    double getBurerImpact();

    double getMentalImpact();

    double getMonolithImpact();

    double getAnomalyImpact();

    double getExplosionImpact();

    double getBoosterPercents();

    void setBoosterPercents(double percents);

    double getDevicePercents();

    void setDevicePercents(double percents);

    double getArmorPercents();

    void setArmorPercents(double percents);

    void addHealth(double health);

    void addRadiation(double radiation);

    boolean addXpPoints(int xp);

    int getXpPoints();

    void setXpPoints(int xp);

    int getLevel();

    void subtractHealth(double health);

    void subtractRadiation(double radiation);

    boolean burerHit();

    boolean controllerHit();

    boolean anomalyHit();

    boolean strongExplosionHit();

    boolean weakExplosionHit();

    boolean fruitPunchHit();

    boolean mentalHit();

    boolean monolithHit();

    boolean emissionHit();

    boolean hasHealthInstant();

    boolean hasRadiationInstant();

    boolean setFraction(Player.FRACTION fraction);

    Player.FRACTION getFraction();

    int getLevelXp();

    Player.STATE getState();

    void setState(Player.STATE state);

    double[] getImpacts();

    boolean hasDetectorAccess();

    void setDetectorAccess(boolean hasAccess);
}
