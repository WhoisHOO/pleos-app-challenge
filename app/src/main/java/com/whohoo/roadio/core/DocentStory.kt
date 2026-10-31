package com.whohoo.roadio.core

data class DocentStory(
    val id: String,
    val title: String,
    val body: String,
    val point: GeoPoint,
    val triggerRadiusM: Double = 350.0,
    val priority: Int = 0,
    // How the place is named and found when it is handed to the car's navigation.
    val placeName: String = title,
    val address: String = "",
)
