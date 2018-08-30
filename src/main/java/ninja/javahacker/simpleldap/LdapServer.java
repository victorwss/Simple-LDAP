package ninja.javahacker.simpleldap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Objects;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * Object used to authenticate users from DN and password.
 *
 * <p>Instances of this class are immutable, serializable and very light to be instantiated. However, caching them is recommended.</p>
 *
 * @author Victor Williams Stafusa da Silva
 */
public final class LdapServer implements Serializable {

    /**
     * The serial version identifier.
     * @see Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * Used to create connections with LDAP.
     * @see Context#INITIAL_CONTEXT_FACTORY
     */
    private static final String INITIAL_CTX = "com.sun.jndi.ldap.LdapCtxFactory";

    /**
     * The hostname of the LDAP server.
     */
    private final String hostname;

    /**
     * The port used to connect to the LDAP server.
     */
    private final int port;

    /**
     * Constructs a {@code LdapServer} instance.
     * @param hostname The hostname of the LDAP server.
     * @param port The port used to connect to the LDAP server.
     * @throws IllegalArgumentException If the {@code} hostname parameter is {@code null} or
     *         if the {@code port} is not in the valid range of 1-65535.
     * @throws LdapConnectionException If there was a problem connecting in the LDAP server.
     */
    public LdapServer(String hostname, int port) throws LdapConnectionException {
        if (hostname == null) throw new IllegalArgumentException("The hostname shouldn't be null.");
        if (port <= 0 || port >= 65536) throw new IllegalArgumentException("Illegal port number.");
        this.hostname = hostname;
        this.port = port;
        connect();
    }

    /**
     * Creates an {@link LdapContext} given the user's DN and password, ensuring that it can be authenticated
     * and allowing further actions to be performed in the provided {@link LdapContext}.
     * @param userDn The user's DN.
     * @param password The user's password.
     * @return An {@link LdapContext} authenticated with the user credentials.
     * @throws UnspecifiedAuthenticationException The user couldn't be authenticated in the LDAP server for some reason.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    public LdapContext createLdapConnection(
            String userDn,
            String password)
            throws UnspecifiedAuthenticationException,
            LdapConnectionException
    {
        Hashtable<String, Object> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CTX);
        env.put(Context.PROVIDER_URL, toString());
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);

        try {
            return new InitialLdapContext(env, null);
        } catch (AuthenticationException x) {
            throw new UnspecifiedAuthenticationException(x);
        } catch (NamingException x) {
            throw new LdapConnectionException(x);
        }
    }

    /**
     * Creates a root {@link LdapContext} ensuring that the server may be reached
     * and allowing further actions to be performed in the provided {@link LdapContext}.
     * @return An initial {@link LdapContext}.
     * @throws LdapConnectionException If there was a problem connecting in the LDAP server.
     */
    public LdapContext connect() throws LdapConnectionException {
        Hashtable<String, Object> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CTX);
        env.put(Context.PROVIDER_URL, toString());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);

        try {
            return new InitialLdapContext(env, null);
        } catch (NamingException x) {
            throw new LdapConnectionException(x);
        }
    }

    /**
     * Try to authenticate the user given by login in the LDAP server.
     * @param userDn The user's DN.
     * @param password The user's password.
     * @return {@code true} if the authentication was successfully.
     *         {@code false} if the user couldn't be found or his/her password is incorrect.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_RETURN_FALSE")
    public boolean tryAuthenticate(String userDn, String password) throws LdapConnectionException {
        try {
            createLdapConnection(userDn, password);
            return true;
        } catch (AuthenticationFailedException x) {
            return false;
        }
    }

    /**
     * Asserts that a user given by DN can authenticate in a LDAP server or throw an exception if otherwise.
     * @param userDn The user's DN.
     * @param password The user's password.
     * @throws UnspecifiedAuthenticationException The user couldn't be authenticated in the LDAP server for some reason.
     * @throws LdapConnectionException If there was a problem connecting or searching for the user in the LDAP server.
     *         Notably this do not means that the user credentials are neither correct nor incorrect, it just denotes that
     *         they couldn't even be verified to start with.
     */
    public void authenticate(String userDn, String password) throws LdapConnectionException, UnspecifiedAuthenticationException {
        createLdapConnection(userDn, password);
    }

    /**
     * Generates a hash value based on the values of this instance fields.
     * @return A hash value based on the values of this instance fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(hostname, port);
    }

    /**
     * Checks if this {@code LdapServer} is equals to another given object.
     *
     * <p>Two {@code LdapServer} instances are equals if they have been constructed using the same parameters.
     * No {@code LdapServer} instance is considered equal to any object of some other class.</p>
     *
     * @param obj The object to be tested as equals to this object.
     *
     * @return If this instance is equal to the given one.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LdapServer)) return false;
        LdapServer other = (LdapServer) obj;
        return this.port == other.port && this.hostname.equals(other.hostname);
    }

    /**
     * Returns the URL used to connect in the LDAP server.
     * @return The URL used to connect in the LDAP server.
     */
    @Override
    public String toString() {
        return "ldap://" + hostname + ":" + port;
    }
}
