package org.maplibre.spatialk.geojson

@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API should be used with caution; please check the documentation.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
public annotation class SensitiveGeoJsonApi
