package org.babyfish.graphql.provider.server.runtime.query

import java.lang.StringBuilder

internal class SqlBuilder {

    val builder = StringBuilder()

    val variables = mutableListOf<Any?>()

    inline fun sql(sql: String) {
        builder.append(sql)
    }

    inline fun variable(value: Any?) {
        variables += value
        builder.append(":")
        builder.append(variables.size)
    }
}