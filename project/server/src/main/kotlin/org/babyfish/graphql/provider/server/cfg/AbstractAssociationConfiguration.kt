package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable

@Configuration
abstract class AbstractAssociationConfiguration<E, T: Immutable> {

    fun filter(vararg args: Arg, block: Filter<T>.() -> Unit) {

    }

    fun redis(enabled: Boolean = true, block: (RedisConfiguration<T>.() -> Unit)? = null) {

    }
}

class AssociationConfiguration<E, T: Immutable>: AbstractAssociationConfiguration<E, T>() {

    fun db(block: AssociationDbConfiguration.() -> Unit) {}
}

class MappedAssociationConfiguration<E, T: Immutable>: AbstractAssociationConfiguration<E, T>()

@Configuration
class AssociationDbConfiguration {

    fun foreignKey(
        columnName: String,
        onDelete: OnDeleteAction = OnDeleteAction.NONE
    ) {}

    fun middleTable(
        tableName: String,
        joinColumn: String,
        targetJoinColumn: String
    ) {}
}

enum class OnDeleteAction {
    NONE,
    CASCADE,
    SET_NULL
}
