package nu.mine.mosher.gedcom;

import java.text.Collator;

public class CollatorFactory {
    public static Collator create() {
        final Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);
        return collator;
    }
}
