package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.InputTypeDSL
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun createImplicitInputTypes(
    sqlClient: SqlClient,
    mappers: List<InputMapper<*, *>>
): Pair<
    Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    List<ImplicitInputType>
> {

    val mapperMap = mappers.associateBy { it::class }
    val resultMap = mutableMapOf<KClass<out InputMapper<*, *>>, ImplicitInputType>()
    val resultList = mutableListOf<ImplicitInputType>()
    for ((mapperType, mapper) in mapperMap) {
        mapper.apply {
            val javaType = GenericTypeResolver.resolveTypeArguments(
                mapperType.java,
                InputMapper::class.java
            )!![0]
            val entityType = sqlClient.entityTypeMap[javaType.kotlin]
                ?: error("Illegal mapper class '${mapperType.qualifiedName}', " +
                    "its first type parameter '${javaType.name}' is not " +
                    "an mapped entity type of current SqlClient")
            if (entityType !is ModelType) {
                error("Internal bug: SqlClient for graphql-provider must retain EntityType as ModelType")
            }
            val dsl = InputTypeDSL<Entity<FakeID>, FakeID>(entityType)
            (mapper as InputMapper<Entity<FakeID>, FakeID>).apply {
                dsl.config()
            }
            dsl.build(mapperMap, mapperType, resultMap, resultList)
        }
    }
    return resultMap to resultList
}