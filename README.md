<h1 align="center">Retrofit Adapters</h1></br>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/skydoves/retrofit-adapters/actions/workflows/android.yml"><img alt="Build Status" src="https://github.com/skydoves/retrofit-adapters/actions/workflows/android.yml/badge.svg"/></a>
  <a href="https://github.com/skydoves"><img alt="Profile" src="https://skydoves.github.io/badges/skydoves.svg"/></a>
  <a href="https://skydoves.github.io/libraries/retrofit-adapters/html/index.html"><img alt="Dokka" src="https://skydoves.github.io/badges/dokka-retrofit-adapters.svg"/></a>
</p>

<p align="center">
🚆 Retrofit adapters for modeling network responses with Kotlin Result, Jetpack Paging3, and Arrow Either.
</p>

<p align="center">
<img src="https://user-images.githubusercontent.com/24237865/178486849-1dd506a6-79d8-4cc5-a986-56c69b3693cb.png"/>
</p>

## Sandwich
If you're interested in a more specified and lightweight Monad sealed API library for modeling Retrofit responses and handling exceptions, check out [Sandwich](https://github.com/skydoves/sandwich).

<img align="right" width="90px" src="https://user-images.githubusercontent.com/24237865/178630165-76855349-ac04-4474-8bcf-8eb5f8c41095.png"/>

## Kotlin's Result

This library allows you to model your Retrofit responses with [Kotlin's Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) class. 

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/retrofit-adapters-result.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22retrofit-adapters-result%22)
<br>

Add the dependency below to your **module**'s `build.gradle.kts` file:

```gradle
dependencies {
    implementation("com.github.skydoves:retrofit-adapters-result:1.1.0")
}
```

### ResultCallAdapterFactory

You can return [Kotlin's Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/) class to the Retrofit's service methods by setting `ResultCallAdapterFactory` like the below:

```kotlin
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("BASE_URL")
    .addConverterFactory(..)
    .addCallAdapterFactory(ResultCallAdapterFactory.create())
    .build()
```

Then you can return the `Result` class with the suspend keyword.

```kotlin
interface PokemonService {

  @GET("pokemon")
  suspend fun fetchPokemonList(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Result<PokemonResponse>
}
```

Finally, you will get the network response, which is wrapped by the `Result` class like the below:

```kotlin
viewModelScope.launch {
  val result = pokemonService.fetchPokemonList()
  if (result.isSuccess) {
    val data = result.getOrNull()
    // handle data
  } else {
    // handle error case
  }
}
```

### Empty Content Response

You can confine the response type as Unit when you need to handle empty body (content) API requests like the below:

```kotlin
@POST("/users/info")
suspend fun updateUserInfo(@Body userRequest: UserRequest): Result<Unit>
```

### Null Body as Failure

A successful response with no body is reported as `Result.success(null)` by default, even when the
service method declares a non-null type. That null then surfaces far away from the call site. Set
`nullBodyAsFailure` to receive a `NullBodyException` failure instead:

```kotlin
val retrofit: Retrofit = Retrofit.Builder()
  .baseUrl("BASE_URL")
  .addConverterFactory(..)
  .addCallAdapterFactory(ResultCallAdapterFactory.create(nullBodyAsFailure = true))
  .build()
```

### Unit Tests by Injecting TestScope

You can also inject your custom `CoroutineScope` into the `ResultCallAdapterFactory` and execute network requests on the scope.

```kotlin
val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
val testScope = TestScope(testDispatcher)
val retrofit: Retrofit = Retrofit.Builder()
  .baseUrl("BASE_URL")
  .addConverterFactory(..)
  .addCallAdapterFactory(ResultCallAdapterFactory.create(testScope))
  .build()
```

> **Note**: For more information about the Testing coroutines, check out the [Testing Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines/test).

<img align="right" width="130px" src="https://user-images.githubusercontent.com/24237865/178630375-bedd3be4-8d1e-4ba4-bf25-2640a16fcf6c.png"/>

## Jetpack's Paging

This library allows you to return the paging source, which is parts of the Jetpack's [Paging library](https://developer.android.com/topic/libraries/architecture/paging/v3-overview).

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/retrofit-adapters-paging.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22retrofit-adapters-paging%22)
<br>

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.skydoves:retrofit-adapters-paging:<version>"
}
```

### PagingCallAdapterFactory

You can return Jetpack's [PagingSource](https://developer.android.com/reference/kotlin/androidx/paging/PagingSource) class to the Retrofit's service methods by setting `PagingCallAdapterFactory` like the below:

```kotlin
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("BASE_URL")
    .addConverterFactory(..)
    .addCallAdapterFactory(PagingCallAdapterFactory.create())
    .build()
```

Then you can return the `NetworkPagingSource` class with the `@PagingKeyConfig` and `@PagingKey` annotations:

```kotlin
interface PokemonService {

  @GET("pokemon")
  @PagingKeyConfig(
    keySize = 20,
    mapper = PokemonPagingMapper::class
  )
  suspend fun fetchPokemonListAsPagingSource(
    @Query("limit") limit: Int = 20,
    @PagingKey @Query("offset") offset: Int = 0,
  ): NetworkPagingSource<PokemonResponse, Pokemon>
}
```

### PagingKeyConfig and PagingKey

To return the `NetworkPagingSource` class, you must attach the `@PagingKeyConfig` and `@PagingKey` annotations to your Retrofit's service methods.

- **@PagingKeyConfig**: Contains paging configurations for the network request and delivery them to the call adapter internally. You should set the `keySize` and `mapper` parameters.
- **@PagingKey**: Marks the parameter in the service interface method as the paging key. This parameter will be paged by incrementing the page values continuously.

## PagingMapper

You should create a paging mapper class, which extends the `PagingMapper<T, R>` interface like the below for transforming the original network response to the list of paging items. This class should be used in the `@PagingKeyConfig` annotation.

```kotlin
class PokemonPagingMapper : PagingMapper<PokemonResponse, Pokemon> {

  override fun map(value: PokemonResponse): List<Pokemon> {
    return value.results
  }
}
```

### Compile Time Validation

The call adapter resolves the paging configuration reflectively, so a mistake normally shows up as a
crash on the first request, and R8 shrinks the mapper classes away because a `KClass` annotation
value is not a keep root. Apply the KSP processor to catch those at compile time and to instantiate
mappers without reflection:

```gradle
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.github.skydoves:retrofit-adapters-paging:<version>")
    ksp("com.github.skydoves:retrofit-adapters-paging-compiler:<version>")
}
```

It reports a missing `@PagingKeyConfig` or `@PagingKey`, a non Int paging key, a non suspend method,
a `keySize` of zero or less, and a mapper that does not implement `PagingMapper`, cannot be
instantiated, or whose type arguments do not match the returned `NetworkPagingSource`.

You will get the network response, which is wrapped by the `NetworkPagingSource` class like the below:

```kotlin
viewModelScope.launch {
  val pagingSource = pokemonService.fetchPokemonListAsPagingSource()
  val pagerFlow = Pager(PagingConfig(pageSize = 20)) { pagingSource }.flow
  stateFlow.emitAll(pagerFlow)
}
```

Finally, you should call the `submitData` method by your `PagingDataAdapter` to bind the paging data. If you want to learn more about the Jetpack's Paging, check out the [Paging library](https://developer.android.com/topic/libraries/architecture/paging/v3-overview). 

<img align="right" width="110px" src="https://user-images.githubusercontent.com/24237865/178630401-9d4472e0-3da2-4e94-8ff9-ee8d7d089df2.svg"/>

## Arrow's Either

This library allows you to model your Retrofit responses with [arrow-kt](https://github.com/arrow-kt/arrow)'s [Either](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/) class. 

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/retrofit-adapters-arrow.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22retrofit-adapters-arrow%22)
<br>

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.skydoves:retrofit-adapters-arrow:<version>"
}
```

### EitherCallAdapterFactory

You can return [Arrow's Either](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/) class to the Retrofit's service methods by setting `EitherCallAdapterFactory` like the below:

```kotlin
val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("BASE_URL")
    .addConverterFactory(..)
    .addCallAdapterFactory(EitherCallAdapterFactory.create())
    .build()
```

Then you can return the `Either` class with the suspend keyword.

```kotlin
interface PokemonService {

  @GET("pokemon")
  suspend fun fetchPokemonListAsEither(
    @Query("limit") limit: Int = 20,
    @Query("offset") offset: Int = 0
  ): Either<Throwable, PokemonResponse>
}
```

Finally, you will get the network response, which is wrapped by the `Either` class like the below:

```kotlin
viewModelScope.launch {
  val either = pokemonService.fetchPokemonListAsEither()
  if (either.isRight()) {
    val data = either.orNull()
    // handle data
  } else {
    // handle error case
  }
}
```

### Empty Content Response

You can confine the response type as Unit when you need to handle empty body (content) API requests like the below:

```kotlin
@POST("/users/info")
suspend fun updateUserInfo(@Body userRequest: UserRequest): Either<Throwable, Unit>
```

### Null Body as Failure

`EitherCallAdapterFactory` takes the same `nullBodyAsFailure` flag, which reports a null body as a
`NullBodyException` on the left instead of a right hand null:

```kotlin
.addCallAdapterFactory(EitherCallAdapterFactory.create(nullBodyAsFailure = true))
```

### Unit Tests by Injecting TestScope

You can also inject your custom `CoroutineScope` into the `EitherCallAdapterFactory` and execute network requests on the scope.

```kotlin
val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
val testScope = TestScope(testDispatcher)
val retrofit: Retrofit = Retrofit.Builder()
  .baseUrl("BASE_URL")
  .addConverterFactory(..)
  .addCallAdapterFactory(EitherCallAdapterFactory.create(testScope))
  .build()
```

> **Note**: For more information about the Testing coroutines, check out the [Testing Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines/test).

<img align="right" width="90px" src="https://user-images.githubusercontent.com/24237865/178630165-76855349-ac04-4474-8bcf-8eb5f8c41095.png"/>

## Error Body Deserialization

This library allows you to deserialize the error body of a Retrofit response into your own error
class. Pick the artifact that matches the json library you already use, so nothing else is pulled
into your build:

| Artifact | Json library |
| --- | --- |
| `retrofit-adapters-serialization` | [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html) |
| `retrofit-adapters-moshi` | [Moshi](https://github.com/square/moshi) |
| `retrofit-adapters-gson` | [Gson](https://github.com/google/gson) |

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/retrofit-adapters-serialization.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22retrofit-adapters-serialization%22)
<br>

Add the one you need to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.skydoves:retrofit-adapters-serialization:<version>"
    // or
    implementation "com.github.skydoves:retrofit-adapters-moshi:<version>"
    // or
    implementation "com.github.skydoves:retrofit-adapters-gson:<version>"
}
```

### Deserialize Error Body

Define your custom error class following your RESTful API format:

```kotlin
@Serializable
public data class ErrorMessage(
  val code: Int,
  val message: String
)
```

Then deserialize a failure with the `deserializeHttpError` extension:

```kotlin
val result = pokemonService.fetchPokemonList()
result.onSuccessSuspend {
  Timber.d("fetched as Result: $it")
}.onFailureSuspend { throwable ->
  val errorBody = throwable.deserializeHttpError<ErrorMessage>()
}
```

It returns `null` instead of throwing when the failure is not an `HttpException`, the error body is
empty, or the payload does not match your error class. Unknown keys are ignored, and reading the
error body does not consume it, so the same failure can be deserialized more than once.

> **Note**: The Moshi artifact resolves generated adapters, so annotate your error model with
> `@JsonClass(generateAdapter = true)` and apply `moshi-kotlin-codegen`.

### onFailureSuspendAsError and onLeftSuspendAsError

`retrofit-adapters-result` and `retrofit-adapters-arrow` carry no json dependency, so they take the
deserializer as a parameter. That lets the same call site work with any of the three artifacts
above:

```kotlin
result.onFailureSuspendAsError({ it.deserializeHttpError<ErrorMessage>() }) { errorModel ->
  // handle the error model
}

either.onLeftSuspendAsError({ it.deserializeHttpError<ErrorMessage>() }) { errorModel ->
  // handle the error model
}
```

## Find this repository useful? :heart:
Support it by joining __[stargazers](https://github.com/skydoves/retrofit-adapters/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/skydoves)__ on GitHub for my next creations! 🤩

# License
```xml
Designed and developed by 2022 skydoves (Jaewoong Eum)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
