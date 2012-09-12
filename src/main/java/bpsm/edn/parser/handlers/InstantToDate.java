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

public class InstantToDate extends AbstractInstantHandler {

    @Override
    protected Object transform(int years, int months, int days, int hours,
            int minutes, int seconds, int nanoseconds, int offsetSign,
            int offsetHours, int offsetMinutes) {
        GregorianCalendar cal = new GregorianCalendar(
                years, months - 1, days, hours, minutes, seconds);
        cal.set(Calendar.MILLISECOND, nanoseconds/1000000);
        cal.setTimeZone(TimeZone.getTimeZone(String.format(
                "GMT%s%02d:%02d", offsetSign < 0 ? "-" : "+",
                        offsetHours, offsetMinutes)));
        return cal.getTime();
    }

}
