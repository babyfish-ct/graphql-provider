package org.babyfish.graphql.provider.dsl.input

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.ImplicitInputPropImpl
import org.babyfish.graphql.provider.meta.impl.ImplicitInputTypeImpl
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.Column
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
abstract class AbstractInputTypeDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    private val modelType: ModelType
) {
    private var name: String? = null

    private var keyProps: List<KProperty1<E, *>>? = null

    private var propRefMap = mutableMapOf<ModelProp, PropRef>()

    protected var insertable: Boolean = false

    protected var updatable: Boolean = true

    protected var deleteable: Boolean = false

    fun name(name: String) {
        if (name === "") {
            throw IllegalArgumentException("name cannot be empty")
        }
    }

    fun keyProps(firstProp: KProperty1<E, *>, vararg restProps: KProperty1<E, *>) {
        keyProps = listOf(firstProp) + restProps.toList()
    }

    fun allScalars() {
        for (prop in modelType.props.values) {
            if (prop.storage is Column && !prop.isReference) {
                propRefMap[prop] = PropRef(prop)
            }
        }
    }

    fun allNonNullScalars() {
        for (prop in modelType.props.values) {
            if (prop.storage is Column && !prop.isReference && !prop.isNullable) {
                propRefMap[prop] = PropRef(prop)
            }
        }
    }

    fun scalar(
        prop: KProperty1<E, *>,
        alias: String? = null
    ) {
        val modelProp = modelProp(prop, PropType.MUTABLE_SCALAR)
        propRefMap[modelProp] = PropRef(
            modelProp,
            alias?.also { validateAlias(it) }
        )
    }

    operator fun KProperty1<E, *>.unaryPlus() {
        scalar(this, null)
    }

    operator fun KProperty1<E, *>.unaryMinus() {
        val modelProp = modelProp(this, PropType.MUTABLE_SCALAR)
        propRefMap -= modelProp
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> referenceId(
        prop: KProperty1<E, X?>,
        alias: String? = null
    ) {
        val modelProp = modelProp(prop, PropType.REFERENCE)
        val actualAlias = alias?.also { validateAlias(it) } ?: "${prop.name}Id"
        propRefMap[modelProp] = PropRef(modelProp, actualAlias)
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> listIds(
        prop: KProperty1<E, List<X>>,
        alias: String? = null
    ) {
        val modelProp = modelProp(prop, PropType.LIST)
        val actualAlias = if (alias !== null) {
            alias.also { validateAlias(it) }
        } else{
            val name = prop.name
            if (name.length < 2) {
                throw IllegalArgumentException(
                    "The property name '$name' is too short," +
                        "so the alias of listIds cannot be automatically inferred, " +
                        "please specify the alias manually"
                )
            }
            if (!name.endsWith("s") || name.endsWith("es")) {
                throw IllegalArgumentException(
                    "The property name '$name' does not end with 's' or ends with 'es'," +
                        "so the alias of listIds cannot be automatically inferred, " +
                        "please specify the alias manually"
                )
            }
            "${name.substring(0, name.length - 1)}Ids"
        }
        propRefMap[modelProp] = PropRef(modelProp, actualAlias)
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> reference(
        prop: KProperty1<E, X?>,
        alias: String? = null,
        block: AssociatedInputTypeDSL<X, XID>.() -> Unit
    ) {
        val modelProp = modelProp(prop, PropType.REFERENCE)
        val targetDsl = AssociatedInputTypeDSL<X, XID>(modelProp.targetType!!).apply {
            block()
        }
        propRefMap[modelProp] = PropRef(
            modelProp,
            alias?.also { validateAlias(it) } ?: prop.name,
            targetDsl
        )
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> reference(
        prop: KProperty1<E, X?>,
        targetMapperType: KClass<out InputMapper<X, XID>>,
        alias: String? = null,
    ) {
        val modelProp = modelProp(prop, PropType.REFERENCE)
        if (InputMapper::class.java.isAssignableFrom(targetMapperType.java)) {
            throw IllegalArgumentException(
                "'${targetMapperType.qualifiedName}' does not inherits '${InputMapper::class.qualifiedName}'"
            )
        }
        if (targetMapperType.typeParameters.isNotEmpty()) {
            throw IllegalArgumentException(
                "'${targetMapperType.qualifiedName}' cannot has type parameters'"
            )
        }
        propRefMap[modelProp] = PropRef(
            modelProp,
            alias?.also { validateAlias(it) } ?: prop.name,
            targetMapperType = targetMapperType
        )
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> list(
        prop: KProperty1<E, List<X>>,
        alias: String? = null,
        block: AssociatedInputTypeDSL<X, XID>.() -> Unit
    ) {
        val modelProp = modelProp(prop, PropType.LIST)
        val targetDsl = AssociatedInputTypeDSL<X, XID>(modelProp.targetType!!).apply {
            block()
        }
        propRefMap[modelProp] = PropRef(
            modelProp,
            alias?.also { validateAlias(it) } ?: prop.name,
            targetDsl
        )
    }

    fun <X: Entity<XID>, XID: Comparable<XID>> list(
        prop: KProperty1<E, List<X>>,
        targetMapperType: KClass<out InputMapper<X, XID>>,
        alias: String? = null,
    ) {
        val modelProp = modelProp(prop, PropType.LIST)
        if (InputMapper::class.java.isAssignableFrom(targetMapperType.java)) {
            throw IllegalArgumentException(
                "'${targetMapperType.qualifiedName}' does not inherits '${InputMapper::class.qualifiedName}'"
            )
        }
        if (targetMapperType.typeParameters.isNotEmpty()) {
            throw IllegalArgumentException(
                "'${targetMapperType.qualifiedName}' cannot has type parameters'"
            )
        }
        propRefMap[modelProp] = PropRef(
            modelProp,
            alias?.also { validateAlias(it) } ?: prop.name,
            targetMapperType = targetMapperType
        )
    }

    internal fun build(
        mappers: Map<KClass<out InputMapper<*, *>>, InputMapper<*, *>>,
        rootMapperType: KClass<out InputMapper<*, *>>,
        resultMap: MutableMap<KClass<out InputMapper<*, *>>, ImplicitInputType>,
        resultList: MutableList<ImplicitInputType>
    ): ImplicitInputType =
        build(
            mappers,
            rootMapperType,
            resultMap,
            resultList,
            null,
            null
        )

    private fun build(
        mappers: Map<KClass<out InputMapper<*, *>>, InputMapper<*, *>>,
        mapperType: KClass<out InputMapper<*, *>>?,
        resultMap: MutableMap<KClass<out InputMapper<*, *>>, ImplicitInputType>,
        resultList: MutableList<ImplicitInputType>,
        parent: ImplicitInputTypeImpl?,
        parentAssociation: ModelProp?
    ): ImplicitInputType {
        val mapper = mapperType?.let {
            mappers[it] ?: error("Internal bug: Illegal mapperType '${mapperType.qualifiedName}'")
        }
        val typeImpl = ImplicitInputTypeImpl(
            name,
            modelType,
            insertable,
            updatable,
            deleteable,
            keyProps,
            mapper,
            parent,
            parentAssociation
        )
        for (propRef in propRefMap.values) {
            val targetScalarType: KClass<*>? =
                propRef.prop.targetType
                    ?.takeIf {
                        propRef.targetDsl === null && propRef.targetMapperType === null
                    }
                    ?.let {
                        it.idProp.returnType
                    }
            val targetImplicitType: ImplicitInputType? =
                propRef.prop.targetType?.let { _ ->
                    propRef.targetDsl?.let { dsl ->
                        dsl.build(mappers, null, resultMap, resultList, typeImpl, propRef.prop)
                    } ?: propRef.targetMapperType?.let {
                        resultMap[it] ?:
                        build(mappers, it, resultMap, resultList)
                    }
                }
            val prop = ImplicitInputPropImpl(
                propRef.alias,
                propRef.prop,
                targetScalarType,
                targetImplicitType,
                propRef.prop.isId && keyProps !== null
            )
            typeImpl.addProp(prop)
        }
        mapperType?.let {
            resultMap[it] = typeImpl
        }
        resultList += typeImpl
        return typeImpl
    }

    private fun modelProp(prop: KProperty1<*, *>, propType: PropType): ModelProp {
        val modelProp = modelType.props[prop.name]
            ?: throw IllegalArgumentException("'$prop' is not valid property of '$modelType'")
        when (propType) {
            PropType.MUTABLE_SCALAR ->
                if (modelProp.storage !is Column || modelProp.isReference) {
                    throw IllegalArgumentException("'$prop' is not mutable scalar property")
                }
            PropType.REFERENCE ->
                if (!modelProp.isReference) {
                    throw IllegalArgumentException("'$prop' is not reference property")
                }
            PropType.LIST ->
                if (!modelProp.isList) {
                    throw IllegalArgumentException("'$prop' is not list property")
                }
        }
        return modelProp
    }

    private class PropRef(
        val prop: ModelProp,
        alias: String? = null,
        val targetDsl: AbstractInputTypeDSL<*, *>? = null,
        val targetMapperType: KClass<out InputMapper<*, *>>? = null
    ) {
        val alias: String = alias ?: prop.name
    }

    private enum class PropType {
        MUTABLE_SCALAR,
        REFERENCE,
        LIST
    }

    companion object {

        @JvmStatic
        private val ALIAS_REGEX = Regex("[\\S]+")

        @JvmStatic
        private fun validateAlias(alias: String) {
            if (!ALIAS_REGEX.matches(alias)) {
                throw IllegalArgumentException("Illegal alias: '$alias'")
            }
        }
    }
}