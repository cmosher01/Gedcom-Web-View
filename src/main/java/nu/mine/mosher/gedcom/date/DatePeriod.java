/*
 * Created on Apr 23, 2005
 */
package nu.mine.mosher.gedcom.date;



/**
 * TODO
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
        this(date, date);
    }

    /**
     * @param dateStart
     * @param dateEnd
     */
    public DatePeriod(final DateRange dateStart, final DateRange dateEnd)
    {
        if (dateStart == null)
        {
            this.dateStart = DateRange.UNKNOWN;
        }
        else
        {
            this.dateStart = dateStart;
        }
        if (dateEnd == null)
        {
            this.dateEnd = DateRange.UNKNOWN;
        }
        else
        {
            this.dateEnd = dateEnd;
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

    public boolean isSingle()
    {
        return this.dateStart.equals(this.dateEnd);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof DatePeriod))
        {
            return false;
        }
        final DatePeriod that = (DatePeriod) object;
        return this.dateStart.equals(that.dateStart)
            && this.dateEnd.equals(that.dateEnd);
    }

    @Override
    public int hashCode()
    {
        return this.dateStart.hashCode() ^ this.dateEnd.hashCode();
    }

    @Override
    public String toString()
    {
        if (isSingle())
        {
            return this.dateStart.toString();
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(this.dateStart.toString());
        sb.append("-");
        sb.append(this.dateEnd.toString());
        return sb.toString();
    }

    @Override
    public int compareTo(final DatePeriod that)
    {
        if (that == null)
        {
            return -1;
        }
        int d = 0;
        if (d == 0)
        {
            d = this.dateStart.compareTo(that.dateStart);
        }
        if (d == 0 || this.dateStart.equals(DateRange.UNKNOWN))
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
        return this.dateStart.compareTo(periodTarget.dateEnd) <= 0
            && periodTarget.dateStart.compareTo(this.dateEnd) <= 0;
    }
}
