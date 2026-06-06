# Contributing

Discuss changes in `#maplibre` on the [OSM-US Slack](https://slack.openstreetmap.us/).

## Before Making Changes

If you're looking to add a feature or fix a bug and there's no issue filed yet,
[file an issue](https://github.com/maplibre/spatial-k/issues/new/choose) first to discuss the change
before you start working on it.

If you're new and looking for things to contribute, see our
[good first issue](https://github.com/maplibre/spatial-k/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22)
label.

Keep pull requests focused on one reviewable change. The reviewer should be able to connect the use
case, public behavior, implementation, and validation without separating unrelated work.

## Set up your development environment

### Mise

This project uses [mise](https://mise.jdx.dev/) for environment management. You can either:

#### Option 1: Use mise (Recommended)

1. Install mise if you haven't already: https://mise.jdx.dev/getting-started.html.
2. Run `mise install` in the project root to install all required tools and set up git hooks.

#### Option 2: Manual Setup

If you prefer not to use mise, check `mise.toml` for the list of required tools and versions, then
install them manually.

### Kotlin Multiplatform

Check out
[the official instructions](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html)
for setting up a Kotlin Multiplatform environment.

### IDE

As there's no stable LSP for Kotlin Multiplatform, you'll want to use either IntelliJ IDEA or
Android Studio for developing Spatial-K. Install the
[dprint](https://plugins.jetbrains.com/plugin/18492-dprint) plugin for format-on-save support — it
orchestrates all the other formatters (ktfmt for Kotlin, etc.).

### Tests

Run `mise run build` to compile and run all checks, including tests across multiple platforms.

To run tests for specific platforms:

- `mise run test:jvm` - Run JVM tests only
- `mise run test:jsnode` - Run JS tests
- `mise run test:wasmjsnode` - Run WASM tests
- `./gradlew :geojson:jvmTest --tests "*SpecificTest*"` - Run specific tests in a module

Tests make use of JSON data loaded from files, so platforms where it's not convenient to load files
from the file system have their tests disabled. This includes mobile native targets, browser
targets, etc.

### Documentation

The documentation website uses [Starlight](https://starlight.astro.build/) and is located in the
`docs` directory. Run `mise run docs:dev` to run a local server to view the docs. The server will
automatically reload when you make changes to the docs.

### Dump ABIs

We commit the ABI files to the repository so that changes to the ABI can be inspected in pull
requests. To regenerate the ABI files after a public API change, run `mise run fix`.

### Formatting

A git pre-commit hook is installed by `mise install` via [hk](https://hk.jdx.dev/). It automatically
formats staged files before each commit.

To run formatters manually:

- `mise run fix` - Format all files
- `mise run check` - Check formatting without modifying files

## Pull Requests

Open a pull request when the change is ready for review and include:

- the problem or use case;
- the public API or behavior change, if any;
- the validation you ran;
- platform limitations or native MapLibre behavior you checked;

If you use AI assistance, follow the [AI policy](./AI_POLICY.md).
