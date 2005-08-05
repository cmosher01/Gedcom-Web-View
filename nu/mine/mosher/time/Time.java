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
 * Objects of this class are immutable. This class actually
 * represents a bridge to <code>java.util.Date</code>.
 * 
 * @author Chris Mosher
 */
public class Time implements Comparable<Time>, Serializable
{
    private long ms;

    private transient String asString;

    /**
     * @param date the <code>java.util.Date</code> this object will wrap
     */
    public Time(final Date date)
    {
        this.ms = date.getTime();

        this.asString = calcString();
    }



    /**
     * Returns this time as a (new) <code>java.util.Date</code>. 
     * @return new <code>java.util.Date</code>
     */
    public Date asDate()
    {
        return new Date(this.ms);
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



    public int compareTo(final Time that)
    {
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
