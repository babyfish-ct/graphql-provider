package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.springframework.asm.ClassVisitor
import org.springframework.asm.ClassWriter
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

interface Factory<T: Immutable> {
    fun create(): T
    fun createDraft(ctx: DraftContext, o: T): Draft<T>

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
    val implType = implementationOf(immutableType.kotlinType.java)
    val draftImplType = draftImplementationOf(immutableType.kotlinType.java)
    val factoryType = createFactoryImplType(immutableType, implType, draftImplType)
    return factoryType.getConstructor().newInstance() as Factory<*>
}

private fun createFactoryImplType(
    immutableType: ImmutableType,
    implType: Class<*>,
    draftImplType: Class<*>
): Class<*> =
    ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            visit(
                BYTECODE_VERSION,
                Opcodes.ACC_PUBLIC,
                "${Type.getInternalName(draftImplType)}{Factory}",
                null,
                "java/lang/Object",
                arrayOf(Type.getInternalName(Factory::class.java))
            )
            writeConstructor()
            writeCreate(implType)
            writeCreateDraft(draftImplType, immutableType)
            visitEnd()
        }
        .toByteArray()
        .let {
            draftImplType.classLoader.defineClass(it)
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

private fun ClassVisitor.writeCreate(implType: Class<*>) {

    val internalName = Type.getInternalName(implType)

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

private fun ClassVisitor.writeCreateDraft(draftImplType: Class<*>, immutableType: ImmutableType) {

    val internalName = Type.getInternalName(draftImplType)
    val draftContextDescriptor = Type.getDescriptor(DraftContext::class.java)
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "createDraft",
        "($draftContextDescriptor${Type.getDescriptor(Immutable::class.java)})${Type.getDescriptor(Draft::class.java)}"
    ) {



        visitTypeInsn(Opcodes.NEW, internalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitTypeInsn(Opcodes.CHECKCAST, modelInternalName)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            internalName,
            "<init>",
            "(${draftContextDescriptor}L$modelInternalName;)V",
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

