// swift-tools-version: 6.0

import PackageDescription

let package = Package(
    name: "SpatialKGeoJsonTests",
    platforms: [
        .macOS(.v14)
    ],
    targets: [
        .binaryTarget(
            name: "SpatialKGeoJson",
            path: "build/XCFrameworks/debug/SpatialKGeoJson.xcframework"
        ),
        .testTarget(
            name: "SpatialKGeoJsonTests",
            dependencies: [
                "SpatialKGeoJson"
            ],
            path: "src/swiftTest"
        ),
    ]
)
