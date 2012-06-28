package nu.mine.mosher.gedcom.servlet.struct;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.mine.mosher.gedcom.date.DatePeriod;
import nu.mine.mosher.gedcom.date.DateRange;
import nu.mine.mosher.gedcom.date.YMD;
import nu.mine.mosher.time.Time;



/*
 * Created on 2006-10-08.
 */
public class Person implements Comparable<Person>
{
    private final UUID uuid;
    private final String ID;
    private final String name;
    private final ArrayList<Event> rEvent;
    private final ArrayList<Partnership> rParnership;
    private final boolean isPrivate;

    private Time birth = null;
    private Time death = null;
    private List<Time> rMarriage = new ArrayList<Time>();
    private List<Time> rDivorce = new ArrayList<Time>();

    private Person father;
    private Person mother;

    /**
     * @param ID
     * @param name
     * @param rEvent
     * @param partnership
     * @param isPrivate
     * @param uuid
     */
    public Person(final String ID, final String name, final ArrayList<Event> rEvent, final ArrayList<Partnership> partnership,
        final boolean isPrivate, final UUID uuid)
    {
        if (uuid == null)
        {
            this.uuid = UUID.randomUUID();
        }
        else
        {
            this.uuid = uuid;
        }
        this.ID = ID;
        this.name = name;
        this.rEvent = rEvent;
        this.rParnership = partnership;
        this.isPrivate = isPrivate;

        Collections.<Event> sort(this.rEvent);
        Collections.<Partnership> sort(this.rParnership);
    }

    public void initKeyDates()
    {
        for (final Event event : this.rEvent)
        {
            if (event.getDate() == null)
            {
                continue;
            }
            if (event.getType().equals("birth"))
            {
                this.birth = event.getDate().getStartDate().getApproxDay();
            }
            else if (event.getType().equals("death"))
            {
                this.death = event.getDate().getStartDate().getApproxDay();
            }
        }
        if (this.birth.asDate().getTime() == 0)
        {
            this.birth = YMD.getMinimum().getApproxTime();
        }
        if (this.death.asDate().getTime() == 0)
        {
            this.death = YMD.getMaximum().getApproxTime();
        }
        if (this.rParnership.size() > 0)
        {
            for (final Partnership par : this.rParnership)
            {
                boolean mar = false;
                boolean div = false;
                for (final Event event : par.getEvents())
                {
                    if (event.getDate() == null)
                    {
                        continue;
                    }
                    if (event.getType().equals("marriage"))
                    {
                        this.rMarriage.add(event.getDate().getStartDate().getApproxDay());
                        mar = true;
                    }
                    else if (event.getType().equals("divorce"))
                    {
                        this.rDivorce.add(event.getDate().getStartDate().getApproxDay());
                        div = true;
                    }
                }
                if (!mar)
                {
                    if (par.getChildren().size() > 0)
                    {
                        final Time birthChild = par.getChildren().get(0).getBirth();
                        final GregorianCalendar cal = new GregorianCalendar();
                        cal.setGregorianChange(new Date(Long.MIN_VALUE));
                        cal.setTime(birthChild.asDate());
                        cal.add(Calendar.YEAR, -1);
                        this.rMarriage.add(new Time(cal.getTime()));
                        mar = true;
                    }
                }
                if (!mar)
                {
                    this.rMarriage.add(YMD.getMinimum().getApproxTime());
                    mar = true;
                }
                if (!div)
                {
                    this.rDivorce.add(this.death);
                    div = true;
                }
                if (!div)
                {
                    this.rDivorce.add(YMD.getMaximum().getApproxTime());
                    div = true;
                }
                assert mar && div;
            }
        }
        else
        {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setGregorianChange(new Date(Long.MIN_VALUE));
            cal.setTime(this.birth.asDate());
            cal.add(Calendar.YEAR, 18);
            this.rMarriage.add(new Time(cal.getTime()));

            this.rDivorce.add(YMD.getMaximum().getApproxTime());
        }
    }

    public Time getBirth()
    {
        return this.birth;
    }

    public Time getDeath()
    {
        return this.death;
    }

    @Override
    public String toString()
    {
        return this.name.replaceAll("/", "");
    }

    private static final Pattern patternName = Pattern.compile("(.*)/(.*)/(.*)");

    public String getClassedName()
    {
        final Matcher matcher = patternName.matcher(this.name);
        if (!matcher.matches())
        {
            /* oops, can't find surname, so don't style it */
            return this.name;
        }

        final StringBuilder sb = new StringBuilder(32);

        sb.append(matcher.group(1));
        sb.append("<span class=\"surname\">");
        sb.append(matcher.group(2));
        sb.append("</span>");
        sb.append(matcher.group(3));

        return sb.toString();
    }

    /**
     * @param father the father to set
     */
    public void setFather(final Person father)
    {
        this.father = father;
    }

    /**
     * @param mother the mother to set
     */
    public void setMother(final Person mother)
    {
        this.mother = mother;
    }

    public Person getFather()
    {
        return this.father;
    }

    public Person getMother()
    {
        return this.mother;
    }

    public ArrayList<Event> getEvents()
    {
        return this.rEvent;
    }

    public ArrayList<Event> getEventsWithin(final DatePeriod period)
    {
        final ArrayList<Event> rWithin = new ArrayList<Event>();
        for (final Event event : this.rEvent)
        {
            if (event.getDate() == null)
            {
                continue;
            }
            if (event.getDate().overlaps(period))
            {
                rWithin.add(event);
            }
        }
        return rWithin;
    }

    public ArrayList<Partnership> getPartnerships()
    {
        return this.rParnership;
    }

    /**
     * Gets the UUID from the gedcom file, or a generated one if there was not
     * one in the file.
     * @return the UUID
     */
    public UUID getUuid()
    {
        return this.uuid;
    }

    public String getID()
    {
        return this.ID;
    }

    public boolean isPrivate()
    {
        return this.isPrivate;
    }

    @Override
    public int compareTo(final Person that)
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

    public ArrayList<FamilyEvent> getFamilyEvents()
    {
        final ArrayList<FamilyEvent> rEventRet = new ArrayList<FamilyEvent>();

        getEventsOfSelf(rEventRet);
        getEventsOfPartnership(rEventRet);
        getEventsOfFather(rEventRet);
        getEventsOfMother(rEventRet);
        getEventsOfSpouses(rEventRet);
        getEventsOfChildren(rEventRet);

        Collections.<FamilyEvent> sort(rEventRet);

        return rEventRet;
    }

    private void getEventsOfSelf(final List<FamilyEvent> rEventRet)
    {
        for (final Event event : this.getEvents())
        {
            rEventRet.add(new FamilyEvent(this, event, "self"));
        }
    }

    private void getEventsOfPartnership(final List<FamilyEvent> rEventRet)
    {
        for (final Partnership part : this.rParnership)
        {
            for (final Event event : part.getEvents())
            {
                rEventRet.add(new FamilyEvent(part.getPartner(), event, "spouse"));
            }
        }
    }

    private void getEventsOfFather(final List<FamilyEvent> rEventRet)
    {
        getEventsOfParent(this.father, "father", rEventRet);
    }

    private void getEventsOfMother(final List<FamilyEvent> rEventRet)
    {
        getEventsOfParent(this.mother, "mother", rEventRet);
    }

    private void getEventsOfParent(final Person parent, final String relation, final List<FamilyEvent> rEventRet)
    {
        if (parent != null)
        {
            for (final Event event : parent.getEventsWithin(getChildhood()))
            {
                rEventRet.add(new FamilyEvent(parent, event, relation));
            }
        }
    }

    private void getEventsOfSpouses(final List<FamilyEvent> rEventRet)
    {
        int p = 0;
        for (final Partnership partnership : this.rParnership)
        {
            final Person partner = partnership.getPartner();
            if (partner != null)
            {
                for (final Event event : partner.getEventsWithin(getPartnerhood(p)))
                {
                    rEventRet.add(new FamilyEvent(partnership.getPartner(), event, "spouse"));
                }
            }
            ++p;
        }
    }

    private void getEventsOfChildren(final List<FamilyEvent> rEventRet)
    {
        for (final Partnership partnership : this.rParnership)
        {
            for (final Person child : partnership.getChildren())
            {
                for (final Event event : child.getEventsWithin(child.getChildhood()))
                {
                    rEventRet.add(new FamilyEvent(child, event, "child"));
                }
            }
        }
    }

    private DatePeriod getChildhood()
    {
        return new DatePeriod(
            new DateRange(new YMD(this.birth)),
            new DateRange(new YMD(this.rMarriage.get(0))));
    }

    private DatePeriod getPartnerhood(final int p)
    {
        return new DatePeriod(
            new DateRange(new YMD(this.rMarriage.get(p))),
            new DateRange(new YMD(this.rDivorce.get(p))));
    }
}
