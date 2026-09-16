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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
  id(libs.plugins.ksp.get().pluginId)
}

android {
  namespace = "com.skydoves.retrofitadaptersdemo"
  compileSdk = Configuration.compileSdk

  defaultConfig {
    applicationId = "com.skydoves.retrofitadaptersdemo"
    minSdk = Configuration.minSdk
    targetSdk = Configuration.targetSdk
    versionCode = Configuration.versionCode
    versionName = Configuration.versionName
  }

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
  }

  lint {
    abortOnError = false
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(project(":retrofit-adapters-result"))
  implementation(project(":retrofit-adapters-arrow"))
  implementation(project(":retrofit-adapters-paging"))
  implementation(project(":retrofit-adapters-serialization"))
  ksp(project(":retrofit-adapters-paging-compiler"))
  implementation(libs.retrofit.moshi)

  // android supports
  implementation(libs.material)
  implementation(libs.lifecycle.viewmodel)

  implementation(libs.arrow)
  implementation(libs.paging)
  implementation(libs.coroutines)
  implementation(libs.retrofit.moshi)
  implementation(libs.moshi)
  ksp(libs.moshi.codegen)

  implementation(libs.glide)
  implementation(libs.timber)
}
