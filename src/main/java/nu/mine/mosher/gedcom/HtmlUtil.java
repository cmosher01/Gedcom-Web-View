package nu.mine.mosher.gedcom;

public final class HtmlUtil {
    private HtmlUtil() {
        throw new IllegalStateException();
    }

    public static String escapeHtml(final String s) {
        return s
        .replaceAll("&","&amp;")
        .replaceAll("<","&lt;")
        .replaceAll(">","&gt;")
        .replaceAll("\"","&quot;");
    }
}
