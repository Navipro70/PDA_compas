package net.afterday.compas.core.player;

import androidx.annotation.NonNull;

import net.afterday.compas.core.events.PlayerEventsListener;
import net.afterday.compas.core.gameState.Frame;
import net.afterday.compas.core.inventory.Inventory;
import net.afterday.compas.core.inventory.items.Item;
import net.afterday.compas.core.serialization.Jsonable;
import net.afterday.compas.persistency.items.ItemDescriptor;

/**
 * Created by spaka on 4/18/2018.
 */

public interface Player extends Jsonable {
    static final int ALIVE = 1;
    //static final int HEALING = 2;
    static final int NON_HUMAN = 2;
    static final int ZOMBIFIED = 2;
    static final int CONTROLLED = 3;
    static final int DEAD = 4;
    int INSTANT_DEATH = 1;
    int ABDUCTED = 2;
    int W_ABDUCTED = 3;
    int SUICIDE_NOT_ALLOWED = 4;

    long SEC30 = 30;
    long MIN1 = 60;
    long MIN2 = 120;
    long MIN3 = 180;
    long MIN5 = 300;
    long MIN7 = 420;
    long MIN15 = 900;
    long MIN30 = 1800;
    long MIN35 = 2100;
    long MIN60 = 3600;

    PlayerProps getPlayerProps();

    Inventory getInventory();

    boolean addItem(ItemDescriptor itemDescriptor, String code);

    boolean dropItem(Item item);

    Frame useItem(Item item);

    void addPlayerEventsListener(PlayerEventsListener playerEventsListener);

    Frame setState(STATE state);

    boolean setFraction(FRACTION fraction);

    boolean reborn();

    enum STATE {
        ALIVE(Player.ALIVE, Player.W_ABDUCTED),   //+
        DEAD_CONTROLLER(Player.DEAD),  //-
        DEAD_ANOMALY(Player.DEAD),  //-
        DEAD_RADIATION(Player.DEAD),  //-
        DEAD_EMISSION(Player.DEAD),  //+ Staigi mirtis
        DEAD_BURER(Player.DEAD),  //-
        DEAD_MENTAL(Player.DEAD),  //-
        CONTROLLED(Player.DEAD, Player.INSTANT_DEATH),  //+   Staigi mirtis
        MENTALLED(Player.DEAD, Player.INSTANT_DEATH),  //+   Staigi mirtis
        W_DEAD_EXPLOSION(Player.DEAD), // Агония от взрыва
        DEAD_EXPLOSION(Player.DEAD),   // Смерть от взрыва
        W_CONTROLLED(Player.DEAD), //-
        W_MENTALLED(Player.DEAD), //-
        W_DEAD_BURER(Player.DEAD),  //-
        W_DEAD_RADIATION(Player.DEAD),  //-
        W_DEAD_ANOMALY(Player.DEAD),
        W_DEAD_FRUITPUNCH(Player.DEAD), //-
        W_ABDUCTED(Player.DEAD, Player.ABDUCTED),  //+ Jei paspaudzia kolba, pereina i abducted
        ABDUCTED(Player.ALIVE, Player.INSTANT_DEATH);  //+ Staigi mirtis

        private final int code;
        private final int suicideType;

        STATE(int code, int suicideType) {
            this.code = code;
            this.suicideType = suicideType;
        }

        STATE(int code) {
            this.code = code;
            this.suicideType = Player.SUICIDE_NOT_ALLOWED;
        }

        public static STATE fromString(String s) {
            switch (s) {
                case "ALIVE":
                    return Player.STATE.ALIVE;
                case "W_DEAD_EXPLOSION":
                    return Player.STATE.W_DEAD_EXPLOSION;
                case "DEAD_EXPLOSION":
                    return Player.STATE.DEAD_EXPLOSION;
                case "DEAD_CONTROLLER":
                    return Player.STATE.DEAD_CONTROLLER;
                case "DEAD_ANOMALY":
                    return Player.STATE.DEAD_ANOMALY;
                case "DEAD_RADIATION":
                    return Player.STATE.DEAD_RADIATION;
                case "DEAD_EMISSION":
                    return STATE.DEAD_EMISSION;
                case "DEAD_BURER":
                    return Player.STATE.DEAD_BURER;
                case "DEAD_MENTAL":
                    return Player.STATE.DEAD_MENTAL;
                case "CONTROLLED":
                    return Player.STATE.CONTROLLED;
                case "MENTALLED":
                    return Player.STATE.MENTALLED;
                case "W_MENTALLED":
                    return Player.STATE.W_MENTALLED;
                case "W_CONTROLLED":
                    return Player.STATE.W_CONTROLLED;
                case "W_DEAD_BURER":
                    return Player.STATE.W_DEAD_BURER;
                case "W_DEAD_RADIATION":
                    return Player.STATE.W_DEAD_RADIATION;
                case "W_DEAD_ANOMALY":
                    return Player.STATE.W_DEAD_ANOMALY;
                case "W_ABDUCTED":
                    return Player.STATE.W_ABDUCTED;
                case "ABDUCTED":
                    return Player.STATE.ABDUCTED;
                case "W_DEAD_FRUITPUNCH":
                    return Player.STATE.W_DEAD_FRUITPUNCH;
            }
            return Player.STATE.DEAD_BURER;
        }

        public int getCode() {
            return this.code;
        }

        public int getSuicideType() {
            return this.suicideType;
        }

        public long getWaitTime() {
            switch (this) {
                case ALIVE:
                    return 0l;
                case DEAD_CONTROLLER:
                    return MIN5;
                case DEAD_ANOMALY:
                    return MIN35;
                case DEAD_RADIATION:
                    return MIN35;
                case DEAD_BURER:
                    return MIN35;
                case DEAD_MENTAL:
                    return MIN5;
                case CONTROLLED:
                    return MIN30;
                case MENTALLED:
                    return MIN30;
                case W_MENTALLED:
                    return MIN1;
                case W_CONTROLLED:
                    return MIN1;
                case W_DEAD_BURER:
                    return MIN5;
                case W_DEAD_RADIATION:
                    return MIN5;
                case W_DEAD_ANOMALY:
                    return MIN5;
                case W_ABDUCTED:
                    return MIN5;
                case ABDUCTED:
                    return MIN60;
                case DEAD_EMISSION:
                    return MIN35;
                case W_DEAD_FRUITPUNCH:
                    return MIN5;
                case W_DEAD_EXPLOSION:
                    return MIN5;
                case DEAD_EXPLOSION:
                    return MIN35;
            }
            return MIN5;
        }

        @NonNull
        @Override
        public String toString() {
            switch (this) {
                case ALIVE:
                    return "ALIVE";
                case DEAD_CONTROLLER:
                    return "DEAD_CONTROLLER";
                case DEAD_ANOMALY:
                    return "DEAD_ANOMALY";
                case DEAD_RADIATION:
                    return "DEAD_RADIATION";
                case DEAD_BURER:
                    return "DEAD_BURER";
                case DEAD_MENTAL:
                    return "DEAD_MENTAL";
                case CONTROLLED:
                    return "CONTROLLED";
                case MENTALLED:
                    return "MENTALLED";
                case W_MENTALLED:
                    return "W_MENTALLED";
                case W_CONTROLLED:
                    return "W_CONTROLLED";
                case W_DEAD_BURER:
                    return "W_DEAD_BURER";
                case W_DEAD_RADIATION:
                    return "W_DEAD_RADIATION";
                case W_DEAD_ANOMALY:
                    return "W_DEAD_ANOMALY";
                case W_ABDUCTED:
                    return "W_ABDUCTED";
                case ABDUCTED:
                    return "ABDUCTED";
                case DEAD_EMISSION:
                    return "DEAD_EMISSION";
                case W_DEAD_FRUITPUNCH:
                    return "W_DEAD_FRUITPUNCH";
                case W_DEAD_EXPLOSION:
                    return "W_DEAD_EXPLOSION";
                case DEAD_EXPLOSION:
                    return "DEAD_EXPLOSION";
            }
            return "Unknown player state!";
        }
    }

    enum FRACTION {
        STALKER(0),
        //Monolito neveikia mentalas, ir emisija, monolitas gydo. Yra vaizdas, bet nera garso
        MONOLITH(1),
        GAMEMASTER(2),
        DARKEN(3),
        MISSION(4);

        FRACTION(int id) {

        }

        public static FRACTION fromString(String fraction) {
            switch (fraction) {
                case "STALKER":
                    return STALKER;
                case "MONOLITH":
                    return MONOLITH;
                case "GAMEMASTER":
                    return GAMEMASTER;
                case "DARKEN":
                    return DARKEN;
                case "MISSION":
                    return MISSION;
            }
            return null;
        }

        @NonNull
        @Override
        public String toString() {
            switch (this) {
                case STALKER:
                    return "STALKER";
                case MONOLITH:
                    return "MONOLITH";
                case GAMEMASTER:
                    return "GAMEMASTER";
                case DARKEN:
                    return "DARKEN";
                case MISSION:
                    return "MISSION";
            }
            return "UnknownFraction";
        }
    }

    enum COMMAND {
        REVIVE,
        KILL,
        ACTIVATE_DETECTOR
    }
}
