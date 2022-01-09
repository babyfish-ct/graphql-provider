package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.runtime.baseName
import org.babyfish.graphql.provider.kimmer.runtime.draftContextName
import org.babyfish.graphql.provider.kimmer.runtime.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeConstructor(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "(${args.draftContextDescriptor}${args.modelDescriptor})V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            draftContextName(),
            args.draftContextDescriptor
        )

        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )

        visitInsn(Opcodes.RETURN)
    }
}