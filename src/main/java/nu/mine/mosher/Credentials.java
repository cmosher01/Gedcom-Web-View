package nu.mine.mosher;

import nu.mine.mosher.security.password.*;
import spark.Request;

import java.util.*;

public class Credentials {
    private static final String BASIC = "Basic ";

    private final boolean valid;

    private Credentials(final boolean valid) {
        this.valid = valid;
    }



    public interface Store {
        String passwordFor(String user);
    }

    public static Credentials fromSession(final Request request, final Store store) {
        return new Credentials(checkValid(request.headers("Authorization"), store));
    }

    public boolean valid() {
        return this.valid;
    }



    private static boolean checkValid(final String authorization, final Store store) {
        if (Objects.isNull(authorization) || authorization.isEmpty()) {
            return false;
        }

        if (!authorization.startsWith(BASIC)) {
            return false;
        }

        final String base64Credentials = authorization.substring(BASIC.length()); // username:password

        final String[] credentials = new String(Base64.getDecoder().decode(base64Credentials)).split(":", -1);

        if (credentials.length < 2) {
            return false;
        }

        final String passwordHash = store.passwordFor(credentials[0]);
        if (passwordHash.isEmpty()) {
            return false;
        }

        try {
            if (!StrongHash.isPasswordValid(credentials[1], passwordHash)) {
                return false;
            }
        } catch (final HashedString.InvalidFormat invalidFormat) {
            invalidFormat.printStackTrace();
            return false;
        }

        return true;
    }
}
