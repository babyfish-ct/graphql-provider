package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.KProperty1

@Configuration
class EntityConfiguration<E> {

    fun db(block: EntityDbConfiguration.() -> Unit) {}

    fun redis(enabled: Boolean) {}

    fun graphql(block: EntityGraphQLConfiguration.() -> Unit) {}

    fun <T> id(prop: KProperty1<E, T>, block: IdConfiguration<T>.() -> Unit) {}

    fun <T> scalar(prop: KProperty1<E, T>, block: ScalarConfiguration<T>.() -> Unit) {}

    fun <T: Immutable> reference(prop: KProperty1<E, T?>, block: AssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> list(prop: KProperty1<E, List<T>>, block: AssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> mappedReference(prop: KProperty1<E, T>, mappedBy: KProperty1<T, *>, block: MappedAssociationConfiguration<E, T>.() -> Unit) {}

    fun <T: Immutable> mappedList(prop: KProperty1<E, List<T>>, mappedBy: KProperty1<T, *>, block: MappedAssociationConfiguration<E, T>.() -> Unit) {}

    fun <T> computed(prop: KProperty1<E, T>, block: ComputedConfiguration<E, T>.() -> Unit) {

    }
}

@Configuration
class EntityDbConfiguration {

    fun table(tableName: String) {}
}

@Configuration
class EntityGraphQLConfiguration {

    fun defaultListBatchSize(value: Int) {}

    fun defaultBatchSize(value: Int) {}
}