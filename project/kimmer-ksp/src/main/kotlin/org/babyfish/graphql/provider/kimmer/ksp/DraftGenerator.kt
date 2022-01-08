package org.babyfish.graphql.provider.kimmer.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import java.io.OutputStreamWriter
import javax.lang.model.type.TypeMirror

class DraftGenerator(
    private val codeGenerator: CodeGenerator,
    private val sysTypes: SysTypes,
    private val file: KSFile,
    private val modelClassDeclarations: List<KSClassDeclaration>
) {
    fun generate(files: List<KSFile>) {
        val draftFileName =
            file.fileName.let {
                var lastDotIndex = it.lastIndexOf('.')
                if (lastDotIndex === -1) {
                    "${it}Draft"
                } else {
                    "${it.substring(0, lastDotIndex)}Draft"
                }
            }
        codeGenerator.createNewFile(
            Dependencies(false, *files.toTypedArray()),
            file.packageName.asString(),
            draftFileName
        ).use {
            val fileSpec = FileSpec
                .builder(
                    file.packageName.asString(),
                    draftFileName
                ).apply {
                    for (classDeclaration in modelClassDeclarations) {
                        addType(classDeclaration)
                    }
                }.build()
            val writer = OutputStreamWriter(it, Charsets.UTF_8)
            fileSpec.writeTo(writer)
            writer.flush()
        }
    }

    private fun FileSpec.Builder.addType(classDeclaration: KSClassDeclaration) {
        addType(
            TypeSpec.interfaceBuilder(
                classDeclaration.simpleName.asString() + "2"
            ).apply {
                addTypeVariable(
                    TypeVariableName("E", ClassName(file.packageName.asString(), classDeclaration.simpleName.asString()))
                )
                for (prop in classDeclaration.getDeclaredProperties()) {
                    addProp(prop)
                }
            }.build()
        )
    }

    private fun TypeSpec.Builder.addProp(prop: KSPropertyDeclaration) {
        if (prop.isMutable) {
            throw GeneratorException("The property '${prop.qualifiedName!!.asString()}' cannot be mutable")
        }
        val (targetDeclaration, isList) = prop.findTargetDeclaration()
    }

    private fun KSPropertyDeclaration.findTargetDeclaration(): Pair<KSClassDeclaration?, Boolean> {
        val type = type.resolve()
        if (sysTypes.mapType.isAssignableFrom(type)) {
            throw GeneratorException("The property '${qualifiedName!!.asString()}' cannot be map")
        }
        val isList = sysTypes.collectionType.isAssignableFrom(type)
        val targetType = when {
            sysTypes.connectionType.isAssignableFrom(type) -> {
                val declaration = type.declaration
                if (type.arguments.isNotEmpty() ||
                    declaration !is KSClassDeclaration ||
                    declaration.classKind !== ClassKind.INTERFACE
                ) {
                    throw GeneratorException(
                        "The property '${qualifiedName!!.asString()}' must " +
                            "be derived interface of connection without type arguments"
                    )
                }
                type.findConnectionNodeType() ?: throw GeneratorException(
                    "Cannot get connection node type from '${qualifiedName!!.asString()}'"
                )
            }
            isList -> {
                if (type.declaration != sysTypes.listType.declaration) {
                    throw GeneratorException("The property '${qualifiedName!!.asString()}' must be list")
                }
                type.arguments[0].type?.resolve() ?: throw GeneratorException(
                    "Cannot get list element type from '${qualifiedName!!.asString()}'"
                )
            }
            sysTypes.immutableType.isAssignableFrom(type) -> {
                type
            }
            else -> null
        } ?: return null to false

        val declaration = targetType.declaration
        if (declaration !is KSClassDeclaration ||
            declaration.classKind != ClassKind.INTERFACE ||
            declaration.typeParameters.isNotEmpty()
        ) {
            throw GeneratorException("The property '${qualifiedName!!.asString()}' is not valid association")
        }
        return declaration to isList
    }

    private fun KSType.findConnectionNodeType(): KSType? {
        if (this == sysTypes.connectionType) {
            return arguments[0].type?.resolve()
        }
        val declaration = declaration
        if (declaration is KSClassDeclaration) {
            for (superType in declaration.superTypes) {
                val result = superType.resolve().findConnectionNodeType()
                if (result !== null) {
                    return result
                }
            }
        }
        return null
    }
}