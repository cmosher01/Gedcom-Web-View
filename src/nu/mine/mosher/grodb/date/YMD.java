package nu.mine.mosher.grodb.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import nu.mine.mosher.time.Time;

/**
 * Represents a date, specified as a year, month, and day, allowing
 * for some values to be unknown. An unknown day or month is specified as zero.
 * Objects of this class are immutable and thread-safe.
 * 
 * @author Chris Mosher
 */
public class YMD implements Comparable<YMD>
{
	/*
	 * One-based year, month, and day.
	 * Gregorian calendar is assumed.
	 * Zero indicates that field is "unknown"
	 * Negative year means B.C.
	 */
	private final int year;
	private final int month;
	private final int day;

	private transient final int hash;
	private transient final Time approx;



	/**
	 * @param year
	 */
	public YMD(final int year)
	{
		this(year,0,0);
	}

	/**
	 * @param year
	 * @param month
	 */
	public YMD(final int year, final int month)
	{
		this(year,month,0);
	}

	/**
	 * @param year
	 * @param month
	 * @param day
	 */
	public YMD(final int year, final int month, final int day)
	{
		this.year = year;
		this.month = month;
		this.day = day;
		this.approx = calcApprox();
		this.hash = calcHash();
	}


	/**
	 * @param time
	 */
	public YMD(final Time time)
	{
		final GregorianCalendar cal = new GregorianCalendar();
    	cal.setGregorianChange(new Date(Long.MIN_VALUE));
    	cal.setTime(time.asDate());
    	this.year = cal.get(Calendar.YEAR);
    	this.month = cal.get(Calendar.MONTH)+1;
    	this.day = cal.get(Calendar.DAY_OF_MONTH);
		this.approx = calcApprox();
		this.hash = calcHash();
	}



	/**
	 * @return the day, or zero if unknown
	 */
	public int getDay()
    {
        return this.day;
    }

    /**
     * @return the month (1 means January), or zero if unknown
     */
    public int getMonth()
    {
        return this.month;
    }

    /**
     * @return the year, or zero if unknown. (negative means BC)
     */
    public int getYear()
    {
        return this.year;
    }

    /**
     * Gets the exact <code>Time</code> represented by this <code>YMD</code>,
     * assuming it is exact. Throws otherwise.
     * @return the <code>Time</code> representing this exact <code>YMD</code> (at noon, local time).
     * @throws IllegalStateException if this <code>YMD</code> if any of year, month, or day are zero
     */
    public Time getExactTime()
    {
    	if (!isExact())
    	{
    		throw new IllegalStateException();
    	}

    	return this.approx;
    }

	/**
	 * Gets a <code>Time</code> that can be used as an approximation
	 * of this <code>YMD</code> for computation purposes.
	 * Never display this value to the user!
	 * @return an approximate <code>Time</code> for this <code>YMD</code>
	 */
	public Time getApproxTime()
	{
		return this.approx;
	}

    /**
     * Gets if this <code>YMD</code> is exact.
     * @return <code>true</code> if exact
     */
    public boolean isExact()
    {
    	return valid(this.year) && valid(this.month) && valid(this.day);
    }

	/**
	 * Returns a new <code>YMD</code> representing January 1, 9999 BC.
	 * @return Jan. 1, 9999 BC
	 */
	public static YMD getMinimum()
    {
    	return new YMD(-9999,1,1);
    }

	/**
	 * Returns a new <code>YMD</code> representing December 31, AD 9999.
	 * @return Dec. 31, AD 9999
	 */
    public static YMD getMaximum()
    {
    	return new YMD(9999,12,31);
    }



	@Override
	public boolean equals(final Object object)
	{
		if (!(object instanceof YMD))
		{
			return false;
		}

		final YMD that = (YMD)object;
		return
			this.year == that.year &&
			this.month == that.month &&
			this.day == that.day;
	}

    @Override
	public int hashCode()
    {
    	return this.hash;
    }

    @Override
    public String toString()
    {
    	final StringBuilder sb = new StringBuilder();
    	if (this.year != 0)
    	{
        	if (this.year < 0)
        	{
        		sb.append('-');
        	}
    		sb.append(String.format("%04d",Integer.valueOf(Math.abs(this.year))));
    		if (this.month > 0)
    		{
        		sb.append(String.format("-%02d",Integer.valueOf(this.month)));
        		if (this.day > 0)
        		{
        			sb.append(String.format("-%02d",Integer.valueOf(this.day)));
        		}
    		}
    	}
    	return sb.toString();
    }

    @Override
	public int compareTo(final YMD that)
    {
    	return this.approx.compareTo(that.approx);
    }



    private static boolean valid(final int i)
	{
		return i != 0;
	}

	private Time calcApprox()
	{
		int m = this.month;
		int d = this.day;

		// if month and day are missing, assume mid-year (July 3).
		if (m == 0 && d == 0)
		{
			m = 7;
			d = 3;
		}
		// if just day is missing, assume mid-month (the 15th).
		else if (d == 0)
		{
			d = 15;
		}

    	return createTime(this.year,m,d);
	}

	private static Time createTime(final int year, final int month, final int day)
	{
		final GregorianCalendar cal = new GregorianCalendar();
    	cal.setGregorianChange(new Date(Long.MIN_VALUE));

    	cal.set(year,month-1,day,12,0,0);
    	cal.set(Calendar.MILLISECOND,0);

    	return new Time(cal.getTime());
	}

    private int calcHash()
    {
    	return this.approx.hashCode();
    }
}
