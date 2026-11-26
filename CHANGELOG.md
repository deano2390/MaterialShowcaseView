# Changelog

## 2.0.0

A modernisation release. It is a drop-in replacement for 1.3.7: the public API is source- and
binary-compatible for both Java and Kotlin callers.

### Build and platform
* Rewritten in Kotlin.
* Built with Gradle 9, Android Gradle Plugin 9 and compileSdk 36. jcenter is no longer used.
* minSdk raised from 12 to 21.
* The sample app now uses AndroidX and draws edge-to-edge.
* Added unit tests (Robolectric), instrumented tests, a public API check (`apiCheck`) and CI.

### Fixes
* Fixed crashes when a showcase was dismissed more than once, for example by a double tap or by
  `setDismissOnTouch(true)` handling every touch event: `NullPointerException` in
  `animateOut`, and "Cannot start this animator on a detached view!".
  (#81, #145, #152, #157, #167)
* An exit animation that was cancelled (for example by opening Recents) now still removes the
  showcase, so sequences no longer get stuck. (#46)
* A showcase removed before its delay elapsed could still become visible.
* Fixed a leaked layout listener that kept updating removed showcases.
* Fixed `OutOfMemoryError` in `onDraw`: the mask is no longer drawn into a full-screen bitmap. (#35, #154)
* The navigation bar is now handled with window insets, so content and buttons are no longer hidden
  behind it with gesture navigation, in landscape or in edge-to-edge apps. (#20, #33, #103, #119, #137, #171)
* The highlight now lines up with the target when the showcase isn't at the window origin (decor
  padding, dialog-themed activities). (#164)
* `ViewTarget` bounds now use the laid-out size, not the measured size.
* Showcases without a target, or with `Target.NONE`, no longer crash; the content is centred. (#23, #69, #121)
* A `MaterialShowcaseView` created with its constructor rather than the `Builder` no longer crashes
  when shown.
* Skipping a single-use showcase is remembered, so it doesn't show again.
* Sequence listeners now get the correct item position without single-use; it used to always be 0. (#129)
* Closing the Activity mid-sequence no longer marks the sequence as finished.
* `ShowcaseConfig` no longer overrides colours it didn't set, and its shape padding now takes
  effect. (#75, #191)
* Setting a title no longer makes the content text semi-transparent, and an empty title no longer
  leaves a gap. (#83)
* `hasFired()` no longer throws when single-use isn't enabled.
* Text sizes are in `sp` and respect the user's font size.
* The skip button no longer takes up an invisible tappable area when it has no text.
* `PrefsManager` holds the application context and no longer throws after `close()`.
* Tooltips: no longer shown (and crashing) before the showcase is displayed. `customView(int)` now
  works instead of always crashing. `MyContext(Context)` works. Tooltips are positioned correctly in
  multi-window mode.
* `IAnimationFactory.animateTargetToPoint` now actually moves the highlight.
* Calling `show()` off the main thread fails immediately with a clear message. (#168)
* Accessibility: content behind the showcase is hidden from screen readers while it's shown, the
  showcase is announced, and the dismiss and skip buttons are focusable buttons. (#175)

### Added
* Text size and typeface setters: `setTitleTextSize`, `setContentTextSize`, `setDismissTextSize`,
  `setSkipTextSize`, `setTitleStyle`, `setContentStyle` and `setSkipTextColor` on the Builder, and
  matching options on `ShowcaseConfig`. Based on #196 by @amodkanthe. (#84, #113, #151, #194)
* `MaterialShowcaseSequence.setOnSkippedListener` and `setOnSequenceFinishedListener`, and
  `MaterialShowcaseView.wasSkipped()`. Based on #204 by @Danjosh0211 and #87 by @BehnamMaboodi.
  (#60, #106, #107)
* `RoundedRectangleShape` and `Builder.withRoundedRectangleShape()`. Based on #204 by @Danjosh0211. (#159)
* `MaterialShowcaseView.hasFired(context, id)` to check a single-use showcase or sequence without
  building it. (#24, #93)
* `Builder.setDismissOnBackPress(true)` / `ShowcaseConfig.dismissOnBackPress`: the back button
  closes the showcase and ends a sequence. (#88)
* The highlight follows the target when the content underneath scrolls. (#149)

### API
* Added `MaterialShowcaseView.removeShowcaseListener(IShowcaseListener)`. The old
  `removeShowcaseListener(MaterialShowcaseSequence)` overload never did anything and is deprecated.
* Added `ShowcaseConfig.titleTextColor`.
* Kotlin callers can pass nullable values (such as `Fragment.activity`) where 1.x accepted them,
  and implement the listener interfaces with nullable or non-null parameters, as before.
* `getSoftButtonsBarSizePort()` and `ShowcaseTooltip.MyContext(Fragment)` are deprecated.
