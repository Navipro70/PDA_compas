package net.afterday.compas.persistency.hardcoded;

import net.afterday.compas.core.influences.Emission;
import net.afterday.compas.core.influences.Influence;
import net.afterday.compas.persistency.influences.InfluencesPersistency;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

        wifiInfls.add("86:0d:8e:ab:9d:09");
        wifiInfls.add("86:0d:8e:ab:98:d4");
        wifiInfls.add("86:0d:8e:ab:9c:44");
        wifiInfls.add("86:0d:8e:ab:98:12"); 
        wifiInfls.add("86:0d:8e:ab:97:7b");
        wifiInfls.add("86:0d:8e:ab:99:a9"); 
        wifiInfls.add("86:0d:8e:ab:9d:c7"); 
        wifiInfls.add("86:0d:8e:ab:9c:3f"); 
        wifiInfls.add("86:0d:8e:ab:98:54"); 
        wifiInfls.add("86:0d:8e:ab:9c:13"); 
        wifiInfls.add("be:dd:c2:23:69:ae"); 
        wifiInfls.add("ce:50:e3:37:c0:1a"); 
        wifiInfls.add("ce:50:e3:37:bd:2d");
        wifiInfls.add("ee:fa:bc:2f:fe:bb");
        wifiInfls.add("be:dd:c2:9d:f5:ce"); 
        wifiInfls.add("be:dd:c2:9d:f2:cf"); 
        wifiInfls.add("ce:50:e3:37:bf:b5"); 
        wifiInfls.add("be:dd:c2:9d:e6:a0"); 
        wifiInfls.add("ce:50:e3:55:8f:81"); 
        wifiInfls.add("b6:e6:2d:44:ad:ef"); 
        wifiInfls.add("82:7d:3a:3e:d7:3f"); 
        wifiInfls.add("86:0d:8e:88:c0:fd"); 
        wifiInfls.add("ce:50:e3:0c:82:f3"); 
        wifiInfls.add("82:7d:3a:3e:d7:a8"); 
        wifiInfls.add("ce:50:e3:55:8e:a3"); 
        wifiInfls.add("ce:50:e3:55:8e:0a"); 
        wifiInfls.add("ce:50:e3:37:bc:33"); 
        wifiInfls.add("ce:50:e3:37:bb:2f"); 
        wifiInfls.add("be:dd:c2:9d:f4:b7"); 
        wifiInfls.add("ce:50:e3:55:8d:d8"); 
        wifiInfls.add("86:0d:8e:ab:99:7d"); 
        wifiInfls.add("86:0d:8e:ab:9b:95"); 
        wifiInfls.add("be:dd:c2:9d:65:55"); 
        wifiInfls.add("ee:fa:bc:30:45:17"); 
        wifiInfls.add("be:dd:c2:9d:ed:95"); 
        wifiInfls.add("86:0d:8e:ab:9b:32"); 
        wifiInfls.add("86:0d:8e:ab:99:e0"); 
        wifiInfls.add("86:0d:8e:ab:98:2a"); 
        wifiInfls.add("be:dd:c2:9d:ea:fd"); 
        wifiInfls.add("86:0d:8e:ab:98:5d"); 
        wifiInfls.add("86:0d:8e:ab:9b:36");
        wifiInfls.add("86:0d:8e:ab:9c:75");
        wifiInfls.add("be:dd:c2:9d:f5:06");
        wifiInfls.add("86:0d:8e:ab:9c:46"); 
        wifiInfls.add("86:0d:8e:ab:9b:55");
        wifiInfls.add("ce:50:e3:37:bc:75");
        wifiInfls.add("86:0d:8e:ab:9c:2d");
        wifiInfls.add("ce:50:e3:37:bc:e2");
        wifiInfls.add("86:0d:8e:ab:96:76"); 
        wifiInfls.add("86:0d:8e:ab:9d:fc"); 
        wifiInfls.add("86:0d:8e:ab:9d:a8"); 
        wifiInfls.add("86:0d:8e:ab:9a:80"); 
        wifiInfls.add("be:dd:c2:9d:f2:88"); 
        wifiInfls.add("86:0d:8e:ab:99:b8"); 
        wifiInfls.add("86:0d:8e:ab:9d:e2");

        return wifiInfls;
    }

    @Override
    public List<Emission> getEmissions()
    {
        List<Emission> emissions = new ArrayList<Emission>();

        emissions.add(emission(at(9, 18, 15, 0), 10, 15)); 
        emissions.add(emission(at(9, 19, 21, 0), 10, 15));
        emissions.add(emission(at(9, 20, 8, 0), 5, 15, true));  

        emissions.add(emission(at(9, 20, 15, 0), 10, 15));        
        emissions.add(emission(at(9, 20, 21, 0), 10, 20)); 
        emissions.add(emission(at(9, 20, 23, 0), 5, 15, true));  
        emissions.add(emission(at(9, 21, 2, 0), 10, 20));     
        emissions.add(emission(at(9, 21, 4, 0), 5, 15, true)); 
        emissions.add(emission(at(9, 21, 6, 0), 5, 15));
        emissions.add(emission(at(9, 21, 9, 0), 10, 20)); 
        emissions.add(emission(at(9, 21, 12, 0), 5, 15, true));
        emissions.add(emission(at(9, 21, 14, 30), 10, 15)); 
        emissions.add(emission(at(9, 21, 16, 45), 10, 20, true));
        emissions.add(emission(at(9, 21, 18, 15), 5, 15));
        emissions.add(emission(at(9, 21, 21, 00), 20, 60));

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
