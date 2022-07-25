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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.skydoves.retrofit.adapters.paging.annotations.PagingKey
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Retrofit
import retrofit2.awaitResponse

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * NetworkPagingSource is a implementation of [PagingSource] that was designed to load pages of data
 * for an instance of [PagingData] and to be combined with [Pager]. You return [NetworkPagingSource]
 * for your Retrofit service method by adding [PagingCallAdapterFactory] to your [Retrofit.Builder].
 *
 * If you want to return [NetworkPagingSource], you must use the [PagingKeyConfig] and [PagingKey]
 * to your method and parameter for providing proper paging configurations to the adapter factory.
 *
 * [PagingData] queries data from its [NetworkPagingSource] in response, which came from the network
 * response as the user scrolls in a RecyclerView. To control how and when a PagingData queries data
 * from its [PagingSource], see [PagingConfig], which defines behavior such as [PagingConfig.pageSize]
 * and [PagingConfig.prefetchDistance].
 *
 * Basically, the default page key starts from the [offsetPageKey] that will be decided by the
 * call value of the parameter, which is annotated with [PagingKey].
 *
 * @property offsetPageKey A default page key that decides offset of the paging key value.
 * @property mapper Maps the response type [T] to the list of [R].
 * @property call A lambda function that returns [Call] interface to continuously fetch network data.
 */
public class NetworkPagingSource<T : Any, R : Any> constructor(
  private val offsetPageKey: Int,
  private val mapper: PagingMapper<T, R>,
  private val call: suspend (page: Int) -> Call<T>
) : PagingSource<Int, R>() {

  override fun getRefreshKey(state: PagingState<Int, R>): Int? {
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(SINGLE_PAGE_SIZE) ?: anchorPage?.nextKey?.minus(SINGLE_PAGE_SIZE)
    }
  }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> {
    try {
      val pageKey = params.key ?: offsetPageKey
      val response = call(pageKey).awaitResponse()
      if (response.isSuccessful) {
        val body = response.body()
        if (body == null) {
          val invocation = call(pageKey).request().tag(Invocation::class.java)
          val method = invocation?.method()
          throw KotlinNullPointerException(
            "Response from " +
              method?.declaringClass?.name +
              '.' +
              method?.name +
              " was null but response body type was declared as non-null"
          )
        } else {
          return LoadResult.Page(
            data = mapper.map(body),
            prevKey = if (pageKey == offsetPageKey) null else pageKey.minus(SINGLE_PAGE_SIZE),
            nextKey = pageKey.plus(SINGLE_PAGE_SIZE)
          )
        }
      } else {
        throw HttpException(response)
      }
    } catch (exception: Exception) {
      return LoadResult.Error(exception)
    }
  }

  private companion object {
    private const val SINGLE_PAGE_SIZE = 1
  }
}
