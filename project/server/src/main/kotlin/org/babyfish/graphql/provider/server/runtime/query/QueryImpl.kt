package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass

internal class QueryImpl<T: Immutable>(
    entityTypeMap: Map<ImmutableType, EntityType>,
    type: KClass<T>
): AbstractQuery<T>(TableAliasAllocator(), entityTypeMap, type) {

    override fun SqlBuilder.render() {
        sql("select ")
        var separator: String? = null
        for (prop in table.entityType.props.values) {
            prop.column?.let {
                if (separator === null) {
                    separator = ", "
                }
                sql(table.alias)
                sql(".")
                sql(it.name)
            }
        }
        renderWithoutSelection()
    }
}