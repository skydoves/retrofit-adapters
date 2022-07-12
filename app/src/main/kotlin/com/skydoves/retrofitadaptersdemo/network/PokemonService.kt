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

package com.skydoves.retrofitadaptersdemo.network

import arrow.core.Either
import com.skydoves.retrofit.adapters.paging.NetworkPagingSource
import com.skydoves.retrofit.adapters.paging.annotations.PagingKey
import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig
import retrofit2.http.GET
import retrofit2.http.Query

public interface PokemonService {

  @GET("pokemon")
  @PagingKeyConfig(
    keySize = 20,
    mapper = PokemonPagingMapper::class
  )
  public suspend fun fetchPokemonListAsPagingSource(
    @Query("limit") limit: Int = 20,
    @PagingKey @Query("offset") offset: Int = 0,
  ): NetworkPagingSource<PokemonResponse, Pokemon>

  @GET("pokemon")
  public suspend fun fetchPokemonList(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Result<PokemonResponse>

  @GET("pokemon")
  public suspend fun fetchPokemonListAsEither(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Either<Throwable, PokemonResponse>
}
