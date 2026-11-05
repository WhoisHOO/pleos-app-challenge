package app.roadio.core

/**
 * The few things the driver can say after tapping 말로 하기. Roadio acts only on these words and answers nothing else:
 * the car's speech engine turns speech into text, and [of] picks the command out of it.
 */
enum class VoiceCommand {
    /** End the story being told; the next one comes at its place. */
    SKIP,

    /** Tell the last story again from the start. */
    REPLAY,

    /** Turn the docent off (and stop the story). */
    OFF,

    /** Turn the docent on. */
    ON;

    companion object {
        // In this order: stopping wins over everything ("그만 다시" stops); "다음 이야기 시작" is the next story, and
        // "다시 켜 줘" turns the docent on rather than telling the story again.
        private val WORDS = listOf(
            OFF to listOf(
                "그만", "멈춰", "멈추", "멈춤", "조용", "정지", "중지", "스톱",
                // 끄다 as a verb only: a bare 꺼 is often 것 ("다음 꺼" is the next one).
                "꺼줘", "꺼주", "꺼봐", "꺼라", "꺼요", "끄고", "끄자", "끄기", "끄다", "끄세요", "끄라",
            ),
            SKIP to listOf("다음", "넘겨", "넘어가", "건너뛰", "스킵"),
            ON to listOf("켜", "시작"),
            REPLAY to listOf("다시", "한번더", "한번만더", "반복"),
        )

        // 꺼 for 것 after the words it follows ("다음 꺼", "이 꺼"), so it is not taken for 끄다.
        private val THING = Regex("(다음|이|그|저|내|제)꺼")

        // "끄지 마", "멈추지 말고": a request not to; nothing is done.
        private val DO_NOT = listOf("지마", "지말")

        /** The command in what the driver said, or null when it holds none. Spaces and punctuation do not matter. */
        fun of(heard: String?): VoiceCommand? {
            val words = heard?.filter { it.isLetterOrDigit() }?.replace(THING, "$1거") ?: return null
            if (words.isEmpty() || DO_NOT.any(words::contains)) return null
            // "이야기 꺼", "소리 꺼": 꺼 at the very end is the verb.
            if (words.endsWith("꺼")) return OFF
            return WORDS.firstOrNull { (_, keys) -> keys.any(words::contains) }?.first
        }
    }
}
