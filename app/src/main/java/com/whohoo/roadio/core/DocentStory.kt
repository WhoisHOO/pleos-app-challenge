package com.whohoo.roadio.core

data class DocentStory(
    val id: String,
    val title: String,
    val body: String,
    val point: GeoPoint,
    val triggerRadiusM: Double = 350.0,
    val priority: Int = 0,
)
