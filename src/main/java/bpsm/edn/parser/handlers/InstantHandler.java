// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.handlers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bpsm.edn.model.Tag;
import bpsm.edn.parser.EdnException;
import bpsm.edn.parser.TagHandler;

public class InstantHandler implements TagHandler {

    private static final Pattern INSTANT = Pattern.compile(
            "(\\d\\d\\d\\d)(?:-(\\d\\d)(?:-(\\d\\d)(?:[T](\\d\\d)(?::(\\d\\d)"+
            "(?::(\\d\\d)(?:[.](\\d+))?)?)?)?)?)?"+
            "(?:[Z]|([-+])(\\d\\d):(\\d\\d))?");
    
    public Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
            throw new EdnException(tag.toString() + " expects a String.");
        }
        Matcher m = INSTANT.matcher((String)value);
        if (!m.matches()) {
            throw new EdnException("Can't parse " + tag.toString() + "\"" + value + "\"");
        }
        
        int years = parseIntOrElse(m.group(1), Integer.MIN_VALUE);
        int months = parseIntOrElse(m.group(2), 1);
        int days = parseIntOrElse(m.group(3), 1);
        int hours = parseIntOrElse(m.group(4), 0);
        int minutes = parseIntOrElse(m.group(5), 0);
        int seconds = parseIntOrElse(m.group(6), 0);
        int nanoseconds;
        if (m.group(7) == null) {
            nanoseconds = 0;
        } else {
            String s = m.group(7);
            s = s + "000000000".substring(s.length());
            nanoseconds = Integer.parseInt(s);
        }
        int offsetSign;
        if (m.group(8) == null) {
            offsetSign = 0;
        } else {
            offsetSign = "-".equals(m.group(8)) ? -1 : 1;
        }
        int offsetHours = parseIntOrElse(m.group(9), 0);
        int offsetMinutes = parseIntOrElse(m.group(10), 0);

        GregorianCalendar cal = new GregorianCalendar(
                years, months - 1, days, hours, minutes, seconds);
        cal.set(Calendar.MILLISECOND, nanoseconds/1000000);
        cal.setTimeZone(TimeZone.getTimeZone(String.format(
                "GMT%s%02d:%02d", offsetSign < 0 ? "-" : "+",
                        offsetHours, offsetMinutes)));
        return cal.getTime();
    }
    
    private int parseIntOrElse(String s, int alternative) {
        if (s == null) {
            return alternative;
        }
        return Integer.parseInt(s);
    }

}
