package uz.aifu.lms.files.domain;

/**
 * Mirrors the {@code files.provider} CHECK constraint (V013). Only {@link #LOCAL} has an adapter
 * ({@code files.app.storage.LocalFileStorage}) — {@code S3} is ADR-008's named production default,
 * schema-ready, not yet implemented (see {@code docs/BUILD-STATUS.md}'s "What remains").
 */
public enum FileProvider {
    LOCAL,
    S3
}
