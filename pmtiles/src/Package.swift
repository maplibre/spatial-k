// swift-tools-version: 6.0

import Foundation
import PackageDescription

var targets: [Target] = [
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
]

if FileManager.default.fileExists(atPath: "swiftTest") {
    targets.append(
        .testTarget(
            name: "SpatialKPmtilesDocsTests",
            dependencies: [
                "SpatialKPmtiles"
            ],
            path: "swiftTest",
            resources: [
                .process("Resources")
            ]
        )
    )
}

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
    targets: targets
)
