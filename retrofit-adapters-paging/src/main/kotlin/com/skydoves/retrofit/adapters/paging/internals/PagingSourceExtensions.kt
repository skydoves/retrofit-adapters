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
@file:Suppress("UNCHECKED_CAST")

package com.skydoves.retrofit.adapters.paging.internals

import com.skydoves.retrofit.adapters.paging.NetworkPagingSource
import com.skydoves.retrofit.adapters.paging.PagingMapper
import com.skydoves.retrofit.adapters.paging.annotations.PagingKey
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import retrofit2.Call
import retrofit2.Invocation
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * Creates an instance of [NetworkPagingSource] based on [Call] and [PagingKeyConfig]. Create a new
 * [Call] interface that executes the next paging network request continuously by [NetworkPagingSource].
 *
 * @param call The original call interface that fetches network data.
 * @param pagingKeyConfig Paging Key configuration.
 */
internal fun <T : Any, R : Any> PagingSourceCall<T, R>.pagingSource(
  call: Call<T>,
  pagingKeyConfig: PagingKeyConfig,
): NetworkPagingSource<T, R> {
  val invocation = requireNotNull(call.request().tag(Invocation::class.java)) {
    "The call is missing a Retrofit Invocation tag, so the paging key cannot be resolved."
  }
  val method = invocation.method()
  val keyIndex = method.pagingKeyIndex()
  val argsField = call.argsField(method)

  return NetworkPagingSource(
    offsetPageKey = invocation.arguments()[keyIndex] as Int,
    mapper = PagingMappers.create(pagingKeyConfig.mapper.java) as PagingMapper<T, R>,
  ) { page ->
    val nextCall = call.clone()
    val args = argsField.get(nextCall) as Array<Any>
    args[keyIndex] = page * pagingKeyConfig.keySize
    nextCall
  }
}

/**
 * Resolves the index of the [PagingKey] annotated parameter.
 *
 * This reads Java parameter annotations rather than Kotlin reflection, because Retrofit already
 * ships a `-keepattributes RuntimeVisibleParameterAnnotations` rule, so they survive R8 while
 * Kotlin metadata does not have the same guarantee.
 */
private fun Method.pagingKeyIndex(): Int {
  val index = parameterAnnotations.indexOfFirst { annotations ->
    annotations.any { it is PagingKey }
  }
  require(index != -1) {
    "$declaringClass.$name must include the @PagingKey annotation to the page value parameter."
  }
  return index
}

/**
 * Locates Retrofit's internal argument array on the call implementation.
 *
 * The field is matched by type rather than by name: `retrofit2.OkHttpCall` is package private and
 * R8 renames its fields, so a name based lookup breaks in minified release builds. The module also
 * ships a consumer R8 rule that keeps the field itself.
 */
private fun Call<*>.argsField(method: Method): Field {
  var type: Class<*>? = javaClass
  while (type != null) {
    val field = type.declaredFields.firstOrNull { it.type == Array<Any>::class.java }
    if (field != null) {
      return field.apply { isAccessible = true }
    }
    type = type.superclass
  }
  throw IllegalStateException(
    "Unable to locate the argument array on ${javaClass.name} while paging " +
      "$method. The Retrofit version in use may be incompatible with retrofit-adapters-paging.",
  )
}
