package app.docentrip.data

import android.content.Context
import app.docentrip.core.DocentStory
import app.docentrip.core.GeoPoint
import app.docentrip.core.RoutePath
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One drive: the road, the stories along it and the stops the car's navigation should guide through. */
data class DocentripRoute(
    val name: String,
    val points: List<GeoPoint>,
    val stories: List<DocentStory>,
    val navigationStops: List<String>,
) {
    val path: RoutePath = RoutePath(points)
}

/** Reads a route from a JSON file, the format of `assets/route.json`. */
object RouteJson {

    private val format = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): DocentripRoute {
        val file = runCatching { format.decodeFromString<RouteFile>(raw) }
            .getOrElse { throw IllegalArgumentException("The route is not valid JSON: ${it.message}", it) }
        require(file.stories.isNotEmpty()) { "The route needs at least one story." }
        require(file.points.size >= 2) { "The route needs at least two points." }

        val path = RoutePath(file.points.map { GeoPoint(it.lat, it.lon) })
        val stories = file.stories.map { story ->
            story.atFraction?.let { require(it in 0.0..1.0) { "Story ${story.id}: atFraction must be between 0 and 1." } }
            val point = when {
                story.lat != null && story.lon != null -> GeoPoint(story.lat, story.lon)
                story.atFraction != null -> path.pointAt(path.totalDistanceM * story.atFraction)
                else -> throw IllegalArgumentException("Story ${story.id} needs lat and lon, or atFraction.")
            }
            DocentStory(
                id = story.id,
                title = story.title,
                body = story.body,
                point = point,
                triggerRadiusM = story.triggerRadiusM,
                priority = story.priority,
                placeName = story.placeName ?: story.title,
                address = story.address,
            )
        }

        val ids = stories.map { it.id }
        require(ids.size == ids.toSet().size) { "Story ids must be unique." }
        require(file.navigationStops.all { it in ids }) { "Every navigation stop must be a story id." }

        return DocentripRoute(
            name = file.name,
            points = path.points,
            stories = stories,
            navigationStops = file.navigationStops,
        )
    }
}

/** Loads the route packaged with the app. */
object RouteStore {

    const val FILE = "route.json"

    fun load(context: Context): DocentripRoute =
        context.assets.open(FILE).bufferedReader().use { RouteJson.parse(it.readText()) }
}

@Serializable
private data class RouteFile(
    val name: String = "Docentrip",
    val points: List<LatLon>,
    val stories: List<StoryFile>,
    val navigationStops: List<String> = emptyList(),
)

@Serializable
private data class LatLon(
    val lat: Double,
    val lon: Double,
)

@Serializable
private data class StoryFile(
    val id: String,
    val title: String,
    val body: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val atFraction: Double? = null,
    val triggerRadiusM: Double = 350.0,
    val priority: Int = 0,
    val placeName: String? = null,
    val address: String = "",
)
