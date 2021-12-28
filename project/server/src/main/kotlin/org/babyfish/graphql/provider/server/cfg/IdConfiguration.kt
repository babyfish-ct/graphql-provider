package org.babyfish.graphql.provider.server.cfg

class IdConfiguration<T> {

    fun db(block: IdColumnConfiguration<T>.() -> Unit) {}
}

class IdColumnConfiguration<T> : AbstractColumnConfiguration<T>() {
}