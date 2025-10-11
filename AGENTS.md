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
