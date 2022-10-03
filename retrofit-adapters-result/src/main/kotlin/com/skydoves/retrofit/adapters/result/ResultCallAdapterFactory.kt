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

package com.skydoves.retrofit.adapters.result

import com.skydoves.retrofit.adapters.result.internals.ResultCallAdapter
import com.skydoves.retrofit.adapters.result.internals.ResultDeferredCallAdapter
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
 * @since 1.0.0
 *
 * Creates [ResultCallAdapter] instances, which return [Result] type of the response from the
 * service interface method with the `suspend` keyword. You can create the [ResultCallAdapterFactory]
 * with your own [CoroutineScope], which executes network requests.
 *
 * Also, [ResultCallAdapterFactory] allows you to return [Deferred] of the [Result].
 *
 * @property coroutineScope A coroutine scope that runs network requests.
 */
public class ResultCallAdapterFactory private constructor(
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
        if (rawType != Result::class.java) {
          return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        val paramType = getRawType(resultType)
        return ResultCallAdapter(
          resultType = resultType,
          paramType = paramType,
          coroutineScope = coroutineScope
        )
      }

      Deferred::class.java -> {
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawType = getRawType(callType)
        if (rawType != Result::class.java) {
          return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        val paramType = getRawType(resultType)
        return ResultDeferredCallAdapter<Any>(
          resultType = resultType,
          paramType = paramType,
          coroutineScope = coroutineScope
        )
      }
      else -> return null
    }
  }

  public companion object {
    /**
     * @author skydoves (Jaewoong Eum)
     * @since 1.0.0
     *
     * Create an instance of [ResultCallAdapterFactory].
     *
     * @param coroutineScope A coroutine scope that runs network requests.
     */
    @JvmStatic
    public fun create(
      coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): ResultCallAdapterFactory = ResultCallAdapterFactory(coroutineScope = coroutineScope)
  }
}
