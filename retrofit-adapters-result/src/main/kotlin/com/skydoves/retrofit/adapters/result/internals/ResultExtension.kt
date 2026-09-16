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

package com.skydoves.retrofit.adapters.result.internals

import com.skydoves.retrofit.adapters.core.NullBodyException
import kotlinx.coroutines.CancellationException
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * Returns [Result] from the [Response] instance and [Call] interface.
 *
 * @param nullBodyAsFailure Reports a successful response with a null body as a
 * [NullBodyException] failure instead of a successful null value.
 */
internal fun <T> Response<T>.toResult(
  paramType: Type,
  nullBodyAsFailure: Boolean,
): Result<T?> = try {
  when {
    !isSuccessful -> Result.failure(HttpException(this))
    paramType == Unit::class.java -> Result.success(Unit as T)
    else -> {
      val body = body()
      if (body == null && nullBodyAsFailure) {
        Result.failure(nullBodyException())
      } else {
        Result.success(body)
      }
    }
  }
} catch (e: CancellationException) {
  throw e
} catch (e: Throwable) {
  Result.failure(e)
}

/**
 * Describes which service method produced a null body, matching the message Retrofit itself uses
 * for non-null return types.
 */
internal fun Response<*>.nullBodyException(): NullBodyException {
  val request = raw().request
  val method = request.tag(Invocation::class.java)?.method()
  val origin = if (method != null) {
    "${method.declaringClass.name}.${method.name}"
  } else {
    request.url.toString()
  }
  return NullBodyException(
    "Response from $origin was null but the response body type was declared as non-null",
  )
}
