package nu.mine.mosher.gedcom;



import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import nu.mine.mosher.gedcom.exception.GedcomParseException;
import nu.mine.mosher.gedcom.exception.IllegalLevel;
import nu.mine.mosher.gedcom.exception.InvalidID;
import nu.mine.mosher.gedcom.exception.MissingTag;



/**
 * A tool to parse a GEDCOM document. Given a <code>Reader</code> containing a
 * GEDCOM transmission, this parser returns a series of <code>GedcomLine</code>
 * objects representing the lines of the transmission.
 * @author Chris Mosher
 */
class GedcomParser implements Iterable<GedcomLine>
{
    private final BufferedReader in;

    /**
     * Initializes the <code>GedcomParser</code> to read lines from the given
     * GEDCOM transmission.
     * @param in the GEDCOM transmission to read from
     */
    public GedcomParser(final BufferedReader in)
    {
        this.in = in;
    }

    @Override
    public Iterator<GedcomLine> iterator()
    {
        return new Iter();
    }

    private class Iter implements Iterator<GedcomLine>
    {
        private GedcomLine lineNext;
        private GedcomParseException exception;

        @SuppressWarnings("synthetic-access")
        private void prepareNext()
        {
            try
            {
                this.lineNext = nextLine();
                this.exception = null;
            }
            catch (final GedcomParseException e)
            {
                this.lineNext = null;
                this.exception = e;
            }
        }

        private void checkNext() throws NoSuchElementException
        {
            if (this.lineNext == null)
            {
                final NoSuchElementException noElement = new NoSuchElementException();
                noElement.initCause(this.exception);
                throw noElement;
            }
        }

        /**
		 * 
		 */
        public Iter()
        {
            prepareNext();
        }

        @Override
        public boolean hasNext()
        {
            return this.lineNext != null;
        }

        @Override
        public GedcomLine next() throws NoSuchElementException
        {
            checkNext();
            final GedcomLine returned = this.lineNext;
            prepareNext();

            return returned;
        }

        @Override
        public void remove() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Parses the next line from this parser's GEDCOM transmission.
     * @return a (new) <code>GedcomLine</code> object representing the next
     *         GEDCOM line read from the transmission. Returns <code>null</code>
     *         at the end of the transmission.
     * @throws GedcomParseException
     */
    private GedcomLine nextLine() throws GedcomParseException
    {
        final String sLine = getNextNonblankLine();
        if (sLine == null)
        {
            return null;
        }

        return parseLine(sLine);
    }

    private String getNextNonblankLine() throws GedcomParseException
    {
        try
        {
            String s = this.in.readLine();
            while (s != null && s.trim().length() == 0)
            {
                s = this.in.readLine();
            }
            return s;
        }
        catch (final IOException e)
        {
            throw new GedcomParseException("Error reading from input source.",
                e);
        }
    }

    private static GedcomLine parseLine(final String sLine)
        throws IllegalLevel, MissingTag, InvalidID
    {
        final StringTokenizer st = new StringTokenizer(sLine);
        if (!st.hasMoreTokens())
        {
            // should never happen, because lines with only white-space
            // are skipped in the read loop above.
            throw new IllegalLevel(sLine, new GedcomLine(-1, "", "", ""));
        }

        final String sLevel = st.nextToken();
        int level = -1;
        try
        {
            level = Integer.parseInt(sLevel);
        }
        catch (final NumberFormatException e)
        {
            level = -1;
        }

        if (!st.hasMoreTokens()) // missing tag
        {
            throw new MissingTag(sLine, new GedcomLine(level, "", "", ""));
        }
        final String sID, sTag;
        final String sIDorTag = st.nextToken();
        if (sIDorTag.startsWith("@"))
        {
            sID = sIDorTag;
            if (!st.hasMoreTokens()) // missing tag
            {
                throw new MissingTag(sLine, new GedcomLine(level, sID, "", ""));
            }
            sTag = st.nextToken();
        }
        else
        {
            sID = "";
            sTag = sIDorTag;
        }

        String sValue = "";
        if (st.hasMoreTokens())
        {
            sValue = st.nextToken("\0"); // rest of line
            sValue = sValue.substring(1); // skip one space after tag
        }

        if (level < 0 || 99 < level)
        {
            throw new IllegalLevel(sLine, new GedcomLine(level, sID, sTag,
                sValue));
        }
        if (level > 0 && sID.length() > 0)
        {
            throw new InvalidID(sLine, new GedcomLine(level, sID, sTag, sValue));
        }

        return new GedcomLine(level, sID, sTag, sValue);
    }
}
