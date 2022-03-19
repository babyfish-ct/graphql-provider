package org.babyfish.graphql.provider

import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ConvertFromInput(
    val value: KClass<out InputMapper<*, *>>
)