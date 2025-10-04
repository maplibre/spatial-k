set windows-shell := ["powershell.exe", "-c"]

_default:
    @just --list

pre-commit-install:
    pre-commit install

pre-commit-uninstall:
    pre-commit uninstall

format:
    pre-commit run --all-files || true

test-jvm:
    ./gradlew jvmTest

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
