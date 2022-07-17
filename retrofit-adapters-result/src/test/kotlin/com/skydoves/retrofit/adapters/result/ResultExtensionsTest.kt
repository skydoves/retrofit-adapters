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

@file:OptIn(ExperimentalTime::class)

package com.skydoves.retrofit.adapters.result

import app.cash.turbine.test
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
internal class ResultExtensionsTest {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()

  @Test
  fun `onSuccessSuspend Test`() = runTest {
    val result = Result.success(123)

    val flow = flow {
      result.onSuccessSuspend { emit(it) }
    }

    flow.test {
      val item = awaitItem()
      assertThat(item, `is`(123))
      awaitComplete()
    }
  }

  @Test
  fun `onFailureSuspend Test`() = runTest {
    val exception = RuntimeException("Exception")
    val result = Result.failure<Int>(exception)

    val flow = flow {
      result.onFailureSuspend { emit(it) }
    }

    flow.test {
      val item = awaitItem()
      assertThat(item, `is`(exception))
      awaitComplete()
    }
  }

  @Test
  fun `foldSuspend Test`() = runTest {
    val success = Result.success(123)

    val successFlow = flow {
      success.foldSuspend(
        onSuccess = { emit(it) },
        onFailure = { }
      )
    }

    successFlow.test {
      val item = awaitItem()
      assertThat(item, `is`(123))
      awaitComplete()
    }

    val exception = RuntimeException("Exception")
    val failure = Result.failure<Int>(exception)

    val flow = flow {
      failure.foldSuspend(
        onSuccess = { },
        onFailure = { emit(exception) }
      )
    }

    flow.test {
      val item = awaitItem()
      assertThat(item, `is`(exception))
      awaitComplete()
    }
  }

  @Test
  fun `mapSuspend Test`() = runTest {
    val result = Result.success(123)

    val flow = flow {
      result.mapSuspend { emit(it.toString()) }
    }

    flow.test {
      val item = awaitItem()
      assertThat(item, `is`("123"))
      awaitComplete()
    }
  }

  @Test
  fun `recoverSuspend Test`() = runTest {
    val exception = RuntimeException("Exception")
    val result = Result.failure<Int>(exception)

    val flow = flow {
      result.recoverSuspend { emit(123) }
    }

    flow.test {
      val item = awaitItem()
      assertThat(item, `is`(123))
      awaitComplete()
    }
  }
}
