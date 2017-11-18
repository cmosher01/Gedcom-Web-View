package nu.mine.mosher;

import java.text.Collator;
import java.util.UUID;

@SuppressWarnings({ "unused", "WeakerAccess" }) /* Many of these methods are used only in templates */
public final class Util {
    private Util() {
        throw new IllegalStateException();
    }

    public static String e(final String s) {
        return s.replace("<","&lt;");
    }

    public static String q(final String s) {
        if (s == null) {
            return "";
        }
        final String t = s.trim();
        if (t.isEmpty()) {
            return "";
        }
        return "\u201c"+t+"\u201d";
    }

    public static UUID uuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final Throwable e) {
            return null;
        }
    }

    public static String styleCitation(final String citation) {
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
}
