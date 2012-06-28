package nu.mine.mosher.gedcom.servlet.struct;

import nu.mine.mosher.gedcom.date.DatePeriod;

/*
 * Created on 2006-10-08.
 */
public class Event implements Comparable<Event>
{
	private final String type;
	private final DatePeriod date;
	private final String place;
	private final String note;
	private final Source source;

	/**
	 * @param type
	 * @param date
	 * @param place
	 * @param note 
	 * @param source 
	 */
	public Event(final String type, final DatePeriod date, final String place, final String note, final Source source)
	{
		this.type = type;
		this.date = date;
		this.place = place;
		this.note = note;
		this.source = source;
	}

	public String getType()
	{
		return this.type;
	}
	public DatePeriod getDate()
	{
		return this.date;
	}
	public String getPlace()
	{
		return this.place;
	}
	public String getNote()
	{
		return this.note;
	}
	public Source getSource()
	{
		return this.source;
	}

	@Override
	public int compareTo(final Event that)
	{
		if (this.date == null)
		{
			return +1;
		}
		if (that.date == null)
		{
			return -1;
		}
		return this.date.compareTo(that.date);
	}
}
