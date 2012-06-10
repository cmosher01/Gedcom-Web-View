package nu.mine.mosher.gedcom.exception;



import nu.mine.mosher.gedcom.GedcomLine;



public class MissingTag extends GedcomParseException
{
    public MissingTag(final String rawGedcomLine,
        final GedcomLine parsedGedcomLine)
    {
        super("GEDCOM line does not have any tag", rawGedcomLine,
            parsedGedcomLine);
    }
}
