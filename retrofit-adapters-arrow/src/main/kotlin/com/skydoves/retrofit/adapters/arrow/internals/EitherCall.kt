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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [EitherCall] is an implementation of Retrofit's [Call] interface, which executes
 * the network data, handles and decides whether they succeed or fail.
 *
 * @property proxy Proxy of the original http request.
 * @property coroutineScope A coroutine scope that launches network requests.
 */
internal class EitherCall<T : Any>(
  private val proxy: Call<T>,
  private val paramType: Type,
  private val coroutineScope: CoroutineScope,
) : Call<Either<Throwable, T?>> {

  override fun enqueue(callback: Callback<Either<Throwable, T?>>) {
    coroutineScope.launch {
      try {
        val response = proxy.awaitResponse()
        val either = response.toEither(paramType)
        callback.onResponse(this@EitherCall, Response.success(either))
      } catch (e: Exception) {
        val either = e.left()
        callback.onResponse(this@EitherCall, Response.success(either))
      }
    }
  }

  override fun execute(): Response<Either<Throwable, T?>> =
    runBlocking(coroutineScope.coroutineContext) {
      val either = proxy.execute().toEither(paramType)
      Response.success(either)
    }

  override fun clone(): Call<Either<Throwable, T?>> =
    EitherCall(proxy.clone(), paramType, coroutineScope)

  override fun request(): Request = proxy.request()
  override fun timeout(): Timeout = proxy.timeout()
  override fun isExecuted(): Boolean = proxy.isExecuted
  override fun isCanceled(): Boolean = proxy.isCanceled
  override fun cancel() = proxy.cancel()
}
