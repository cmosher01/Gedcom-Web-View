/*
 * Created on Nov 6, 2006
 */
package nu.mine.mosher.grodb.date;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * TODO
 *
 * @author Chris Mosher
 */
public class YMDTest
{
	@Test
	public void typical()
	{
		final YMD ymd = new YMD(2006,11,6);
		assertEquals("2006-11-06",ymd.toString());
	}

	@Test
	public void yearBC()
	{
		final YMD ymd = new YMD(-30,1,1);
		assertEquals("-0030-01-01",ymd.toString());
	}

	@Test
	public void year3digits()
	{
		final YMD ymd = new YMD(400,11,6);
		assertEquals("0400-11-06",ymd.toString());
	}

	@Test
	public void year2digits()
	{
		final YMD ymd = new YMD(37,11,6);
		assertEquals("0037-11-06",ymd.toString());
	}

	@Test
	public void yearAndMonthOnly()
	{
		final YMD ymd = new YMD(2000,5);
		assertEquals("2000-05",ymd.toString());
	}

	@Test
	public void yearOnly()
	{
		final YMD ymd = new YMD(1066);
		assertEquals("1066",ymd.toString());
	}

	@Test
	public void minimum()
	{
		assertEquals("-9999-01-01",YMD.getMinimum().toString());
	}

	@Test
	public void maximum()
	{
		assertEquals("9999-12-31",YMD.getMaximum().toString());
	}

	@Test
	public void zero()
	{
		final YMD ymd = new YMD(0);
		assertEquals("",ymd.toString());
	}
}
