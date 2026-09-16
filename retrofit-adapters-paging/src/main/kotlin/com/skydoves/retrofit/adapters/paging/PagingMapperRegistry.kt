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
 * @since 1.2.0
 *
 * Creates [PagingMapper] instances without reflection.
 *
 * Mapper classes are only referenced as a `KClass` value inside [PagingKeyConfig], which R8 does
 * not treat as a keep root, so it shrinks them away and reflective instantiation then fails in
 * minified release builds. Applying the `retrofit-adapters-paging-compiler` KSP processor
 * generates an implementation of this interface that constructs every mapper directly, which both
 * keeps the classes alive and removes the reflection.
 *
 * Implementations must have a no-argument constructor.
 */
public interface PagingMapperRegistry {

  /**
   * @author skydoves (Jaewoong Eum)
   * @since 1.2.0
   *
   * Returns a new [PagingMapper] instance for the given [mapperClass], or null if this registry
   * does not know about it.
   */
  public fun create(mapperClass: Class<*>): PagingMapper<*, *>?
}
