package nu.mine.mosher.gedcom.date;



import java.util.Date;

import nu.mine.mosher.time.Time;



/**
 * Represents an date that could fall sometime within a given range. For
 * example, "some date between 1840 and 1846 inclusive," would be represented as
 * a a range with earliest year 1840 and latest year 1846. A range of "unknown"
 * is represented by a full range (earliest = Jan. 1, 9999 BC, and latest = Dec.
 * 31, 9999 AD). Date is "exact" if and only if earliest.equals(latest).
 * @author Chris Mosher
 */
public class DateRange implements Comparable<DateRange>
{
    /*
     * YMD always represent Gregorian calendar. Range of possible dates is given
     * by earliest thru latest (inclusive). Date is exact if and only if
     * earliest.equals(latest).
     */
    private final YMD earliest;
    private final YMD latest;

    private transient final int hash;
    private transient final Time approx;
    private transient final boolean exact;

    /**
     * Represents an unknown date (Jan. 1, 9999 BC to Dec. 31, 9999 AD)
     */
    public static final DateRange UNKNOWN = new DateRange(null);

    /**
     * Indicates that the (supposed) earliest and latest dates were given
     * incorrectly, such that latest is less than earliest.
     * @author Chris Mosher
     */
    public static class DatesOutOfOrder extends Exception
    {
        private DatesOutOfOrder(final YMD earliestGreater, final YMD latestLesser)
        {
            super("Latest date (" + latestLesser + ") is less than earliest date (" + earliestGreater + ").");
        }
    }

    /**
     * Initializes an exact date.
     * @param ymdExact the exact date (earliest = latest = ymdExact)
     */
    public DateRange(final YMD ymdExact)
    {
        if (ymdExact != null)
        {
            this.earliest = this.latest = ymdExact;
            this.exact = true;
        }
        else
        {
            this.earliest = YMD.getMinimum();
            this.latest = YMD.getMaximum();
            this.exact = false;
        }
        this.approx = calcApprox();
        this.hash = calcHash();
    }

    /**
     * Initializes a date range.
     * @param earliest earliest possible date
     * @param latest latest possible date
     * @throws DatesOutOfOrder if <code>latest</code> is less than
     *             <code>earliest</code>
     */
    @SuppressWarnings("synthetic-access")
    public DateRange(final YMD earliest, final YMD latest) throws DatesOutOfOrder
    {
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
            throw new DatesOutOfOrder(this.earliest, this.latest);
        }

        this.approx = calcApprox();
        this.hash = calcHash();
        this.exact = this.earliest.equals(this.latest);
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
        return this.exact;
    }

    /**
     * @return an approximation of this range
     */
    public Time getApproxDay()
    {
        return this.approx;
    }

    /**
     * Compares this range to another object to see if it has the same value
     * (both earliest and latest dates)
     * @return true if object is a DateRange that is equal
     */
    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof DateRange))
        {
            return false;
        }

        final DateRange that = (DateRange) object;

        return this.earliest.equals(that.earliest)
            && this.latest.equals(that.latest);
    }

    /**
     * Returns a hash code for this object.
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return this.hash;
    }

    /**
     * Returns a string representation of this range, indended for display to
     * the end user, and not intended for persistence.
     * @return displayable string
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        if (isExact())
        {
            sb.append(this.earliest.toString());
        }
        else if (equals(UNKNOWN))
        {
            sb.append("[unknown]");
        }
        else
        {
            sb.append(this.earliest.toString());
            sb.append("?");
            sb.append(this.latest.toString());
        }
        return sb.toString();
    }

    /**
     * Compares this range to the given range. Not consistent with equals.
     * @param that range to compare to
     * @return -1, 0, or +1; for less, equal, or greater
     */
    @Override
    public int compareTo(final DateRange that)
    {
        return this.approx.compareTo(that.approx);
    }

    private Time calcApprox()
    {
        if (this.earliest.equals(YMD.getMinimum()) && this.latest.equals(YMD.getMaximum()))
        {
            return new Time(new Date(0));
        }
        if (this.earliest.equals(YMD.getMinimum()))
        {
            return this.latest.getApproxTime();
        }
        if (this.latest.equals(YMD.getMaximum()))
        {
            return this.earliest.getApproxTime();
        }

        /* optimization: don't bother making a new Time object if exact */
        if (this.exact)
        {
            return this.earliest.getApproxTime();
        }

        /* @formatter:off */
        return
            new Time(
                new Date(
                    (
                        this.earliest.getApproxTime().asDate().getTime()
                        +
                        this.latest.getApproxTime().asDate().getTime()
                    )
                    /
                    2
                )
            );
        /* @formatter:on */
    }

    private int calcHash()
    {
        int h = 17;

        h *= 37;
        h += this.earliest.hashCode();
        h *= 37;
        h += this.latest.hashCode();

        return h;
    }
}
