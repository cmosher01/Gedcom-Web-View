/*
 * Created on April 19, 2005
 */
package nu.mine.mosher.gedcom.date.parser;



import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;

import nu.mine.mosher.gedcom.date.DatePeriod;
import nu.mine.mosher.gedcom.date.DateRange;
import nu.mine.mosher.gedcom.date.DateRange.DatesOutOfOrder;
import nu.mine.mosher.gedcom.date.YMD;

import org.junit.Test;



/**
 * @author Chris Mosher
 */
@SuppressWarnings(
{ "static-method", "javadoc" })
public class GedcomDateValueParserTest
{
    @Test
    public void nominal() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("1 JAN 2001");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(2001, 1, 1)))));
    }

    @Test
    public void nominalRange() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("BET 3 JUL 1966 AND 3 AUG 1966");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1966, 7, 3),new YMD(1966, 8, 3)))));
    }

    @Test
    public void nominalPeriod() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("FROM 3 JUL 1966 TO 3 AUG 1966");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1966, 7, 3)),new DateRange(new YMD(1966, 8, 3)))));
    }

    @Test
    public void nominalCirca() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("ABT 1400");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1400,0,0,true)))));
    }

    @Test
    public void nominalOnlyYear() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("1492");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1492)))));
    }

    @Test
    public void nominalOnlyMonthAndYear() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("DEC 1941");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1941,12)))));
    }

    @Test
    public void nominalInterpreted() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("INT 31 MAR 1850     (Easter 1850)");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1850,3,31)))));
    }

    @Test
    public void nominalBCE() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("32 BC");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(-32)))));
    }

    @Test
    public void nominalAD() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("1860 AD");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1860)))));
    }

    @Test
    public void nominalOS() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("@#DJULIAN@ 11 FEB 1731/2");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1732,2,22,false,true)))));
    }

    @Test
    public void leadingZero() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("09 DEC 2000");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(2000,12,9)))));
    }

    @Test(expected=ParseException.class)
    public void zeroYear() throws ParseException, DateRange.DatesOutOfOrder
    {
        parse("1 JAN 0");
    }

    @Test(expected=ParseException.class)
    public void onlyDayAndMonth() throws ParseException, DateRange.DatesOutOfOrder
    {
        parse("25 JUL");
    }

    @Test
    public void unlabeledDateWithSlashedYearIsDetectedAsJulian() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("11 FEB 1731/2");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(1732,2,22,false,true)))));
    }

    @Test(expected=ParseException.class)
    public void slashedYearCannotBeGregorian() throws ParseException, DateRange.DatesOutOfOrder
    {
        parse("@#DGREGORIAN@ 11 FEB 1731/2");
    }

    @Test
    public void omittedSpaceAfterCalendarEscapeIsAllowed() throws ParseException, DateRange.DatesOutOfOrder
    {
        final DatePeriod period = parse("@#DGREGORIAN@2 FEB 2222");
        assertThat(period, is(new DatePeriod(new DateRange(new YMD(2222,2,2)))));
    }

/*
*/

private static DatePeriod parse(final String s) throws ParseException, DatesOutOfOrder
    {
        final GedcomDateValueParser parser = new GedcomDateValueParser(
            new StringReader(s));

        return parser.parse();
    }
}
/*
 * TODO french, hebrew, other

function nominalHebrew(doh) {
    doh.is(GedcomDateParser.parse(
            "@#DHEBREW@ 1 TSH 5771"), 
            {year:5771,month:7,day:1,hebrew:true});
}

function unknownCalendar(doh) {
        doh.is(GedcomDateParser.parse(
                "@#DNEPALI@ 25 FALGUN 2067"),
                "NEPALI: 25 FALGUN 2067");
},
*/
