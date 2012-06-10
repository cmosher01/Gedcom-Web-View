package nu.mine.mosher.gedcom.exception;



import nu.mine.mosher.gedcom.GedcomLine;



public class InvalidLevel extends Exception
{
    public InvalidLevel(final GedcomLine line)
    {
        super("GEDCOM line has invalid level number at line: " + line);
    }
}
