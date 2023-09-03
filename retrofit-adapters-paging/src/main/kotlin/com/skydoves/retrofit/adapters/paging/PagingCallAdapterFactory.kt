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
package com.skydoves.retrofit.adapters.paging

import android.content.res.Resources.NotFoundException
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import com.skydoves.retrofit.adapters.paging.internals.PagingCallAdapter
import com.skydoves.retrofit.adapters.paging.internals.PagingSourceCall
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.createInstance

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * Creates [PagingSourceCall] instances, which return [NetworkPagingSource] type of the response from the
 * service interface method with the `suspend` keyword.
 *
 * You can integrate [PagingCallAdapterFactory] with Jetpack's Paging3 library for paging network data.
 */
public class PagingCallAdapterFactory private constructor() : CallAdapter.Factory() {

  override fun get(
    returnType: Type,
    annotations: Array<out Annotation>,
    retrofit: Retrofit,
  ): CallAdapter<*, *>? {
    when (getRawType(returnType)) {
      Call::class.java -> {
        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rawType = getRawType(callType)
        if (rawType != NetworkPagingSource::class.java) {
          return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        val pagingKeyConfig = annotations.filterIsInstance<PagingKeyConfig>().firstOrNull()
          ?: throw NotFoundException(
            "Missing @PagingKeyConfig annotation for a method, " +
              "which returns NetworkPagingSource type.",
          )

        val mapper = pagingKeyConfig.mapper.createInstance()
        if (mapper !is PagingMapper<*, *>) {
          throw NotFoundException(
            "The mapper parameter class must implement PagingMapper " +
              "interface: ${pagingKeyConfig.mapper}",
          )
        }

        return PagingCallAdapter(resultType, pagingKeyConfig)
      }

      else -> return null
    }
  }

  public companion object {
    /**
     * @author skydoves (Jaewoong Eum)
     * @since 1.0.0
     *
     * Create an instance of [PagingCallAdapterFactory].
     */
    @JvmStatic
    public fun create(): PagingCallAdapterFactory = PagingCallAdapterFactory()
  }
}
