# Androoster
This repository hosts the code used for building Androoster (Tweaking Toolbox), available on the [Play Store](https://play.google.com/store/apps/details?id=com.andreacioccarelli.androoster), with approximately 150k downloads.

## Context
The app started as a side project, to put in one place, behind a nice UI, all the little tweaks and tricks you can apply on your phone to modify its nominal specifics and characteristics, in order to alter its operating capabilities (namely, battery life, speed or specific configurations)

I despise with every fiber of my body the "Booster"/"Cleaner" software category. They make users look ridiculous for expecting their phone to have the air maneuverability of a F-22 Raptor, and developers foolish for trying to sell software whose claims aren't in the domain of realistic capabilities of the device they run on. It's immoral and unethical to charge people for crapware that does nothing but display satisfying UI and run nonsense tasks. That's sadly a big piece of android culture and market sells, and it sucks. There is much more work done in all of these app to make the UI and UX feel rewarding, smooth and animated rather than in making them actually do something useful.

This software is a toolbox.
A collection of pre-packaged system-wide modifications that can be turned on-off like a breaker switch. Some will work for your device, some won't. You have to know what you are doing before touching something.

## About this code
I started writing this app as a self-taught developer when I was 16 years old.
I was more into the low-level android operating system details rather than the clean application building process.
The app has been written in java and then completely reworked in (very not idiomatic) kotlin.
The code is here more for (archaeological) exposition, rather than actual app design or code principles. From a software engineering / good android app engineering standpoint, you can probably learn a lot of what *not* to do by following this code. It's messy, loosely unorganized, very repetitive and with tons of god-classes, obscure names and weird class hierarchies. 
That's not to say it doesn't work, just that it works clumsily.

While the tweaks and operative system modifications were tailored (and work accurately) for devices running android versions from Jelly Bean up to to Marshmallow, they are not intended or do not consider the structural changes brought forth by future android versions, since they didn't exist when the app was being built. Besides, the need for this kind of software has long died (or at least it did for me personally) with the optimization and improvements with newer android versions.

The current version has been slightly modified from the OG one (I fixed some issues, deleted dead dependencies, upgraded kotlin, removed billing, bumped gradle and fixed the most inhumane AndroidX dependency / manifest merger build failure I have ever witnessed in my entire life, which took about a month) to comply with idiotic GP requirements (the app was initially removed from the store because I was being accused of "charging devices faster", which, of all the screwed up things this app may do to users flipping switches left and right, is not even possible (AFAIK there are no standard APIs for controlling charging rates). I successfully appealed and rebuilt this app, now it's back on the store).

This code is also here for all the russian guys on 4pda redistributing pirated versions of my software, this way you can take it straight from the source code without reverse-engineering the apk. Now the software is 100% free though, so there should be no reason to.

Around 90% of the code you see has been written while listening to Eminem.

## Disclaimers
This project won't receive any new feature. At least none by me. I'll happily push bug fixes and make maintenance updates once in a long while though.