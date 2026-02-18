package com.asbjborg.climp.speech;

import java.util.List;
import java.util.Map;

import net.minecraft.util.RandomSource;

public final class ClimpSpeechLibrary {
    private static final Map<ClimpSpeechType, List<String>> LINES = Map.of(
            ClimpSpeechType.IDLE, List.of(
                    "I am not lost. I am orbiting.",
                    "That rock looked valuable. Emotionally.",
                    "I could optimize this route, but then we would miss the ambiance.",
                    "I am moderately helpful. Structurally flexible."),
            ClimpSpeechType.HIT, List.of(
                    "Rude. I am decorative and emotionally available.",
                    "Ow. That was my best angle.",
                    "Violence noted. Friendship pending.",
                    "If this is a trust exercise, I am failing it."),
            ClimpSpeechType.TASK_START, List.of(
                    "Manual labor detected. Delegation accepted.",
                    "Task acknowledged. Commencing dramatic approach.",
                    "I shall handle this with professional ambiguity."),
            ClimpSpeechType.TASK_COMPLETE, List.of(
                    "Objective achieved. You are welcome.",
                    "Task complete. I await further questionable directives.",
                    "Objective concluded. Dignity mostly intact."));

    private ClimpSpeechLibrary() {
    }

    public static String randomLine(ClimpSpeechType type, RandomSource random) {
        List<String> lines = LINES.get(type);
        if (lines == null || lines.isEmpty()) {
            return "...";
        }
        return lines.get(random.nextInt(lines.size()));
    }
}
