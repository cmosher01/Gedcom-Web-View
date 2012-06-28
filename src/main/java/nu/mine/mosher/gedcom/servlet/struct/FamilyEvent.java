/*
 * Created on Nov 5, 2006
 */
package nu.mine.mosher.gedcom.servlet.struct;

public class FamilyEvent implements Comparable<FamilyEvent>
{
	private final Person person;
	private final Event event;
	private final String relation;
	/**
	 * @param person
	 * @param event
	 * @param relation
	 */
	public FamilyEvent(final Person person, final Event event, final String relation)
	{
		this.person = person;
		this.event = event;
		this.relation = relation;
	}
	/**
	 * @return the event
	 */
	public Event getEvent()
	{
		return this.event;
	}
	/**
	 * @return the person
	 */
	public Person getPerson()
	{
		return this.person;
	}
	/**
	 * @return the relation
	 */
	public String getRelation()
	{
		return this.relation;
	}

	public boolean isSelf()
	{
		return this.relation.equals("self");
	}

	public boolean isPrivate()
	{
		if (this.person == null)
		{
			return false;
		}
		return this.person.isPrivate();
	}

	@Override
	public int compareTo(final FamilyEvent that)
	{
		return this.event.compareTo(that.event);
	}
}
