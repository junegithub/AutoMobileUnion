package com.fx.zfcar.car.base

import org.junit.Assert.assertEquals
import org.junit.Test

class MapCameraPolicyTest {
    @Test
    fun selectedMarkerZoomKeepsUserZoomWhenCloserThanDefault() {
        assertEquals(18f, MapCameraPolicy.selectedMarkerZoom(18f), 0.001f)
    }

    @Test
    fun selectedMarkerZoomUsesDefaultWhenUserZoomIsFartherOut() {
        assertEquals(15f, MapCameraPolicy.selectedMarkerZoom(11f), 0.001f)
    }
}
