package uz.aifu.lms.scheduling.domain;

/**
 * The part of the day a group studies in ("smena"): each shift has its own pairs, numbered from one
 * ("1-para" of the afternoon shift starts at 13:30).
 */
public enum Shift {
    MORNING,
    AFTERNOON,
    EVENING
}
