/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private Date date;

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

    private void writeObject(ObjectOutputStream s) throws IOException
    {
        s.writeLong(date.getTime());
    }

    private void readObject(ObjectInputStream s) throws IOException
    {
        date = new Date(s.readLong());
    }
}
