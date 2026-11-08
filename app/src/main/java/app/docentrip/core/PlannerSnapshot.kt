package app.docentrip.core

/**
 * What the planner remembers about a drive, so that a new process can go on without telling stories again or losing
 * the quiet stretch after the last one. Stored as a small line-based string in the app's settings.
 */
data class PlannerSnapshot(
    val startedAtM: Map<String, Double> = emptyMap(),
    val quietSinceM: Double = Double.NEGATIVE_INFINITY,
) {

    fun encode(): String = buildString {
        append(quietSinceM)
        for ((id, atM) in startedAtM) append('\n').append(id).append('\t').append(atM)
    }

    companion object {
        fun decode(raw: String?): PlannerSnapshot? {
            if (raw.isNullOrBlank()) return null
            val lines = raw.lines()
            val quietSinceM = lines.first().toDoubleOrNull() ?: return null
            val started = buildMap {
                for (line in lines.drop(1)) {
                    val id = line.substringBefore('\t', "")
                    val atM = line.substringAfter('\t', "").toDoubleOrNull() ?: continue
                    if (id.isNotEmpty()) put(id, atM)
                }
            }
            return PlannerSnapshot(started, quietSinceM)
        }
    }
}
