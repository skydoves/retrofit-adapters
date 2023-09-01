<h1 align="center">Retrofit Adapters</h1></br>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=19"><img alt="API" src="https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat"/></a>
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

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.skydoves:retrofit-adapters-result:1.0.7"
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

## Kotlin Serialization

This library allows you to deserialize your error body of the Retrofit response as your custom error class with [Kotlin's Serialization](https://kotlinlang.org/docs/serialization.html).

> For more information about setting up the plugin and dependency, check out [Kotlin's Serialization](https://kotlinlang.org/docs/serialization.html).

[![Maven Central](https://img.shields.io/maven-central/v/com.github.skydoves/retrofit-adapters-serialization.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.skydoves%22%20AND%20a:%22retrofit-adapters-serialization%22)
<br>

Add the dependency below to your **module**'s `build.gradle` file:

```gradle
dependencies {
    implementation "com.github.skydoves:retrofit-adapters-serialization:<version>"
}
```

### Deserialize Error Body

You can deserialize your error body with the `deserializeHttpError` extension and your custom error class. First, define your custom error class following your RESTful API formats as seen in the below:

```kotlin
@Serializable
public data class ErrorMessage(
  val code: Int,
  val message: String
)
```

Next, gets the result of the error class to the `throwable` instance with the `deserializeHttpError` extension like the below:

```kotlin
val result = pokemonService.fetchPokemonList()
result.onSuccessSuspend {
  Timber.d("fetched as Result: $it")
}.onFailureSuspend { throwable ->
  val errorBody = throwable.deserializeHttpError<ErrorMessage>()
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
