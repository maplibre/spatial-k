package org.maplibre.spatialk.units

public sealed interface Dimension {
    public data object Length : Dimension

    public data object Area : Dimension
}
