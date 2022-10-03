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

package com.skydoves.retrofit.adapters.arrow.internals

import arrow.core.Either
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * Returns [Either] from the [Response] instance and [Call] interface.
 */
internal fun <T : Any?> Response<T>.toEither(call: Call<T>, paramType: Type): Either<Throwable, T> {
  return Either.catch {
    if (isSuccessful) {
      body()
        // You can confine the response type to Unit if you need to handle empty body, e.g, 204 No content.
        ?: if (paramType == Unit::class.java) {
          Unit as T
        } else {
          val invocation = call.request().tag(Invocation::class.java)!!
          val method = invocation.method()
          throw KotlinNullPointerException(
            "Response from " +
              method.declaringClass.name +
              '.' +
              method.name +
              " was null but response body type was declared as non-null"
          )
        }
    } else {
      throw HttpException(this)
    }
  }
}
