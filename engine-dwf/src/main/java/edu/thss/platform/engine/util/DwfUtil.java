package edu.thss.platform.engine.util;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("DwfUtil")
public class DwfUtil {
    private static Logger log = LoggerFactory.getLogger(DwfUtil.class);

    public DateTime timestampConvert(String timeStamp) {

        DateTime dateTime = new DateTime(Long.valueOf(timeStamp));
        log.info("timestampConvert------------" + dateTime);
        return dateTime;
    }

    /*public String countDown(String timeStamp, int minutes){
        DateTime dateTime = new DateTime(Long.valueOf(timeStamp));
        dateTime = dateTime.minusMinutes(minutes);
        String ret = dateTime.toString();
        ret = ret.substring(0, ret.lastIndexOf("."));

        log.info("timestampConvert:------------"+ret);

        return ret;
    }*/

    public String countDown(String timeStamp, int... args) {
        DateTime dateTime = new DateTime(Long.valueOf(timeStamp));
        if (args.length > 0) {
            dateTime = dateTime.minusDays(args[0]);
            System.out.println("args[0]" + dateTime);
        }
        if (args.length > 1) {
            dateTime = dateTime.minusHours(args[1]);
            System.out.println("args[1]" + dateTime);
        }
        if (args.length > 2) {
            dateTime = dateTime.minusMinutes(args[2]);
            System.out.println("args[2]" + dateTime);
        }
        String ret = dateTime.toString();
        ret = ret.substring(0, ret.lastIndexOf("."));

        log.info("timestampConvert:------------" + ret);

        return ret;
    }

}
