package ninja.javahacker.simpleldap;

/**
 * Denotes that a LDAP authentication problem happened due to the password being incorrect.
 * @author Victor Williams Stafusa da Silva
 */
public class IncorrectPasswordException extends AuthenticationFailedException {

    /**
     * The serial version identifier.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Sole constructor.
     * @param cause The cause of this exception containing further details.
     */
    public IncorrectPasswordException(Throwable cause) {
        super(cause);
    }
}
