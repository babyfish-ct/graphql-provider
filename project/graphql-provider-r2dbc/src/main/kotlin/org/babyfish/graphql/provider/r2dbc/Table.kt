package org.babyfish.graphql.provider.r2dbc

abstract class Table(
    val name: String
) {
    protected fun <XID> reference(
        columnName: String,
        entity: Entity<XID>,
        block: ReferenceColumnOptions<XID>.() -> Unit
    ): Column {
        TODO()
    }
}