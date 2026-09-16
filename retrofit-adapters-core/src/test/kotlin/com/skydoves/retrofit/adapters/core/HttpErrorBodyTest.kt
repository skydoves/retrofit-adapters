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

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@RunWith(JUnit4::class)
internal class HttpErrorBodyTest {

  private fun httpException(body: String): HttpException = HttpException(
    Response.error<Unit>(400, body.toResponseBody("application/json".toMediaType())),
  )

  @Test
  fun `reads the error body`() {
    assertThat(httpException("""{"code":10}""").httpErrorBody(), `is`("""{"code":10}"""))
  }

  @Test
  fun `the error body can be read more than once`() {
    val exception = httpException("""{"code":10}""")

    assertThat(exception.httpErrorBody(), `is`("""{"code":10}"""))
    assertThat(exception.httpErrorBody(), `is`("""{"code":10}"""))
    assertThat(exception.httpErrorBody(), `is`("""{"code":10}"""))
  }

  @Test
  fun `returns null for a non http throwable`() {
    assertThat(IOException("boom").httpErrorBody(), `is`(nullValue()))
  }
}
