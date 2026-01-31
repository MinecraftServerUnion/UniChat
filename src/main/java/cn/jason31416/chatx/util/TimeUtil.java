package cn.jason31416.chatx.util;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static long convertToMillis(String time) throws NumberFormatException {
        long tot = 0;
        long curValue = 0;
        for(char i: time.toCharArray()){
            if(Character.isDigit(i)) curValue=(i-'0')+curValue*10;
            else {
                switch (Character.toLowerCase(i)) {
                    case 'w' -> tot += TimeUnit.DAYS.toMillis(curValue*7);
                    case 'd' -> tot += TimeUnit.DAYS.toMillis(curValue);
                    case 'h' -> tot += TimeUnit.HOURS.toMillis(curValue);
                    case 'm' -> tot += TimeUnit.MINUTES.toMillis(curValue);
                    case 's' -> tot += TimeUnit.SECONDS.toMillis(curValue);
                    default -> throw new NumberFormatException("Invalid time format!");
                }
                curValue = 0;
            }
        }
        return tot;
    }
    public static String displayMillis(long millis) {
        long weeks = TimeUnit.MILLISECONDS.toDays(millis)/7;
        long days = TimeUnit.MILLISECONDS.toDays(millis)%7;
        long hours = TimeUnit.MILLISECONDS.toHours(millis)%24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis)%60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)%60;

        StringBuilder sb = new StringBuilder();
        if (weeks > 0) {
            sb.append(weeks);
            sb.append('w');
        }
        if (days > 0) {
            sb.append(days);
            sb.append('d');
        }
        if (hours > 0 && weeks == 0) {
            sb.append(hours);
            sb.append('h');
        }
        if (minutes > 0 && weeks == 0 && days == 0) {
            sb.append(minutes);
            sb.append('m');
        }
        if(weeks == 0 && days == 0 && hours == 0) {
            sb.append(seconds);
            sb.append('s');
        }
        return sb.toString();
    }
}
