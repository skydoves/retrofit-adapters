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

import com.skydoves.retrofit.adapters.paging.annotations.PagingKeyConfig

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.0
 *
 * [PagingMapper] must be used in the [PagingKeyConfig] annotation, which maps the response
 * type [T] to a list of [R].
 *
 * ```kotlin
 * public class PokemonPagingMapper : PagingMapper<PokemonResponse, Pokemon> {

 *   override fun map(value: PokemonResponse): List<Pokemon> {
 *     return value.results
 *   }
 * }
 * ```
 */
public fun interface PagingMapper<T : Any, R : Any> {

  /**
   * @author skydoves (Jaewoong Eum)
   * @since 1.0.0
   *
   * Maps the response type [T] to the list of [R].
   */
  public fun map(value: T): List<R>
}
