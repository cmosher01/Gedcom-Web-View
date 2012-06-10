package nu.mine.mosher.gedcom;



/**
 * Represents one GEDCOM entry (usually one line). Objects of this class are
 * immutable.
 * @author Chris Mosher
 */
public class GedcomLine
{
    private final int level;
    private final String id;
    private final String tagString;
    private final String value;
    private final String pointer;
    private final GedcomTag tag;

    /**
     * Initializes a <code>GedcomLine</code>.
     * @param level
     * @param id
     * @param tag
     * @param value
     */
    GedcomLine(final int level, final String id, final String tag,
        final String value)
    {
        this.level = level;
        this.id = getPointer(id);
        this.tagString = tag;
        final String v = getPointer(value);
        if (v.length() > 0)
        {
            this.pointer = v;
            this.value = "";
        }
        else
        {
            this.value = replaceAts(value);
            this.pointer = "";
        }

        this.tag = parseTag();
    }

    private GedcomLine(final String id, final int level, final String pointer,
        final GedcomTag tag, final String tagString, final String value)
    {
        this.id = id;
        this.level = level;
        this.pointer = pointer;
        this.tag = tag;
        this.tagString = tagString;
        this.value = value;
    }

    private GedcomTag parseTag()
    {
        try
        {
            return GedcomTag.valueOf(this.tagString);
        }
        catch (final IllegalArgumentException e)
        {
            return GedcomTag.UNKNOWN;
        }
    }

    private static String getPointer(final String s)
    {
        if (!s.startsWith("@") || !s.endsWith("@") || s.length() < 3)
        {
            return "";
        }

        final String pointer = s.substring(1, s.length() - 1);
        if (pointer.indexOf('@') >= 0)
        {
            return "";
        }
        return pointer;
    }

    private static String replaceAts(final String s)
    {
        return s.replaceAll("@@", "@");
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(256);
        if (hasID())
        {
            sb.append(this.id);
            sb.append(": ");
        }
        sb.append(this.tagString);
        sb.append(" ");
        if (isPointer())
        {
            sb.append("--> ");
            sb.append(this.pointer);
        }
        else
        {
            appendFilteredValue(this.value, sb);
        }
        return sb.toString();
    }

    public void dump(final StringBuilder appendTo)
    {
        appendTo.append(this.level);
        appendTo.append(",");
        if (hasID())
        {
            appendTo.append("id=");
            appendTo.append(this.id);
            appendTo.append(",");
        }
        appendTo.append("tag=");
        appendTo.append(this.tagString);
        appendTo.append(",");
        if (isPointer())
        {
            appendTo.append("pointer=");
            appendTo.append(this.pointer);
        }
        else
        {
            appendTo.append("value=\"");
            appendFilteredValue(this.value, appendTo);
            appendTo.append("\"");
        }
    }

    private static void appendFilteredValue(final String value,
        final StringBuilder appendTo)
    {
        appendTo.append(value.replaceAll("\n", "[NEWLINE]"));
    }

    /**
     * @return if this line has an ID
     */
    public boolean hasID()
    {
        return this.id.length() > 0;
    }

    /**
     * @return if this line has a pointer
     */
    public boolean isPointer()
    {
        return this.pointer.length() > 0;
    }

    /**
     * @return the ID for this line
     */
    public String getID()
    {
        return this.id;
    }

    /**
     * @return the level number of this line
     */
    public int getLevel()
    {
        return this.level;
    }

    /**
     * @return the pointer value, if any, in this line
     */
    public String getPointer()
    {
        return this.pointer;
    }

    /**
     * @return the GEDCOM tag on this line
     */
    public GedcomTag getTag()
    {
        return this.tag;
    }

    public String getTagString()
    {
        return this.tagString;
    }

    /**
     * @return the actual value of this line
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Handles CONT tags by appending the given string to the value of this
     * line, and returning a new <code>GedcomLine</code>.
     * @param sContinuedLine
     * @return new <code>GedcomLine</code>
     */
    GedcomLine contValue(final String sContinuedLine)
    {
        return new GedcomLine(this.id, this.level, this.pointer, this.tag,
            this.tagString, this.value + "\n" + sContinuedLine);
    }

    /**
     * Handles CONC tags by appending the given string to the value of this
     * line, and returning a new <code>GedcomLine</code>.
     * @param sConcatenatedLine
     * @return new <code>GedcomLine</code>
     */
    GedcomLine concValue(final String sConcatenatedLine)
    {
        return new GedcomLine(this.id, this.level, this.pointer, this.tag,
            this.tagString, this.value + sConcatenatedLine);
    }
}
