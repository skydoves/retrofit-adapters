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
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [PagingSourceCall] is an implementation of Retrofit's [Call] interface, which executes
 * the network data, handles and decides whether they succeed or fail.
 *
 * @param proxy Proxy of the original http request.
 * @param pagingKeyConfig Paging key configuration.
 */
internal class PagingSourceCall<T : Any, R : Any>(
  private val proxy: Call<T>,
  private val pagingKeyConfig: PagingKeyConfig
) : Call<NetworkPagingSource<T, R>> {

  override fun execute(): Response<NetworkPagingSource<T, R>> {
    val pagingSource = pagingSource(proxy, pagingKeyConfig)
    return Response.success(pagingSource)
  }

  override fun enqueue(callback: Callback<NetworkPagingSource<T, R>>) {
    val pagingSource = pagingSource(proxy, pagingKeyConfig)
    callback.onResponse(this@PagingSourceCall, Response.success(pagingSource))
  }

  override fun clone(): Call<NetworkPagingSource<T, R>> =
    PagingSourceCall(proxy.clone(), pagingKeyConfig)

  override fun request(): Request = proxy.request()
  override fun timeout(): Timeout = proxy.timeout()
  override fun isExecuted(): Boolean = proxy.isExecuted
  override fun isCanceled(): Boolean = proxy.isCanceled
  override fun cancel() = proxy.cancel()
}
