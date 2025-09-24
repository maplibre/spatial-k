# Contributing

## Find or file an issue to work on

If you're looking to add a feature or fix a bug and there's no issue filed yet,
it's good to
[file an issue](https://github.com/maplibre/spatial-k/issues/new/choose)
first to have a discussion about the change before you start working on it.

If you're new and looking for things to contribute, see our
[good first issue](https://github.com/maplibre/spatial-k/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22)
label.

## Set up your development environment

### Kotlin Multiplatform

Check out
[the official instructions](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html)
for setting up a Kotlin Multiplatform environment.

### IDE

As there's no stable LSP for Kotlin Multiplatform, you'll want to use either
IntelliJ IDEA or Android Studio for developing Spatial-K.

### Tests

Run `./gradlew build` to compile and run all checks, including tests across multiple platforms.

Tests make use of JSON data loaded from files, so platforms where it's not convenient to load
files from the file system have their tests disabled. This includes mobile native targets,
browser targets, etc.
