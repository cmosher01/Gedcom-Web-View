package nu.mine.mosher;

public class RbacRole {
    private final boolean signedIn;
    private final boolean authorized;

    public RbacRole(final boolean signedIn, final boolean authorized) {
        this.signedIn = signedIn;
        this.authorized = authorized;
        if (this.authorized && !this.signedIn) {
            throw new IllegalStateException();
        }
    }

    public boolean signedIn() {
        return this.signedIn;
    }

    public boolean authorized() {
        return this.authorized;
    }
}
