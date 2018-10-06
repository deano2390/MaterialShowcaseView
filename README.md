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

Add the jitpack repo to your your project's build.gradle at the end of repositories [Why?](#why-jitpack)

/build.gradle
```groovy
allprojects {
	repositories {
		jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Then add the dependency to your module's build.gradle:

/app/build.gradle
```groovy
compile 'com.github.deano2390:MaterialShowcaseView:1.2.0'
```

NOTE: Some people have mentioned that they needed to add the @aar suffix to get it to resolve from JitPack:
```groovy
compile 'com.github.deano2390:MaterialShowcaseView:1.2.0@aar'
```

# How to use
--------
This is the basic usage of a single showcase view, you should check out the sample app for more advanced usage.

```java

	// single example
	new MaterialShowcaseView.Builder(this)
		.setTarget(mButtonShow)
		.setDismissText("GOT IT")
		.setContentText("This is some amazing feature you should know about")
		.setDelay(withDelay) // optional but starting animations immediately in onCreate can make them choppy
		.singleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
		.show();
                
                
                
                
	// sequence example            
	ShowcaseConfig config = new ShowcaseConfig();
	config.setDelay(500); // half second between each showcase view

	MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

	sequence.setConfig(config);

	sequence.addSequenceItem(mButtonOne,
		"This is button one", "GOT IT");

	sequence.addSequenceItem(mButtonTwo,
		"This is button two", "GOT IT");

	sequence.addSequenceItem(mButtonThree,
		"This is button three", "GOT IT");

	sequence.start();
                
```

This is the advance usage of MaterialShowCase using Adapters, this will encapsulate everything for you.

**In your Activity**
```java

	    @Override
        protected void onStart() {
            super.onStart();
            // With Pool Management to run 1 Sequence or 1 View at a time.
            // Material Show Case Initialization
            MaterialShowCaseAdapter.initialize(this, CustomAdapter.class);
            // Simple Material Show Case Initialization
            MaterialShowCaseAdapter.initialize(this, R.array.showcase_simple_activity_adapter_example, "SimpleActivityAdapterExample");
        }

```

**If you're using custom adapter**
```java

	public class CustomAdapter extends MaterialShowCaseAdapter {

        public CustomAdapter(Activity activity) {
            super(
                    activity,
                    MaterialShowCaseAdapter.SHOW_CASE_DELAY, // DELAY
                    "ActivityAdapterExample" // ID
            );
        }

        @Override // Setup of all show cases views
        public void setup() {
            super.addToQueue(R.id.btn_one, "", "This is button one","GOT IT", MaterialShowCaseViewShape.CIRCLE);
            super.addToQueue(R.id.btn_two, "", "This is button two","GOT IT", MaterialShowCaseViewShape.RECTANGLE);
            super.addToQueue(R.id.btn_three, "", "This is button three","GOT IT", MaterialShowCaseViewShape.RECTANGLE);
        }

        @Override // Behavior
        public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
            super.onDismiss(materialShowcaseView, i);
            switch (currentQueueItemId){
                case 1:
                    Toast.makeText(activity, "You GOT Button One, nice!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

```

**If you're using simple adapter, use a resource array**, you can use this for simple show cases.
```xml

    <!--Simple Activity Adapter Example-->
        <string-array name="showcase_simple_activity_adapter_example_titles">
            <item></item>
            <item></item>
            <item></item>
        </string-array>
        <string-array name="showcase_simple_activity_adapter_example_views">
            <item>@id/btn_one</item>
            <item>@id/btn_two</item>
            <item>@id/btn_three</item>
        </string-array>
        <string-array name="showcase_simple_activity_adapter_example_contents">
            <item>This is button one</item>
            <item>This is button two</item>
            <item>This is button three</item>
        </string-array>
        <string-array name="showcase_simple_activity_adapter_example_buttons">
            <item>GOT IT</item>
            <item>GOT IT</item>
            <item>GOT IT</item>
        </string-array>
        <string-array name="showcase_simple_activity_adapter_example_shapes">
            <item>circle</item>
            <item>rectangle</item>
            <item>rectangle</item>
        </string-array>
        <array name="showcase_simple_activity_adapter_example">
            <item>@array/showcase_simple_activity_adapter_example_views</item>
            <item>@array/showcase_simple_activity_adapter_example_titles</item>
            <item>@array/showcase_simple_activity_adapter_example_contents</item>
            <item>@array/showcase_simple_activity_adapter_example_buttons</item>
            <item>@array/showcase_simple_activity_adapter_example_shapes</item>
        </array>
    <!--Simple Activity Adapter Example-->

```

# Why Jitpack
------------
Publishing libraries to Maven is a chore that takes time and effort. Jitpack.io allows me to release without ever leaving GitHub so I can release easily and more often.

# Apps using MaterialShowcaseView
---------------------------------

  * [Say It! - English Learning](https://play.google.com/store/apps/details?id=com.cesarsk.say_it) : An Android App aimed to improve your English Pronunciation. 
    * [Github Page](https://github.com/cesarsk/say_it)


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
