package com.whohoo.roadio.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceCommandTest {

    @Test
    fun theFewCommandsAreUnderstood() {
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음"))
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음 이야기"))
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("넘겨 줘"))
        assertEquals(VoiceCommand.REPLAY, VoiceCommand.of("다시 들려줘"))
        assertEquals(VoiceCommand.REPLAY, VoiceCommand.of("한 번 더"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("그만"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("이야기 꺼 줘"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("멈춰"))
        assertEquals(VoiceCommand.ON, VoiceCommand.of("도슨트 켜 줘"))
        assertEquals(VoiceCommand.ON, VoiceCommand.of("시작"))
    }

    @Test
    fun spacingAndPunctuationDoNotMatter() {
        assertEquals(VoiceCommand.REPLAY, VoiceCommand.of("한번더."))
        assertEquals(VoiceCommand.REPLAY, VoiceCommand.of(" 한  번  더 ? "))
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음!"))
    }

    @Test
    fun overlappingWordsFollowTheOrder() {
        // Stopping wins: the driver wants quiet.
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("그만 다시"))
        // "다시 켜 줘" is turning the docent back on, not telling the story again.
        assertEquals(VoiceCommand.ON, VoiceCommand.of("다시 켜 줘"))
        assertEquals(VoiceCommand.ON, VoiceCommand.of("다시 시작"))
    }

    @Test
    fun theNextOneIsNotTurningOff() {
        // 꺼 is often 것 in speech: "다음 꺼" is the next one.
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음 꺼"))
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음 꺼 틀어 줘"))
        assertEquals(VoiceCommand.SKIP, VoiceCommand.of("다음 이야기 시작해 줘"))
        // 꺼 as the verb still turns the docent off.
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("소리 꺼"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("이거 꺼 줘"))
        assertNull(VoiceCommand.of("꺼내 줘"))
    }

    @Test
    fun askingNotToDoesNothing() {
        assertNull(VoiceCommand.of("끄지 마"))
        assertNull(VoiceCommand.of("멈추지 말고 계속"))
    }

    @Test
    fun moreWaysToSayIt() {
        assertEquals(VoiceCommand.REPLAY, VoiceCommand.of("한 번만 더 들려줘"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("조용해"))
        assertEquals(VoiceCommand.OFF, VoiceCommand.of("멈추세요"))
    }

    @Test
    fun anythingElseIsNoCommand() {
        assertNull(VoiceCommand.of(null))
        assertNull(VoiceCommand.of(""))
        assertNull(VoiceCommand.of("  ...  "))
        assertNull(VoiceCommand.of("광화문은 언제 지어졌어?"))
        assertNull(VoiceCommand.of("음악 틀어 줘"))
    }
}
