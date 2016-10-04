// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

/**
 * ParsedInstant contains the pared contents of a <a
 * href="http://www.ietf.org/rfc/rfc3339.txt">RFC3339</a> style time
 * stamp.
 *
 * @see AbstractInstantHandler
 */
public class ParsedInstant {

    /**
     * The year: -9999-9999.
     */
    public final int years;

    /**
     * Month of the year: 01-12. Defaults to 1 of missing.
     */
    public final int months;

    /**
     * Day of the month: 01-28, 01-29, 01-30, 01-31 based on
     * month/year. Defaults to 0 of missing.
     */
    public final int days;

    /**
     * Hours of the day: 00-23. Defaults to 0 of missing.
     */
    public final int hours;

    /**
     * Minutes of the hour: 00-59. Defaults to 0 of missing.
     */
    public final int minutes;

    /**
     * Seconds of the minute: 00-58, 00-59, 00-60 based on leap second
     * rules. Defaults to 0 of missing.
     */
    public final int seconds;

    /**
     * Nanoseconds of the second: 0-999999999. Defaults to 0 if missing.
     */
    public final int nanoseconds;

    /**
     * The sign of the timezone offset as follows:
     * <pre>{@code
     * -1 for the sign "-"
     * +1 for the sign "+"
     *  0 when non-numeric offset "Z" is given
     * }</pre>
     * Defaults to 0 if missing.
     */
    public final int offsetSign;

    /**
     * The whole hour component of the time zone offset: [0-23]. If
     * the offset is given as "Z", this will be 0. Defaults to 0 if
     * missing.
     */
    public final int offsetHours;

    /**
     * The minute component of the time zone offset: [0-59]. If the
     * offset is given as "Z", this will be 0. Defaults to 0 if
     * missing.
     */
    public final int offsetMinutes;

    public ParsedInstant(int years, int months, int days, int hours,
            int minutes, int seconds, int nanoseconds, int offsetSign,
            int offsetHours, int offsetMinutes) {
        super();
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.nanoseconds = nanoseconds;
        this.offsetSign = offsetSign;
        this.offsetHours = offsetHours;
        this.offsetMinutes = offsetMinutes;
    }

    @Override
	public String toString() {
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d%s%02d:%02d",
                years, months, days, hours, minutes, seconds, nanoseconds,
                offsetSign > 0 ? "+" : "-", offsetHours, offsetMinutes);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + days;
        result = prime * result + hours;
        result = prime * result + minutes;
        result = prime * result + months;
        result = prime * result + nanoseconds;
        result = prime * result + offsetHours;
        result = prime * result + offsetMinutes;
        result = prime * result + offsetSign;
        result = prime * result + seconds;
        result = prime * result + years;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ParsedInstant other = (ParsedInstant) obj;
        if (days != other.days) {
            return false;
        }
        if (hours != other.hours) {
            return false;
        }
        if (minutes != other.minutes) {
            return false;
        }
        if (months != other.months) {
            return false;
        }
        if (nanoseconds != other.nanoseconds) {
            return false;
        }
        if (offsetHours != other.offsetHours) {
            return false;
        }
        if (offsetMinutes != other.offsetMinutes) {
            return false;
        }
        if (offsetSign != other.offsetSign) {
            return false;
        }
        if (seconds != other.seconds) {
            return false;
        }
        if (years != other.years) {
            return false;
        }
        return true;
    }

}
