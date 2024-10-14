/**
 * Provides a simple utility for connecting in a LDAP server.
 */
module ninja.javahacker.simpleldap {
    requires transitive static lombok;
    requires transitive static com.github.spotbugs.annotations;
    requires transitive java.naming;
    exports ninja.javahacker.simpleldap;
}
