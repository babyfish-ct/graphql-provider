package org.babyfish.graphql.provider.kimmer.runtime.asm.impl

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi
import org.babyfish.graphql.provider.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeType(type: ImmutableType) {

    val implInternalName = implInternalName(type)

    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        implInternalName(type),
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(Type.getInternalName(type.kotlinType.java), Type.getInternalName(ImmutableSpi::class.java))
    )

    writeConstructor()
    writeCopyConstructor(type)

    for (prop in type.props.values) {
        writeProp(prop, implInternalName)
    }

    writeRuntimeType(type)
    writeLoaded(type)
    writeThrowable(type)
    writeValue(type)

    writeHashCode(type)
    writeEquals(type)

    visitEnd()
}