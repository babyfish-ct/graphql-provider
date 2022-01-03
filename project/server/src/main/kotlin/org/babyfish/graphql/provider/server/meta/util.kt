package org.babyfish.graphql.provider.server.meta

fun databaseIdentifier(name: String): String {
    var prevUpper = true
    return name.toCharArray().joinToString("") {
        val upper = it.isUpperCase()
        val result = if (!prevUpper && upper) {
            "_$it"
        } else {
            it.toString()
        }
        prevUpper = upper
        result
    }
}