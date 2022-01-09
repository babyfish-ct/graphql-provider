package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.runtime.*
import org.babyfish.graphql.provider.kimmer.runtime.asm.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun ClassVisitor.writeResolve(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{resolve}",
        "()${Type.getDescriptor(Immutable::class.java)}"
    ) {

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )
        visitVarInsn(Opcodes.ASTORE, baseSlot)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            modifiedName(),
            args.modelImplDescriptor
        )
        visitVarInsn(Opcodes.ASTORE, modifiedSlot)

        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitCond(Opcodes.IFNONNULL) {
            visitVarInsn(Opcodes.ALOAD, baseSlot)
            visitInsn(Opcodes.ARETURN)
        }

        for (prop in args.immutableType.props.values) {
            if (prop.targetType !== null) {
                visitResolveProp(prop, args)
            }
        }

        visitVarInsn(Opcodes.ALOAD, baseSlot)
        visitTypeInsn(Opcodes.CHECKCAST, immutableSpiInternalName)
        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitInsn(Opcodes.ICONST_1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            immutableSpiInternalName,
            "equals",
            "(Ljava/lang/Object;Z)Z",
            true
        )
        visitCond(Opcodes.IFEQ) {
            visitVarInsn(Opcodes.ALOAD, baseSlot)
            visitInsn(Opcodes.ARETURN)
        }
        visitVarInsn(Opcodes.ALOAD, modifiedSlot)
        visitInsn(Opcodes.ARETURN)
    }
}

private fun MethodVisitor.visitResolveProp(prop: ImmutableProp, args: GeneratorArgs) {

    val propInternalName = Type.getInternalName(prop.returnType.java)
    val propDesc = Type.getDescriptor(prop.returnType.java)

    visitVarInsn(Opcodes.ALOAD, modifiedSlot)
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        draftContextName(),
        draftContextDescriptor
    )
    visitVarInsn(Opcodes.ALOAD, modifiedSlot)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.modelImplInternalName,
        prop.name,
        propDesc
    )
    if (prop.isList) {
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            draftContextInternalName,
            "resolve",
            "(Ljava/util/List;)Ljava/util/List;",
            true
        )
    } else {
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            draftContextInternalName,
            "resolve",
            "($immutableDescriptor)$immutableDescriptor",
            true
        )
        visitTypeInsn(Opcodes.CHECKCAST, propInternalName)
    }
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        prop.name,
        propDesc
    )
}

private const val baseSlot = 1
private const val modifiedSlot = 2
private val draftContextDescriptor = Type.getDescriptor(DraftContext::class.java)
private val draftContextInternalName = Type.getInternalName(DraftContext::class.java)
private val immutableDescriptor = Type.getDescriptor(Immutable::class.java)
private val immutableSpiInternalName = Type.getInternalName(ImmutableSpi::class.java)