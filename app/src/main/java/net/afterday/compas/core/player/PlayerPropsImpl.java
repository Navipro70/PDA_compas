package net.afterday.compas.core.player;

import static java.lang.Double.max;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import net.afterday.compas.core.influences.Influence;
import net.afterday.compas.core.serialization.Jsonable;

/**
 * Created by spaka on 4/18/2018.
 */

public class PlayerPropsImpl implements PlayerProps {
    public static final int LEVEL_XP = 50;
    private static final String TAG = "PlayerPropsImpl";
    private Player.STATE state;
    private double radiation;
    private double radiationImpact;
    private double health;
    private double boosterPercents = 0;
    private double devicePercents = 0;
    private double armorPercents = 0;
    private double[] impacts;
    private boolean burerHit;
    private boolean strongExplosionHit;
    private boolean weakExplosionHit;
    private boolean controllerHit;
    private boolean anomalyHit;
    private boolean fruitPunchHit;
    private boolean mentalHit;
    private boolean monolithHit;
    private boolean emissionHit;
    private boolean hasHealthInstant,
            hasRadiationInstant;
    private boolean hasDetectorAccess = false;
    private Player.FRACTION fraction;
    private int xpPoints;
    private JsonObject o;

    public PlayerPropsImpl(Player.STATE state) {
        this.state = state;
    }

    public PlayerPropsImpl(PlayerProps pProps) {
        state = pProps.getState();
        radiation = pProps.getRadiation();
        health = pProps.getHealth();
        boosterPercents = pProps.getBoosterPercents();
        devicePercents = pProps.getDevicePercents();
        armorPercents = pProps.getArmorPercents();
        xpPoints = pProps.getXpPoints();
        hasHealthInstant = pProps.hasHealthInstant();
        hasRadiationInstant = pProps.hasRadiationInstant();
        hasDetectorAccess = pProps.hasDetectorAccess();
        fraction = pProps.getFraction();
    }

    public PlayerPropsImpl(Jsonable jsonable) {
        if (jsonable != null) {
            JsonObject o = jsonable.toJson();
            health = o.has("health") ? o.get("health").getAsDouble() : 100;
            radiation = o.has("radiation") ? o.get("radiation").getAsDouble() : 0;
            xpPoints = o.has("xpPoints") ? o.get("xpPoints").getAsInt() : 0;
            hasHealthInstant = o.has("hasHealthInstant") && o.get("hasHealthInstant").getAsBoolean();
            hasRadiationInstant = o.has("hasRadiationInstant") && o.get("hasRadiationInstant").getAsBoolean();
            hasDetectorAccess = o.has("hasDetectorAccess") && o.get("hasDetectorAccess").getAsBoolean();
            state = o.has("state") ? Player.STATE.fromString(o.get("state").getAsString()) : Player.STATE.ALIVE;
            fraction = o.has("fraction") ? Player.FRACTION.fromString(o.get("fraction").getAsString()) : Player.FRACTION.STALKER;
        }
    }

    @Override
    public double getHealth() {
        return health;
        //Nemirtingumas testavimo tikslams
        //return Math.max(health, 1);
    }

    public void setHealth(double health) {
        if (health > 100) {
            health = 100;
        } else if (health < 0) {
            health = 0;
        }
        Log.d(TAG, "setHealth: " + health);
        this.health = health;
    }

    @Override
    public double getRadiation() {
        return radiation;
    }

    public void setRadiation(double radiation) {
        //Log.d(TAG, "setRadiation: " + radiation);
        if (radiation > 16) {
            radiation = 16;
        } else if (radiation < 0) {
            radiation = 0;
        }
        this.radiation = radiation;
    }

    @Override
    public double getArtefactImpact() {
        if (impacts != null && impacts.length > Influence.ARTEFACT) {
            return impacts[Influence.ARTEFACT];
        }
        return 0;
    }

    @Override
    public long getController() {
        return 0;
    }

    @Override
    public long getZombified() {
        return 0;
    }

    @Override
    public double getMental() {
        return 0;
    }

    @Override
    public double getRadiationImpact() {
        if (impacts != null && impacts.length >= Influence.RADIATION) {
            return impacts[Influence.RADIATION];
        }
        return 0;
    }

    public void setRadiationImpact(double radiationImpact) {
        this.radiationImpact = radiationImpact;
    }

    @Override
    public double getHealthImpact() {
        if (impacts != null) {
            if (fraction == Player.FRACTION.MONOLITH && impacts.length >= Influence.MONOLITH) {
                return impacts[Influence.MONOLITH];
            }
            if (fraction == Player.FRACTION.DARKEN && impacts.length >= Influence.RADIATION) {
                return impacts[Influence.RADIATION];
            } else if (impacts.length >= Influence.HEALTH) {
                return impacts[Influence.HEALTH];
            }
        }
        return 0d;
    }

    @Override
    public double getControllerImpact() {
        if (impacts != null && impacts.length >= Influence.CONTROLLER) {
            return impacts[Influence.CONTROLLER];
        }
        return 0;
    }

    @Override
    public double getBurerImpact() {
        if (impacts != null && impacts.length >= Influence.BURER) {
            return impacts[Influence.BURER];
        }
        return 0;
    }

    @Override
    public double getMentalImpact() {
        if (impacts != null && impacts.length >= Influence.MENTAL) {
            return impacts[Influence.MENTAL];
        }
        return 0;
    }

    @Override
    public double getMonolithImpact() {
        if (impacts != null && impacts.length >= Influence.MONOLITH) {
            return impacts[Influence.MONOLITH];
        }
        return 0;
    }

    @Override
    public double getAnomalyImpact() {
        if (impacts != null && impacts.length >= Influence.FRUITPUNCH) {
            return max(impacts[Influence.ANOMALY], 
                      impacts[Influence.FRUITPUNCH]);
        }
        return 0;
    }

    @Override
    public double getExplosionImpact() {
        if (impacts != null && impacts.length >= Influence.EXPLOSION) {
            return impacts[Influence.EXPLOSION];
        }
        return 0;
    }

    @Override
    public double getBoosterPercents() {
        //return 100;
        return boosterPercents;
    }

    @Override // Указывает, что этот метод переопределяет метод из родительского класса/интерфейса
    public void setBoosterPercents(double percents) {
        boosterPercents = normalize(percents);
    }

    @Override
    public double getDevicePercents() {
        //return 100;
        return devicePercents;
    }

    @Override
    public void setDevicePercents(double percents) {
        devicePercents = normalize(percents);
    }

    @Override
    public double getArmorPercents() {
        //return 100;
        return armorPercents;
    }

    @Override
    public void setArmorPercents(double percents) {
        armorPercents = normalize(percents);
    }

    @Override
    public void addHealth(double health) {
        setHealth(this.health + health);
    }

    @Override
    public void addRadiation(double radiation) {
        setRadiation(this.radiation + radiation);
    }

    @Override
    public boolean addXpPoints(int xp) {
        int oldLevel = getLevel();
        xpPoints += xp;
        return oldLevel != getLevel();
    }

    @Override
    public int getXpPoints() {
        return xpPoints;
    }

    @Override
    public void setXpPoints(int xp) {
        xpPoints = xp;
    }

    @Override
    public int getLevel() {
        return calcLevel(xpPoints);
    }

    @Override
    public void subtractHealth(double health) {
        setHealth(this.health - health);
    }

    @Override
    public void subtractRadiation(double radiation) {
        setRadiation(this.radiation - radiation);
    }

    @Override
    public Player.STATE getState() {
        return state;
    }

    @Override
    public void setState(Player.STATE state) {
        this.state = state;
    }

    private int calcLevel(int xp) {
        //Log.d(TAG, "calcLevel: " + xp + " --- " + xp / 50);
        return Math.min(1 + (xp / 50), 5);
    }

    public void setAnomalyHit(boolean anomalyHit) {
        this.anomalyHit = anomalyHit;
    }

    public void setFruitPunchHit(boolean fruitPunchHit) {
        this.fruitPunchHit = fruitPunchHit;
    }

    public void setStrongExplosionHit(boolean hit) {
        strongExplosionHit = hit;
    }

    public void setWeakExplosionHit(boolean hit) {
        weakExplosionHit = hit;
    }

    private double normalize(double number) {
        if (number > 100) {
            return 100;
        } else if (number < 0) {
            return 0;
        }
        return number;
    }

    public void setBurerHit(boolean hit) {
        burerHit = hit;
    }

    public void setControllerHit(boolean hit) {
        controllerHit = hit;
    }

    public void setMentalHit(boolean hit) {
        mentalHit = hit;
    }

    public void setMonolithHit(boolean hit) {
        monolithHit = hit;
    }

    public void setEmissionHit(boolean hit) {
        emissionHit = true;
    }

    @Override
    public boolean burerHit() {
        return burerHit;
    }

    @Override
    public boolean strongExplosionHit() {
        return strongExplosionHit;
    }

    @Override
    public boolean weakExplosionHit() {
        return weakExplosionHit;
    }

    @Override
    public boolean controllerHit() {
        return controllerHit;
    }

    @Override
    public boolean anomalyHit() {
        return anomalyHit;
    }

    @Override
    public boolean fruitPunchHit() {
        return fruitPunchHit;
    }

    @Override
    public boolean mentalHit() {
        return mentalHit;
    }

    @Override
    public boolean monolithHit() {
        return monolithHit;
    }

    @Override
    public boolean emissionHit() {
        return emissionHit;
    }

    public void setHasHealthInstant(boolean hasHealthInstant) {
        this.hasHealthInstant = hasHealthInstant;
    }

    public void setHasRadiationInstant(boolean hasRadiationInstant) {
        this.hasRadiationInstant = hasRadiationInstant;
    }

    @Override
    public boolean hasHealthInstant() {
        return hasHealthInstant;
    }

    @Override
    public boolean hasRadiationInstant() {
        return hasRadiationInstant;
    }

    @Override
    public boolean setFraction(Player.FRACTION fraction) {
        if (this.fraction == fraction) {
            return false;
        }
        this.fraction = fraction;
        return true;
    }

    @Override
    public Player.FRACTION getFraction() {
        return fraction;
    }

    @Override
    public int getLevelXp() {
        return 100 / LEVEL_XP * (xpPoints % LEVEL_XP);
    }

    @NonNull
    @Override
    public String toString() {
        String str = "Player props:\n";
        str += "Radiation: " + radiation + ",\n";
        str += "RadiationImpact: " + radiationImpact + ",\n";
        str += "Health: " + health + ",\n";
        str += "State: " + state.toString();
        return str;
    }

    @Override
    public double[] getImpacts() {
        return impacts;
//        int max = 0;
//        int index = -1;
//        for(int i = 0; i < impacts.length; i++)
//        {
//            if(impacts[i] > max)
//            {
//                index = i;
//            }
//        }
//        return index;
    }

    public void setImpacts(double[] influences) {
        impacts = influences;
    }

    @Override
    public boolean hasDetectorAccess() {
        return hasDetectorAccess;
    }

    @Override
    public void setDetectorAccess(boolean hasAccess) {
        this.hasDetectorAccess = hasAccess;
    }

    @Override
    public JsonObject toJson() {
        o = new JsonObject();
        o.addProperty("health", health);
        o.addProperty("radiation", radiation);
        o.addProperty("xpPoints", xpPoints);
        o.addProperty("hasHealthInstant", hasHealthInstant);
        o.addProperty("hasRadiationInstant", hasRadiationInstant);
        o.addProperty("hasDetectorAccess", hasDetectorAccess);
        if (fraction != null) {
            o.addProperty("fraction", fraction.toString());
        }
        o.addProperty("state", state.toString());
        return o;
    }
}