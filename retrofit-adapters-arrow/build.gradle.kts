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
import com.skydoves.retrofit.adapters.Configuration

plugins {
  id(libs.plugins.android.library.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
  id(libs.plugins.nexus.plugin.get().pluginId)
}

apply(from = "${rootDir}/scripts/publish-module.gradle.kts")

mavenPublishing {
  val artifactId = "retrofit-adapters-arrow"
  coordinates(
    Configuration.artifactGroup,
    artifactId,
    rootProject.extra.get("libVersion").toString()
  )

  pom {
    name.set(artifactId)
    description.set(
      "Retrofit call adapters for modeling network responses using Kotlin Result, " +
        "Jetpack Paging3, and Arrow Either."
    )
  }
}

android {
  compileSdk = Configuration.compileSdk
  namespace = "com.skydoves.retrofit.adapters.arrow"
  defaultConfig {
    minSdk = Configuration.minSdk
    consumerProguardFiles("consumer-rules.pro")
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
  }

  lint {
    abortOnError = false
  }
}

dependencies {
  implementation(libs.coroutines)
  implementation(libs.okhttp)
  implementation(libs.arrow)
  api(libs.retrofit)

  // unit test
  testImplementation(project(":retrofit-adapters-test"))
  testImplementation(libs.junit)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.inline)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.arch.test)
  testImplementation(libs.mock.webserver)
  testImplementation(libs.retrofit.moshi)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.turbine)
}
