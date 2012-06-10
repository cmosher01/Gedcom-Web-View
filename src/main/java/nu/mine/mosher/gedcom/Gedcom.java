package nu.mine.mosher.gedcom;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import nu.mine.mosher.gedcom.exception.InvalidLevel;



/**
 * Handles reading in a GEDCOM file and parsing into an internal representation.
 * Still a work-in-progress.
 * @author Chris Mosher
 */
public final class Gedcom
{
    private Gedcom()
    {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] rArg) throws InvalidLevel, IOException
    {
        if (rArg.length != 1)
        {
            throw new IllegalArgumentException("usage: java Gedcom gedcomfile");
        }

        GedcomTree gt = parseFile(new File(rArg[0]));
        System.out.println(gt.toString());
    }

    public static GedcomTree parseFile(File in) throws IOException,
        UnsupportedEncodingException, FileNotFoundException, InvalidLevel
    {
        String charset = getCharset(in);
        return readTree(in, charset);
    }

    protected static GedcomTree readTree(File fileIn, String charset)
        throws UnsupportedEncodingException, FileNotFoundException,
        InvalidLevel
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                fileIn), charset));
            return readTree(in);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (Throwable ignore)
                {
                    ignore.printStackTrace();
                }
            }
        }
    }

    public static GedcomTree readTree(final BufferedReader reader)
        throws InvalidLevel
    {
        final GedcomParser parser = new GedcomParser(reader);

        final GedcomTree tree = new GedcomTree();
        for (final GedcomLine line : parser)
        {
            tree.appendLine(line);
        }

        final GedcomConcatenator concat = new GedcomConcatenator(tree);
        concat.concatenate();

        return tree;
    }

    public static String getCharset(final File f) throws IOException
    {
        InputStream in = null;
        try
        {
            in = new FileInputStream(f);
            return guessGedcomCharset(in);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (Throwable ignore)
                {
                    ignore.printStackTrace();
                }
            }
        }
    }

    public static String guessGedcomCharset(final InputStream in)
        throws IOException
    {
        // read first four bytes of input stream
        int b0 = in.read();
        if (b0 == -1)
            return "";
        int b1 = in.read();
        if (b1 == -1)
            return "";
        int b2 = in.read();
        if (b2 == -1)
            return "";
        int b3 = in.read();
        if (b3 == -1)
            return "";

        // build a word from the first two bytes,
        // assuming little-endian byte order
        int w0 = 0;
        w0 |= b1;
        w0 <<= 8;
        w0 |= b0;

        // build a longword from the first four bytes,
        // assuming little-endian byte order
        int i0 = 0;
        i0 |= b3;
        i0 <<= 8;
        i0 |= b2;
        i0 <<= 8;
        i0 |= b1;
        i0 <<= 8;
        i0 |= b0;

        if (i0 == 0x0000feff || i0 == 0x00000030)
        {
            return "UTF-32";
        }

        if (i0 == 0xfffe0000 || i0 == 0x30000000)
        {
            return "UTF-32";
        }

        if (w0 == 0x0000feff || w0 == 0x00000030)
        {
            return "UTF-16";
        }

        if (w0 == 0x0000fffe || w0 == 0x00003000)
        {
            return "UTF-16";
        }

        if (b0 == 0x000000ef && b1 == 0x000000bb && b2 == 0x000000bf)
        {
            return "UTF-8";
        }

        BufferedReader bin = new BufferedReader(new InputStreamReader(in,
            "US-ASCII"));
        bin.readLine(); // ignore rest of header line

        String s = bin.readLine();
        while (s != null && s.length() > 0 && s.charAt(0) != '0')
        {
            if (s.startsWith("1 CHAR"))
            {
                s = s.toUpperCase();
                if (s.indexOf("WIN", 6) >= 0)
                {
                    return "windows-1252";
                }
                if (s.indexOf("ANSI", 6) >= 0)
                {
                    return "windows-1252";
                }
                if (s.indexOf("UTF-8", 6) >= 0)
                {
                    return "UTF-8";
                }
                if (s.indexOf("DOS", 6) >= 0)
                {
                    return "Cp850";
                }
                if (s.indexOf("PC", 6) >= 0)
                {
                    return "Cp850";
                }
                if (s.indexOf("OEM", 6) >= 0)
                {
                    return "Cp850";
                }
                if (s.indexOf("ASCII", 6) >= 0)
                {
                    return "windows-1252";
                }
                if (s.indexOf("MAC", 6) >= 0)
                {
                    return "MacRoman";
                }
                if (s.indexOf("ANSEL", 6) >= 0)
                {
                    return "x-gedcom-ansel";
                }
            }
            s = bin.readLine();
        }

        return "US-ASCII";
    }
}
