/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.io.Serializable;
import java.util.Date;

/**
 * Improved version of <code>java.util.Date</code>.
 * Objects of this class are immutable.
 * 
 * @author Chris Mosher
 */
public class Time implements Comparable, Serializable
{
    private final Date date;

    /**
     * @param ms
     */
    public Time(long ms)
    {
        date = new Date(ms);
    }

    public long getTime()
    {
        return date.getTime();
    }

    public int compareTo(Object o)
    {
        return date.compareTo(o);
    }

    public boolean equals(Object o)
    {
        return date.equals(o);
    }

    public int hashCode()
    {
        return date.hashCode();
    }

    public String toString()
    {
        return ""+date.getTime();
    }
}
