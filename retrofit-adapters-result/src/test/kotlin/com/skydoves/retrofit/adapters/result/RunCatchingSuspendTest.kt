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
package com.skydoves.retrofit.adapters.result

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(JUnit4::class)
internal class RunCatchingSuspendTest {

  @Test
  fun `encapsulates a thrown exception as a failure`() = runBlocking {
    val result = runCatchingSuspend { throw IOException("boom") }

    assertThat(result.isFailure, `is`(true))
    assertThat(result.exceptionOrNull() is IOException, `is`(true))
  }

  @Test
  fun `encapsulates a receiver block as a failure`() = runBlocking {
    val result = "receiver".runCatchingSuspend { throw IOException(this) }

    assertThat(result.isFailure, `is`(true))
    assertThat(result.exceptionOrNull()?.message, `is`("receiver"))
  }

  @Test
  fun `cancelling the caller cancels the block instead of encapsulating it`() = runBlocking {
    val resumedAfterCancellation = AtomicBoolean(false)
    val scope = CoroutineScope(Dispatchers.Default)

    val job = scope.launch {
      runCatchingSuspend {
        delay(10_000)
        "never"
      }
      // Reaching this line means the CancellationException was folded into a Result.
      resumedAfterCancellation.set(true)
    }

    delay(200)
    job.cancel()
    job.join()

    assertThat(
      "CancellationException was swallowed into Result.failure",
      resumedAfterCancellation.get(),
      `is`(false),
    )
  }
}
