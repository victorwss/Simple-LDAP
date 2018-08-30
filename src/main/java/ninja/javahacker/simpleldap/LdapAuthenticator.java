package ninja.javahacker.simpleldap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Objects;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

/**
 * Object used to find the DN from a given login in LDAP and authenticate users from login and password.
 *
 * <p>Instances of this class are immutable, serializable and very light to be instantiated. However, caching them is recommended.</p>
 *
 * @author Victor Williams Stafusa da Silva
 */
public final class LdapAuthenticator implements Serializable {

    /**
     * The serial version identifier.
     * @see Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * The LDAP server used for authentication.
     */
    private final LdapServer server;

    /**
     * The root DN used to connect to the server.
     */
    private final String rootDn;

    /**
     * The password of the root DN used to connect to the server.
     */
    private final String rootPassword;

    /**
     * The base DN used to search for user DNs in order to find their logins.
     */
    private final String baseDn;

    /**
     * Create a {@code LdapAuthenticator} instance.
     * @param server The LDAP server used for authentication.
     * @param rootDn The root DN used to connect to the server.
     * @param rootPassword The password of the root DN used to connect to the server.
     * @param baseDn The base DN used to search for user DNs in order to find their logins.
     * @throws LdapConnectionException If there is a problem connecting to the LDAP server. This is specially plausible if
     *         any of the parameters, except for {@code baseDn} is incorrect or if there is some other problem in the proccess of
     *         stablishing a LDAP connection.
     * @throws IllegalArgumentException If any of the parameters is {@code null}.
     */
    public LdapAuthenticator(LdapServer server, String rootDn, String rootPassword, String baseDn) throws LdapConnectionException {
        if (server == null) throw new IllegalArgumentException("The server shouldn't be null.");
        if (rootDn == null) throw new IllegalArgumentException("The admin user shouldn't be null.");
        if (rootPassword == null) throw new IllegalArgumentException("The admin password shouldn't be null.");
        if (baseDn == null) throw new IllegalArgumentException("The base DN shouldn't be null.");
        this.server = server;
        this.rootDn = rootDn;
        this.rootPassword = rootPassword;
        this.baseDn = baseDn;
        try {
            server.createLdapConnection(rootDn, rootPassword);
        } catch (AuthenticationFailedException x) {
            throw new LdapConnectionException(x);
        }
    }

    /**
     * Create a {@code LdapAuthenticator} instance.
     * @param hostname The hostname of the LDAP server.
     * @param port The port used to connect to the LDAP server used for authentication.
     * @param rootDn The root DN used to connect to the server used for authentication.
     * @param rootPassword The password of the root DN used to connect to the server.
     * @param baseDn The base DN used to search for user DNs in order to find their logins.
     * @throws LdapConnectionException If there is a problem connecting to the LDAP server. This is specially plausible if
     *         any of the parameters, except for {@code baseDn} is incorrect or if there is some other problem in the proccess of
     *         stablishing a LDAP connection.
     * @throws IllegalArgumentException If any of the parameters is {@code null} or
     *         if the {@code port} is not in the valid range of 1-65535.
     */
    public LdapAuthenticator(String hostname, int port, String rootDn, String rootPassword, String baseDn)
            throws LdapConnectionException
    {
        this(new LdapServer(hostname, port), rootDn, rootPassword, baseDn);
    }

    /**
     * Escape a login name to ensure that some sensible characters are properly escaped.
     * @param name The login name to be escaped.
     * @return The escaped login name.
     */
    private static String escape(String name) {
        return name
                .replace("(", "\\28")
                .replace(")", "\\29")
                .replace("*", "\\2a")
                .replace("\\", "\\5c")
                .replace("\u0000", "\\00");
    }

    /**
     * Finds the DN of an user in the LDAP server given his/her login.
     * @param login The user's login.
     * @return The user's DN.
     * @throws UserNotFoundException The user couldn't be found in the LDAP server.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     */
    public String findDn(String login) throws LdapConnectionException, UserNotFoundException {
        LdapContext ctx;
        try {
            ctx = server.createLdapConnection(rootDn, rootPassword);
        } catch (UnspecifiedAuthenticationException x) {
            throw new LdapConnectionException(x);
        }

        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String[] atributosParaRetornar = { "distinguishedName" };
        sc.setReturningAttributes(atributosParaRetornar);

        try {
            String filtro = "(&(sAMAccountName=" + escape(login) + "))";
            NamingEnumeration<SearchResult> cursor = ctx.search(baseDn, filtro, sc);
            if (!cursor.hasMoreElements()) throw new UserNotFoundException();

            SearchResult result = cursor.nextElement();
            Attributes att = result.getAttributes();
            return (String) att.get("distinguishedName").get();
        } catch (NamingException x) {
            throw new LdapConnectionException(x);
        }
    }

    /**
     * Asserts that a user given by login can authenticate in a LDAP server or throw an exception if otherwise.
     * @param login The user's login.
     * @param password The user's password.
     * @throws UserNotFoundException The user couldn't be found in the LDAP server.
     * @throws IncorrectPasswordException The user could be found, but his/her password is incorrect.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    public void authenticate(
            String login,
            String password)
            throws LdapConnectionException,
            UserNotFoundException,
            IncorrectPasswordException
    {
        String dn = findDn(login);
        try {
            server.authenticate(dn, password);
        } catch (AuthenticationFailedException x) {
            throw new IncorrectPasswordException(x);
        }
    }

    /**
     * Try to authenticate the user given by login in the LDAP server.
     * @param login The user's login.
     * @param password The user's password.
     * @return {@code true} if the authentication was successfully.
     *         {@code false} if the user couldn't be found or his/her password is incorrect.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_RETURN_FALSE")
    public boolean tryAuthenticate(String login, String password) throws LdapConnectionException {
        try {
            authenticate(login, password);
            return true;
        } catch (UserNotFoundException | IncorrectPasswordException x) {
            return false;
        }
    }

    /**
     * Generates a hash value based on the values of this instance fields.
     * @return A hash value based on the values of this instance fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(server, rootDn, rootPassword, baseDn);
    }

    /**
     * Checks if this {@code LdapAuthenticator} is equals to another given object.
     *
     * <p>Two {@code LdapAuthenticator} instances are equals if they have been constructed using the same parameters.
     * No {@code LdapAuthenticator} instance is considered equal to any object of some other class.</p>
     *
     * @param obj The object to be tested as equals to this object.
     *
     * @return If this instance is equal to the given one.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LdapAuthenticator)) return false;
        LdapAuthenticator other = (LdapAuthenticator) obj;
        return this.server.equals(other.server)
                && this.rootDn.equals(other.rootDn)
                && this.rootPassword.equals(other.rootPassword)
                && this.baseDn.equals(other.baseDn);
    }

    /**
     * Returns a {@link String} presenting the fields of this {@code LdapAuthenticator} except by the {@code rootPassword}.
     * @return A {@link String} presenting the fields of this {@code LdapAuthenticator} except by the {@code rootPassword}.
     */
    @Override
    public String toString() {
        return "(server: " + server + ", ROOT: " + rootDn + ", PASSWORD: [not show], Base DN: " + baseDn + ")";
    }
}
