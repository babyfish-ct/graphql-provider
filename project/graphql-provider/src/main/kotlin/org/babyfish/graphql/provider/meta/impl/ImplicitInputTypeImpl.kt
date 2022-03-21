package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.ImplicitInputProp
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.kimmer.sql.AbstractSaveOptionsDSL
import org.babyfish.kimmer.sql.AssociatedSaveOptionsDSL
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SaveOptionsDSL
import kotlin.reflect.KProperty1

internal class ImplicitInputTypeImpl(
    name: String?,
    override val modelType: ModelType,
    private val insertable: Boolean,
    private val updatable: Boolean,
    private val deletable: Boolean,
    private val keyProps: List<KProperty1<*, *>>?,
    private val mapper: InputMapper<*, *>?,
    private val parent: ImplicitInputTypeImpl?,
    private val parentAssociation: ModelProp?
): ImplicitInputType {

    override val name: String = when {
        name !== null -> name
        parent !== null -> "${parent.name}_${parentAssociation!!.name}"
        else -> (mapper ?: error("Internal bug: root implicit must be created by mapper"))::class
            .simpleName
            ?.let {
                when {
                    it.endsWith("InputMapper") ->
                        it.substring(0, it.length - 6)
                    it.endsWith("Mapper") ->
                        "${it.substring(0, it.length - 6)}Input"
                    else ->
                        null
                }
            }
            ?: "${modelType.graphql.name}Input"
    }

    private val _props = mutableMapOf<String, ImplicitInputProp>()

    override val props: Map<String, ImplicitInputProp>
    get() = _props

    fun addProp(prop: ImplicitInputProp) {
        _props.put(prop.name, prop)?.let {
            throw ModelException(
                "Illegal input mapper ${mapperPath}, " +
                    "The alias '${prop.name}' is used by both '${it.modelProp}' and ${it.modelProp}"
            )
        }
    }

    private val mapperPath: String
        get() =
            parent?.let {
                "${parent.mapperPath}/${parentAssociation!!.name}"
            } ?: mapper!!::class.qualifiedName!!

    @Suppress("UNCHECKED_CAST")
    override val saveOptionsBlock: AbstractSaveOptionsDSL<*>.() -> Unit
        get() = {
            if (parent === null) {
                val dsl = this as SaveOptionsDSL<*>
                if (!insertable) {
                    dsl.updateOnly()
                }
                if (!updatable) {
                    dsl.insertOnly()
                }
            } else {
                val dsl = this as AssociatedSaveOptionsDSL
                if (insertable) {
                    dsl.createAttachedObjects()
                }
                if (deletable) {
                    dsl.deleteDetachedObjects()
                }
            }
            val abstractDsl = this as AbstractSaveOptionsDSL<Entity<FakeID>>
            this@ImplicitInputTypeImpl.keyProps?.let {
                val list = it as List<KProperty1<Entity<FakeID>, FakeID>>
                abstractDsl.keyProps(list.first(), *list.subList(1, list.size).toTypedArray())
            }
            for (prop in props.values) {
                val targetImplicitType = prop.targetImplicitType ?: continue
                if (prop.isReference) {
                    abstractDsl.reference(
                        prop.modelProp.kotlinProp as KProperty1<Entity<FakeID>, Entity<*>?>,
                        targetImplicitType.saveOptionsBlock as (
                            AssociatedSaveOptionsDSL<*>.() -> Unit
                        )
                    )
                } else if (prop.isList) {
                    abstractDsl.list(
                        prop.modelProp.kotlinProp as KProperty1<Entity<FakeID>, List<Entity<*>>>,
                        targetImplicitType.saveOptionsBlock as (
                            AssociatedSaveOptionsDSL<*>.() -> Unit
                        )
                    )
                }
            }
        }

    override fun toString(): String =
        modelType.toString()
}