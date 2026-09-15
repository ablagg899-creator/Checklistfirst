# Aaron TAP

A portrait Android incremental tap/clicker game with a long progression curve.

## Features

- Power Core tapping with combo scaling, critical hits, fever and TAP FRENZY ×10.
- 9 upgrade categories: Power Glove, Auto Tapper, Crit Core, Combo Engine, Coin Magnet, Star Reactor, Drone Swarm, Quantum Tap and Time Drive.
- Random 2× Points, Critical Frenzy and Mega Tap events.
- Random timed boss battles and Golden Bonus collectibles.
- Achievements, lifetime milestones, daily rewards, daily quests and rebirth/prestige.
- Offline passive earnings and local save data.
- Large-number formatting: K, M, B, T, Qa, Qi and scientific notation.
- Juicy neon visuals, floating rewards, sound effects and vibration feedback.
- Three phone-sized sections: GAME, STATS and COLLECT.

## APK build

GitHub Actions builds a debug APK and release APK on every push to `main`. Open the workflow run and download the `aaron-tap-apks` artifact.

## Local build

Use Java 17, Android SDK 36 and Gradle 8.13:

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
