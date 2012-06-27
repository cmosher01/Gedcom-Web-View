/*
 * Created on Nov 6, 2006
 */
package nu.mine.mosher.gedcom.date;


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;



/**
 * @author Chris Mosher
 */
@SuppressWarnings({"static-method","javadoc"})
public class YMDTest
{
    @Test
    public void nominal()
    {
        final YMD ymd = new YMD(2006, 11, 6);
        assertEquals("2006-11-06", ymd.toString());
    }

    @Test
    public void yearBC()
    {
        final YMD ymd = new YMD(-30, 1, 1);
        assertEquals("-0030-01-01", ymd.toString());
    }

    @Test
    public void year3digits()
    {
        final YMD ymd = new YMD(400, 11, 6);
        assertEquals("0400-11-06", ymd.toString());
    }

    @Test
    public void year2digits()
    {
        final YMD ymd = new YMD(37, 11, 6);
        assertEquals("0037-11-06", ymd.toString());
    }

    @Test
    public void yearAndMonthOnly()
    {
        final YMD ymd = new YMD(2000, 5);
        assertEquals("2000-05", ymd.toString());
    }

    @Test
    public void yearOnly()
    {
        final YMD ymd = new YMD(1066);
        assertEquals("1066", ymd.toString());
    }

    @Test
    public void minimum()
    {
        assertEquals("[before]", YMD.getMinimum().toString());
    }

    @Test
    public void maximum()
    {
        assertEquals("[after]", YMD.getMaximum().toString());
    }

    @Test
    public void nominalEquals()
    {
        final YMD ymd1 = new YMD(1966, 7, 3);
        final YMD ymd2 = new YMD(1966, 7, 3);
        assertThat(ymd2, not(sameInstance(ymd1)));
        assertTrue(ymd1.equals(ymd2));
        assertTrue(ymd2.equals(ymd1));
    }

    @Test
    public void nominalNotEquals()
    {
        final YMD ymd1 = new YMD(1966, 7, 3);
        final YMD ymd2 = new YMD(1966, 7, 4);
        assertThat(ymd2, not(sameInstance(ymd1)));
        assertFalse(ymd1.equals(ymd2));
        assertFalse(ymd2.equals(ymd1));
    }

    @SuppressWarnings("unused")
    @Test(expected=RuntimeException.class)
    public void zero()
    {
        new YMD(0);
    }
}
