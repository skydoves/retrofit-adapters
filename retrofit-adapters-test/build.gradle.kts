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

plugins {
  id("kotlin")
  id(libs.plugins.ksp.get().pluginId)
}

dependencies {
  implementation(libs.coroutines)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.moshi)
  ksp(libs.moshi.codegen)

  // unit test
  implementation(libs.junit)
  implementation(libs.mockito.core)
  implementation(libs.mockito.inline)
  implementation(libs.mockito.kotlin)
  implementation(libs.arch.test)
  implementation(libs.mock.webserver)
  implementation(libs.retrofit.moshi)
  implementation(libs.coroutines.test)
  implementation(libs.turbine)
}
