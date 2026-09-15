# Tap Galaxy

A mobile incremental clicker game inspired by classic idle/clicker games.

## Gameplay

- Tap the glowing Power Core to earn coins.
- Build a combo by tapping rapidly for bigger payouts.
- Buy upgrades for stronger taps, passive income, critical taps, and larger combo multipliers.
- Progress is saved locally on the device.
- The game is designed for one-handed portrait play.

## Upgrades

- **Power Glove** — increases coins per tap.
- **Auto Bot** — generates coins every second.
- **Crit Core** — adds a chance for a 3x critical tap.
- **Combo Engine** — raises the maximum combo multiplier.

## APK

Every push to `main` builds a debug APK and an unsigned release APK through GitHub Actions. The artifacts are named `checklist-first-debug-apk` and `checklist-first-release-apk`.

## Build locally

Use Java 17 and Android SDK 36, then run:

```bash
gradle assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.
