package ninja.javahacker.simpleldap;

/**
 * Denotes that the LDAP server coudn't be connected or that it did not answered in an expected way.
 *
 * @author Victor Williams Stafusa da Silva
 */
public class LdapConnectionException extends Exception {

    /**
     * The serial version identifier.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Sole constructor.
     * @param cause The cause of this exception containing further details.
     */
    public LdapConnectionException(Throwable cause) {
        super(cause);
    }
}
