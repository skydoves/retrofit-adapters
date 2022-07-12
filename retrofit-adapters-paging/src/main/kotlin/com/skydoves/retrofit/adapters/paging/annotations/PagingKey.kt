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

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [PagingKey] marks the parameter in the service interface method as the paging key.
 * The parameter value decides where to start the paging key.
 *
 * ```kotlin
 *   @GET("pokemon")
 *   @PagingKeyConfig(
 *     keySize = 20,
 *     mapper = PokemonPagingMapper::class
 *   )
 *   public suspend fun fetchPokemonList(
 *      @Query("limit") limit: Int = 20,
 *      @PagingKey @Query("offset") offset: Int = 0, // mark this field as the paging key.
 *   ): NetworkPagingSource<PokemonResponse, Pokemon>
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class PagingKey
