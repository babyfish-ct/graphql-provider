package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.graphql.provider.starter.meta.EntityProp
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.runtime.expression.Expression
import org.babyfish.graphql.provider.starter.runtime.expression.PropExpression
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

internal class TableImpl<T: Immutable>(
    private val query: AbstractQuery<*>,
    val entityType: EntityType,
    val parent: TableImpl<*>? = null,
    val parentProp: EntityProp? = null,
    var joinType: JoinType = JoinType.INNER
): JoinableTable<T>, Renderable {

    val alias: String

    val middleTableAlias: String?

    private var _nonIdPropAccessed = parent === null

    init {
        if ((parent === null) != (parentProp === null)) {
            error("Internal bug: Bad constructor arguments for TableImpl")
        }
        middleTableAlias = parentProp?.middleTable?.let { query.tableAliasAllocator.allocate() }
        alias = query.tableAliasAllocator.allocate()
    }

    private val childTableMap = mutableMapOf<String, TableImpl<*>>()

    override fun <X> get(prop: KProperty1<T, X?>): Expression<X> {
        val entityProp = entityType.props[prop.name] ?: error("No property '${prop.name}'")
        if (entityProp.targetType !== null) {
            throw IllegalArgumentException(
                "Can not get '${prop.name}' form table because it's association, " +
                    "please use joinReference, joinList or joinConnection"
            )
        }
        if (!entityProp.isId) {
            _nonIdPropAccessed = true
        }
        return PropExpression(this, entityProp)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <X : Immutable> joinReference(prop: KProperty1<T, X?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isReference != true) {
            throw IllegalArgumentException("'$prop' is not reference")
        }
        val existing = childTableMap[prop.name]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X>
        }
        val newTable = TableImpl<X>(query, entityProp.targetType!!, this, entityProp, joinType)
        childTableMap[prop.name] = newTable
        _nonIdPropAccessed = true
        return newTable
    }

    override fun <X : Immutable> joinList(prop: KProperty1<T, List<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isList != true) {
            throw IllegalArgumentException("'$prop' is not list")
        }
        val existing = childTableMap[prop.name]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X>
        }
        val newTable = TableImpl<X>(query, entityProp.targetType!!, this, entityProp, joinType)
        childTableMap[prop.name] = newTable
        _nonIdPropAccessed = true
        return newTable
    }

    override fun <X : Immutable> joinConnection(prop: KProperty1<T, Connection<X>?>, joinType: JoinType): JoinableTable<X> {
        val entityProp = entityType.props[prop.name]
        if (entityProp?.isConnection != true) {
            throw IllegalArgumentException("'$prop' is not connection")
        }
        val existing = childTableMap[prop.name]
        if (existing !== null) {
            if (existing.joinType != joinType) {
                existing.joinType = JoinType.INNER
            }
            return existing as JoinableTable<X>
        }
        val newTable = TableImpl<X>(query, entityProp.targetType!!, this, entityProp, joinType)
        childTableMap[prop.name] = newTable
        return newTable
    }

    override fun SqlBuilder.render() {
        renderSelf()
        for (childTable in childTableMap.values) {
            childTable.apply {
                render()
            }
        }
    }

    private fun SqlBuilder.renderSelf() {
        if (parentProp?.mappedBy !== null) {
            inverseJoin()
        } else if (parentProp !== null) {
            join()
        } else {
            sql(" from ")
            sql(entityType.database.tableName)
            sql(" as ")
            sql(alias)
        }
    }

    private fun SqlBuilder.join() {

        val parent = parent!!
        val prop = parentProp!!
        val middleTable = prop.middleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                parent.entityType.idProp.column!!.name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.joinColumnName
            )
            joinImpl(
                joinType,
                middleTableAlias!!,
                middleTable.targetJoinColumnName,
                entityType.database.tableName,
                alias,
                entityType.idProp.column!!.name
            )
        } else {
            joinImpl(
                joinType,
                parent.alias,
                prop.column!!.name,
                entityType.database.tableName,
                alias,
                parent.entityType.idProp.column!!.name
            )
        }
    }

    private fun SqlBuilder.inverseJoin() {

        val parent = parent!!
        val inverseProp = parentProp?.mappedBy!!
        val middleTable = inverseProp.middleTable

        if (middleTable !== null) {
            joinImpl(
                joinType,
                parent.alias,
                parent.entityType.idProp.column!!.name,
                middleTable.tableName,
                middleTableAlias!!,
                middleTable.targetJoinColumnName
            )
            joinImpl(
                joinType,
                middleTableAlias!!,
                middleTable.joinColumnName,
                entityType.database.tableName,
                alias,
                entityType.idProp.column!!.name
            )
        } else {
            joinImpl(
                joinType,
                parent.alias,
                parent.entityType.idProp.column!!.name,
                entityType.database.tableName,
                alias,
                inverseProp.column!!.name
            )
        }
    }

    private fun SqlBuilder.joinImpl(
        joinType: JoinType,
        previousAlias: String,
        previousColumnName: String,
        newTableName: String,
        newAlias: String,
        newColumnName: String
    ) {
        sql(" ")
        sql(joinType.name.lowercase())
        sql(" join ")
        sql(newTableName)
        sql(" as ")
        sql(newAlias)
        sql(" on ")
        sql(previousAlias)
        sql(".")
        sql(previousColumnName)
        sql(" = ")
        sql(newAlias)
        sql(".")
        sql(newColumnName)
    }
}