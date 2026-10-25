# Contributing

Discuss changes in `#maplibre` on the [OSM-US Slack](https://slack.openstreetmap.us/).

For a feature or bug fix without an issue,
[file one](https://github.com/maplibre/spatial-k/issues/new/choose) first to discuss the change. New
contributors can start with
[good first issues](https://github.com/maplibre/spatial-k/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22).

If you use AI assistance, follow the [AI policy](./AI_POLICY.md).

## Set up

Install [mise](https://mise.jdx.dev/getting-started.html) and run `mise install` in the project
root. This installs the pinned tools and Git hooks. `mise tasks --all` lists the available tasks. If
you manage tools yourself, use the versions in `mise.toml`.

Use IntelliJ IDEA or Android Studio with the
[Kotlin Multiplatform setup](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html).
Kotlin Multiplatform has no stable LSP. Install the
[dprint plugin](https://plugins.jetbrains.com/plugin/18492-dprint) for format-on-save support; it
also runs the Kotlin and Java formatters.

For commands without a mise task, use `mise exec -- <command>`.

## Check changes

`mise run build` compiles the libraries and runs the checks and tests available on the host. Choose
focused test tasks for the behavior and platforms your change can affect:

- `mise run test:jvm` — JVM tests.
- `mise run test:jsnode` — JS tests in Node.
- `mise run test:wasmjsnode` — WASM tests in Node.
- `mise run test:native` — native tests for the current platform.
- `mise run test:swift` — Swift interop tests on macOS with Xcode.
- `mise exec -- ./gradlew :geojson:jvmTest --tests "*SpecificTest*"` — a specific module's JVM test.

`mise run test` runs all test tasks. Some resource-based suites disable browser and simulator
runners where their test resources are unavailable.

Keep tests focused on regressions in the changed behavior. Use the floating-point comparison helpers
from `testutil` to account for platform precision differences.

### Formatting and API checks

`mise run check` reports formatting and lint problems; `mise run fix` fixes what it can. The
pre-commit hook formats staged files and runs checks automatically.

After an intentional API change, run `mise run fix`, review changes to the module's committed `api/`
files, and include them in the same PR.

## Documentation

The site uses [Starlight](https://starlight.astro.build/) and lives in `docs`. Run
`mise run docs:dev` to serve it locally, or `mise run docs:build --version dev` to build it.

Site pages explain common tasks; KDoc defines the API contract. Update the test snippets used by
documentation examples when changing their APIs. Put migration instructions in the PR description.

## Pull requests

Keep each PR focused on one reviewable change. Use Conventional Commits for titles, such as
`fix(units): correct infinity checks`; the title becomes the squash-merge commit message.

Follow the [PR template](.github/PULL_REQUEST_TEMPLATE.md) and the
[PR writing guidance](AGENTS.md#pull-requests). Explain the problem and resulting behavior in plain
language, with a small before/after example when useful. Mention any migration steps or unresolved
limitations.

Describe what changed tests catch and checks outside CI, rather than repeating routine CI results.
Use draft status for unfinished work or changes awaiting human review.
