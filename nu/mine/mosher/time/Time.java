/*
 * Created on July 19, 2004
 */
package nu.mine.mosher.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Improved version of <code>java.util.Date</code>.
 * Objects of this class are immutable. This class actually
 * represents a bridge to <code>java.util.Date</code>.
 * 
 * @author Chris Mosher
 */
public class Time implements Comparable<Time>
{
    private static final String ISO8601_RFC3339_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final SimpleDateFormat fmtDateTime = new SimpleDateFormat(ISO8601_RFC3339_DATE_TIME_FORMAT);



    private final long ms;

    private transient final String asString;
    private transient final int hash;



    /**
     * @param date the <code>java.util.Date</code> this object will wrap
     */
    public Time(final Date date)
    {
        this(date.getTime());
    }

    private Time(final long ms)
    {
    	this.ms = ms;
        if (this.ms != 0)
        {
            this.asString = fmtDateTime.format(new Date(this.ms));
        }
        else
        {
	        this.asString = "";
        }
        this.hash = (int)(this.ms ^ (this.ms >>> 32));
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
        return this.hash;
    }

    /**
     * This time, as a string in the format:
     * <code>yyyy-MM-dd'T'HH:mm:ss.SSSZ</code> (as in <code>SimpleDateFormat</code>),
     * or an empty string if this time is zero.
     * @return time as a string
     */
    @Override
    public String toString()
    {
    	return this.asString;
    }



    @Override
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



    /**
     * @param sTime
     * @return a new Time object
     * @throws ParseException
     */
    public static Time readFromString(String sTime) throws ParseException
    {
        long ms = 0;
        if (sTime.length() > 0)
        {
        	String t = sTime;
            if (t.endsWith("Z"))
            {
                t = t.substring(0,t.length()-1)+"+0000";
            }
            ms = fmtDateTime.parse(t).getTime();
        }
        return new Time(ms);
    }
}
