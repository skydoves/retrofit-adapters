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
package com.skydoves.retrofit.adapters.paging.annotations

import com.skydoves.retrofit.adapters.paging.PagingMapper
import com.skydoves.retrofit.adapters.paging.internals.PagingCallAdapter
import kotlin.reflect.KClass

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [PagingKeyConfig] contains the paging configurations on the service interface method to give information
 * to the internal [PagingCallAdapter].
 *
 * ```kotlin
 *   @GET("pokemon")
 *   @PagingKeyConfig(
 *     keySize = 20,
 *     mapper = PokemonPagingMapper::class
 *   )
 *   public suspend fun fetchPokemonList(
 *      @Query("limit") limit: Int = 20,
 *      @PagingKey @Query("offset") offset: Int = 0
 *   ): NetworkPagingSource<PokemonResponse, Pokemon>
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class PagingKeyConfig(
  /**
   * Key size decides the size of the paging.
   * This key size will be multiplied by the paging key value and decides the next paging offset.
   */
  val keySize: Int,
  /**
   * Maps the response type to the list of a paging item, which must extends [PagingMapper].
   */
  val mapper: KClass<*>,
)
