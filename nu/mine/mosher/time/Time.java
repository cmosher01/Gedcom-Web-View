/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Improved version of <code>java.util.Date</code>.
 * Objects of this class are immutable.
 * 
 * @author Chris Mosher
 */
public class Time implements Comparable, Serializable
{
    private long ms;

    private transient String asString;

    /**
     * @param milliseconds since Java epoch, as a <code>long</code>
     */
    public Time(final long milliseconds)
    {
        this.ms = milliseconds;

        this.asString = calcString();
    }



    /**
     * @return milliseconds since Java epoch, as a <code>long</code>,
     * as passed in to the constructor
     */
    public long getTime()
    {
        return this.ms;
    }



    public int compareTo(final Object object)
    {
        final Time that = (Time)object;
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

    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof Time))
        {
            return false;
        }
        final Time that = (Time)object;
        return this.ms == that.ms;
    }

    @Override
    public int hashCode()
    {
        return (int)(this.ms ^ (this.ms >>> 32));
    }

    /**
     * This time, as a string in the format:
     * <code>yyyy/MM/dd HH:mm:ss.SSS</code> (as in <code>SimpleDateFormat</code>).
     * @return time as a string
     */
    @Override
    public String toString()
    {
    	return this.asString;
    }



    private void writeObject(final ObjectOutputStream s) throws IOException
    {
        s.writeLong(this.ms);
    }

    private void readObject(final ObjectInputStream s) throws IOException
    {
        this.ms = s.readLong();
        this.asString = calcString();
    }

    private String calcString()
    {
        final Format fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return fmt.format(new Date(this.ms));
    }
}
