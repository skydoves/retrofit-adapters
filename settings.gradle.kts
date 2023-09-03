pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "retrofit-adapters-demo"
include(":app")
include(":retrofit-adapters-arrow")
include(":retrofit-adapters-result")
include(":retrofit-adapters-paging")
include(":retrofit-adapters-test")
include(":retrofit-adapters-serialization")
