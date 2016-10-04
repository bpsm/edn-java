// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.bpsm.edn.EdnSyntaxException;


public class InstantUtils {

    private static final Pattern INSTANT = Pattern.compile(
            "(\\d\\d\\d\\d)(?:-(\\d\\d)(?:-(\\d\\d)" +
                    "(?:[T](\\d\\d)(?::(\\d\\d)(?::(\\d\\d)(?:[.](\\d{1,9}))?)?)?)?)?)?" +
            "(?:[Z]|([-+])(\\d\\d):(\\d\\d))?");

    static ParsedInstant parse(String value) {
        Matcher m = INSTANT.matcher(value);
        if (!m.matches()) {
            throw new EdnSyntaxException("Can't parse " + "\"" + value + "\"");
        }

        final int years = Integer.parseInt(m.group(1));
        final int months = parseIntOrElse(m.group(2), 1);
        final int days = parseIntOrElse(m.group(3), 1);
        final int hours = parseIntOrElse(m.group(4), 0);
        final int minutes = parseIntOrElse(m.group(5), 0);
        final int seconds = parseIntOrElse(m.group(6), 0);
        final int nanoseconds = parseNanoseconds(m.group(7));
        final int offsetSign = parseOffsetSign(m.group(8));
        final int offsetHours = parseIntOrElse(m.group(9), 0);
        final int offsetMinutes = parseIntOrElse(m.group(10), 0);

        // extra-grammatical restrictions from RFC3339

        if (months < 1 || 12 < months) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid month in '%s'",
                            months, value));
        }
        if (days < 1 || daysInMonth(months, isLeapYear(years)) < days) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid day in '%s'",
                            days, value));
        }
        if (hours < 0 || 23 < hours) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid hour in '%s'",
                            hours, value));
        }
        if (minutes < 0 || 59 < minutes) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid minute in '%s'",
                            minutes, value));
        }
        if (seconds < 0 || (minutes == 59 ? 60 : 59) < seconds) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid second in '%s'",
                            seconds, value));
        }
        assert 0 <= nanoseconds && nanoseconds <= 999999999:
            "nanoseconds are assured to be in [0..999999999] by INSTANT Pattern";
        assert -1 <= offsetSign && offsetSign <= 1:
            "parser assuers offsetSign is -1, 0 or 1.";
        if (offsetHours < 0 || 23 < offsetHours) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid offset hour in '%s'",
                            offsetHours, value));
        }
        if (offsetMinutes < 0 || 59 < offsetMinutes) {
            throw new EdnSyntaxException(
                    String.format("'%02d' is not a valid offset minute in '%s'",
                            offsetMinutes, value));
        }

        return new ParsedInstant(years, months, days, hours, minutes, seconds,
                nanoseconds, offsetSign, offsetHours, offsetMinutes);
    }

    static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    static int daysInMonth(int month, boolean isLeapYear) {
        int i = (month - 1) + 12 * (isLeapYear ? 1 : 0);
        return DAYS_IN_MONTH[i];
    }

    private static final byte[] DAYS_IN_MONTH = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, // non-leap-year
        31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, // leap year
    };

    private static int parseOffsetSign(String s) {
        if (s == null) {
            return 0;
        } else {
            return "-".equals(s) ? -1 : 1;
        }
    }

    static int parseNanoseconds(String s) {
        if (s == null) {
            return 0;
        } else if (s.length() < 9) {
            return Integer.parseInt(s + "000000000".substring(s.length()));
        } else {
            return Integer.parseInt(s);
        }
    }

    private static int parseIntOrElse(String s, int alternative) {
        if (s == null) {
            return alternative;
        }
        return Integer.parseInt(s);
    }


    static Timestamp makeTimestamp(ParsedInstant pi) {
        GregorianCalendar c = makeCalendar(pi);
        Timestamp ts = new Timestamp((c.getTimeInMillis() / 1000L) * 1000L);
        ts.setNanos(pi.nanoseconds);
        return ts;
    }

    static Date makeDate(ParsedInstant pi) {
        return makeCalendar(pi).getTime();
    }

    static GregorianCalendar makeCalendar(ParsedInstant pi) {
        final TimeZone tz = getTimeZone(pi.offsetSign, pi.offsetHours, pi.offsetMinutes);
        final GregorianCalendar cal = new GregorianCalendar(tz);
        cal.set(GregorianCalendar.YEAR, pi.years);
        cal.set(GregorianCalendar.MONTH, pi.months - 1);
        cal.set(GregorianCalendar.DAY_OF_MONTH, pi.days);
        cal.set(GregorianCalendar.HOUR_OF_DAY, pi.hours);
        cal.set(GregorianCalendar.MINUTE, pi.minutes);
        cal.set(GregorianCalendar.SECOND, pi.seconds);
        int millis = pi.nanoseconds / NANOSECS_PER_MILLISEC;
        cal.set(GregorianCalendar.MILLISECOND, millis);
        return cal;
    }

    private static final int TZ_LIMIT = 23;

    private static final TimeZone[] TZ_CACHE;
    static {
        TimeZone[] tzs = new TimeZone[TZ_LIMIT*2+1];
        for (int h = -TZ_LIMIT; h <= TZ_LIMIT; h++) {
            tzs[h+TZ_LIMIT] = TimeZone.getTimeZone(String.format("GMT%+03d:00", h));
        }
        TZ_CACHE = tzs;
    }

    private static TimeZone getTimeZone(int offsetSign, int offsetHours, int offsetMinutes) {
        if (offsetMinutes == 0 && offsetHours <= TZ_LIMIT) {
            int i = offsetHours * (offsetSign < 0 ? -1 : 1) + TZ_LIMIT;
            return TZ_CACHE[i];
        }
        final String tzID = String.format("GMT%s%02d:%02d",
                (offsetSign > 0 ? "+" : "-"),
                offsetHours, offsetMinutes);
        TimeZone tz = TimeZone.getTimeZone(tzID);
        return tz;
    }

    private static final int NANOSECS_PER_MILLISEC = 1000000;

    /**
     * Return a String suitable for use as an edn {@code #inst}, given
     * a {@link GregorianCalendar}.
     * @param cal must not be null.
     * @return an RFC3339 compatible string.
     */
    public static String calendarToString(GregorianCalendar cal) {
        String s = String.format("%1$tFT%1$tT.%1$tL%1$tz", cal);
        /* s is almost right, but is missing the colon in the offset */
        assert Pattern.matches(".*[-+][0-9]{4}$", s);
        int n = s.length();
        return s.substring(0, n-2) + ":" + s.substring(n-2);
    }

    /**
     * Return a String suitable for use as an edn {@code #inst}, given
     * a {@link Date}.
     * @param date must not be null.
     * @return an RFC3339 compatible string.
     */
    public static String dateToString(Date date) {
        GregorianCalendar c = new GregorianCalendar(GMT);
        c.setTime(date);
        String s = calendarToString(c);
        assert s.endsWith("+00:00");
        return s.substring(0, s.length() - 6) + "-00:00";
    }

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


    /**
     * Return a String suitable for use as an edn {@code #inst}, given
     * a {@link Timestamp}.
     * @param ts must not be null.
     * @return an RFC3339 compatible string.
     */
    public static String timestampToString(Timestamp ts) {
        return TIMESTAMP_FORMAT.get().format(ts)
                + String.format(".%09d-00:00", ts.getNanos());
    }

    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMAT =
            new ThreadLocal<SimpleDateFormat>() {
        @Override
		protected SimpleDateFormat initialValue() {
            SimpleDateFormat f = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss");
            f.setTimeZone(GMT);
            return f;
        }
    };

}

/*
 * Notes
 *
 * RFC3339 says to use -00:00 when the timezone is unknown (+00:00
 * implies a known GMT)
 */
