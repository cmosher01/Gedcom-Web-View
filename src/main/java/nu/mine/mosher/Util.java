package nu.mine.mosher;

import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.model.*;
import nu.mine.mosher.xml.SimpleXml;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import static nu.mine.mosher.gedcom.GedcomTag.*;

@SuppressWarnings({ "unused", "WeakerAccess" }) /* Many of these methods are used only in templates */
public final class Util {
    private static final URL TEISH = initTeish();

    private static URL initTeish() {
        try {
            return new URL("https://raw.githack.com/cmosher01/Tei-Server/master/src/main/resources/teish.xslt");
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

        return pub;
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
        if (looksLikeTei(page)) {
            return teiStyleOrError(page);
        }

        /* legacy-style citation */
        return buildCitation(cita);
    }

    private static String teiStyleOrError(final String page) {
        try {
            return teiStyle(page);
        } catch (final Throwable e) {
            /* invalid XML, so display it raw */
            e.printStackTrace();
            return esc(page);
        }
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

        final String publ = links(noPunc(parsePublication(src.getPublication())));
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
            .replaceAll("Family History Library Film", "FHL microfilm")
            .replaceAll("Family History Film", "FHL microfilm");
    }

    private static String filterTitle(final String title) {
        return title
            /* remove Web: */
            .replaceAll("Web: ", "");
    }

    private static String noPunc(final String s) {
        return s.replaceFirst("[.,;:]$","");
    }

    private static String joinWords(final String a, final String b) {
        if (b.isEmpty()) {
            return a;
        }
        if (a.isEmpty()) {
            return b;
        }
        return a + " " + b;
    }
    public static String styleTranscripts(final Citation citation) {
        return joinWords(styleTranscript(citation.getExtraText()), styleTranscript(citation.getSource().getText()));
    }

    public static String styleTranscript(final String s) {
        if (s.trim().isEmpty()) {
            return "";
        }

        if (looksLikeTei(s)) {
            return teiStyleOrError(s);
        }

        if (looksLikeHtml(s)) {
            return removeDoctype(s);
        }

        return filterPlainTranscript(s);
    }

    private static String filterPlainTranscript(final String s) {
        return links(qq(s));
    }

    private static boolean looksLikeHtml(final String s) {
        final String low = s.toLowerCase();
        return
            low.startsWith("<html") ||
            low.startsWith("<!doctype html") ||
            low.contains("<table") ||
            low.contains("<img") ||
            low.contains("<p>") ||
            low.contains("<br") ||
            low.contains("<div") ||
            low.contains("<span") ||
            low.contains("<i>") ||
            low.contains("<u>") ||
            low.contains("href");
    }

    private static boolean looksLikeTei(final String s) {
        return s.startsWith("<bibl") || s.startsWith("<text") || s.startsWith("<?xml");
    }

    public static String qq(final String s) {
        return s.replaceAll("(^|\\W)\"(\\S.*?\\S)\"(\\W|$)", "$1\u201c$2\u201d$3");
    }

    public static String links(final String s) {
        return s
            .replaceAll("\\b(\\w+?://\\S+?)(\\s|[]<>{}\"|\\\\^`]|$)", "<a href=\"$1\">$1</a>$2")
            .replaceAll("([^/.]www\\.[a-zA-Z]\\S*?)(\\s|[]<>{}\"|\\\\^`]|$)", "<a href=\"http://$1\">$1</a>$2");
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String unk(final String s) {
        if (s == null || s.isEmpty()) {
            return "\u00a0<span class=\"placeholder\">\u2e3a</span>";
        }
        return markupUnknownDateFields(s);
    }

    private static String markupUnknownDateFields(String s) {
        return s
            .replace("XXXX", "<span class=\"placeholder\">XXXX</span>")
            .replace("XX", "<span class=\"placeholder\">XX</span>");
    }

    public static String eventDate(final Event e) {
        if (e == null || e.getDate() == null || e.getDate().getTabularString().equals("?")) {
            return unk("");
        }
        return unk(e.getDate().getTabularString());
    }

    private static final Set<String> setPrimaryEventTypes = new HashSet<>();
    static {
        setPrimaryEventTypes.add(EventNames.getName(BIRT));
        setPrimaryEventTypes.add(EventNames.getName(DEAT));
        setPrimaryEventTypes.add(EventNames.getName(MARR));
        setPrimaryEventTypes.add(EventNames.getName(DIV));
        setPrimaryEventTypes.add(EventNames.getName(ANUL));
        setPrimaryEventTypes.add("name");
    }
    private static final Set<String> setSecondaryEventTypes = new HashSet<>();
    static {
        setSecondaryEventTypes.add(EventNames.getName(CHR));
        setSecondaryEventTypes.add(EventNames.getName(BAPM));
        setSecondaryEventTypes.add(EventNames.getName(BURI));
        setSecondaryEventTypes.add(EventNames.getName(CREM));
        setSecondaryEventTypes.add(EventNames.getName(RESI));
        setSecondaryEventTypes.add(EventNames.getName(CENS));
        setSecondaryEventTypes.add(EventNames.getName(PROB));
        setSecondaryEventTypes.add(EventNames.getName(WILL));
        setSecondaryEventTypes.add(EventNames.getName(MARB));
        setSecondaryEventTypes.add(EventNames.getName(MARC));
        setSecondaryEventTypes.add(EventNames.getName(MARL));
        setSecondaryEventTypes.add(EventNames.getName(MARS));
    }
    private static final Pattern LABELLED_EVENT_TYPE = Pattern.compile("([A-Za-z \\[\\]]+)(: *.*)");
    public static String eventType(final Event e) {
        final String fullType = e.getType();
        final Matcher match = LABELLED_EVENT_TYPE.matcher(fullType);

        String label;
        String value;
        if (match.matches()) {
            label = match.group(1);
            value = match.group(2);
            if (label.equalsIgnoreCase("name")) {
                if (value.startsWith(": ")) {
                    value = value.substring(2); // remove ": "
                }
                value = ": "+markupPersonName(value);
            }
        } else {
            label = fullType;
            value = "";
        }

        final String cls;
        if (setPrimaryEventTypes.contains(label)) {
            cls = "vitalTypePrimaryLabel";
        } else if (setSecondaryEventTypes.contains(label)) {
            cls = "vitalTypeSecondaryLabel";
        } else {
            cls = "vitalTypeLabel";
        }

        return "<span class=\""+cls+"\">"+label+"</span>"+value;
    }

    private static String markupPersonName(String value) {
        return "<span class=\"personNameEvent\">"+markupSurname(value)+"</span>";
    }

    private static String markupSurname(String value) {
        final StringBuilder sb = new StringBuilder(value.length()+40);
        boolean inSurname = false;
        for(int cp : value.codePoints().toArray()) {
            if (Character.isBmpCodePoint(cp)) {
                final char c = (char)cp;
                if (c == '/') {
                    if (inSurname) {
                        inSurname = false;
                        sb.append("</span>");
                    } else {
                        inSurname = true;
                        sb.append("<span class=\"personSurnameEvent\">");
                    }
                } else {
                    sb.append(c);
                }
            } else if (Character.isValidCodePoint(cp)) {
                sb.append(Character.highSurrogate(cp));
                sb.append(Character.lowSurrogate(cp));
            } else {
                sb.append('?');
            }
        }
        if (inSurname) {
            // the case of the missing closing slash
            sb.append("</span>");
        }
        return sb.toString();
    }

    public static Collator createCollator() {
        final Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);
        return collator;
    }



    public static String teiStyle(final String tei) throws SAXParseException, IOException, TransformerException {
        if (tei.startsWith("<bibl")) {
            return teiStyle(wrapTeiBibl(filterBibl(tei)));
        }
        if (tei.startsWith("<text")) {
            return teiStyle(wrapTeiText(tei));
        }
        if (!tei.startsWith("<?xml")) {
            return tei;
        }
        return removeDoctype(new SimpleXml(tei).transform(readFromUrl(TEISH)));
    }

    /* kludge to remove leading space on citation */
    private static String filterBibl(final String bibl) {
        return bibl.replaceFirst("<bibl>\\s+", "<bibl>");
    }

    private static String removeDoctype(final String html) {
        return html.replaceFirst("(?is)^\\s*<!doctype.+?>\\s*", "");
    }

    private static String wrapTeiText(final String text) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            //"<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>" +
            "<TEI xml:lang=\"en\" xmlns=\"http://www.tei-c.org/ns/1.0\">" +
            "<teiHeader>" +
            "<fileDesc>" +
            "<titleStmt/>" +
            "<publicationStmt/>" +
            "<sourceDesc/>" +
            "</fileDesc>" +
            "</teiHeader>" +
            text +
            "</TEI>";
    }

    private static String wrapTeiBibl(final String bibl) {
        return wrapTeiText(
            "<text xml:lang=\"en\">" +
            "<body>" +
            "<ab>" +
            bibl +
            "</ab>" +
            "</body>" +
            "</text>");
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

    public static String footnum(final int i) {
        // kludge assumes less than 100 footnotes total
        final int f = i+1;
        return (f < 10 ? "\u2007" : "") + f;
    }

    public static boolean isCitation(final NoteList notes, final int i) {
        final int f = i+1;
        return notes.getNote(f) instanceof Citation;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean isApid(final Optional<AncestryPersona> apid) {
        return apid.isPresent() && apid.get().getLink().isPresent();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static String getApid(final Optional<AncestryPersona> apid) {
        if (!isApid(apid)) {
            throw new IllegalStateException("invalid _APID");
        }
        //noinspection OptionalGetWithoutIsPresent
        return apid.get().getLink().get().toASCIIString();
    }

    public static boolean hasIssue(final Person person) {
        for (Partnership part : person.getPartnerships()) {
            if (part.getChildRelations().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasLineage(final Person person) {
        return 0 < person.getFathers().size() + person.getMothers().size();
    }

    public static boolean privatize(final Privatizable p, final RbacRole role) {
        if (Objects.isNull(p)) {
            return false;
        }
        return p.isPrivate() && !role.authorized();
    }

    public static String getBirthdate(final Person person) {
        Event birth = null;
        for (final Event event : person.getEvents()) {
            if (event.getType().equals("birth")) {
                birth = event;
            }
        }
        return Util.eventDate(birth);
    }
}
