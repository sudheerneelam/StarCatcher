# Star Catcher

A simple arcade game for Android. Drag your finger to move the basket, catch the falling stars, and dodge the bombs. Each level has a star target, and levels get faster with more bombs. There are 10 levels, and progress is saved on the device.

Ads (Google AdMob):
- Banner ad at the bottom of the menu and game screens
- Full-screen ad after every 2nd finished level or game over

Built with Kotlin, no game engine, and minimal dependencies.

## Run it

1. Install Android Studio (Koala or newer).
2. Open the `StarCatcher` folder (File > Open). Let Gradle sync finish; it downloads what it needs.
3. Plug in a phone with USB debugging on, or start an emulator, then press Run.

## Ads: test mode vs. real money

The project uses Google's official **test ad IDs**, so ads show right away and nothing you tap counts. Do not click real ads on your own app, as AdMob can ban the account.

To earn from ads:
1. Create an app at admob.google.com and add one Banner and one Interstitial ad unit.
2. Open `app/src/main/res/values/strings.xml` and replace the three `admob_*` values with your own IDs.
3. Change `applicationId` in `app/build.gradle.kts` to your own unique id (for example `com.yourname.starcatcher`).

## Publish

In Android Studio: Build > Generate Signed App Bundle, then upload the `.aab` to Google Play Console. You will need a privacy policy URL, since the app uses ads.

## Tweak the game

- Difficulty per level: `Levels.kt` (target score, speed, spawn rate, bomb chance)
- Ad frequency: `SHOW_EVERY` in `AdManager.kt`
- Lives, basket size, item size: constants at the bottom of `GameView.kt`
