// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "SpatialKPmtilesSwiftTests",
    platforms: [
        .macOS(.v14)
    ],
    targets: [
        .binaryTarget(
            name: "SpatialKPmtiles",
            path: "../build/XCFrameworks/debug/SpatialKPmtiles.xcframework"
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
