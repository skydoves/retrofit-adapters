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
import com.skydoves.retrofit.adapters.test.PokemonResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

internal interface PokemonService {

  @GET("pokemon")
  suspend fun fetchPokemonList(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Either<Throwable, PokemonResponse>

  @GET("pokemon")
  fun fetchPokemonListAsCall(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Call<Either<Throwable, PokemonResponse>>

  @GET("pokemon")
  suspend fun fetchPokemonListEmptyBody(): Either<Throwable, Unit>

  @GET("pokemon")
  suspend fun fetchPokemonListOptional(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Either<Throwable, PokemonResponse?>
}
