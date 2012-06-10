/*
 * Created on April 19, 2005
 */
package nu.mine.mosher.gedcom.date.parser;



import java.io.StringReader;

import org.junit.Test;



/**
 * TODO
 * @author Chris Mosher
 */
@SuppressWarnings("static-method")
public class GedcomDateValueParserTest
{
    @Test
    public void testDateValue() throws ParseException
    {
        String s = "1 JAN 2001";
        GedcomDateValueParser parser = new GedcomDateValueParser(
            new StringReader(s));
        parser.DateValue();
    }
}
