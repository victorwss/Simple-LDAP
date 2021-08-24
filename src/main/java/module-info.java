/**
 * Provides a simple utility for connecting in a LDAP server.
 */
@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module ninja.javahacker.simpleldap {
    requires transitive static com.github.spotbugs.annotations;
    requires transitive java.naming;
    exports ninja.javahacker.simpleldap;
}
