package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.graphql.provider.meta.impl.invokeByRegistryMode
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.spi.createSqlClient
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@Suppress("UNCHECKED_CAST")
internal fun createSqlClientByEntityMappers(
    mappers: List<EntityMapper<out Entity<*>, *>>,
    jdbcExecutor: JdbcExecutor?,
    r2dbcExecutor: R2dbcExecutor?,
    dialect: Dialect?
): SqlClient =
    createSqlClient(
        jdbcExecutor = jdbcExecutor ?: defaultJdbcExecutor,
        r2dbcExecutor = r2dbcExecutor ?: defaultR2dbcExecutor,
        dialect = dialect,
        metaFactory = MetaFactoryImpl()
    ) {
        val dynamicConfigurationRegistry = DynamicConfigurationRegistry()

        dynamicConfigurationRegistryScope(dynamicConfigurationRegistry) {
            for (mapper in mappers) {
                for (function in mapper::class.declaredFunctions) {
                    if (function.name != "config") {
                        if (function.visibility == KVisibility.PUBLIC && !function.isSuspend) {
                            invokeByRegistryMode(mapper, function)
                        }
                    }
                }
            }
        }
        for (mapper in mappers) {
            (mapper as EntityMapper<Entity<*>, *>).apply {
                val kotlinType = mapper.immutableType.kotlinType as KClass<out Entity<*>>
                val modelType = entity(kotlinType) as ModelTypeImpl
                modelType.isMapped = true
                (mapper as EntityMapper<Entity<String>, String>).apply {
                    EntityTypeDSL<Entity<String>, String>(modelType, this@createSqlClient).config()
                }
            }
        }
    }

private class MetaFactoryImpl: MetaFactory {

    override fun createEntityType(
        immutableType: ImmutableType
    ): ModelTypeImpl =
        ModelTypeImpl(this, immutableType)

    override fun createEntityProp(
        declaringType: EntityType,
        kotlinProp: KProperty1<*, *>
    ): EntityPropImpl =
        ModelPropImpl(declaringType as ModelTypeImpl, kotlinProp)
}