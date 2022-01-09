package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeRuntimeType(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    val desc = "()${Type.getDescriptor(ImmutableType::class.java)}"
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        desc
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{type}",
            desc,
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal fun ClassVisitor.writeThrowable(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{throwable}",
        "(Ljava/lang/String;)Ljava/lang/Throwable;"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{throwable}",
            "(Ljava/lang/String;)Ljava/lang/Throwable;",
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal fun ClassVisitor.writeLoaded(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{loaded}",
        "(Ljava/lang/String;)Z"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{loaded}",
            "(Ljava/lang/String;)Z",
            true
        )
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeValue(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{value}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{value}",
            "(Ljava/lang/String;)Ljava/lang/Object;",
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal fun ClassVisitor.writeHashCode(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "()I"
    ) {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "hashCode",
            "()I",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}

internal fun ClassVisitor.writeEquals(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;)Z"
    ) {
        visitModelGetter(args)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "equals",
            "(Ljava/lang/Object;)Z",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}
