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
public class Time implements Comparable
{
    private final Date date;

    /**
     * @param ms
     */
    public Time(long ms)
    {
        this.date = new Date(ms);
    }

    public long getTime()
    {
        return this.date.getTime();
    }

    public int compareTo(Object o)
    {
        return this.date.compareTo(o);
    }

    public boolean equals(Object o)
    {
        return this.date.equals(o);
    }

    public int hashCode()
    {
        return this.date.hashCode();
    }
}
