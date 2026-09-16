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
package com.skydoves.retrofit.adapters.core

import retrofit2.HttpException
import java.io.IOException
import kotlin.text.Charsets

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * Returns the error body of this [Throwable] as a string, or null when it is not an
 * [HttpException] or carries no error body.
 *
 * `ResponseBody.string()` consumes and closes the body, so reading it a second time fails. This
 * reads through a peeked source instead, which leaves the original body untouched and lets several
 * error deserializers run over the same failure.
 */
public fun Throwable.httpErrorBody(): String? {
  if (this !is HttpException) return null
  val errorBody = response()?.errorBody() ?: return null

  return try {
    val charset = errorBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
    errorBody.source().peek().readString(charset)
  } catch (e: IOException) {
    null
  } catch (e: IllegalStateException) {
    // The body was already consumed and closed elsewhere.
    null
  }
}
