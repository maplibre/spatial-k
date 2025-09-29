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

benchmark:
    ./gradlew :geojson:benchmark

build-dokka:
    ./gradlew :dokkaGenerateHtml

build-docs: build-dokka
    mkdocs build -s

serve-docs: build-dokka
    mkdocs serve
