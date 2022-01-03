package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import kotlin.reflect.KClass

data class DraftTypeInfo(
    val draftType: KClass<*>,
    val immutableType: KClass<*>,
    val superDraftTypes: List<KClass<*>>
) {
    companion object {
        fun of(draftType: KClass<out Draft<*>>): DraftTypeInfo =
            parseDraftTypeInfo(draftType)
    }
}

private fun parseDraftTypeInfo(draftType: KClass<out Draft<*>>): DraftTypeInfo {
    if (!draftType.java.isInterface) {
        throw IllegalArgumentException("The type '${draftType::qualifiedName}' must be an interface")
    }
    val isSync = SyncDraft::class.java.isAssignableFrom(draftType.java)
    val isAsync = AsyncDraft::class.java.isAssignableFrom(draftType.java)
    if (isSync && isAsync) {
        throw IllegalArgumentException("The type '${draftType::qualifiedName}' cannot be both '${SyncDraft::class.qualifiedName}' and '${AsyncDraft::class.qualifiedName}'")
    }
    val ctx = SuperContext().apply {
        acceptSuperTypes(draftType)
    }

    if (ctx.immutableTypes.isEmpty()) {
        throw IllegalArgumentException("The draft interface '${draftType.qualifiedName}' does not implement any immutable interface")
    }
    if (ctx.immutableTypes.size > 1) {
        throw IllegalArgumentException("The draft interface '${draftType.qualifiedName}' not implements several immutable interfaces: ${ctx.immutableTypes.joinToString { "'$it'" }}")
    }
    if (isSync || isAsync) {
        if (ctx.draftTypes.isEmpty()) {
            throw IllegalArgumentException("The draft interface '${draftType.qualifiedName}' does not implement any draft interface")
        }
        if (ctx.draftTypes.size > 1) {
            throw IllegalArgumentException("The draft interface '${draftType.qualifiedName}' not implements several draft interfaces: ${ctx.draftTypes.joinToString { "'$it'" }}")
        }
        return DraftTypeInfo(
            draftType = ctx.draftTypes[0],
            immutableType = ctx.immutableTypes[0],
            superDraftTypes = SuperContext().let {
                it.acceptSuperTypes(ctx.draftTypes[0])
                it.draftTypes
            }
        )
    } else {
        return DraftTypeInfo(
            draftType = draftType,
            immutableType = ctx.immutableTypes[0],
            superDraftTypes = ctx.draftTypes
        )
    }
}

private class SuperContext {

    var immutableTypes = mutableListOf<KClass<*>>()

    var draftTypes = mutableListOf<KClass<*>>()

    fun acceptSuperTypes(type: KClass<*>) {
        for (supertype in type.supertypes) {
            val classifier = supertype.classifier as KClass<*>
            if (classifier is KClass<*>) {
                acceptType(classifier)
            }
        }
    }

    private fun acceptType(type: KClass<*>) {
        if (type.java.isInterface) {
            if (Draft::class.java.isAssignableFrom(type.java)) {
                if (Draft::class != type && SyncDraft::class != type && AsyncDraft::class != type && addDraftType(type)) {
                    acceptSuperTypes(type)
                }
            } else if (Immutable::class.java.isAssignableFrom(type.java) &&
                Immutable::class != type &&
                addImmutableType(type)
            ) {
                acceptSuperTypes(type)
            }
        }
    }

    private fun addDraftType(type: KClass<*>): Boolean {
        for (index in draftTypes.indices) {
            val existsType = draftTypes[index].java
            if (type.java.isAssignableFrom(existsType)) {
                return false
            }
            if (!existsType.isAssignableFrom(type.java)) {
                draftTypes[index] = type
                return true
            }
        }
        draftTypes += type
        return true
    }

    private fun addImmutableType(type: KClass<*>): Boolean {
        for (index in immutableTypes.indices) {
            val existsType = immutableTypes[index].java
            if (type.java.isAssignableFrom(existsType)) {
                return false
            }
            if (!existsType.isAssignableFrom(type.java)) {
                immutableTypes[index] = type
                return true
            }
        }
        immutableTypes += type
        return true
    }
}