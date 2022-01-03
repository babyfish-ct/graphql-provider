package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.server.Connection
import org.babyfish.graphql.provider.server.meta.EntityImpl
import kotlin.reflect.KProperty1

@Configuration
class EntityConfiguration<E: Any> internal constructor(private val entity: EntityImpl){

    fun db(block: EntityDbConfiguration.() -> Unit) {
        EntityDbConfiguration(this.entity.database).block()
    }

    fun redis(enabled: Boolean) {
        this.entity.redis.enabled = enabled
    }

    fun graphql(block: EntityGraphQLConfiguration.() -> Unit) {
        EntityGraphQLConfiguration(this.entity.graphql).block()
    }

    fun <T> id(prop: KProperty1<E, T>, block: IdConfiguration<T>.() -> Unit) {}

    fun <T> scalar(prop: KProperty1<E, T>, block: ScalarConfiguration<T>.() -> Unit) {}

    fun <T: Immutable> reference(prop: KProperty1<E, T?>, block: AssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> list(prop: KProperty1<E, List<T>>, block: AssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> connection(prop: KProperty1<E, out Connection<T>>, bock: AssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> mappedReference(prop: KProperty1<E, T>, mappedBy: KProperty1<T, *>, block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null) {}

    fun <T: Immutable> mappedList(prop: KProperty1<E, List<T>>, mappedBy: KProperty1<T, *>, block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null) {}

    fun <T: Immutable> mappedConnection(prop: KProperty1<E, out Connection<T>>, block: (MappedAssociationConfiguration<E, T>.() -> Unit)? = null) {}

    fun <T> computed(prop: KProperty1<E, T>, block: ComputedConfiguration<E, T>.() -> Unit) {

    }
}

@Configuration
class EntityDbConfiguration internal constructor(
    private val database: EntityImpl.DatabaseImpl
) {
    fun table(tableName: String) {
        database.userTableName = tableName
    }
}

@Configuration
class EntityGraphQLConfiguration internal constructor(
    private val graphql: EntityImpl.GraphQLImpl
) {

    fun defaultBatchSize(value: Int) {
        graphql.defaultBatchSize = value
    }

    fun defaultCollectionBatchSize(value: Int) {
        graphql.defaultCollectionBatchSize = value
    }
}
