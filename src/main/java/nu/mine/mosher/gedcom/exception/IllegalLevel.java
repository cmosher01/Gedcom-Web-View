package nu.mine.mosher.gedcom.exception;



import nu.mine.mosher.gedcom.GedcomLine;



public class IllegalLevel extends GedcomParseException
{
    public IllegalLevel(final String rawGedcomLine,
        final GedcomLine parsedGedcomLine)
    {
        super("GEDCOM line has an invalid level number", rawGedcomLine,
            parsedGedcomLine);
    }
}
