# Monster Tamer: A New Journey

An original Android turn-based monster-taming RPG with a classic 16/32-bit JRPG presentation. It is inspired by the *feel* of classic console RPGs, while using original names, systems, characters, creatures, and artwork rather than copying Dragon Quest assets.

## Included gameplay

- Explore Greenvale and six themed regions.
- Turn-based battles with Attack, Tame, Guard, and Run.
- **120 distinct monster species** available to discover and tame.
- Taming odds improve when a monster is weakened.
- Tamed monsters join the roster and the first four become the active party.
- Party monsters level up after victories.
- Hero leveling, XP, HP, gold, healing, saving, and recovery.
- Monster Book with 7 pages of collectible species.
- Town/world, battle, monster collection, and camp screens.
- Original pixel-inspired visuals drawn for the app plus AI-generated promotional concept art.
- Offline/local save data through Android SharedPreferences.

## Build

GitHub Actions builds both debug and release APKs on every push to `main`. Open the Actions run and download the `monster-tamer-apks` artifact.

Local build requirements: Java 17, Android SDK 36, and Gradle 8.13.

```bash
gradle assembleDebug assembleRelease
```
