/*
 * Copyright (C) 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.retrofit.adapters.paging.compiler

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStreamWriter

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * Validates `@PagingKeyConfig` and `@PagingKey` usage while the project compiles, and generates a
 * [PAGING_MAPPER_REGISTRY] implementation that instantiates every mapper directly.
 *
 * Both halves exist for the same reason: everything the paging call adapter needs used to be
 * resolved reflectively at request time, so a mistake surfaced as a runtime crash, and R8 shrank
 * the mapper classes away because a `KClass` annotation value is not a keep root.
 */
internal class PagingSymbolProcessor(
  private val codeGenerator: CodeGenerator,
  private val logger: KSPLogger,
) : SymbolProcessor {

  private val mappers = linkedMapOf<String, KSClassDeclaration>()
  private val originatingFiles = linkedSetOf<KSFile>()
  private var generated = false

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getSymbolsWithAnnotation(PAGING_KEY_CONFIG)
      .filterIsInstance<KSFunctionDeclaration>()
      .forEach { function -> validate(function) }

    return emptyList()
  }

  override fun finish() {
    if (generated) return
    generated = true
    writeRegistry()
  }

  private fun validate(function: KSFunctionDeclaration) {
    val config = function.annotations.firstOrNull {
      it.annotationType.resolve().declaration.qualifiedName?.asString() == PAGING_KEY_CONFIG
    } ?: return

    if (Modifier.SUSPEND !in function.modifiers) {
      logger.error(
        "A method annotated with @PagingKeyConfig must be a suspend function, because Retrofit " +
          "only adapts NetworkPagingSource through a suspending call.",
        function,
      )
    }

    validateReturnType(function)
    val pagingKeyType = validatePagingKey(function)
    validateKeySize(config, function)
    validateMapper(config, function, pagingKeyType)

    function.containingFile?.let { originatingFiles += it }
  }

  private fun validateReturnType(function: KSFunctionDeclaration) {
    val returnType = function.returnType?.resolve() ?: return
    val qualifiedName = returnType.declaration.qualifiedName?.asString()
    if (qualifiedName != NETWORK_PAGING_SOURCE) {
      logger.error(
        "A method annotated with @PagingKeyConfig must return NetworkPagingSource, but returns " +
          "$qualifiedName.",
        function,
      )
    }
  }

  /** Returns the resolved return type so the mapper check can compare its type arguments. */
  private fun validatePagingKey(function: KSFunctionDeclaration): KSType? {
    val keyParameters = function.parameters.filter { parameter ->
      parameter.annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == PAGING_KEY
      }
    }

    when (keyParameters.size) {
      0 -> logger.error(
        "A method annotated with @PagingKeyConfig must mark exactly one parameter with " +
          "@PagingKey, which is the parameter the adapter pages by.",
        function,
      )

      1 -> {
        val keyType = keyParameters.single().type.resolve()
        if (keyType.declaration.qualifiedName?.asString() != INT) {
          logger.error(
            "A @PagingKey parameter must be an Int, but was " +
              "${keyType.declaration.qualifiedName?.asString()}.",
            keyParameters.single(),
          )
        }
      }

      else -> logger.error(
        "A method annotated with @PagingKeyConfig must mark exactly one parameter with " +
          "@PagingKey, but ${keyParameters.size} parameters are marked.",
        function,
      )
    }

    return function.returnType?.resolve()
  }

  private fun validateKeySize(config: KSAnnotation, function: KSFunctionDeclaration) {
    val keySize = config.arguments
      .firstOrNull { it.name?.asString() == "keySize" }?.value as? Int ?: return
    if (keySize <= 0) {
      logger.error(
        "@PagingKeyConfig keySize must be greater than zero, but was $keySize.",
        function,
      )
    }
  }

  private fun validateMapper(
    config: KSAnnotation,
    function: KSFunctionDeclaration,
    returnType: KSType?,
  ) {
    val mapperType = config.arguments
      .firstOrNull { it.name?.asString() == "mapper" }?.value as? KSType ?: return
    val mapper = mapperType.declaration as? KSClassDeclaration ?: return
    val mapperName = mapper.qualifiedName?.asString() ?: return

    val pagingMapperType = mapper.getAllSuperTypes().firstOrNull {
      it.declaration.qualifiedName?.asString() == PAGING_MAPPER
    }

    if (pagingMapperType == null) {
      logger.error(
        "The mapper parameter class must implement the PagingMapper interface: $mapperName.",
        function,
      )
      return
    }

    if (mapper.classKind != ClassKind.CLASS || Modifier.ABSTRACT in mapper.modifiers) {
      logger.error(
        "The mapper parameter class must be a concrete class so it can be instantiated: " +
          "$mapperName.",
        function,
      )
      return
    }

    val instantiable = mapper.getConstructors().any { constructor ->
      constructor.parameters.isEmpty() || constructor.parameters.all { it.hasDefault }
    }
    if (!instantiable) {
      logger.error(
        "The mapper parameter class must have a no-argument constructor: $mapperName.",
        function,
      )
      return
    }

    validateMapperTypeArguments(returnType, pagingMapperType, mapperName, function)

    mappers[mapperName] = mapper
  }

  /**
   * `NetworkPagingSource<T, R>` and `PagingMapper<T, R>` have to line up, otherwise the mismatch
   * only shows up as a ClassCastException on the first page load.
   */
  private fun validateMapperTypeArguments(
    returnType: KSType?,
    pagingMapperType: KSType,
    mapperName: String,
    function: KSFunctionDeclaration,
  ) {
    val sourceArguments = returnType?.arguments ?: return
    val mapperArguments = pagingMapperType.arguments
    if (sourceArguments.size != 2 || mapperArguments.size != 2) return

    sourceArguments.zip(mapperArguments).forEachIndexed { index, (source, mapped) ->
      val sourceName = source.type?.resolve()?.declaration?.qualifiedName?.asString()
      val mappedName = mapped.type?.resolve()?.declaration?.qualifiedName?.asString()
      if (sourceName != null && mappedName != null && sourceName != mappedName) {
        val position = if (index == 0) "response" else "item"
        logger.error(
          "The $position type of NetworkPagingSource is $sourceName but $mapperName maps " +
            "$mappedName. PagingMapper type arguments must match the return type.",
          function,
        )
      }
    }
  }

  private fun writeRegistry() {
    if (mappers.isEmpty()) return

    val dependencies = Dependencies(aggregating = true, *originatingFiles.toTypedArray())
    val file = codeGenerator.createNewFile(dependencies, REGISTRY_PACKAGE, REGISTRY_NAME)

    OutputStreamWriter(file, Charsets.UTF_8).use { writer ->
      writer.write(
        buildString {
          appendLine("// Generated by retrofit-adapters-paging-compiler. Do not edit.")
          appendLine("package $REGISTRY_PACKAGE")
          appendLine()
          appendLine("import com.skydoves.retrofit.adapters.paging.PagingMapper")
          appendLine("import com.skydoves.retrofit.adapters.paging.PagingMapperRegistry")
          appendLine()
          appendLine("public class $REGISTRY_NAME : PagingMapperRegistry {")
          appendLine()
          appendLine("  override fun create(mapperClass: Class<*>): PagingMapper<*, *>? =")
          appendLine("    when (mapperClass) {")
          mappers.keys.forEach { mapperName ->
            appendLine("      $mapperName::class.java ->")
            appendLine("        $mapperName()")
          }
          appendLine("      else -> null")
          appendLine("    }")
          appendLine("}")
        },
      )
    }
  }

  private companion object {
    private const val PAGING_KEY_CONFIG =
      "com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig"
    private const val PAGING_KEY = "com.skydoves.retrofit.adapters.paging.annotations.PagingKey"
    private const val PAGING_MAPPER = "com.skydoves.retrofit.adapters.paging.PagingMapper"
    private const val NETWORK_PAGING_SOURCE =
      "com.skydoves.retrofit.adapters.paging.NetworkPagingSource"
    private const val INT = "kotlin.Int"
    private const val REGISTRY_PACKAGE = "com.skydoves.retrofit.adapters.paging.generated"
    private const val REGISTRY_NAME = "GeneratedPagingMapperRegistry"
    private const val PAGING_MAPPER_REGISTRY =
      "com.skydoves.retrofit.adapters.paging.PagingMapperRegistry"
  }
}
