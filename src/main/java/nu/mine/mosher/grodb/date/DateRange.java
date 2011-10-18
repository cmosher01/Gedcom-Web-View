package nu.mine.mosher.grodb.date;

import java.util.Date;
import nu.mine.mosher.time.Time;

/**
 * TODO
 *
 * @author Chris Mosher
 */
public class DateRange implements Comparable<DateRange>
{
    /*
	 * YMD always represent Gregorian calendar.
	 * Range of possible dates is given by earliest
	 * thru latest (inclusive).
	 * Date is exact iff earliest.equals(latest).
	 */
	private final YMD earliest;
	private final YMD latest;

	/**
	 * Indicates what the preferred display calendar is.
	 * true==Julian, false==Gregorian
	 * Note that this indicates only how to display the date(s),
	 * not how they are stored. Dates are always stored using
	 * the Gregorian calendar. Further, it is only a preference,
	 * and therefore the value may be ignored.
	 */
	private final boolean julian;

	private final boolean circa;

	private transient final int hash;
	private transient final Time approx;



	/**
	 * @param earliest
	 * @param latest
	 */
	public DateRange(final YMD earliest, final YMD latest)
	{
		this(earliest,latest,false,false);
	}

    /**
     * @param earliest
     * @param latest
     * @param circa
     */
    public DateRange(final YMD earliest, final YMD latest, final boolean circa)
    {
		this(earliest,latest,circa,false);
    }

    /**
     * @param earliest
     * @param latest
     * @param circa
     * @param julian
     */
    public DateRange(final YMD earliest, final YMD latest, final boolean circa, final boolean julian)
    {
        this.julian = julian;
		this.circa = circa;

    	if (earliest != null)
    	{
    		this.earliest = earliest;
    	}
    	else
    	{
    		this.earliest = YMD.getMinimum();
    	}

    	if (latest != null)
    	{
            this.latest = latest;
    	}
    	else
		{
			this.latest = YMD.getMaximum();
		}

    	if (this.latest.compareTo(this.earliest) < 0)
		{
			throw new IllegalArgumentException("earliest date must be less than or equal to latest date");
		}

		this.approx = calcApprox();
		this.hash = calcHash();
    }



    /**
	 * @return earliest possible date
	 */
	public YMD getEarliest()
	{
		return this.earliest;
	}

    /**
     * @return latest possible date
     */
    public YMD getLatest()
    {
        return this.latest;
    }

	/**
	 * @return if this represents an exact date
	 */
	public boolean isExact()
	{
		return this.earliest.equals(this.latest);
	}

	/**
	 * @return if this date should be show using the Julian calendar
	 * (if so, the caller is responsible for converting it).
	 */
	public boolean isJulian()
	{
		return this.julian;
	}

    /**
     * @return if this is an approximate date
     */
    public boolean isCirca()
    {
        return this.circa;
    }

	/**
	 * @return an approximation of this range
	 */
	public Time getApproxDay()
	{
		return this.approx;
	}



    @Override
	public boolean equals(final Object object)
    {
    	if (!(object instanceof DateRange))
    	{
    		return false;
    	}

		final DateRange that = (DateRange)object;

		return
			this.earliest.equals(that.earliest) &&
			this.latest.equals(that.latest) &&
			this.julian == that.julian &&
			this.circa == that.circa;
    }

    @Override
	public int hashCode()
    {
    	return this.hash;
    }

    @Override
    public String toString()
    {
    	final StringBuffer sb = new StringBuffer();

		if (this.circa)
		{
			sb.append("c. ");
		}

		if (isExact())
    	{
    		sb.append(this.earliest.toString());
    	}
    	else
    	{
    		sb.append("[between ");
    		sb.append(this.earliest.toString());
    		sb.append(" and ");
    		sb.append(this.latest.toString());
    		sb.append("]");
    	}
		return sb.toString();
    }

    @Override
	public int compareTo(final DateRange that)
    {
        int d = 0;

        if (d == 0)
        {
        	d = this.approx.compareTo(that.approx);
        }

        return d;
    }



    private Time calcApprox()
	{
		return new Time(new Date((this.earliest.getApproxTime().asDate().getTime()+this.latest.getApproxTime().asDate().getTime())/2));
	}

	private int calcHash()
	{
		int h = 17;

		h *= 37;
		h += this.earliest.hashCode();
		h *= 37;
		h += this.latest.hashCode();
		h *= 37;
		h += this.julian ? 0 : 1;
		h *= 37;
		h += this.circa ? 0 : 1;

		return h;
	}
}
