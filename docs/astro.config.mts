// @ts-check

import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import { existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import remarkGfm from "remark-gfm";
import starlightCopyButton from "starlight-copy-button";
import starlightLinksValidator from "starlight-links-validator";
import starlightLlmsTxt from "starlight-llms-txt";
import type { Plugin } from "vite";
import remarkSpatialKSnippets from "./plugins/remark-spatial-k-snippets.mjs";

const base = "/spatial-k";
const docsDir = dirname(fileURLToPath(import.meta.url));

// Redirect generated API directory URLs to index.html in Vite dev, matching static hosting.
const generatedApiIndexMiddleware: Plugin = {
  name: "spatial-k-generated-api-index-middleware",
  configureServer(server) {
    server.middlewares.use((request, response, next) => {
      const pathname = request.url ? new URL(request.url, "http://localhost").pathname : undefined;

      const publicPath = pathname?.startsWith(base) ? pathname.slice(base.length) : pathname;

      if (
        publicPath?.startsWith("/api/") &&
        publicPath.endsWith("/") &&
        existsSync(join(docsDir, "public", publicPath, "index.html"))
      ) {
        response.statusCode = 302;
        response.setHeader("Location", `${base}${publicPath}index.html`);
        response.end();
        return;
      }

      next();
    });
  },
};

export default defineConfig({
  site: "https://maplibre.github.io",
  base,
  redirects: {
    "/api/": `${base}/api/dokka/`,
  },
  vite: {
    plugins: [generatedApiIndexMiddleware],
  },
  markdown: {
    remarkPlugins: [
      remarkGfm,
      [
        remarkSpatialKSnippets,
        {
          rootDir: "..",
          projectVersion: process.env.SPATIAL_K_VERSION ?? "VERSION",
        },
      ],
    ],
  },
  integrations: [
    starlight({
      title: "Spatial K",
      logo: {
        light: "./src/assets/logo-icon-light.svg",
        dark: "./src/assets/logo-icon-dark.svg",
      },
      editLink: {
        baseUrl: "https://github.com/maplibre/spatial-k/edit/main/docs/",
      },
      customCss: ["./src/styles/custom.css"],
      plugins: [
        starlightCopyButton(),
        starlightLlmsTxt({ exclude: ["api/**"] }),
        starlightLinksValidator({
          exclude: ["/api/**", "/spatial-k/api/**"],
        }),
      ],
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/maplibre/spatial-k",
        },
      ],
      sidebar: [
        { label: "Overview", link: "/" },
        {
          label: "Modules",
          items: [
            { label: "GeoJSON", link: "/geojson/" },
            { label: "Turf", link: "/turf/" },
            { label: "Units", link: "/units/" },
            { label: "GPX", link: "/gpx/" },
            { label: "PMTiles", link: "/pmtiles/" },
            { label: "Polyline", link: "/polyline-encoding/" },
          ],
        },
        {
          label: "API Reference",
          link: "/api/dokka/",
          attrs: { target: "_blank", rel: "noopener noreferrer" },
        },
      ],
    }),
  ],
});
