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
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.kotlinFunction

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
  val invocation = call.request().tag(Invocation::class.java)!!
  val method = invocation.method()
  val pagingKey = method.kotlinFunction?.parameters?.indexOfFirst { it.hasAnnotation<PagingKey>() }
  if (pagingKey == null || pagingKey == -1) {
    throw RuntimeException(
      method.declaringClass.name +
        '.' +
        method.name +
        " must include the @PagingKey annotation to the page value parameter.",
    )
  }
  val keyIndex = pagingKey - 1
  return NetworkPagingSource(
    offsetPageKey = invocation.arguments()[keyIndex] as Int,
    mapper = pagingKeyConfig.mapper.createInstance() as PagingMapper<T, R>,
  ) { page ->
    val nextCall = call.clone()
    val argsField = nextCall.javaClass.getDeclaredField("args")
    argsField.isAccessible = true
    val array = argsField.get(nextCall) as Array<Any>
    array[keyIndex] = page * pagingKeyConfig.keySize
    nextCall
  }
}
