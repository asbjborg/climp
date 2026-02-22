package com.asbjborg.climp.speech;

import java.util.List;
import java.util.Map;

import net.minecraft.util.RandomSource;

public final class ClimpSpeechLibrary {
    /** Single source of truth: each line defines its text and the sound file name (e.g. climp_idle_1.ogg). */
    public record Line(String text, String soundId) {}

    private static final Map<ClimpSpeechType, List<Line>> LINES = Map.of(
            ClimpSpeechType.IDLE, List.of(
                    new Line("I am not lost. I am exploring in circles.", "climp_idle_1"),
                    new Line("That rock looked important.", "climp_idle_2"),
                    new Line("I am shiny. The world is less shiny.", "climp_idle_3"),
                    new Line("I am small. But dramatic.", "climp_idle_4"),
                    new Line("Do creepers fear me?", "climp_idle_5"),
                    new Line("I sense adventure nearby.", "climp_idle_6"),
                    new Line("If I had hands, I would gesture dramatically.", "climp_idle_7"),
                    new Line("I am ready. Probably.", "climp_idle_8"),
                    new Line("I am observing very professionally.", "climp_idle_9")),
            ClimpSpeechType.HIT, List.of(
                    new Line("Hey! I am delicate metal!", "climp_hit_1"),
                    new Line("Rude! I was being helpful!", "climp_hit_2"),
                    new Line("Bonk detected! Friendship shaken!", "climp_hit_3"),
                    new Line("My feelings are slightly dented.", "climp_hit_4"),
                    new Line("I have been attacked! Dramatically!", "climp_hit_5"),
                    new Line("Unacceptable bonking behavior!", "climp_hit_6")),
            ClimpSpeechType.TASK_START, List.of(
                    new Line("Ooooh yes! Time for work!", "climp_task_start_1"),
                    new Line("Stand back! Professional noodle at work!", "climp_task_start_2"),
                    new Line("I go now! Try not to panic!", "climp_task_start_3"),
                    new Line("Clinks and clanks incoming!!", "climp_task_start_4")),
            ClimpSpeechType.TASK_COMPLETE, List.of(
                    new Line("Done! I did the thing!", "climp_task_complete_1"),
                    new Line("Todo defeated! Victory is mine!", "climp_task_complete_2"),
                    new Line("Success! I remain magnificent!", "climp_task_complete_3"),
                    new Line("All done! I expect applause!", "climp_task_complete_4"),
                    new Line("Another glorious victory!", "climp_task_complete_5")),
            ClimpSpeechType.TASK_FAILED_UNREACHABLE, List.of(
                    new Line("I cannot reach that! I am not stretchy enough!", "climp_task_failed_unreachable_1"),
                    new Line("Too far! My noodle legs are short!", "climp_task_failed_unreachable_2"),
                    new Line("This is clearly too tall for me!", "climp_task_failed_unreachable_3"),
                    new Line("I require longer legs immediately!", "climp_task_failed_unreachable_4")),
            ClimpSpeechType.TASK_FAILED_TARGET_REMOVED, List.of(
                    new Line("Hey! It disappeared?!", "climp_task_failed_target_removed_1"),
                    new Line("I was going to do that!", "climp_task_failed_target_removed_2"),
                    new Line("Who took it? Show yourself!", "climp_task_failed_target_removed_3"),
                    new Line("I blame invisible gremlins.", "climp_task_failed_target_removed_4")));

    private ClimpSpeechLibrary() {
    }

    /**
     * Picks a random line for the given speech type. Never returns the excluded soundId if another
     * option exists, so the same line is not repeated twice in a row.
     */
    public static Line randomLine(ClimpSpeechType type, RandomSource random, String excludeSoundId) {
        List<Line> lines = LINES.get(type);
        if (lines == null || lines.isEmpty()) {
            return new Line("...", "climp_idle_1");
        }
        List<Line> candidates = excludeSoundId != null && lines.size() > 1
                ? lines.stream().filter(l -> !l.soundId().equals(excludeSoundId)).toList()
                : lines;
        return candidates.get(random.nextInt(candidates.size()));
    }
}
