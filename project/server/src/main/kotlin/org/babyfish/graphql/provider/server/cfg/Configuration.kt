package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.KClass

fun <E: Immutable> entity(type: KClass<E>, block: EntityConfiguration<E>.() -> Unit) {

}

@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class Configuration
