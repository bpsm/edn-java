// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static us.bpsm.edn.parser.InstantUtils.calendarToString;
import static us.bpsm.edn.parser.InstantUtils.dateToString;
import static us.bpsm.edn.parser.InstantUtils.makeCalendar;
import static us.bpsm.edn.parser.InstantUtils.makeDate;
import static us.bpsm.edn.parser.InstantUtils.makeTimestamp;
import static us.bpsm.edn.parser.InstantUtils.parse;
import static us.bpsm.edn.parser.InstantUtils.parseNanoseconds;
import static us.bpsm.edn.parser.InstantUtils.timestampToString;

import java.util.Arrays;

import org.junit.Test;

import us.bpsm.edn.EdnException;
import us.bpsm.edn.parser.ParsedInstant;


public class InstantUtilsTest {

    @Test
    public void testParse() {
        assertEquals(pi(1987, 1, 1, 0, 0, 0, 0, 0, 0, 0),
                parse("1987"));
        assertEquals(pi(1987, 6, 1, 0, 0, 0, 0, 0, 0, 0),
                parse("1987-06"));
        assertEquals(pi(1987, 6, 5, 0, 0, 0, 0, 0, 0, 0),
                parse("1987-06-05"));
        assertEquals(pi(1987, 6, 5, 4, 0, 0, 0, 0, 0, 0),
                parse("1987-06-05T04"));
        assertEquals(pi(1987, 6, 5, 4, 3, 0, 0, 0, 0, 0),
                parse("1987-06-05T04:03"));
        assertEquals(pi(1987, 6, 5, 4, 3, 2, 0, 0, 0, 0),
                parse("1987-06-05T04:03:02"));
        assertEquals(pi(1987, 6, 5, 4, 3, 2, 123456789, 0, 0, 0),
                parse("1987-06-05T04:03:02.123456789"));
        assertEquals(pi(1987, 6, 5, 4, 3, 2, 123000000, 0, 0, 0),
                parse("1987-06-05T04:03:02.123"));
        assertEquals(pi(1987, 6, 5, 4, 3, 2, 123456789, -1, 7, 30),
                parse("1987-06-05T04:03:02.123456789-07:30"));
    }

    static ParsedInstant pi(int years, int months, int days, int hours,
            int minutes, int seconds, int nanoseconds, int offsetSign,
            int offsetHours, int offsetMinutes) {
        return new ParsedInstant(years, months, days, hours, minutes, seconds,
                nanoseconds, offsetSign, offsetHours, offsetMinutes);
    }

    @Test
    public void testExtragrammaticalVerificationAfterParse() {
        for (String x: Arrays.asList(
                "1987-01-01",
                "1987-01-31",
                "1987-02-01",
                "1987-02-28",
                "2000-02-29",
                "1987-03-01",
                "1987-03-31",
                "1987-04-01",
                "1987-04-30",
                "1987-05-01",
                "1987-05-31",
                "1987-06-01",
                "1987-06-30",
                "1987-07-01",
                "1987-07-31",
                "1987-08-01",
                "1987-08-31",
                "1987-09-01",
                "1987-09-30",
                "1987-10-01",
                "1987-10-31",
                "1987-11-01",
                "1987-11-30",
                "1987-12-01",
                "1987-12-31",
                "1987-01-01T00",
                "1987-01-01T23",
                "1987-01-01T00:00",
                "1987-01-01T00:59",
                "1987-01-01T00:00:00",
                "1987-01-01T00:00:59",
                "1987-01-01T00:59:60",
                "1987-01-01T00:00:00.0",
                "1987-01-01T00:00:00.999999999",
                "1987+23:00",
                "1987+00:59"
                )) {
            parse(x);
        }
        for (String x: Arrays.asList(
                "1987-00",
                "1987-13",
                "1987-01-00",
                "1987-01-32",
                "1987-02-29",
                "2000-02-30",
                "1987-03-32",
                "1987-04-31",
                "1987-05-32",
                "1987-06-31",
                "1987-07-32",
                "1987-08-32",
                "1987-09-31",
                "1987-10-32",
                "1987-11-31",
                "1987-12-32",
                "1987-01-01T24",
                "1987-01-01T00:60",
                "1987-01-01T00:59:61",
                "1987-01-01T00:58:60",
                "1987-01-01T00:00:00.1999999999",
                "1987+24:00",
                "1987+00:60"
                )) {
            try {
                parse(x);
                fail(x);
            } catch (EdnException ignored) {
                // pass
            }
        }
    }



    @Test
    public void testParseNanoseconds() {
        assertEquals(0, parseNanoseconds(""));
        assertEquals(0, parseNanoseconds("0"));
        assertEquals(123000000, parseNanoseconds("123"));
        assertEquals(123456000, parseNanoseconds("123456"));
        assertEquals(123456789, parseNanoseconds("123456789"));
    }


    @Test
    public void testCalendar() {
        testCalendar("1987-01-01T00:00:00.000+00:00", "1987");
        testCalendar("1987-06-05T04:03:02.123-07:30",
                     "1987-06-05T04:03:02.123-07:30");
        testCalendar("1987-06-05T04:03:02.123+03:00",
                     "1987-06-05T04:03:02.123+03:00");
        testCalendar("1987-01-01T00:00:00.000+03:00",
                     "1987+03:00");
    }

    void testCalendar(String expect, String input) {
        assertEquals(expect, calendarToString(makeCalendar(parse(input))));
    }


    @Test
    public void testDate() {
        testDate("1987-01-01T00:00:00.000-00:00",
                 "1987");
        testDate("1986-12-31T21:00:00.000-00:00",
                 "1987+03:00");
        testDate("1987-06-05T10:33:02.123-00:00",
                 "1987-06-05T10:33:02.123456789-00:00");
        testDate("1987-06-05T01:03:02.123-00:00",
                 "1987-06-05T04:03:02.123456789+03:00");
    }

    void testDate(String expect, String input) {
        assertEquals(expect, dateToString(makeDate(parse(input))));
    }


    @Test
    public void testTimestamp() {
        testTimestamp("1987-01-01T00:00:00.000000000-00:00",
                      "1987");
        testTimestamp("1986-12-31T21:00:00.000000000-00:00",
                      "1987+03:00");
        testTimestamp("1987-06-05T10:33:02.123456789-00:00",
                      "1987-06-05T10:33:02.123456789-00:00");
        testTimestamp("1987-06-05T01:03:02.123456789-00:00",
                      "1987-06-05T04:03:02.123456789+03:00");
    }

    void testTimestamp(String expect, String input) {
        assertEquals(expect, timestampToString(makeTimestamp(parse(input))));
    }

}
