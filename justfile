#!/usr/bin/env just --justfile

set windows-shell := ["powershell.exe", "-c"]

_default:
    @just --list

pre-commit-install:
    pre-commit install

pre-commit-uninstall:
    pre-commit uninstall

detekt:
    ./gradlew detekt

format:
    pre-commit run --all-files --hook-stage manual || true

test-jvm:
    ./gradlew jvmTest

test-js:
    ./gradlew jsNodeTest

test-wasm:
    ./gradlew wasmJsNodeTest

native_test_task := if os() == "macos" {
    if arch() == "aarch64" { "macosArm64Test" }
    else { "macosX64Test" }
} else if os() == "linux" {
    if arch() == "aarch64" { "linuxArm64Test" }
    else { "linuxX64Test" }
} else if os() == "windows" {
    "mingwX64Test"
} else {
    error("Unsupported platform: {{os()}}-{{arch()}}")
}

test-native:
    ./gradlew {{native_test_task}}

test: test-jvm test-js test-wasm test-native

build:
    ./gradlew build

benchmark:
    ./gradlew benchmark

coverage:
    ./gradlew :koverHtmlReport

build-dokka:
    ./gradlew :dokkaGenerateHtml

build-docs:
    ./gradlew :mkdocsBuild

serve-docs:
    ./gradlew :mkdocsServe
