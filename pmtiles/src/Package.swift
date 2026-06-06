// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "SpatialKPmtilesSwiftTests",
    platforms: [
        .macOS(.v14)
    ],
    targets: [
        .binaryTarget(
            name: "SpatialKPmtilesKotlin",
            path: "../build/XCFrameworks/debug/SpatialKPmtilesKotlin.xcframework"
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
