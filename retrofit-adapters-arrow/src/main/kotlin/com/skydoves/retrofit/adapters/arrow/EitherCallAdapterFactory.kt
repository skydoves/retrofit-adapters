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

package com.skydoves.retrofit.adapters.arrow

import arrow.core.Either
import com.skydoves.retrofit.adapters.arrow.internals.EitherCallAdapter
import com.skydoves.retrofit.adapters.arrow.internals.EitherDeferredCallAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 *
 * Creates [EitherCallAdapterFactory] instances, which return [Either] type of the response from the
 * service interface method with the `suspend` keyword. You can create the [EitherCallAdapterFactory]
 * with your own [CoroutineScope], which executes network requests.
 *
 * Also, [EitherCallAdapterFactory] allows you to return [Deferred] of the [Either].
 *
 * @property coroutineScope A coroutine scope that runs network requests.
 */
public class EitherCallAdapterFactory private constructor(
  private val coroutineScope: CoroutineScope
) : CallAdapter.Factory() {

  override fun get(
    returnType: Type,
    annotations: Array<out Annotation>,
    retrofit: Retrofit
  ): CallAdapter<*, *>? {
    when (getRawType(returnType)) {
      Call::class.java -> {
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawType = getRawType(callType)
        if (rawType != Either::class.java) {
          return null
        }

        checkLeftIsThrowable(callType)

        val resultType = getParameterUpperBound(1, callType as ParameterizedType)
        return EitherCallAdapter(resultType, coroutineScope)
      }
      Deferred::class.java -> {
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawType = getRawType(callType)
        if (rawType != Either::class.java) {
          return null
        }

        checkLeftIsThrowable(callType)

        val resultType = getParameterUpperBound(1, callType as ParameterizedType)
        return EitherDeferredCallAdapter<Any>(resultType, coroutineScope)
      }
      else -> return null
    }
  }

  private fun checkLeftIsThrowable(callType: Type) {
    val throwableType = getParameterUpperBound(0, callType as ParameterizedType)
    val throwable = getRawType(throwableType)
    if (throwable != Throwable::class.java) {
      throw IllegalArgumentException(
        "Either type ($throwableType) is wrong; Throwable type must be placed " +
          "in the left side like Either<Throwable, T>"
      )
    }
  }

  public companion object {
    /**
     * @author skydoves (Jaewoong Eum)
     *
     * Create an instance of [EitherCallAdapterFactory].
     *
     * @param coroutineScope A coroutine scope that runs network requests.
     */
    @JvmStatic
    public fun create(
      coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): EitherCallAdapterFactory = EitherCallAdapterFactory(coroutineScope = coroutineScope)
  }
}
