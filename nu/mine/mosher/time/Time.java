/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Improved version of <code>java.util.Date</code>.
 * Objects of this class are immutable.
 * 
 * @author Chris Mosher
 */
public class Time implements Comparable, Serializable
{
    private long ms;

    /**
     * @param milliseconds
     */
    public Time(long milliseconds)
    {
        this.ms = milliseconds;
    }

    public long getTime()
    {
        return ms;
    }

    public int compareTo(Object o)
    {
        Time that = (Time)o;
        if (this.ms < that.ms)
        {
            return -1;
        }
        if (that.ms < this.ms)
        {
            return +1;
        }
        return 0;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Time))
        {
            return false;
        }
        Time that = (Time)o;
        return this.ms == that.ms;
    }

    public int hashCode()
    {
        return (int)(ms ^ (ms >>> 32));
    }

    public String toString()
    {
        return ""+ms;
    }

    private void writeObject(ObjectOutputStream s) throws IOException
    {
        s.writeLong(ms);
    }

    private void readObject(ObjectInputStream s) throws IOException
    {
        ms = s.readLong();
    }
}
