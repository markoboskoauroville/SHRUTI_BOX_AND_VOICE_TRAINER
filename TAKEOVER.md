# TAKEOVER: Mantra Shruti Box

**Started 21.10.2026.** Marko sent a photo of a shruti box: *"an emulation of a Shruti Box. When I tap the picture of
the Shruti Box, which you draw in vectors, it should open the valves of certain sounds. Very simple. Please make the APK
now and give it to me to download."*

## Where it is

- Repository markoboskoauroville/SHRUTI_BOX_AND_VOICE_TRAINER (private; was MANTRA_SHRUTI_BOX), `~/Developer/SHRUTI_BOX_AND_VOICE_TRAINER` on the Mac.
- `app/src/main/java/com/mantra/shrutibox/`: `Reeds.kt` (tuning, wavetable, mixer; no Android), `Engine.kt`
  (AudioTrack, 48 kHz float, its own thread while the app is on screen), `MainActivity.kt` (the drawing, the taps,
  KEY, OFF, vN). The icon template is `app/src/main/icon/`, the version drawn into it at build time.
- Releases: the two newest stay. The signing key never changes (a new key is a new app to Android).

## The versions

| v | What | Tried by Marko |
|---|---|---|
| 1 | The box in vectors, 13 valves (8 + 5), synthesised reeds with beating and breathing bellows, KEY C–B, OFF | on the Pixel 7 emulator (no sound out there); Marko not yet |
| 11 | A MIDI controller plays it (Launchkey Mini MK4 25 by USB): keys held, velocity | Marko: held keys "It works"; velocity built, his Launchkey sends a fixed 127 |
| 12 | Every key at its own pitch (free reeds), the box follows the octave played, ◀ ▶, Android's gear | on the Pixel 7 by adb |
| 13 | Shruti Box and Voice Trainer: trainer difficulty and range, CUSTOM presets (save, ✎), sequencer with BPM and XFADE | on the Pixel 7 by adb, every piece tapped and screenshot |

## Next, if he asks

A bellows speed or volume control; a drone that keeps sounding with the screen off (needs a foreground service of
the mediaPlayback type); just intonation instead of equal temperament (a shruti box is often tuned to Sa and Pa).

## v11–v13 in the code (22.10.2026)

- `Midi.kt`: every MIDI device that sends, opened as it appears; (channel, note, velocity), 0 = released.
- `Reeds.kt`: `Mixer` has the 13 valves (moved by `octave`) and 128 free reeds (`setNote`) for keys outside the screen's
  octave; `crossfadeS` slows every swell and fade (the sequencer's XFADE); `Shruti.valveInWindow`, `octaveFor`, `noteFreq`.
- `Auto.kt`: `AutoRun.walk` (MIDI notes through the trainer's range), `DIFFICULTIES`, `PIANO`; `SeqRun` (one beat a step).
- CUSTOM presets are JSON in the preferences (`custom`: name and valves relative to Sa).
