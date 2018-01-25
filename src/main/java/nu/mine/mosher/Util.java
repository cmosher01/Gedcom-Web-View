package nu.mine.mosher;

import nu.mine.mosher.gedcom.model.Citation;
import nu.mine.mosher.gedcom.model.MultimediaReference;
import nu.mine.mosher.gedcom.model.Source;
import nu.mine.mosher.xml.SimpleXml;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "WeakerAccess" }) /* Many of these methods are used only in templates */
public final class Util {
    private static final URL TEISH = initTeish();

    private static URL initTeish() {
        try {
            return new URL("https://cdn.rawgit.com/cmosher01/teish/1.5/src/main/resources/teish.xslt");
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private Util() {
        throw new IllegalStateException();
    }



    public static String esc(final String s) {
        return s
            .replace("&", "&amp;")
            .replace("<","&lt;")
            .replace(">","&gt;")
            .replace("\"","&quot;");
    }

    public static UUID uuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final Throwable e) {
            return null;
        }
    }

    private static final Pattern patternName = Pattern.compile("Name: (.*?)(;|$)");
    private static final Pattern patternLocn = Pattern.compile("Location: (.*?)(;|$)");
    private static final Pattern patternDate = Pattern.compile("Date: (.*?)(;|$)");
    private static final Pattern patternFirstField = Pattern.compile("^(.*?)(;|$)");

    private static String parsePublication(final String pub) {
        Matcher m;
        String name = "";
        String location = "";
        String date = "";

        m = patternName.matcher(pub);
        if (m.find()) {
            name = safe(m.group(1));
        }

        m = patternLocn.matcher(pub);
        if (m.find()) {
            location = safe(m.group(1));
        }

        m = patternDate.matcher(pub);
        if (m.find()) {
            date = safe(m.group(1));
        }

        if (is(location) && is(date) && !is(name)) {
            m = patternFirstField.matcher(pub);
            if (m.find()) {
                name = safe(m.group(1));
            }
        }

        if (is(name) && is(date) && is(location)) {
            return location+": "+name+", "+date;
        }

        return noPunc(pub);
    }

    private static boolean is(final String s) {
        return (s != null) && !s.isEmpty();
    }

    private static String safe(final String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static String styleCitation(final Citation cita) {
        final String page = cita.getPage();

        /* full, TEI-style, citation */
        if (page.startsWith("<bibl") || page.startsWith("<text") || page.startsWith("<?xml")) {
            try {
                return teiStyle(page);
            } catch (final Throwable e) {
                /* invalid XML, so display it raw */
                e.printStackTrace();
                return esc(page);
            }
        }

        /* legacy-style citation */
        return buildCitation(cita);
    }

    private static String buildCitation(final Citation cita) {
        final StringBuilder sb = new StringBuilder(128);

        final Source src = cita.getSource();

        final String author = noPunc(src.getAuthor());
        if (is(author)) {
            sb.append(author);
        }

        final String title = noPunc(filterTitle(src.getTitle()));
        if (is(author) && is(title)) {
            sb.append(", ");
        }
        if (is(title)) {
            sb.append("<i>").append(title).append("</i>");
        }

        final String publ = parsePublication(src.getPublication());
        if (is(publ)) {
            sb.append(" (").append(publ).append(")");
        }

        final String page = links(noPunc(filterPage(safe(cita.getPage()))));
        if (is(page)) {
            sb.append(", ").append(page);
        }

        sb.append(".");

        return sb.toString();
    }

    private static String filterPage(final String page) {
        return page
            /* Ancestry.com tends to use semi-colons in its citations */
            .replace(';', ',')
            .replaceAll("Page:", "p.")
            .replaceAll("Family History Library Film:", "FHL microfilm");
    }

    private static String filterTitle(final String title) {
        return title
            /* remove Web: */
            .replaceAll("Web: ", "");
    }

    private static String noPunc(final String s) {
        return s.replaceFirst("[.,;:]$","");
    }

    public static String styleTranscripts(final Citation citation) {
        return styleTranscript(citation.getExtraText()) + styleTranscript(citation.getSource().getText());
    }

    public static String styleTranscript(final String s) {
        if (s.startsWith("<bibl") || s.startsWith("<text") || s.startsWith("<?xml")) {
            try {
                return teiStyle(s);
            } catch (final Throwable e) {
                /* invalid XML, so display it raw */
                e.printStackTrace();
                return esc(s);
            }
        }
        return s;
    }

//    public static String qq(final String s) {
//        return s.replaceAll("(^|\\W)\"(\\S.*?\\S)\"(\\W|$)", "$1\u201c$2\u201d$3");
//    }

    public static String links(final String s) {
        return s.replaceAll("\\b(\\w+?://\\S+?)\\s", "<a href=\"$1\">$1</a> ");
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
        if (tei.startsWith("<text")) {
            return teiStyle(wrapTeiText(tei));
        }
        if (!tei.startsWith("<?xml")) {
            return tei;
        }
        return new SimpleXml(tei).transform(readFromUrl(TEISH));
    }

    private static String wrapTeiText(final String text) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            //            "<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n" +
            "<TEI xml:lang=\"en\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n" +
            "  <teiHeader>\n" +
            "    <fileDesc>\n" +
            "      <titleStmt/>\n" +
            "      <publicationStmt/>\n" +
            "      <sourceDesc/>\n" +
            "    </fileDesc>\n" +
            "  </teiHeader>\n" +
               text +
            "</TEI>\n";
    }

    private static String wrapTeiBibl(final String bibl) {
        return wrapTeiText(
            "<text xml:lang=\"en\">\n" +
            "  <body>\n" +
            "    <ab>\n" +
                   bibl +
            "    </ab>\n" +
            "  </body>\n" +
            "</text>\n");
    }

    public static String readFromUrl(final URL source) throws IOException {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(source.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining("\n"));
        }
    }

    public static String getAttLink(final MultimediaReference att) {
        if (att.wasUri()) {
            return att.toString();
        }

        return replaceFilePathPrefix(standardizePathCharacters(att.toString()));
    }

    private static String replaceFilePathPrefix(final String s)
    {
        // TODO: make prefixes configurable
        return s.replaceFirst("^.*/Family Tree Maker/", "/ftm/");
    }

    public static String standardizePathCharacters(final String pathAnyOs) {
        return pathAnyOs.replaceAll("\\\\", "/");
    }

    public static int size(final Collection<?> r) {
        return r.size();
    }

    public static<T> List<T> asList(final Collection<T> r) {
        return new ArrayList<>(r);
    }
}
