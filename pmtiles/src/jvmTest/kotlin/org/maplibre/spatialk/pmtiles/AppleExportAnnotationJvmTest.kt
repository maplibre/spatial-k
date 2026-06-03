package org.maplibre.spatialk.pmtiles

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class AppleExportAnnotationJvmTest {
    @Test
    fun commonArchiveSourceContainsAppleExportAnnotations() {
        val archiveSource =
            sourceFile("src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/PmTilesArchive.kt")
        val modelsSource =
            sourceFile("src/commonMain/kotlin/org/maplibre/spatialk/pmtiles/Models.kt")

        assertTrue(archiveSource.contains("@HiddenFromObjC public fun warnings()"))
        assertTrue(archiveSource.contains("@HiddenFromObjC\n        @Throws"))
        assertTrue(
            archiveSource.contains("@ObjCName(name = \"warningAt\", swiftName = \"warning\")")
        )
        assertTrue(
            modelsSource.contains("@Throws(PmTilesException::class, CancellationException::class)")
        )
        assertTrue(
            modelsSource.contains("@Throws(PmTilesException::class)\n    public fun fromZxy")
        )
    }

    @Test
    fun appleApiSourceContainsAppleExportAnnotations() {
        val appleSource =
            sourceFile("src/appleMain/kotlin/org/maplibre/spatialk/pmtiles/AppleApi.kt")

        assertTrue(appleSource.contains("@ObjCName(name = \"open\", swiftName = \"open\")"))
        assertTrue(
            appleSource.contains("@Throws(PmTilesException::class, CancellationException::class)")
        )
    }

    private fun sourceFile(relativePath: String): String = Files.readString(Path.of(relativePath))
}
