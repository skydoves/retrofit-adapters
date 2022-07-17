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
import arrow.core.Either.Left
import arrow.core.Either.Right

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Applies `ifLeft` if this is a [Left] or `ifRight` if this is a [Right].
 *
 * Example:
 * ```
 * val result: Either<Exception, Value> = possiblyFailingOperation()
 * result.fold(
 *      { log("operation failed with $it") },
 *      { log("operation succeeded with $it") }
 * )
 * ```
 *
 * @param ifLeft the function to apply if this is a [Left]
 * @param ifRight the function to apply if this is a [Right]
 * @return the results of applying the function
 */
public suspend inline fun <A, B, C> Either<A, B>.foldSuspend(
  crossinline ifLeft: suspend (A) -> C,
  crossinline ifRight: suspend (B) -> C
): C =
  when (this) {
    is Right -> ifRight(value)
    is Left -> ifLeft(value)
  }

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Performs the given suspend [onRight] on the encapsulated value if this [Either] instance represents [Right].
 * Returns the original Result unchanged.
 *
 * @param onRight Performs on the encapsulated value if this [Either] instance represents [Right].
 */
public suspend inline fun <A, B, C> Either<A, B>.onRightSuspend(
  crossinline onRight: suspend suspend (B) -> C
): Either<A, B> = apply {
  if (this is Right) {
    onRight(value)
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Performs the given suspend [onLeft] on the encapsulated value if this [Either] instance represents [Left].
 * Returns the original Result unchanged.
 *
 * @param onLeft Performs on the encapsulated value if this [Either] instance represents [Left].
 */
public suspend inline fun <A, B, C> Either<A, B>.onLeftSuspend(
  crossinline onLeft: suspend suspend (A) -> C
): Either<A, B> = apply {
  if (this is Left) {
    onLeft(value)
  }
}
