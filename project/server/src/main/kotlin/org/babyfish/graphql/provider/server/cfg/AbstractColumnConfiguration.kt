package org.babyfish.graphql.provider.server.cfg

@Configuration
abstract class AbstractColumnConfiguration<T> {
    fun column(columnName: String) {}
}

fun AbstractColumnConfiguration<out Number>.length(length: Int) {

}

fun AbstractColumnConfiguration<out Number>.scale(scale: Int) {

}