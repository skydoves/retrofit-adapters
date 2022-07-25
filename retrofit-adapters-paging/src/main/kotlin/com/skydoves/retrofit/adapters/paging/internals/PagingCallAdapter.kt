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

package com.skydoves.retrofit.adapters.paging.internals

import com.skydoves.retrofit.adapters.paging.NetworkPagingSource
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [CallAdapter] that delegates network call request and creates an instance of the [PagingSourceCall].
 *
 * @property resultType Type of the result from the http request.
 * @property pagingKeyConfig Paging key configurations.
 */
internal class PagingCallAdapter(
  private val resultType: Type,
  private val pagingKeyConfig: PagingKeyConfig
) : CallAdapter<Type, Call<NetworkPagingSource<Type, Type>>> {

  override fun responseType(): Type = resultType

  override fun adapt(call: Call<Type>): Call<NetworkPagingSource<Type, Type>> {
    return PagingSourceCall(
      proxy = call,
      pagingKeyConfig = pagingKeyConfig
    )
  }
}
