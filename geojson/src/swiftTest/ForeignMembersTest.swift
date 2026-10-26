import SpatialKGeoJson
import XCTest

final class ForeignMembersTest: XCTestCase {
    func testConstructWithSwiftDictionary() {
        let point = Point(longitude: 12.3, latitude: 45.6, altitude: nil, bbox: nil, foreignMembers: [:])

        XCTAssertEqual(
            GeoJsonObjectCompanion.shared.toJson(geoJsonObject: point),
            #"{"type":"Point","coordinates":[12.3,45.6]}"#
        )
    }

    func testConstructWithDecodedForeignMembers() {
        let json = #"{"type":"Point","coordinates":[12.3,45.6],"title":"Example"}"#
        let decoded = Point.companion.fromJson(json: json)

        let point = Point(coordinates: decoded.coordinates, bbox: nil, foreignMembers: decoded.foreignMembers)

        XCTAssertEqual(GeoJsonObjectCompanion.shared.toJson(geoJsonObject: point), json)
    }
}
