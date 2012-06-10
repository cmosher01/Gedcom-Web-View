package nu.mine.mosher.gedcom.exception;



import nu.mine.mosher.gedcom.GedcomLine;



public class GedcomParseException extends Exception
{
    private final String rawGedcomLine;
    private final GedcomLine parsedGedcomLine;

    public GedcomParseException(final String message,
        final String rawGedcomLine, final GedcomLine parsedGedcomLine)
    {
        super(message);
        this.rawGedcomLine = rawGedcomLine;
        this.parsedGedcomLine = parsedGedcomLine;
    }

    public GedcomParseException(final String message, final Throwable cause)
    {
        super(message, cause);
        this.rawGedcomLine = "";
        this.parsedGedcomLine = null;
    }

    public String getRawGedcomLine()
    {
        return this.rawGedcomLine;
    }

    public GedcomLine getParsedGedcomLine()
    {
        return this.parsedGedcomLine;
    }
}
