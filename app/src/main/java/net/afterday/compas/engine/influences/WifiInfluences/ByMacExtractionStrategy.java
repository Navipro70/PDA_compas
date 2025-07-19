package net.afterday.compas.engine.influences.WifiInfluences;

import android.net.wifi.ScanResult;

import net.afterday.compas.core.influences.InfluencesPack;
import net.afterday.compas.engine.influences.InfluenceExtractionStrategy;

import java.util.List;

/**
 * Created by spaka on 4/3/2018.
 */

public class ByMacExtractionStrategy extends AbstractWifiExtractor implements InfluenceExtractionStrategy<List<ScanResult>, InfluencesPack> {
    private List<String> modules;

    public ByMacExtractionStrategy(List<String> modules) {
        this.modules = modules;
    }

    @Override
    public InfluencesPack makeInfluences(List<ScanResult> i) {
        return extract(i);
    }

    @Override
    boolean isValid(ScanResult scanResult) {
        return modules.contains(scanResult.BSSID);
    }
}
