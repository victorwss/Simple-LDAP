package ninja.javahacker.simpleldap;

import javax.naming.AuthenticationException;

/**
 * Denotes that a LDAP authentication problem was not specified.
 * @author Victor Williams Stafusa da Silva
 */
public class UnspecifiedAuthenticationException extends AuthenticationFailedException {

    /**
     * The serial version identifier.
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Sole constructor.
     * @param cause The cause of this exception containing further details.
     */
    public UnspecifiedAuthenticationException(AuthenticationException cause) {
        super(cause);
    }
}
