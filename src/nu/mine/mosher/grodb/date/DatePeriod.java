/*
 * Created on Apr 23, 2005
 */
package nu.mine.mosher.grodb.date;

/**
 * TODO
 *
 * @author Chris Mosher
 */
public class DatePeriod implements Comparable<DatePeriod>
{
	private final DateRange dateStart;
	private final DateRange dateEnd;

	/**
	 * @param date
	 */
	public DatePeriod(final DateRange date)
	{
		this(date,date);
	}

	/**
	 * @param dateStart
	 * @param dateEnd
	 */
	public DatePeriod(final DateRange dateStart, final DateRange dateEnd)
	{
		this.dateStart = dateStart;
		if (this.dateStart == null)
		{
			throw new IllegalStateException("dateStart cannot be null.");
		}
		this.dateEnd = dateEnd;
		if (this.dateEnd == null)
		{
			throw new IllegalStateException("dateEnd cannot be null.");
		}
	}

	/**
	 * @return the start date
	 */
	public DateRange getStartDate()
	{
		return this.dateStart;
	}

	/**
	 * @return the end date
	 */
	public DateRange getEndDate()
	{
		return this.dateEnd;
	}

	@Override
	public boolean equals(final Object object)
	{
		if (!(object instanceof DatePeriod))
		{
			return false;
		}
		final DatePeriod that = (DatePeriod)object;
		return this.dateStart.equals(that.dateStart) && this.dateEnd.equals(that.dateEnd);
	}

	@Override
	public int hashCode()
	{
		return this.dateStart.hashCode() ^ this.dateEnd.hashCode();
	}

	@Override
	public String toString()
	{
		if (this.dateStart.equals(this.dateEnd))
		{
			return this.dateStart.toString();
		}
		return this.dateStart+"\u2013"+this.dateEnd;
	}

	@Override
	public int compareTo(final DatePeriod that)
	{
		int d = 0;
		if (d == 0)
		{
			d = this.dateStart.compareTo(that.dateStart);
		}
		if (d == 0)
		{
			d = this.dateEnd.compareTo(that.dateEnd);
		}
		return d;
	}

	/**
	 * Checks to see if this DatePeriod overlaps the given DatePeriod.
	 * @param periodTarget
	 * @return true if they overlap
	 */
	public boolean overlaps(final DatePeriod periodTarget)
	{
		return this.dateStart.compareTo(periodTarget.dateEnd) <= 0 && periodTarget.dateStart.compareTo(this.dateEnd) <= 0;
	}
}
