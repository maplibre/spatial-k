import fs from "node:fs";
import path from "node:path";

const snippetPattern = /^([ \t]*)--8<-- "([^":]+):([^"]+)"[ \t]*$/gm;
const versionPattern = /\{\{\s*gradle\.project_version\s*\}\}/g;

export default function remarkSpatialKSnippets(options = {}) {
  const rootDir = path.resolve(options.rootDir ?? "..");
  const projectVersion = options.projectVersion ?? "VERSION";
  const cache = new Map();

  return function transform(tree, file) {
    visit(tree, (node) => {
      if (node.type !== "code" && node.type !== "text") return;

      node.value = node.value.replace(versionPattern, projectVersion);

      if (node.type !== "code") return;

      node.value = node.value.replace(snippetPattern, (_match, indent, sourcePath, marker) => {
        const absolutePath = path.resolve(rootDir, sourcePath);
        const cacheKey = `${absolutePath}:${marker}`;
        let snippet = cache.get(cacheKey);

        if (snippet === undefined) {
          snippet = readSnippet(absolutePath, marker);
          cache.set(cacheKey, snippet);
        }

        return indentSnippet(snippet, indent);
      });
    });
  };
}

function visit(node, callback) {
  callback(node);

  if (!Array.isArray(node.children)) return;

  for (const child of node.children) {
    visit(child, callback);
  }
}

function readSnippet(filePath, marker) {
  const source = fs.readFileSync(filePath, "utf8");
  const lines = source.split(/\r?\n/);
  const start = lines.findIndex((line) => line.includes(`--8<-- [start:${marker}]`));

  if (start === -1) {
    throw new Error(`Snippet start marker not found: ${filePath}:${marker}`);
  }

  const end = lines.findIndex(
    (line, index) => index > start && line.includes(`--8<-- [end:${marker}]`),
  );

  if (end === -1) {
    throw new Error(`Snippet end marker not found: ${filePath}:${marker}`);
  }

  return dedent(lines.slice(start + 1, end)).join("\n");
}

function dedent(lines) {
  const minimumIndent = lines
    .filter((line) => line.trim().length > 0)
    .reduce((minimum, line) => {
      const indent = line.match(/^[ \t]*/)?.[0].length ?? 0;
      return Math.min(minimum, indent);
    }, Number.POSITIVE_INFINITY);

  if (!Number.isFinite(minimumIndent) || minimumIndent === 0) return lines;

  return lines.map((line) => line.slice(Math.min(minimumIndent, line.length)));
}

function indentSnippet(snippet, indent) {
  if (indent.length === 0) return snippet;
  return snippet
    .split("\n")
    .map((line) => (line.length === 0 ? line : `${indent}${line}`))
    .join("\n");
}
