package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.springframework.asm.ClassVisitor
import org.springframework.asm.ClassWriter
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass

internal fun draftImplementationOf(draftType: Class<out Draft<*>>): Class<out Draft<*>> =
    cacheLock.read {
        cacheMap[draftType]
    } ?: cacheLock.write {
        cacheMap[draftType]
            ?: createDraftImplementation(draftType).also {
                cacheMap[draftType] = it
            }
    }

private val cacheMap = WeakHashMap<Class<out Draft<*>>, Class<out Draft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createDraftImplementation(draftType: Class<out Draft<*>>): Class<out Draft<*>> {
    val draftTypeInfo = DraftTypeInfo.of(draftType.kotlin)
    val immutableType = ImmutableType.of(draftTypeInfo.immutableType as KClass<out Immutable>)
    return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            writeType(immutableType, draftTypeInfo.draftType.java)
        }
        .toByteArray()
        .let {
            immutableType.kotlinType.java.classLoader.defineClass(it)
        } as Class<out Draft<*>>
}

private fun ClassVisitor.writeType(type: ImmutableType, draftJavaType: Class<*>) {
    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        draftImplInternalName(type.kotlinType.java),
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(Type.getInternalName(draftJavaType))
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        draftContextName(),
        Type.getDescriptor(DraftContext::class.java)
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        baseName(),
        Type.getDescriptor(type.kotlinType.java)
    )

    visitEnd()
}

private fun ClassVisitor.visitConstructor() {

}
