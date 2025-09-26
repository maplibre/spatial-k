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

dokka-html:
    ./gradlew dokkaGenerateHtml

kover-html:
    ./gradlew :koverHtmlReport

docs-build: dokka-html
    mkdocs build

docs-serve: dokka-html
    mkdocs serve
