/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.util.Date;

/**
 * Improved version of <code>java.util.Date</code>.
 * 
 * @author Chris Mosher
 */
public class Time
{
    private final Date date;

    /**
     * @param date
     */
    public Time(long ms)
    {
        this.date = new Date(ms);
    }
}
