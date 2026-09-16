import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Designed and developed by 2022 skydoves (Jaewoong Eum)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.kotlin.binary.compatibility)
  alias(libs.plugins.nexus.plugin)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
}

apiValidation {
  ignoredProjects.addAll(listOf("app"))
}

// binary-compatibility-validator registers its tasks by reacting to the kotlin-android plugin,
// which AGP 9 replaced with built in Kotlin support. The Android library modules therefore lost
// their api tasks, so they are wired up here against the release variant classes instead.
subprojects {
  pluginManager.withPlugin("com.android.library") {
    if (name in rootProject.the<kotlinx.validation.ApiValidationExtension>().ignoredProjects) {
      return@withPlugin
    }

    // `name` resolves to the applied plugin inside this block and to the task inside the task
    // blocks, so the project name is captured explicitly.
    val projectName = project.name
    val referenceApiFile = layout.projectDirectory.file("api/$projectName.api")

    val apiBuild = tasks.register<kotlinx.validation.KotlinApiBuildTask>("apiBuild") {
      group = "other"
      description = "Builds the public ABI of the release variant."
      // Resolved lazily: the Android compile tasks do not exist while the plugin is being applied.
      dependsOn("compileReleaseKotlin")
      inputClassesDirs.from(provider { tasks.named("compileReleaseKotlin").get().outputs.files })
      outputApiFile.set(layout.buildDirectory.file("api/$projectName.api"))
    }

    tasks.register<kotlinx.validation.KotlinApiCompareTask>("apiCheck") {
      group = "verification"
      description = "Checks the public ABI against the checked in dump."
      projectApiFile.set(referenceApiFile)
      generatedApiFile.set(apiBuild.flatMap { it.outputApiFile })
    }

    tasks.register<Copy>("apiDump") {
      group = "other"
      description = "Writes the public ABI to the checked in dump."
      from(apiBuild.flatMap { it.outputApiFile })
      into(layout.projectDirectory.dir("api"))
    }

    // The aggregate tasks are created by the validator plugin later, so hook them lazily.
    val projectPath = path
    rootProject.tasks.matching { it.name == "apiCheck" || it.name == "apiDump" }
      .configureEach { dependsOn("$projectPath:${this.name}") }
  }

  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("${layout.buildDirectory.get()}/**/*.kt")
      ktlint().editorConfigOverride(
        mapOf(
          "indent_size" to "2",
          "continuation_indent_size" to "2"
        )
      )
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
      trimTrailingWhitespace()
      endWithNewline()
    }
    format("kts") {
      target("**/*.kts")
      targetExclude("${layout.buildDirectory.get()}/**/*.kts")
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"), "(^(?![\\/ ]\\*).*$)")
      trimTrailingWhitespace()
      endWithNewline()
    }
  }

  if (!name.contains("app")) {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
      compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        freeCompilerArgs.addAll(
          listOf(
            "-Xexplicit-api=strict",
          )
        )
      }
    }
  }

  tasks.withType(JavaCompile::class.java).configureEach {
    this.targetCompatibility = libs.versions.jvmTarget.get()
    this.sourceCompatibility = libs.versions.jvmTarget.get()
  }
}
