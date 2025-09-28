set windows-shell := ["powershell.exe", "-c"]

_default:
    @just --list

pre-commit-install:
    pre-commit install

pre-commit-uninstall:
    pre-commit uninstall

format:
    pre-commit run --all-files

build:
    ./gradlew build

coverage:
    ./gradlew :koverHtmlReport

_build-dokka:
    ./gradlew :dokkaGenerateHtml

build-docs: _build-dokka
    mkdocs build

serve-docs: _build-dokka
    mkdocs serve
