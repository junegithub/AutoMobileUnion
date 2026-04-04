package com.fx.zfcar.net

data class TrackShareRequest(
    val carId: String,
    val start: String,
    val end: String,
    val minute: String
)
