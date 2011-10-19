package nu.mine.mosher.time;



import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;



/**
 * Smoke test of {@link Time} class.
 * 
 * @author christopher_mosher
 */
public class TimeTest
{
	/**
	 * Nominal case: do we get the Date we put in?
	 */
	@Test
	public void nominal()
	{
		final Date original = new Date();

		final Time uut = new Time(original);
		final Date actual = uut.asDate();

		assertThat(actual, equalTo(original));
	}

	/**
	 * Nominal string case: do we get the string we passed in, if properly
	 * formatted?
	 * 
	 * @throws ParseException
	 *             if the string format is invalid
	 */
	@Test
	public void nominalString() throws ParseException
	{
		final String original = new SimpleDateFormat(Time.ISO8601_RFC3339_DATE_TIME_FORMAT).format(new Date());

		final Time uut = Time.readFromString(original);

		final String actual = uut.toString();

		assertThat(actual, equalTo(original));
	}

	/**
	 * Is an instance of Time immutable?
	 */
	@SuppressWarnings("boxing")
	@Test
	public void immutability()
	{
		final Date mutable = new Date();

		final long msOriginal = mutable.getTime();

		final Time uut = new Time(mutable);
		assertThat(uut.asDate().getTime(), equalTo(msOriginal));

		mutable.setTime(149);
		assertThat(uut.asDate().getTime(), equalTo(msOriginal));

		uut.asDate().setTime(941);
		assertThat(uut.asDate().getTime(), equalTo(msOriginal));
	}
}
