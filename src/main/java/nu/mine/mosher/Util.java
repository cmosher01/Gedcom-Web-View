package nu.mine.mosher;

import nu.mine.mosher.xml.SimpleXml;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings({ "unused", "WeakerAccess" }) /* Many of these methods are used only in templates */
public final class Util {
    private static final URL TEISH = initTeish();

    private static URL initTeish() {
        try {
            return new URL("https://cdn.rawgit.com/cmosher01/teish/1.3/src/main/resources/teish.xslt");
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private Util() {
        throw new IllegalStateException();
    }

    public static String e(final String s) {
        return s.replace("<", "&lt;");
    }

    public static String q(final String s) {
        if (s == null) {
            return "";
        }
        final String t = s.trim();
        if (t.isEmpty()) {
            return "";
        }
        return "\u201c" + t + "\u201d";
    }

    public static UUID uuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final Throwable e) {
            return null;
        }
    }

    public static String styleCitation(final String citation) {
        try {
            return tryStyleCitation(citation);
        } catch (final Throwable e) {
            e.printStackTrace();
            /* fall back to displaying without formatting */
            return citation;
        }
    }

    public static String tryStyleCitation(final String citation) throws SAXParseException, IOException, TransformerException {
        if (citation.startsWith("<bibl") || citation.startsWith("<?xml")) {
            return teiStyle(citation);
        }
        return period(links(italics(qq(citation))));
    }

    public static String italics(final String s) {
        return s.replaceAll("\\b_(.+?)_\\b", "<span class=\"published\">$1</span>");
    }

    public static String qq(final String s) {
        return s.replaceAll("(^|\\W)\"(\\S.*?\\S)\"(\\W|$)", "$1\u201c$2\u201d$3");
    }

    public static String links(final String s) {
        return s.replaceAll("\\b(\\w+?://\\S+?)\\s", "<a href=\"$1\">$1</a> ");
    }

    public static String period(final String s) {
        return s.replaceFirst("(\\w)$", "$1.");
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static Collator createCollator() {
        final Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);
        return collator;
    }



    public static String teiStyle(final String tei) throws SAXParseException, IOException, TransformerException {
        if (tei.startsWith("<bibl")) {
            return teiStyle(wrapTeiBibl(tei));
        }
        if (!tei.startsWith("<?xml")) {
            return tei;
        }
        return new SimpleXml(tei).transform(readFromUrl(TEISH));
    }

    private static String wrapTeiBibl(final String bibl) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//            "<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n" +
            "<TEI xml:lang=\"en\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n" +
            "    <teiHeader>\n" +
            "        <fileDesc/>\n" +
            "        <profileDesc/>\n" +
            "    </teiHeader>\n" +
            "    <text xml:lang=\"en\">\n" +
            "        <body>\n" +
            "            <ab>\n" +
            bibl +
            "            </ab>\n" +
            "        </body>\n" +
            "    </text>\n" +
            "</TEI>\n";
    }

    public static String readFromUrl(final URL source) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(source.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining("\n"));
        }
    }
}
