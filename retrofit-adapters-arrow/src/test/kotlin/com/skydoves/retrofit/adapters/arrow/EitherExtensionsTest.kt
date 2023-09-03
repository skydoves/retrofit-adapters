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

package com.skydoves.retrofit.adapters.arrow

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
internal class EitherExtensionsTest {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()

  @Test
  fun `onRightSuspend Test`() = runTest {
    val either = 123.right()

    val flow = flow {
      either.onRightSuspend { emit(it) }
    }

    flow.test {
      val item = awaitItem()
      MatcherAssert.assertThat(item, Is.`is`(123))
      awaitComplete()
    }
  }

  @Test
  fun `onLeftSuspend Test`() = runTest {
    val either = 123.left()

    val flow = flow {
      either.onLeftSuspend { emit(it) }
    }

    flow.test {
      val item = awaitItem()
      MatcherAssert.assertThat(item, Is.`is`(123))
      awaitComplete()
    }
  }

  @Test
  fun `onFoldSuspend Test`() = runTest {
    val right = 123.right()

    val rightFlow = flow {
      right.foldSuspend(
        ifLeft = { },
        ifRight = { emit(it) },
      )
    }

    rightFlow.test {
      val item = awaitItem()
      MatcherAssert.assertThat(item, Is.`is`(123))
      awaitComplete()
    }

    val left = 123.left()

    val leftFlow = flow {
      left.foldSuspend(
        ifLeft = { emit(it) },
        ifRight = { },
      )
    }

    leftFlow.test {
      val item = awaitItem()
      MatcherAssert.assertThat(item, Is.`is`(123))
      awaitComplete()
    }
  }
}
