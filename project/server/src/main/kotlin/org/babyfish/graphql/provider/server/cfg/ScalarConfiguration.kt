package org.babyfish.graphql.provider.server.cfg

class ScalarConfiguration<T> {

    fun db(block: ColumnConfiguration<T>.() -> Unit) {

    }
}

class ColumnConfiguration<T> : AbstractColumnConfiguration<T>() {

    fun nullable() {}
}