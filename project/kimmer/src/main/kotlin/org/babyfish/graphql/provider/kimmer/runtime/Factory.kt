package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.asm.*
import org.babyfish.graphql.provider.kimmer.runtime.asm.BYTECODE_VERSION
import org.babyfish.graphql.provider.kimmer.runtime.asm.draft.draftImplementationOf
import org.babyfish.graphql.provider.kimmer.runtime.asm.impl.implementationOf
import org.babyfish.graphql.provider.kimmer.runtime.asm.implInternalName
import org.babyfish.graphql.provider.kimmer.runtime.asm.sync.syncDraftImplementationOf
import org.babyfish.graphql.provider.kimmer.runtime.asm.syncDraftImplInternalName
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.ClassWriter
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

internal interface Factory<T: Immutable> {
    fun create(): T
    fun createDraft(ctx: DraftContext, o: T?): Draft<T>

    companion object {

        @JvmStatic
        fun <T: Immutable> of(draftType: KClass<out Draft<T>>): Factory<T> =
            factoryOf(draftType.java) as Factory<T>

        @JvmStatic
        fun <T: Immutable> of(draftType: Class<out Draft<T>>): Factory<T> =
            factoryOf(draftType) as Factory<T>
    }
}

private val cacheMap = mutableMapOf<Class<*>, Factory<*>>()

private val cacheLock = ReentrantReadWriteLock()

private fun factoryOf(draftType: Class<*>): Factory<*> =
    cacheLock.read {
        cacheMap[draftType]
    } ?: cacheLock.write {
        cacheMap[draftType]
            ?: createFactory(draftType).also {
                cacheMap[draftType] = it
            }
    }

private fun createFactory(draftType: Class<*>): Factory<*> {
    val immutableType = ImmutableType.fromDraftType(draftType as Class<out Draft<*>>)
    val isSync = SyncDraft::class.java.isAssignableFrom(draftType)
    val isAsync = AsyncDraft::class.java.isAssignableFrom(draftType)
    if (isSync && isAsync) {
        throw IllegalArgumentException(
            "draftType cannot be subtype of both ${SyncDraft::class.qualifiedName} " +
                "or ${AsyncDraft::class.qualifiedName}"
        )
    } else if (!isSync && !isAsync) {
        throw IllegalArgumentException(
            "draftType must be subtype of ${SyncDraft::class.qualifiedName} " +
                "or ${AsyncDraft::class.qualifiedName}"
        )
    }
    implementationOf(immutableType.kotlinType.java)
    draftImplementationOf(immutableType.kotlinType.java)
    syncDraftImplementationOf(immutableType.kotlinType.java)

    val factoryType = createFactoryImplType(immutableType, isAsync)
    return factoryType.getConstructor().newInstance() as Factory<*>
}

private fun createFactoryImplType(
    immutableType: ImmutableType,
    isAsync: Boolean
): Class<*> =
    ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            visit(
                BYTECODE_VERSION,
                Opcodes.ACC_PUBLIC,
                "${Type.getInternalName(immutableType.kotlinType.java)}{Factory}",
                null,
                "java/lang/Object",
                arrayOf(Type.getInternalName(Factory::class.java))
            )
            writeConstructor()
            writeCreate(immutableType)
            writeCreateDraft(immutableType, isAsync)
            visitEnd()
        }
        .toByteArray()
        .let {
            immutableType.kotlinType.java.classLoader.defineClass(it)
        }

private fun ClassVisitor.writeConstructor() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.RETURN)
    }
}

private fun ClassVisitor.writeCreate(immutableType: ImmutableType) {

    val internalName = implInternalName(immutableType)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "create",
        "()${Type.getDescriptor(Immutable::class.java)}"
    ) {
        visitTypeInsn(Opcodes.NEW, internalName)
        visitInsn(Opcodes.DUP)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            internalName,
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeCreateDraft(
    immutableType: ImmutableType,
    isAsync: Boolean
) {
    val internalName = if (isAsync) {
        ""
    } else {
        syncDraftImplInternalName(immutableType)
    }
    val targetDraftContextInternalName = if (isAsync) {
        ASYNC_DRAFT_CONTEXT_INTERNAL_NAME
    } else {
        SYNC_DRAFT_CONTEXT_INTERNAL_NAME
    }
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "createDraft",
        "($DRAFT_CONTEXT_DESCRIPTOR$IMMUTABLE_DESCRIPTOR)$DRAFT_DESCRIPTOR"
    ) {

        visitTypeInsn(Opcodes.NEW, internalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitTypeInsn(Opcodes.CHECKCAST, targetDraftContextInternalName)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitTypeInsn(Opcodes.CHECKCAST, modelInternalName)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            internalName,
            "<init>",
            "(L$targetDraftContextInternalName;L$modelInternalName;)V",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

