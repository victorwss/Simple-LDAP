package ninja.javahacker.simpleldap;

/**
 * Denotes that a LDAP authentication problem happened due to the username being not found.
 * @author Victor Williams Stafusa da Silva
 */
public class UserNotFoundException extends AuthenticationFailedException {

    /**
     * The serial version identifier.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Sole constructor.
     */
    public UserNotFoundException() {
    }
}
