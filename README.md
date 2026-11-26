*Looking for collaborators to help maintain this library, drop me a line at me@deanwild.co.uk if you want to help.*

# MaterialShowcaseView
A Material Design themed ShowcaseView for Android


This library is heavily inspired by the original [ShowcaseView library][1].

Since Google introduced the Material design philosophy I have seen quite a few apps with a nice clean, flat showcase view (the Youtube app is a good example). The only library out there however is the [original one][1]. This was a great library for a long time but the theming is now looking a bit dated.

![Logo](http://i.imgur.com/QIMYRJh.png)


![Animation][2]

# Gradle
--------

[![jitpack][4]][5]

Add the JitPack repository to `settings.gradle.kts` ([why JitPack?](#why-jitpack)):

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.deano2390:MaterialShowcaseView:2.1.0")
}
```

<details>
<summary>Groovy DSL</summary>

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}

// app/build.gradle
dependencies {
    implementation 'com.github.deano2390:MaterialShowcaseView:2.1.0'
}
```
</details>

Requires minSdk 21 or higher.

# How to use
--------
This is the basic usage of a single showcase view. Check out the [sample app](sample) for more.

Kotlin:

```kotlin
// single example
MaterialShowcaseView.Builder(this)
    .setTarget(button)
    .setTitleText("Hello")
    .setContentText("This is some amazing feature you should know about")
    .setDismissText("GOT IT")
    .setDelay(500) // optional, but starting animations immediately in onCreate can make them choppy
    .singleUse(SHOWCASE_ID) // a unique ID used to make sure it is only shown once
    .show()

// sequence example
val config = ShowcaseConfig().apply {
    delay = 500 // half a second between each showcase view
}

MaterialShowcaseSequence(this, SHOWCASE_ID).apply {
    setConfig(config) // set the config before adding items
    addSequenceItem(buttonOne, "This is button one", "GOT IT")
    addSequenceItem(buttonTwo, "This is button two", "GOT IT")
    addSequenceItem(buttonThree, "This is button three", "GOT IT")
    start()
}
```

Java:

```java
// single example
new MaterialShowcaseView.Builder(this)
    .setTarget(button)
    .setContentText("This is some amazing feature you should know about")
    .setDismissText("GOT IT")
    .setDelay(500)
    .singleUse(SHOWCASE_ID)
    .show();

// sequence example
ShowcaseConfig config = new ShowcaseConfig();
config.setDelay(500);

MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
sequence.setConfig(config);
sequence.addSequenceItem(buttonOne, "This is button one", "GOT IT");
sequence.addSequenceItem(buttonTwo, "This is button two", "GOT IT");
sequence.addSequenceItem(buttonThree, "This is button three", "GOT IT");
sequence.start();
```

## Customising

```kotlin
MaterialShowcaseView.Builder(this)
    .setTarget(button)
    .withRoundedRectangleShape()         // or withCircleShape(), withOvalShape(), withRectangleShape(), withoutShape()
    .setShapePadding(24)
    .setMaskColour(0xDD335075.toInt())   // the alpha channel controls how much shows through
    .setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
    .setContentTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
    .setTitleStyle(myTypeface)
    .setDismissOnTouch(true)             // tap anywhere to dismiss
    .setTargetTouchable(true)            // let taps through to the target...
    .setDismissOnTargetTouch(true)       // ...and dismiss when it's tapped
    .setDismissOnBackPress(true)         // back closes it (and ends a sequence)
    .setDismissButtonColor(accentColour) // a filled button instead of plain text (or setDismissBackground)
    .setShapeBorder(Color.WHITE, 4)      // outline the highlight
    .show()
```

Sequences can tell you how they ended:

```kotlin
sequence.setOnSkippedListener { item, position -> /* skipped at this item */ }
sequence.setOnSequenceFinishedListener { skipped -> /* completed, or skipped */ }
```

To avoid building a single-use showcase that has already been shown:

```kotlin
if (!MaterialShowcaseView.hasFired(context, SHOWCASE_ID)) { /* build and show it */ }
```

`MaterialShowcaseView.isShowing(activity)` tells you whether a showcase (or a sequence) is on
screen. In UI tests, call `MaterialShowcaseView.setShowcasesEnabled(false)` so overlays don't get
in the way; nothing is shown or recorded as shown while disabled.

Showcases must be shown from the main thread. To show one again, call
`MaterialShowcaseView.resetSingleUse(context, SHOWCASE_ID)` or `MaterialShowcaseView.resetAll(context)`.

# Migrating to 2.0
------------------

2.0 is a drop-in replacement for 1.x: every public API from 1.3.7 is still there, for both Java
and Kotlin callers. What changed:

* **Requirements:** minSdk 21. The library is now written in Kotlin, so it brings in `kotlin-stdlib`,
  `androidx.core` and `androidx.activity`.
* **Gradle:** use `implementation` (not `compile`). The `@aar` suffix is no longer needed.
* **Behaviour fixes** you might notice:
  * Setting a title no longer fades the content text to 50% opacity, and an empty title no longer
    leaves a gap.
  * Text sizes are in `sp`, so they follow the user's font size setting.
  * A `ShowcaseConfig` only overrides the values you actually set on it. It used to reset colours you
    had set on individual items.
  * `setDismissOnTouch(true)` dismisses when the finger lifts, not on the first touch event.
  * System bars are handled with window insets, so content never ends up under the navigation bar.
    This works with gesture navigation, landscape and edge-to-edge apps.
  * Skipping a single-use showcase is remembered, the same as dismissing it.
  * In a sequence, the item listeners now get the correct position, with or without single-use.
* **Kotlin callers:** the old Java-style calls such as `config.setDelay(500)` still compile but are
  deprecated in favour of property syntax (`config.delay = 500`). The IDE quick-fix converts them.
  One rare case does break: a Kotlin *subclass* of `CircleShape`, `OvalShape`, `RectangleShape`,
  `ShowcaseConfig` or `ViewTarget` that overrides a getter such as `getRadius()` must override the
  property (`override var radius`) instead.

See the [CHANGELOG](CHANGELOG.md) for the full list of fixes.

# Why Jitpack
------------
Publishing libraries to Maven is a chore that takes time and effort. Jitpack.io allows me to release without ever leaving GitHub so I can release easily and more often.

# Apps using MaterialShowcaseView
---------------------------------

  * [Say It! - English Learning](https://play.google.com/store/apps/details?id=com.cesarsk.say_it) : An Android App aimed to improve your English Pronunciation. 
    * [Github Page](https://github.com/cesarsk/say_it)
    
  * [Queskr](https://play.google.com/store/apps/details?id=com.queskr.www.queskrandroidapp) : Social Q&A at your fingertips

# Learning Resources
[https://medium.com/@yashgirdhar/android-material-showcase-view-part-1-22abd5c65b85][6]

[https://1bucketlist.blogspot.com/2017/03/android-material-showcase-view-1.html][7]

[https://blog.fossasia.org/tag/material-showcase-view/][8]



# License
-------

    Copyright 2015 Dean Wild

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





[1]: https://github.com/amlcurran/ShowcaseView
[2]: http://i.imgur.com/rFHENgz.gif
[3]: https://code.google.com/p/android-flowtextview/
[4]: https://img.shields.io/github/release/deano2390/MaterialShowcaseView.svg?label=JitPack
[5]: https://jitpack.io/#deano2390/MaterialShowcaseView
[6]: https://medium.com/@yashgirdhar/android-material-showcase-view-part-1-22abd5c65b85
[7]: https://1bucketlist.blogspot.com/2017/03/android-material-showcase-view-1.html
[8]: https://blog.fossasia.org/tag/material-showcase-view/
