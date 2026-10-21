# TAKEOVER: Mantra Shruti Box

**Started 21.10.2026.** Marko sent a photo of a shruti box: *"an emulation of a Shruti Box. When I tap the picture of
the Shruti Box, which you draw in vectors, it should open the valves of certain sounds. Very simple. Please make the APK
now and give it to me to download."*

## Where it is

- Repository markoboskoauroville/MANTRA_SHRUTI_BOX (private), `~/Developer/MANTRA_SHRUTI_BOX` on the Mac.
- `app/src/main/java/com/mantra/shrutibox/`: `Reeds.kt` (tuning, wavetable, mixer; no Android), `Engine.kt`
  (AudioTrack, 48 kHz float, its own thread while the app is on screen), `MainActivity.kt` (the drawing, the taps,
  KEY, OFF, vN). The icon template is `app/src/main/icon/`, the version drawn into it at build time.
- Releases: the two newest stay. The signing key never changes (a new key is a new app to Android).

## The versions

| v | What | Tried by Marko |
|---|---|---|
| 1 | The box in vectors, 13 valves (8 + 5), synthesised reeds with beating and breathing bellows, KEY C–B, OFF | not yet |

## Next, if he asks

A bellows speed or volume control; a drone that keeps sounding with the screen off (needs a foreground service of
the mediaPlayback type); just intonation instead of equal temperament (a shruti box is often tuned to Sa and Pa).
