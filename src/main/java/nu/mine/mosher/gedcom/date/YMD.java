package nu.mine.mosher.gedcom.date;



import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nu.mine.mosher.time.Time;



/**
 * Represents a date, specified as a year, month, and day, allowing for some
 * values to be unknown. An unknown day or month is specified as zero. Objects
 * of this class are immutable and thread-safe. Gregorian calendar is always
 * assumed; this class does not do any converting of dates between different
 * calendars.
 * @author Chris Mosher
 */
public class YMD implements Comparable<YMD>
{
    /**
     * One-based year. Negative year means B.C.; positive means A.D. Must be
     * -9999 to -1, or 1 to 9999.
     */
    private final int year;

    /**
     * month (1 = January) (0 = unknown)
     */
    private final int month;

    /**
     * day of month (1-31) (0 = unknown)
     */
    private final int day;

    /**
     * Indicates what the preferred display calendar is. true==Julian,
     * false==Gregorian Note that this indicates only how to display the
     * date(s), not how they are stored. Dates are always stored using the
     * Gregorian calendar. Further, it is only a preference, and therefore the
     * value may be ignored.
     */
    private final boolean julian;

    /**
     * Indicates the date is an approximation.
     */
    private final boolean circa;

    private transient final int hash;
    private transient final Time approx;

    /**
     * Initializes this YMD with the given year, and an unknown month and day.
     * @param year the year (-9999 to -1, or 1 to 9999)
     */
    public YMD(final int year)
    {
        this(year, 0);
    }

    /**
     * Initializes this YMD with the given year and month, and an unknown day.
     * @param year the year (-9999 to -1, or 1 to 9999)
     * @param month the month (1=Jan. to 12=Dec.) or 0 meaning "unknown"
     */
    public YMD(final int year, final int month)
    {
        this(year, month, 0, false, false);
    }

    /**
     * Initializes this YMD with the given year, month and day.
     * @param year the year (-9999 to -1, or 1 to 9999)
     * @param month the month (1=Jan. to 12=Dec.) or 0 meaning "unknown"
     * @param day the day within the month (1-31) or 0 meaning "unknown"
     */
    public YMD(final int year, final int month, final int day)
    {
        this(year, month, day, false, false);
    }

    /**
     * Initializes this YMD with the given year, month and day, and the given
     * "circa" (true or false, to indicate approximation or not).
     * @param year the year (-9999 to -1, or 1 to 9999)
     * @param month the month (1=Jan. to 12=Dec.) or 0 meaning "unknown"
     * @param day the day within the month (1-31) or 0 meaning "unknown"
     * @param circa true if this date is an approximation
     */
    public YMD(final int year, final int month, final int day, final boolean circa)
    {
        this(year, month, day, circa, false);
    }

    /**
     * Initializes this YMD with the given year, month and day, and the given
     * "circa" (true or false, to indicate approximation or not).
     * @param year the year (-9999 to -1, or 1 to 9999)
     * @param month the month (1=Jan. to 12=Dec.) or 0 meaning "unknown"
     * @param day the day within the month (1-31) or 0 meaning "unknown"
     * @param circa true if this date is an approximation
     * @param julian true if this date is preferred to be shown in the Julian
     *            calendar (it does not indicate that the given year, month, and
     *            date are Julian--they are Gregorian regardless)
     */
    public YMD(final int year, final int month, final int day, final boolean circa, final boolean julian)
    {
        this.year = year;
        if (this.year <= -10000 || this.year == 0 || +10000 <= this.year)
        {
            throw new IllegalStateException("Invalid year: " + this.year);
        }
        this.month = month;
        if (this.month < 0 || 12 < this.month)
        {
            throw new IllegalStateException("Invalid month: " + this.month);
        }
        this.day = day;
        if (this.day < 0 || 31 < this.day)
        {
            throw new IllegalStateException("Invalid day: " + this.day);
        }
        this.circa = circa;
        this.julian = julian;

        this.approx = calcApprox();
        this.hash = calcHash();
    }

    /**
     * Initializes this YMD with the year, month and day taken from the given
     * {@link Time}.
     * @param time <code>Time</code> to get the year, month, and day of (must be
     *            Gregorian)
     */
    public YMD(final Time time)
    {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.setTime(time.asDate());
        this.year = cal.get(Calendar.YEAR);
        this.month = cal.get(Calendar.MONTH) + 1;
        this.day = cal.get(Calendar.DAY_OF_MONTH);
        this.circa = false;
        this.julian = false;
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
     * Returns if this date is preferred to be shown using the Julian calendar.
     * @return true if should be (converted to and) displayed in Julian
     */
    public boolean isJulian()
    {
        return this.julian;
    }

    /**
     * Returns if this date is an approximation.
     * @return if this date is an approximation
     */
    public boolean isCirca()
    {
        return this.circa;
    }

    /**
     * Gets the exact <code>Time</code> represented by this <code>YMD</code>,
     * assuming it is exact. Throws otherwise.
     * @return the <code>Time</code> representing this exact <code>YMD</code>
     *         (at noon, local time).
     * @throws IllegalStateException if this <code>YMD</code> if any of year,
     *             month, or day are zero
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
     * Gets a <code>Time</code> that can be used as an approximation of this
     * <code>YMD</code> for computation purposes. Never display this value to
     * the user!
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
        return valid(this.month) && valid(this.day) && !this.circa;
    }

    /**
     * Returns a new <code>YMD</code> representing January 1, 9999 BC.
     * @return Jan. 1, 9999 BC
     */
    public static YMD getMinimum()
    {
        return new YMD(-9999, 1, 1);
    }

    /**
     * Returns a new <code>YMD</code> representing December 31, AD 9999.
     * @return Dec. 31, AD 9999
     */
    public static YMD getMaximum()
    {
        return new YMD(9999, 12, 31);
    }

    /**
     * Compares this date to the given object. Note that two "unknown" values
     * (zero) are still considered equal.
     * @param object the object to compare this object to
     * @return true if the given object is a <code>YMD</code> with the same
     *         values for year, month, and day.
     */
    @Override
    public boolean equals(final Object object)
    {
        if (!(object instanceof YMD))
        {
            return false;
        }

        final YMD that = (YMD) object;
        return this.year == that.year && this.month == that.month
            && this.day == that.day;
    }

    /**
     * Gets the hash code for this object.
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return this.hash;
    }

    /**
     * Formats this object into a string, intended to be shown to the end user.
     * @return string for display
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        if (this.circa)
        {
            sb.append("c. ");
        }

        /* this check is an optimization */
        if (this.year == 9999 || this.year == -9999)
        {
            if (equals(YMD.getMaximum()))
            {
                return "[after]";
            }
            if (equals(YMD.getMinimum()))
            {
                return "[before]";
            }
        }
        if (this.year < 0)
        {
            sb.append('-');
        }
        sb.append(String.format("%04d",
            Integer.valueOf(Math.abs(this.year))));
        if (this.month > 0)
        {
            sb.append(String.format("-%02d", Integer.valueOf(this.month)));
            if (this.day > 0)
            {
                sb.append(String.format("-%02d", Integer.valueOf(this.day)));
            }
        }

        return sb.toString();
    }

    /**
     * Compares this date to the given date. Not consistent with
     * <code>equals</code>. Uses heuristics to provide nice sequencing, intended
     * for display to the end-user.
     * @return -1, 0, or 1, for less, equal, or greater
     */
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

        return createTime(this.year, m, d);
    }

    private static Time createTime(final int year, final int month,
        final int day)
    {
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setGregorianChange(new Date(Long.MIN_VALUE));

        cal.set(year, month - 1, day, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Time(cal.getTime());
    }

    private int calcHash()
    {
        return this.approx.hashCode();
    }
}
