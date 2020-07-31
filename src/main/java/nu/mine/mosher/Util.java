package nu.mine.mosher;

import nu.mine.mosher.collection.NoteList;
import nu.mine.mosher.gedcom.model.*;
import nu.mine.mosher.gedcom.model.Source;
import nu.mine.mosher.xml.TeiToXhtml5;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Collator;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import static nu.mine.mosher.gedcom.GedcomTag.*;

@SuppressWarnings({ "unused", "WeakerAccess" }) /* Many of these methods are used only in templates */
public final class Util {
    private static final List<PathReplacement> pathReplacements = readPathReplacements();

    private static List<PathReplacement> readPathReplacements() {
        try {
            return
                Files.lines(Paths.get("gedcom/REGEX_PATH_TO_URL")).
                map(PathReplacement::parse).
                collect(Collectors.toUnmodifiableList());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
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
            sb.append(esc(author));
        }

        final String title = noPunc(filterTitle(src.getTitle()));
        if (is(author) && is(title)) {
            sb.append(", ");
        }
        if (is(title)) {
            sb.append("<i>").append(esc(title)).append("</i>");
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
            return htmlToXhtml("   <!doctype  html>   \n   "+s);
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
            low.contains("<hr") ||
            low.contains("href=");
    }

    private static boolean looksLikeTei(final String s) {
        return s.startsWith("<bibl") || s.startsWith("<text") || s.startsWith("<?xml");
    }

    public static String qq(final String s) {
        return s.replaceAll("(^|\\W)\"(\\S.*?\\S)\"(\\W|$)", "$1\u201c$2\u201d$3");
    }

    public static String links(final String s) {
        return esc(s)
            .replaceAll("\\b(\\w+?://\\S+?)(\\s|[<>{}\"|\\\\^`\\]]|$)", "<a href=\"$1\">$1</a>$2")
            .replaceAll("([^/.]www\\.[a-zA-Z]\\S*?)(\\s|[<>{}\"|\\\\^`\\]]|$)", "<a href=\"http://$1\">$1</a>$2");
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
            .replace("XXXX", "<span class=\"placeholder\">\u00d7\u00d7\u00d7\u00d7</span>")
            .replace("XX", "<span class=\"placeholder\">\u00d7\u00d7</span>");
    }

    public static String eventDate(final Event e) {
        if (e == null || e.getDate() == null || e.getDate().getTabularString().equals("?")) {
            return unk("");
        }
        return unk(e.getDate().getTabularString().replaceAll("-", "\u2012"));
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



    public static String teiStyle(final String tei) throws SAXException, IOException, TransformerException, ParserConfigurationException {
        if (tei.startsWith("<bibl")) {
            return teiStyle(wrapTeiBibl(filterBibl(tei)));
        }
        if (tei.startsWith("<text")) {
            return teiStyle(wrapTeiText(tei));
        }
        if (!tei.startsWith("<?xml")) {
            return tei;
        }

        return putTeiThroughPipeline(tei);
    }

    private static String putTeiThroughPipeline(final String tei) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream(2048);
        final BufferedInputStream inXml = new BufferedInputStream(new ByteArrayInputStream(tei.getBytes(StandardCharsets.UTF_8)));
        final BufferedOutputStream outXhtml5 = new BufferedOutputStream(result);
        TeiToXhtml5.transform(inXml, outXhtml5, false);
        outXhtml5.close();

        String xhtml5 = result.toString(StandardCharsets.UTF_8.name());
        // kludge to remove XHTML namespace:
        xhtml5 = xhtml5.replace("xmlns=\"http://www.w3.org/1999/xhtml\"", "");
        return xhtml5;
    }

    /*
        Kludge to remove leading space on citation, and add missing double quotes around unpublished titles.
        this is far from perfect. TODO find a better solution
    */
    private static String filterBibl(final String bibl) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputStream streamXml = new ByteArrayInputStream(bibl.getBytes(StandardCharsets.UTF_8));
        final Document document = factory.newDocumentBuilder().parse(streamXml);

        final NodeList titles = document.getElementsByTagName("title");

        for (int i = 0; i < titles.getLength(); ++i) {
            final Node node = titles.item(i);
            if (node instanceof Element) {
                final String level = ((Element)node).getAttribute("level");
                if (level.equalsIgnoreCase("u") || level.equalsIgnoreCase("a")) {
                    {
                        final Node before = node.getPreviousSibling();
                        if (Objects.isNull(before) || !(before instanceof Text)) {
                            node.getParentNode().insertBefore(document.createTextNode("\u201C"), node);
                        } else {
                            final String t = ((Text)before).getWholeText();
                            if (!(t.endsWith("\u201C") || t.endsWith("\""))) {
                                node.getParentNode().insertBefore(document.createTextNode("\u201C"), node);
                            }
                        }
                    }

                    {
                        final Node after = node.getNextSibling();
                        if (Objects.isNull(after) || !(after instanceof Text)) {
                            node.getParentNode().appendChild(document.createTextNode("\u201D"));
                        } else {
                            final String t = ((Text)after).getWholeText();
                            if (!(t.startsWith("\u201D") || t.startsWith("\""))) {
                                node.getParentNode().insertBefore(document.createTextNode("\u201D"), after);
                            }
                        }
                    }
                }
            }
        }

        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, Boolean.TRUE.toString());
        final StringWriter out = new StringWriter(1024);
        transformer.transform(new DOMSource(document), new StreamResult(out));

        return out.toString().replaceFirst("<bibl>\\s+", "<bibl>");
    }

    private static String wrapTeiText(final String text) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
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
            "<text>" +
            "<body>" +
            "<ab>" +
            bibl +
            "</ab>" +
            "</body>" +
            "</text>");
    }

    public static String getAttLink(final MultimediaReference att) {
        if (att.wasUri()) {
            return esc(att.toString());
        }

        return applyPathReplacements(att.toString());
    }

    private static String applyPathReplacements(String s) {
        for (final PathReplacement repl : Util.pathReplacements) {
            s = repl.applyTo(s);
        }
        return s;
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
        return esc(apid.get().getLink().get().toASCIIString());
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

    private static String htmlToXhtml(final String html) {
        final org.jsoup.nodes.Document document = Jsoup.parseBodyFragment(html);
        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().charset(StandardCharsets.UTF_8);

        final List<org.jsoup.nodes.Node> garbage = new ArrayList<>(4);

        for (final org.jsoup.nodes.Node node : document.childNodes()) {
            if (!(node instanceof org.jsoup.nodes.Element)) {
                garbage.add(node);
            } else if (!((org.jsoup.nodes.Element)node).tagName().equalsIgnoreCase("HTML")) {
                garbage.add(node);
            } else {
                final org.jsoup.nodes.Element elem = (org.jsoup.nodes.Element)node;
                elem.tagName("DIV");
                for (final org.jsoup.nodes.Node node2 : elem.childNodes()) {
                    if (!(node2 instanceof org.jsoup.nodes.Element)) {
                        garbage.add(node2);
                    } else if (!((org.jsoup.nodes.Element)node2).tagName().equalsIgnoreCase("BODY")) {
                        garbage.add(node2);
                    } else {
                        final org.jsoup.nodes.Element elem2 = (org.jsoup.nodes.Element)node2;
                        elem2.tagName("DIV");
                    }
                }
            }
        }

        for (final org.jsoup.nodes.Node node : garbage) {
            node.remove();
        }

        return document.html();
    }



    private static class PathReplacement {
        private final String regex;
        private final String replacement;
        public static PathReplacement parse(final String s) {
            final String[] p = s.split(";", 2);
            return new PathReplacement(p[0],p[1]);
        }
        public PathReplacement(String regex, String replacement) {
            this.regex = regex;
            this.replacement = replacement;
        }
        public String applyTo(final String path) {
            return path.replaceAll(this.regex, this.replacement);
        }
    }
}
