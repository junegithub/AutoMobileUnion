package com.fx.zfcar.car.base

object MapCameraPolicy {
    private const val DEFAULT_SELECTED_MARKER_ZOOM = 15f

    fun selectedMarkerZoom(currentZoom: Float): Float {
        return maxOf(currentZoom, DEFAULT_SELECTED_MARKER_ZOOM)
    }
}
