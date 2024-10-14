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
import lombok.NonNull;

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
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /**
     * Used internally by the {@link #findDn(String)} method.
     */
    private static final String[] DISTINGUISHED_NAME = { "distinguishedName" };

    /**
     * The LDAP server used for authentication.
     */
    @NonNull
    private final LdapServer server;

    /**
     * The root DN used to connect to the server.
     */
    @NonNull
    private final String rootDn;

    /**
     * The password of the root DN used to connect to the server.
     */
    @NonNull
    private final String rootPassword;

    /**
     * The base DN used to search for user DNs in order to find their logins.
     */
    @NonNull
    private final String baseDn;

    /**
     * Create a {@code LdapAuthenticator} instance.
     * @param server The LDAP server used for authentication.
     * @param rootDn The root DN used to connect to the server.
     * @param rootPassword The password of the root DN used to connect to the server.
     * @param baseDn The base DN used to search for user DNs in order to find their logins.
     * @throws IllegalArgumentException If any of the parameters is {@code null}.
     * @throws LdapConnectionException If there is a problem connecting to the LDAP server. This is specially plausible if
     *         any of the parameters, except for {@code baseDn} is incorrect or if there is some other problem in the process of
     *         establishing a LDAP connection.
     */
    public LdapAuthenticator(
            @NonNull LdapServer server,
            @NonNull String rootDn,
            @NonNull String rootPassword,
            @NonNull String baseDn)
            throws LdapConnectionException
    {
        this.server = server;
        this.rootDn = rootDn;
        this.rootPassword = rootPassword;
        this.baseDn = baseDn;
        try {
            server.createLdapConnection(rootDn, rootPassword);
        } catch (UnspecifiedAuthenticationException x) {
            throw new LdapConnectionException(x);
        }
    }

    /**
     * Create a {@code LdapAuthenticator} instance.
     * @param hostname The host name of the LDAP server.
     * @param port The port used to connect to the LDAP server used for authentication.
     * @param rootDn The root DN used to connect to the server used for authentication.
     * @param rootPassword The password of the root DN used to connect to the server.
     * @param baseDn The base DN used to search for user DNs in order to find their logins.
     * @throws LdapConnectionException If there is a problem connecting to the LDAP server. This is specially plausible if
     *         any of the parameters, except for {@code baseDn} is incorrect or if there is some other problem in the proccess of
     *         establishing a LDAP connection.
     * @throws IllegalArgumentException If any of the parameters is {@code null} or
     *         if the {@code port} is not in the valid range of 1-65535.
     */
    public LdapAuthenticator(
            @NonNull String hostname,
            int port,
            @NonNull String rootDn,
            @NonNull String rootPassword,
            @NonNull String baseDn)
            throws LdapConnectionException
    {
        this(new LdapServer(hostname, port), rootDn, rootPassword, baseDn);
    }

    /**
     * Escape a login name to ensure that some sensible characters are properly escaped.
     * @param name The login name to be escaped.
     * @return The escaped login name.
     * @throws IllegalArgumentException If the parameter is {@code null}.
     */
    private static String escape(@NonNull String name) {
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
     * @throws IllegalArgumentException If any of the parameters is {@code null}.
     * @throws UserNotFoundException The user couldn't be found in the LDAP server.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     */
    @SuppressFBWarnings(
            value = "LDAP_INJECTION",
            justification = "We use the escape method exactly to avoid this, but SpotBugs can't understand it."
    )
    public String findDn(@NonNull String login) throws LdapConnectionException, UserNotFoundException {
        LdapContext ctx;
        try {
            ctx = server.createLdapConnection(rootDn, rootPassword);
        } catch (UnspecifiedAuthenticationException x) {
            throw new LdapConnectionException(x);
        }

        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(DISTINGUISHED_NAME);

        try {
            String filter = "(&(sAMAccountName=" + escape(login) + "))";
            NamingEnumeration<SearchResult> cursor = ctx.search(baseDn, filter, sc);
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
     * @throws IllegalArgumentException If any of the parameters is {@code null}.
     * @throws UserNotFoundException The user couldn't be found in the LDAP server.
     * @throws IncorrectPasswordException The user could be found, but his/her password is incorrect.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    public void authenticate(
            @NonNull String login,
            @NonNull String password)
            throws LdapConnectionException,
            UserNotFoundException,
            IncorrectPasswordException
    {
        String dn = findDn(login);
        try {
            server.authenticate(dn, password);
        } catch (UnspecifiedAuthenticationException x) {
            throw new IncorrectPasswordException(x);
        }
    }

    /**
     * Try to authenticate the user given by login in the LDAP server.
     * @param login The user's login.
     * @param password The user's password.
     * @return {@code true} if the authentication was successfully.
     *         {@code false} if the user couldn't be found or his/her password is incorrect.
     * @throws IllegalArgumentException If any of the parameters is {@code null}.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    @SuppressFBWarnings(
            value = "EXS_EXCEPTION_SOFTENING_RETURN_FALSE",
            justification = "That is precisely the purpose of this method."
    )
    public boolean tryAuthenticate(
            @NonNull String login,
            @NonNull String password)
            throws LdapConnectionException
    {
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
