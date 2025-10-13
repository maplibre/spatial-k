# Agent Guidelines for Spatial K

## Module Overview

- `geojson` - GeoJSON implementation and DSL for building GeoJSON objects
- `turf` - Port of Turf.js geospatial analysis functions in pure Kotlin
- `units` - Library for working with units of measure
- `testutil` - Shared testing utilities and resource loading helpers

## Build/Test Commands

- `./gradlew build` - Compile and run all checks across platforms
- `./gradlew jvmTest` - Run JVM tests only
- `./gradlew jsNodeTest` - Run JS tests
- `./gradlew wasmJsNodeTest` - Run WASM tests
- `./gradlew :geojson:jvmTest --tests "*SpecificTest*"` - Run single test
- `./gradlew :koverHtmlReport` - Generate coverage report
- `./gradlew updateLegacyAbi` - Dump JVM and KLIB ABI after API changes
- `./gradlew :mkdocsBuild` - Build and check MkDocs documentation
- `./gradlew detekt` - Check for undocumented public APIs
- `pre-commit run --all-files --hook-stage manual`
    - Run this command before committing changes to ensure code formatting and
      ABI compliance.

## Build Organization

- Convention plugins in `buildSrc/src/main/kotlin/` configure common behavior
- `base-module.gradle.kts` - Kotlin setup with JVM toolchain and Java target
- `multiplatform-module.gradle.kts` - Extend base module with all supported
  multiplatform targets
- `published-library.gradle.kts` - Maven publishing, Dokka, Kover, and ABI
  validation
- `test-resources.gradle.kts` - Resource loading for test files
- Documentation uses Material for MkDocs in `docs/` directory

## Code Style Guidelines

- Public APIs use `public` modifier explicitly
- Package structure: `org.maplibre.spatialk.{module}`
- Use kotlinx.serialization for JSON handling
- Some tests use resource files from `src/commonTest/resources/` for JSON data
- Mobile/browser tests disabled due to file system resource loading limitations
- For floating-point comparisons in tests, use helpers from `testutil` instead
  of `assertEquals()` to handle platform-specific precision differences
- When writing KDoc comments, prefer adding imports over writing fully qualified
  names

## Commit Guidelines

Never make a commit unless explicitly asked to do so. Such permission only
extends to that one commit, not to future commits in that session.

When making commits, always include a signoff in the commit message following
this format:

```
🤖 Generated with [Agent Name](https://agent-url)

Co-Authored-By: Agent Name <example@agent-domain>
```

Examples:

- Claude: `🤖 Generated with [Claude Code](https://claude.com/claude-code)` and
  `Co-Authored-By: Claude <noreply@anthropic.com>`
- OpenCode: `🤖 Generated with [OpenCode](https://opencode.ai)` and
  `Co-Authored-By: OpenCode <noreply@opencode.ai>`
- Amp: `🤖 Generated with [Amp](https://ampcode.com)` and
  `Co-Authored-By: Amp <amp@ampcode.com>`

Each coding agent should use their own Author and URL but maintain the same
format.
