plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    // Not applied (AGP 9 has built-in Kotlin support); declared to pin a newer Kotlin compiler.
    alias(libs.plugins.kotlin.android) apply false
}
