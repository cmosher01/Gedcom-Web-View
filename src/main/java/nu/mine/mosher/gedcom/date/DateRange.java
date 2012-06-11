package nu.mine.mosher.gedcom.date;



import java.util.Date;

import nu.mine.mosher.time.Time;



/**
 * TODO
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

    public static final DateRange UNKNOWN = new DateRange(null, null);

    public DateRange(final YMD exact)
    {
        this(exact, exact);
    }

    /**
     * @param earliest
     * @param latest
     */
    public DateRange(final YMD earliest, final YMD latest)
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
            throw new IllegalArgumentException(
                "earliest date must be less than or equal to latest date");
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
        /* optimization: same object */
        if (this.earliest == this.latest)
        {
            return true;
        }
        return this.earliest.equals(this.latest);
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

        final DateRange that = (DateRange) object;

        return this.earliest.equals(that.earliest)
            && this.latest.equals(that.latest);
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
        if (this.earliest.equals(YMD.getMinimum()))
        {
            return this.latest.getApproxTime();
        }
        if (this.latest.equals(YMD.getMaximum()))
        {
            return this.earliest.getApproxTime();
        }

        /* optimization: don't bother making a new Time object if this.isExact */
        if (this.isExact())
        {
            return this.earliest.getApproxTime();
        }

        return new Time(new Date(
            (this.earliest.getApproxTime().asDate().getTime() + this.latest
                .getApproxTime().asDate().getTime()) / 2));
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
