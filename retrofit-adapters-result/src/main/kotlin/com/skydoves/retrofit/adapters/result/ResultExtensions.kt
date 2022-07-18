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

@file:Suppress("UNCHECKED_CAST")

package com.skydoves.retrofit.adapters.result

import com.skydoves.retrofit.adapters.serialization.deserializeHttpError
import retrofit2.HttpException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Calls the specified suspend function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the suspend [block] function execution and encapsulating it as a failure.
 *
 * @param block Returns its encapsulated result if invocation was successful,
 */
public suspend inline fun <R> runCatchingSuspend(
  crossinline block: suspend () -> R
): Result<R> {
  return try {
    Result.success(block())
  } catch (e: Throwable) {
    Result.failure(e)
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Calls the specified suspend function [block] with this value as its receiver and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating it as a failure.
 *
 * @param block Returns its encapsulated result if invocation was successful,
 */
public suspend inline fun <T, R> T.runCatchingSuspend(
  crossinline block: suspend T.() -> R
): Result<R> {
  return try {
    Result.success(block())
  } catch (e: Throwable) {
    Result.failure(e)
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Performs the given suspend [action] on the encapsulated value if this instance represents success.
 * Returns the original Result unchanged.
 *
 * @param action Performs on the encapsulated value if this instance represents success.
 */
public suspend inline fun <T> Result<T>.onSuccessSuspend(
  crossinline action: suspend (value: T) -> Unit
): Result<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  if (isSuccess) action(getOrThrow())
  return this
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Performs the given suspend [action] on the encapsulated [Throwable] exception if this instance represents failure.
 * Returns the original [Result] unchanged.
 *
 * @param action Performs on the encapsulated value if this instance represents failure.
 */
public suspend inline fun <T> Result<T>.onFailureSuspend(
  crossinline action: suspend (exception: Throwable) -> Unit
): Result<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  exceptionOrNull()?.let { action(it) }
  return this
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Performs the given suspend [action] on the encapsulated custom error model [E] if this instance represents failure.
 * The `errorBody()` of the [HttpException] will be deserialized to your custom error model [E].
 * The given suspend [action] can receive null if the error body is empty.
 * Returns the original [Result] unchanged.
 *
 * @param action Performs on the encapsulated value if this instance represents failure.
 */
public suspend inline fun <T, reified E> Result<T>.onFailureSuspendAsError(
  crossinline action: suspend (errorModel: E?) -> Unit
): Result<T> {
  contract {
    callsInPlace(action, InvocationKind.AT_MOST_ONCE)
  }
  exceptionOrNull()?.let { throwable ->
    action(throwable.deserializeHttpError<E>())
  }
  return this
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Returns the result of [onSuccess] for the encapsulated value if this instance represents success or
 * the result of [onFailure] suspend function for the encapsulated [Throwable] exception if it is failure.
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] or by [onFailure] function.
 */
public suspend inline fun <R, T> Result<T>.foldSuspend(
  crossinline onSuccess: suspend (value: T) -> R,
  crossinline onFailure: suspend (exception: Throwable) -> R
): R {
  contract {
    callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
    callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
  }
  return when (val exception = exceptionOrNull()) {
    null -> onSuccess(getOrThrow())
    else -> onFailure(exception)
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Returns the encapsulated result of the given [transform] suspend function applied to the encapsulated value
 * if this instance represents [success][Result.isSuccess] or the
 * original encapsulated [Throwable] exception if it is [failure][Result.isFailure].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] suspend function.
 * See [mapCatching] for an alternative that encapsulates exceptions.
 */
public suspend inline fun <R, T> Result<T>.mapSuspend(
  crossinline transform: suspend (value: T) -> R
): Result<R> {
  contract {
    callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
  }
  return when {
    isSuccess -> Result.success(transform(getOrThrow()))
    else -> Result.failure(exceptionOrNull()!!)
  }
}

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Returns the encapsulated result of the given [transform] suspend function applied to the encapsulated [Throwable] exception
 * if this instance represents [failure][Result.isFailure] or the
 * original encapsulated value if it is [success][Result.isSuccess].
 *
 * Note, that this function rethrows any [Throwable] exception thrown by [transform] suspend function.
 * See [recoverCatching] for an alternative that encapsulates exceptions.
 */
public suspend inline fun <R, T : R> Result<T>.recoverSuspend(
  crossinline transform: suspend (exception: Throwable) -> R
): Result<R> {
  contract {
    callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
  }
  return when (val exception = exceptionOrNull()) {
    null -> this
    else -> Result.success(transform(exception))
  }
}
