package org.babyfish.graphql.provider.kimmer.runtime.asm.impl

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.implInternalName
import org.babyfish.graphql.provider.kimmer.runtime.writeField
import org.babyfish.graphql.provider.kimmer.runtime.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeRuntimeType(type: ImmutableType) {

    val typeInternalName = Type.getInternalName(ImmutableType::class.java)
    val typeDesc = Type.getDescriptor(ImmutableType::class.java)

    writeField(
        Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
        "{immutableType}",
        typeDesc
    )

    writeMethod(
        Opcodes.ACC_STATIC,
        "<clinit>",
        "()V"
    ) {
        visitLdcInsn(Type.getType(type.kotlinType.java))
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            typeInternalName,
            "of",
            "(Ljava/lang/Class;)$typeDesc",
            true
        )
        visitFieldInsn(
            Opcodes.PUTSTATIC,
            implInternalName(type.kotlinType.java),
            "{immutableType}",
            typeDesc
        )
        visitInsn(Opcodes.RETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        "()" + typeDesc
    ) {
        visitFieldInsn(
            Opcodes.GETSTATIC,
            implInternalName(type.kotlinType.java),
            "{immutableType}",
            typeDesc
        )
        visitInsn(Opcodes.ARETURN)
    }
}