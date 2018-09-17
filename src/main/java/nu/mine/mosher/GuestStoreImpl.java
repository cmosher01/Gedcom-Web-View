package nu.mine.mosher;

import nu.mine.mosher.security.password.StrongHash;

/**
 * Example Credentials.Store that has only only user, "guest",
 * with a strong password (that we only store the uncrackable hash of).
 */
final public class GuestStoreImpl implements Credentials.Store {
    private static final String GUEST_USERNAME = "guest";
    private static final String GUEST_PASSWORD_HASH;
    static {
        String h = "";
        try {
            h = StrongHash.hash(System.getenv("GUEST_PASSWORD"));
        } catch (final Throwable e) {
            h= "";
        }
        GUEST_PASSWORD_HASH = h;
    }

    private static final Credentials.Store INSTANCE = new GuestStoreImpl();

    private GuestStoreImpl() {
    }

    public static Credentials.Store instance() {
        return INSTANCE;
    }

    @Override
    public String passwordFor(final String user) {
        if (!user.equals(GUEST_USERNAME)) {
            return "";
        }

        return GUEST_PASSWORD_HASH;
    }
}
