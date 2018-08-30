/**
 * This package provides a simple utility for connecting in an LDAP server.
 *
 * <p>The {@link LdapAuthenticator} class if the main class to be used in order to authenticate users.</p>
 *
 * <p>A typical example of usage, when the LDAP connection settings are fixed and the users should be
 * authenticated by their logins and passwords, would be the following:</p>
 *
 * <pre>
 * import ninja.javahacker.simpleldap.{@link IncorrectPasswordException};
 * import ninja.javahacker.simpleldap.{@link LdapAuthenticator};
 * import ninja.javahacker.simpleldap.{@link LdapConnectionException};
 * import ninja.javahacker.simpleldap.{@link UserNotFoundException};
 *
 * public class MyAuthenticator {
 *     private static final String LDAP_SERVER = "your.ldap.server.hostname";
 *     private static final int LDAP_PORT = 3268;
 *     private static final String ROOT_DN = "ROOT_LDAP_DN";
 *     private static final String ROOT_PW = "ROOT_LDAP_PASSWORD";
 *     private static final String DN = "dc=yourorganization,dc=example,dc=com";
 *
 *     private static final {@link LdapAuthenticator} AUTH = createAuthenticator();
 *
 *     private static {@link LdapAuthenticator} createAuthenticator() {
 *         try {
 *             return new {@link LdapAuthenticator#LdapAuthenticator(String, int, String, String,
 * String) LdapAuthenticator}(LDAP_SERVER, LDAP_PORT, ROOT_DN, ROOT_PW, DN);
 *         } catch ({@link LdapConnectionException} x) { // Bad LDAP configuration parameters!
 *             throw new ExceptionInInitializerError(x);
 *         }
 *     }
 *
 *     private MyAuthenticator() {}
 *
 *     public static void authenticate(
 *             String login,
 *             String password)
 *             throws {@link LdapConnectionException},
 *             {@link UserNotFoundException},
 *             {@link IncorrectPasswordException}
 *     {
 *         AUTH.{@link LdapAuthenticator#authenticate(String, String) authenticate}(login, password);
 *     }
 *
 *     public static boolean tryAuthenticate(
 *             String login,
 *             String password)
 *             throws {@link LdapConnectionException}
 *     {
 *         return AUTH.{@link LdapAuthenticator#tryAuthenticate(String, String) tryAuthenticate}(login, password);
 *     }
 *
 *     public static String findUserDn(String login) throws LdapConnectionException, UserNotFoundException {
 *         return AUTH.{@link LdapAuthenticator#findDn(String) findDn}(login);
 *     }
 * }
 * </pre>
 *
 * In the preceding code, the {@code authenticate} method would be used to authenticate an user,
 * throwing exception if he/she could not be authenticated. On the other hand, the {@code tryAuthenticate}
 * method tells if the user can or can't be authenticated (it still throws an exception if the authentication
 * procedure can't be performed at all). The {@code findUserDn} method is responsible for finding a user DN
 * given his/her login.
 *
 * Another simpler, but probably less useful use case, is authenticating the user by his/her DN instead of his/her login:
 *
 * <pre>
 *     import ninja.javahacker.simpleldap.LdapConnectionException;
 *     import ninja.javahacker.simpleldap.LdapServer;
 *     import ninja.javahacker.simpleldap.UnspecifiedAuthenticationException;
 *
 *     public class MyOtherAuthenticator {
 *         private static final String LDAP_SERVER = "your.ldap.server.hostname";
 *         private static final int LDAP_PORT = 3268;
 *
 *         private static final {@link LdapServer} AUTH = createAuthenticator();
 *
 *         private static {@link LdapServer} createAuthenticator() {
 *             try {
 *                 return new {@link LdapServer#LdapServer(String, int) LdapServer}(LDAP_SERVER, LDAP_PORT);
 *             } catch ({@link LdapConnectionException} x) { // Bad LDAP configuration parameters!
 *                 throw new ExceptionInInitializerError(x);
 *             }
 *         }
 *
 *         public static void authenticate(
 *                 String userDN,
 *                 String password)
 *                 throws {@link LdapConnectionException},
 *                 {@link UnspecifiedAuthenticationException}
 *         {
 *             AUTH.{@link LdapServer#authenticate(String, String) authenticate}(userDN, password);
 *         }
 *
 *         public static boolean tryAuthenticate(
 *                 String userDN,
 *                 String password)
 *                 throws {@link LdapConnectionException}
 *         {
 *             return AUTH.{@link LdapServer#tryAuthenticate(String, String) tryAuthenticate}(userDN, password);
 *         }
 *     }
 * </pre>
 *
 * Of course, the preceding codes works when the LDAP configuration parameters are immutable.
 * If they aren't or should be configured in some other way, you should adapt them to your
 * reality.
 *
 * @author Victor Williams Stafusa da Silva
 */
package ninja.javahacker.simpleldap;