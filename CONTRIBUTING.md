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

## Make CI happy

A Git pre-commit hook is available to ensure that the code is formatted before
every commit.

The easiest way to run all formatters is to install [pre-commit](https://pre-commit.com/), and then:

- To run the formatter once on staged files, `pre-commit run`
- To run the formatter in a git hook, `pre-commit install`
- To stop running the formatter in a git hook, `pre-commit uninstall`

If you'd like to run formatters in your IDE, see the following plugins for IntelliJ IDEA:

- [ktfmt](https://plugins.jetbrains.com/plugin/14912-ktfmt) formats Kotlin code. Enable it in settings and set the style
  to `kotlinlang`.
- [editorconfig](https://plugins.jetbrains.com/plugin/7294-editorconfig) automatically handles things like tab size and
  line endings.
- [prettier](https://plugins.jetbrains.com/plugin/10456-prettier) formats JSON, YAML, and Markdown files. Enable it in
  settings with "automatic configuration" and configure it to run on `**/*.{md,json,yml,yaml}`
