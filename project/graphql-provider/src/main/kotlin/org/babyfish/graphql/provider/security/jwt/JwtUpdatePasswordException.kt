package org.babyfish.graphql.provider.security.jwt

class JwtUpdatePasswordException(
    val reason: Reason
) : RuntimeException(
    "Cannot update password because ${reason.description}"
) {
    enum class Reason(
        internal val description: String
    ) {
        UNAUTHENTICATED("current request is not unauthenticated"),
        ILLEGAL_OLD_PASSWORD("old password is illegal"),
        CONCURRENT_DELETED("the user has been concurrent deleted")
    }
}