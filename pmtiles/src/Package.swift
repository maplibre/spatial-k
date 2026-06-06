// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "SpatialKPmtiles",
    platforms: [
        .iOS(.v17),
        .macOS(.v14),
    ],
    products: [
        .library(
            name: "SpatialKPmtiles",
            targets: [
                "SpatialKPmtiles"
            ]
        )
    ],
    targets: [
        .binaryTarget(
            name: "SpatialKPmtilesKotlin",
            path: "Artifacts/SpatialKPmtilesKotlin.xcframework"
        ),
        .target(
            name: "SpatialKPmtiles",
            dependencies: [
                "SpatialKPmtilesKotlin"
            ],
            path: "swiftMain"
        ),
        .testTarget(
            name: "SpatialKPmtilesDocsTests",
            dependencies: [
                "SpatialKPmtiles"
            ],
            path: "swiftTest",
            resources: [
                .process("Resources")
            ]
        ),
    ]
)
