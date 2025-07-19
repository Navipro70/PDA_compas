package net.afterday.compas.engine.influences;

/**
 * Created by spaka on 4/2/2018.
 */

public interface InfluenceExtractionStrategy<T, I> {
    I makeInfluences(T i);
}
