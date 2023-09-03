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
package com.skydoves.retrofit.adapters.arrow.internals

import arrow.core.Either
import arrow.core.left
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [CallAdapter] that delegates network call request that returns [Deferred] of [Either] type.
 *
 * @property resultType Type of the result from the http request.
 * @property coroutineScope A coroutine scope that launches network requests.
 */
internal class EitherDeferredCallAdapter<T> constructor(
  private val resultType: Type,
  private val paramType: Type,
  private val coroutineScope: CoroutineScope,
) : CallAdapter<T, Deferred<Either<Throwable, T?>>> {

  override fun responseType(): Type {
    return resultType
  }

  @Suppress("DeferredIsResult")
  override fun adapt(call: Call<T>): Deferred<Either<Throwable, T?>> =
    runBlocking(coroutineScope.coroutineContext) {
      val deferred = CompletableDeferred<Either<Throwable, T?>>().apply {
        invokeOnCompletion {
          if (isCancelled && !call.isCanceled) {
            call.cancel()
          }
        }
      }

      val response = call.awaitResponse()
      try {
        val either = response.toEither(paramType)
        deferred.complete(either)
      } catch (e: Exception) {
        val either = e.left()
        deferred.complete(either)
      }

      deferred
    }
}
