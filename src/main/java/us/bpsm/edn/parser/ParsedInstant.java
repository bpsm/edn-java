// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

public class ParsedInstant {
    public final int years;
    public final int months;
    public final int days;
    public final int hours;
    public final int minutes;
    public final int seconds;
    public final int nanoseconds;
    public final int offsetSign;
    public final int offsetHours;
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
