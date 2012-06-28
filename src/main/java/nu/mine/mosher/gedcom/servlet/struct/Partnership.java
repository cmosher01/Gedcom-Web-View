package nu.mine.mosher.gedcom.servlet.struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/*
 * Created on 2006-10-08.
 */
public class Partnership implements Comparable<Partnership>
{
	private final ArrayList<Event> rEvent;
	private final ArrayList<Person> rChild = new ArrayList<Person>();

	private Person partner;

	public Partnership(final ArrayList<Event> rEvent)
	{
		this.rEvent = rEvent;

		Collections.<Event>sort(this.rEvent);
	}

	public void setPartner(final Person partner)
	{
		this.partner = partner;
	}

	public Person getPartner()
	{
		return this.partner;
	}

	public ArrayList<Event> getEvents()
	{
		return this.rEvent;
	}

	public ArrayList<Person> getChildren()
	{
		return this.rChild;
	}

	public void addChildren(final Collection<Person> rChildToAdd)
	{
		this.rChild.addAll(rChildToAdd);

		Collections.<Person>sort(this.rChild);
	}

	public boolean isPrivate()
	{
		return this.partner != null && this.partner.isPrivate();
	}

	@Override
	public int compareTo(final Partnership that)
	{
		if (this.rEvent.isEmpty() && that.rEvent.isEmpty())
		{
			return 0;
		}
		if (!this.rEvent.isEmpty() && that.rEvent.isEmpty())
		{
			return -1;
		}
		if (this.rEvent.isEmpty() && !that.rEvent.isEmpty())
		{
			return +1;
		}
		return this.rEvent.get(0).compareTo(that.rEvent.get(0));
	}
}
