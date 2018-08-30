package ninja.javahacker.simpleldap;

/**
 * Denotes that a LDAP authentication problem happened.
 *
 * <p>Refer to the specific subclasses to inquire what exactly was the authentication problem.</p>
 *
 * @author Victor Williams Stafusa da Silva
 */
public abstract class AuthenticationFailedException extends Exception {

    /**
     * The serial version identifier.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to be used by subclasses only without specifying a cause.
     */
    protected AuthenticationFailedException() {
    }

    /**
     * Constructor to be used by subclasses only specifying a cause.
     * @param cause The cause of this exception containing further details.
     */
    protected AuthenticationFailedException(Throwable cause) {
        super(cause);
    }
}
