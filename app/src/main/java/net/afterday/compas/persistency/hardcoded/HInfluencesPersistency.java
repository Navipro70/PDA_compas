package net.afterday.compas.persistency.hardcoded;

import net.afterday.compas.core.influences.Emission;
import net.afterday.compas.core.influences.Influence;
import net.afterday.compas.persistency.influences.InfluencesPersistency;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Justas Spakauskas on 2/3/2018.
 */

public class HInfluencesPersistency implements InfluencesPersistency
{
    @Override
    public List<Influence> getPossibleInfluences()
    {
        return null;
    }

    @Override
    public List<String> getRegisteredWifiModules()
    {
        List<String> wifiInfls = new ArrayList<>();

        wifiInfls.add("86:0d:7e:ab:9d:09");
        wifiInfls.add("86:0d:7e:ab:98:d4");
        wifiInfls.add("86:0d:8e:ab:9c:74");
        wifiInfls.add("86:0d:8e:ab:98:32"); 
        wifiInfls.add("86:0d:8e:ab:97:6b");
        wifiInfls.add("86:0d:8e:ab:99:a4"); 
        wifiInfls.add("86:0d:8e:ab:9d:c2"); 
        wifiInfls.add("86:0d:8e:ab:9c:2f"); 
        wifiInfls.add("86:0d:8e:ab:98:64"); 
        wifiInfls.add("86:0d:8e:ab:9c:23"); 
        wifiInfls.add("be:dd:c2:23:49:ae"); 
        wifiInfls.add("ce:50:e3:37:c0:3a"); 
        wifiInfls.add("ce:50:e3:37:bd:7d");
        wifiInfls.add("ee:fa:bc:6f:fe:bb");
        wifiInfls.add("be:dd:c2:8d:f5:ce"); 
        wifiInfls.add("be:dd:c2:5d:f2:cf"); 
        wifiInfls.add("ce:50:e3:32:bf:b5"); 
        wifiInfls.add("be:dd:c4:9d:e6:a0"); 
      
        return wifiInfls;
    }

    @Override
    public List<Emission> getEmissions()
    {
        List<Emission> emissions = new ArrayList<Emission>();

        // Emissions
        emissions.add(emission(at(4, 10, 15, 00), 3, 20));
        emissions.add(emission(at(4, 10, 16, 30), 5, 1, true));
        emissions.add(emission(at(4, 10, 18, 40), 2, 30));
        emissions.add(emission(at(4, 10, 21, 35), 5, 15));
        emissions.add(emission(at(4, 10, 23, 50), 7, 1, true));
        emissions.add(emission(at(4, 11, 3, 10), 5, 1, true));
        emissions.add(emission(at(4, 11, 7, 15), 5, 30));
        emissions.add(emission(at(4, 11, 10, 30), 10, 20));

        return emissions;
    }

    private Emission emission(Calendar startAt, int notifyBefore, int duration, boolean isFake)
    {
        return new Emission()
        {
            @Override
            public Calendar getStartTime()
            {
                return startAt;
            }

            @Override
            public int notifyBefore()
            {
                return notifyBefore;
            }

            @Override
            public int duration()
            {
                return duration;
            }

            @Override
            public boolean isFake()
            {
                return isFake;
            }
        };
    }

    private Emission emission(Calendar startAt, int notifyBefore, int duration)
    {
        return emission(startAt, notifyBefore, duration, false);
    }

//    private Emission emission(int afterMins, int notifyBefore, int duration)
//    {
//        return new Emission()
//        {
//            @Override
//            public Calendar getStartTime()
//            {
//                return afterMins(afterMins);
//            }
//
//            @Override
//            public int notifyBefore()
//            {
//                return notifyBefore;
//            }
//
//            @Override
//            public int duration()
//            {
//                return duration;
//            }
//
//            @Override
//            public boolean isFake()
//            {
//
//            }
//        };
//    }

    private Calendar afterMins(int mins)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + mins);
        return c;
    }

    private Calendar at(int month, int day, int hour, int min)
    {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, 0);
        return c;
    }
}
