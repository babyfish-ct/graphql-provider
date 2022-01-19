package org.babyfish.graphql.provider.server.dsl

import kotlin.reflect.KClass

interface ArgumentType<T: Any?> {

    val nullable: Boolean

    fun asNullable(): ArgumentType<T?>
    fun asList(): ArgumentType<List<T>> =
        ListArgumentType(this)

    companion object {
        fun <T: Any> of(type: KClass<T>): ArgumentType<T> =
            ClassArgumentType<T>(type)
    }
}

internal class ClassArgumentType<T: Any>(
    val type: KClass<*>
): ArgumentType<T> {

    override val nullable: Boolean
        get() = false

    override fun asNullable(): ArgumentType<T?> =
        NullableArgumentType(this)
}

internal class NullableArgumentType<T: Any>(
    val target: ArgumentType<T>
): ArgumentType<T?> {

    override val nullable: Boolean
        get() = true

    override fun asNullable(): ArgumentType<T?> = this
}

internal class ListArgumentType<T: Any?>(
    private val element: ArgumentType<T>
): ArgumentType<List<T>> {

    override val nullable: Boolean
        get() = false

    override fun asNullable(): ArgumentType<List<T>?> =
        NullableArgumentType(this)
}
