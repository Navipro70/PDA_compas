package net.afterday.compas.core.influences;

import net.afterday.compas.core.events.EventsPack;

/**
 * Created by Justas Spakauskas on 2/7/2018.
 */

public interface InfluencesPack extends EventsPack {
    static final int WIFI = 0;
    static final int BLUETOOTH = 1;

    boolean influencedBy(int influenceType);

    boolean inDanger();

    boolean isClear();

    long creationTime();

    double[] getInfluences();

    double getInfluence(int inflType);

    void addInfluence(int inflType, double strength);

    boolean isEmission();

    void setEmission(boolean emission);

    int getSource();
}
