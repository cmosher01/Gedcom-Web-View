package nu.mine.mosher.gedcom.servlet;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nu.mine.mosher.gedcom.GedcomLine;
import nu.mine.mosher.gedcom.GedcomTag;
import nu.mine.mosher.gedcom.GedcomTree;
import nu.mine.mosher.gedcom.date.parser.GedcomDateValueParser;
import nu.mine.mosher.gedcom.date.parser.ParseException;
import nu.mine.mosher.gedcom.servlet.struct.Event;
import nu.mine.mosher.gedcom.servlet.struct.EventNames;
import nu.mine.mosher.gedcom.servlet.struct.Partnership;
import nu.mine.mosher.gedcom.servlet.struct.Person;
import nu.mine.mosher.gedcom.servlet.struct.Source;
import nu.mine.mosher.gedcom.date.DatePeriod;
import nu.mine.mosher.gedcom.date.DateRange.DatesOutOfOrder;
import nu.mine.mosher.time.Time;
import nu.mine.mosher.collection.TreeNode;

/**
 * Parses the given <code>GedcomTree</code> into <code>Person</code> objects.
 * 
 * <p>Created on 2006-10-09.</p>
 * @author Chris Mosher
 */
public class Loader
{
	private final GedcomTree gedcom;
	private final String name;

	private final Map<UUID,Person> mapUUIDtoPerson = new HashMap<UUID,Person>();
	private final Map<UUID,Source> mapUUIDtoSource = new HashMap<UUID,Source>();

	private Person first;
	private String description;



	public Loader(final GedcomTree gedcom, final String filename)
	{
		this.gedcom = gedcom;
		this.name = filename;
	}



	public void parse()
	{
		final TreeNode<GedcomLine> root = this.gedcom.getRoot();

		final Collection<TreeNode<GedcomLine>> rNodeTop = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(root,rNodeTop);

		for (final TreeNode<GedcomLine> nodeTop : rNodeTop)
		{
			final GedcomLine lineTop = nodeTop.getObject();
			final GedcomTag tagTop = lineTop.getTag();

			if (tagTop.equals(GedcomTag.HEAD))
			{
				this.description = parseHead(nodeTop);
				break;
			}
		}

		final Map<String,Person> mapIDtoPerson = new HashMap<String,Person>();

		for (final TreeNode<GedcomLine> nodeTop : rNodeTop)
		{
			final GedcomLine lineTop = nodeTop.getObject();
			final GedcomTag tagTop = lineTop.getTag();

			if (tagTop.equals(GedcomTag.INDI))
			{
				final Person person = parseIndividual(nodeTop);
				mapIDtoPerson.put(person.getID(),person);
				storeInUuidMap(person);
				if (this.first == null)
				{
					this.first = person;
				}
			}
		}

		for (final TreeNode<GedcomLine> nodeTop : rNodeTop)
		{
			final GedcomLine lineTop = nodeTop.getObject();
			final GedcomTag tagTop = lineTop.getTag();

			if (tagTop.equals(GedcomTag.FAM))
			{
				parseFamily(nodeTop,mapIDtoPerson);
			}
		}

		for (final Person person: mapIDtoPerson.values())
		{
			person.initKeyDates();
		}
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Person getFirstPerson()
	{
		return this.first;
	}

	public Person lookUpPerson(final UUID uuid)
	{
		return this.mapUUIDtoPerson.get(uuid);
	}

	public Source lookUpSource(final UUID uuid)
	{
		return this.mapUUIDtoSource.get(uuid);
	}


	public void appendAllUuids(final Set<UUID> appendTo)
	{
		appendTo.addAll(this.mapUUIDtoPerson.keySet());
	}

	private void storeInUuidMap(final Person person) {
		final UUID uuid = person.getUuid();
		if (uuid == null)
		{
			return;
		}
		final Person existing = this.mapUUIDtoPerson.get(uuid);
		if (existing != null)
		{
			System.err.println("Duplicate INDI _UID value: "+uuid);
			return;
		}
		this.mapUUIDtoPerson.put(uuid, person);
	}

	private void storeInUuidMap(final Source source) {
		final UUID uuid = source.getUuid();
		if (uuid == null)
		{
			return;
		}
		final Source existing = this.mapUUIDtoSource.get(uuid);
		if (existing != null)
		{
			return;
		}
		this.mapUUIDtoSource.put(uuid, source);
	}



	private static void getChildren(TreeNode<GedcomLine> root, Collection<TreeNode<GedcomLine>> rNodeTop)
	{
		for (final TreeNode<GedcomLine> child : root)
		{
			rNodeTop.add(child);
		}
	}

	private static String parseHead(final TreeNode<GedcomLine> head)
	{
		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(head,rNode);

		for (final TreeNode<GedcomLine> node : rNode)
		{
			final GedcomLine line = node.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.NOTE))
			{
				return line.getValue();
			}
		}
		return "";
	}

	private Person parseIndividual(final TreeNode<GedcomLine> nodeIndi)
	{
		String name = "[unknown]";
		UUID uuid = null;
		final ArrayList<Event> rEvent = new ArrayList<Event>();
		boolean isPrivate = false;

		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(nodeIndi,rNode);

		for (final TreeNode<GedcomLine> node : rNode)
		{
			final GedcomLine line = node.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.NAME))
			{
				name = parseName(node);
			}
			else if (tag.equals(GedcomTag._UID))
			{
				try
				{
					uuid = parseUuid(node);
				}
				catch (final Throwable e)
				{
					System.err.println("Error while parsing inidividual \""+name+"\"");
					e.printStackTrace();
					uuid = null;
				}
			}
			else if (GedcomTag.setIndividualEvent.contains(tag) || GedcomTag.setIndividualAttribute.contains(tag))
			{
				final Event event = parseEvent(node);
				rEvent.add(event);
				if (tag.equals(GedcomTag.BIRT))
				{
					isPrivate = calculatePrivacy(event);
				}
			}
		}

		return new Person(nodeIndi.getObject().getID(),name,rEvent,new ArrayList<Partnership>(),isPrivate,uuid);
	}

	private static UUID parseUuid(final TreeNode<GedcomLine> nodeUuid) {
		final String rawUuid = nodeUuid.getObject().getValue();
		return UUID.fromString(rawUuid);
	}

	private void parseFamily(final TreeNode<GedcomLine> nodeFam, final Map<String,Person> mapIDtoPerson)
	{
		Person husb = null;
		Person wife = null;
		final ArrayList<Person> rChild = new ArrayList<Person>();
		final ArrayList<Event> rEvent = new ArrayList<Event>();
		
		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(nodeFam,rNode);

		for (final TreeNode<GedcomLine> node : rNode)
		{
			final GedcomLine line = node.getObject();
			final GedcomTag tag = line.getTag();

			if (tag.equals(GedcomTag.HUSB))
			{
				husb = lookUpPerson(line.getPointer(),mapIDtoPerson);
			}
			else if (tag.equals(GedcomTag.WIFE))
			{
				wife = lookUpPerson(line.getPointer(),mapIDtoPerson);
			}
			else if (tag.equals(GedcomTag.CHIL))
			{
				final Person child = lookUpPerson(line.getPointer(),mapIDtoPerson);
				rChild.add(child);
			}
			else if (GedcomTag.setFamilyEvent.contains(tag))
			{
				final Event event = parseEvent(node);
				rEvent.add(event);
			}
		}
		buildFamily(husb,wife,rChild,rEvent);
	}

	private static Person lookUpPerson(final String id, final Map<String,Person> mapIDtoPerson)
	{
		return mapIDtoPerson.get(id);
	}

	private static String parseName(final TreeNode<GedcomLine> nodeName)
	{
		return nodeName.getObject().getValue();
	}

	private Event parseEvent(final TreeNode<GedcomLine> nodeEvent)
	{
		final String whichEvent = getEventName(nodeEvent);

		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(nodeEvent,rNode);

		DatePeriod date = null;
		String place = "";
		String note = "";
		Source source = null;

		for (final TreeNode<GedcomLine> node : rNode)
		{
			final GedcomLine line = node.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.DATE))
			{
				final String sDate = line.getValue();
				final GedcomDateValueParser parser = new GedcomDateValueParser(new StringReader(sDate));
				try
				{
					date = parser.parse();
				}
				catch (final ParseException | DatesOutOfOrder e)
				{
					System.err.println("Error while parsing \""+sDate+"\"");
					e.printStackTrace();
					date = null;
				}
			}
			else if (tag.equals(GedcomTag.PLAC))
			{
				place = line.getValue();
			}
			else if (tag.equals(GedcomTag.NOTE))
			{
				note = parseNote(node);
			}
			else if (tag.equals(GedcomTag.SOUR))
			{
				source = parseSource(node);
				if (source != null)
				{
					storeInUuidMap(source);
				}
			}
		}
		// TODO handle case of date == null (see grojs for example)
		return new Event(whichEvent,date,HtmlUtil.escapeHtml(place),HtmlUtil.escapeHtml(note),source);
	}

	private Source parseSource(final TreeNode<GedcomLine> node)
	{
		final String pointingText = getSourcePtText(node);

		final String id = node.getObject().getPointer();
		final TreeNode<GedcomLine> nodeSource = this.gedcom.getNode(id);
		if (nodeSource == null)
		{
			return null;
		}

		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(nodeSource,rNode);

		String author = "";
		String title = "";
		String publication = "";
		String text = "";
		UUID uuid = null;
		for (final TreeNode<GedcomLine> n : rNode)
		{
			final GedcomLine line = n.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.AUTH))
			{
				author = line.getValue();
			}
			else if (tag.equals(GedcomTag.TITL))
			{
				title = line.getValue();
			}
			else if (tag.equals(GedcomTag.PUBL))
			{
				publication = line.getValue();
			}
			else if (tag.equals(GedcomTag.TEXT))
			{
				text = line.getValue();
			}
			else if (tag.equals(GedcomTag._UID))
			{
				try
				{
					uuid = parseUuid(n);
				}
				catch (final Throwable e)
				{
					System.err.println("Error while parsing source \""+title+"\"");
					e.printStackTrace();
					uuid = null;
				}
			}
		}
		return new Source(HtmlUtil.escapeHtml(author),HtmlUtil.escapeHtml(title),HtmlUtil.escapeHtml(publication),HtmlUtil.smartEscapeHtml(pointingText+text),uuid);
	}

	private static String getSourcePtText(TreeNode<GedcomLine> node)
	{
		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(node,rNode);
		for (final TreeNode<GedcomLine> n : rNode)
		{
			final GedcomLine line = n.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.DATA))
			{
				return parseData(node);
			}
		}
		return "";
	}

	private static String parseData(TreeNode<GedcomLine> node)
	{
		final StringBuilder sb = new StringBuilder(256);
		final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
		getChildren(node,rNode);
		for (final TreeNode<GedcomLine> n : rNode)
		{
			final GedcomLine line = n.getObject();
			final GedcomTag tag = line.getTag();
			if (tag.equals(GedcomTag.TEXT))
			{
				sb.append(node.getObject().getValue());
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	private String parseNote(final TreeNode<GedcomLine> node)
	{
		final String id = node.getObject().getPointer();
		final TreeNode<GedcomLine> nodeNote = this.gedcom.getNode(id);
		if (nodeNote == null)
		{
			return "";
		}
		return nodeNote.getObject().getValue();
	}

	private static boolean calculatePrivacy(final Event birth)
	{
		final DatePeriod dateInformation = birth.getDate();
		if (dateInformation == null)
		{
			return false;
		}

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR,-90);
		final Time dateLatestPublicInformation = new Time(cal.getTime());
		return dateLatestPublicInformation.compareTo(dateInformation.getEndDate().getApproxDay()) < 0;
	}

	private static String getEventName(final TreeNode<GedcomLine> node)
	{
		final GedcomTag tag = node.getObject().getTag();
		if (tag.equals(GedcomTag.EVEN))
		{
			final Collection<TreeNode<GedcomLine>> rNode = new ArrayList<TreeNode<GedcomLine>>();
			getChildren(node,rNode);
			for (final TreeNode<GedcomLine> n : rNode)
			{
				final GedcomLine line = n.getObject();
				final GedcomTag t = line.getTag();
				if (t.equals(GedcomTag.TYPE))
				{
					return line.getValue();
				}
			}
		}

		final String eventName = EventNames.getName(tag);
		final String value = node.getObject().getValue();
		if (value.length() == 0)
		{
			return eventName;
		}
		return eventName+": "+value;
	}

	private static void buildFamily(final Person husb, final Person wife, final ArrayList<Person> rChild, final ArrayList<Event> rEvent)
	{
		if (husb != null)
		{
			final Partnership partnership = new Partnership(rEvent);
			partnership.addChildren(rChild);
			if (wife != null)
			{
				partnership.setPartner(wife);
			}
			husb.getPartnerships().add(partnership);
		}
		if (wife != null)
		{
			final Partnership partnership = new Partnership(rEvent);
			partnership.addChildren(rChild);
			if (husb != null)
			{
				partnership.setPartner(husb);
			}
			wife.getPartnerships().add(partnership);
		}
		for (final Person child: rChild)
		{
			if (husb != null)
			{
				child.setFather(husb);
			}
			if (wife != null)
			{
				child.setMother(wife);
			}
		}
	}
}
