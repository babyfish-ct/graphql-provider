package org.babyfish.graphql.provider.r2dbc

abstract class AbstractColumnOptions<T>(
    val name: String,
    val type: Class<T>
) {
    internal var _length: Int? = null
}

class IdColumnOptions<T>(
    name: String, type: Class<T>
): AbstractColumnOptions<T>(name, type)

class GeneralColumnOptions<T>(
    name: String, type: Class<T>
): AbstractColumnOptions<T>(name, type) {
    var nullable: Boolean = false
}

class ReferenceColumnOptions<T>(
    name: String, type: Class<T>
): AbstractColumnOptions<T>(name, type) {

    var onDelete: OnDeleteType = OnDeleteType.NONE
}

var AbstractColumnOptions<String>.length: Int?
    get() = _length
    set(value: Int?) { _length = value }

var AbstractColumnOptions<out Number>.scale: Int?
    get() = 0
    set(value: Int?) {}

enum class OnDeleteType {
    NONE,
    CASCADE,
    SET_NULL
}