package nu.mine.mosher.time;



import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;



public class TimeTest
{
	@Test
	public void nominal()
	{
		final Date original = new Date();

		final Time uut = new Time(original);
		final Date actual = uut.asDate();

		assertThat(actual,equalTo(original));
	}

	@Test
	public void nominalString() throws ParseException
	{
		final String original = new SimpleDateFormat(Time.ISO8601_RFC3339_DATE_TIME_FORMAT).format(new Date());

		final Time uut = Time.readFromString(original);

		final String actual = uut.toString();

		assertThat(actual,equalTo(original));
	}

	@Test
	public void immutability()
	{
		final Date mutable = new Date();

		final long msOriginal = mutable.getTime();

		final Time uut = new Time(mutable);
		assertThat(uut.asDate().getTime(),equalTo(msOriginal));

		mutable.setTime(149);
		assertThat(uut.asDate().getTime(),equalTo(msOriginal));

		uut.asDate().setTime(941);
		assertThat(uut.asDate().getTime(),equalTo(msOriginal));
	}
}
