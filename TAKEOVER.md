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
| 14–19 | Landscape = the box alone; octave labels; Launchkey in DAW mode (pads = sequencer steps, lit; knobs = valve loudness, Shift = upper valves; pitch strip bend, mod strip vibrato; the display shows values) | Marko: pads, knob 1, pitch, modulation confirmed; knobs 2–8 and Shift not yet |
| 20–21 | Band-limited wavetables (no aliasing on high notes, "car alarm" gone); 88 piano keys A0–C8, names without minus | verified through the Pixel mic |
| 22–24 | Salamander Grand piano as a second sound (60 AAC samples, 2.5 MB, CC BY 3.0), decoded in 3.3 s | Marko: "Piano sounds good" |
| 25 | KEYBOARD VELOCITY in settings sets the Launchkey's velocity curve (feature control CC 104); it was on Fixed | Marko: soft 3–11, hard 80–123 |
| 26 | Plays on in the background (media-playback foreground service + notification with Stop); ✕ beside the gear releases everything; Back = background | Marko: "it works in the background" |
| 27 | The sequencer numbering kept across restarts. **Released as v27** (the page serves it) | by adb, restart checked |

## The page

https://mantrashrutiandvoicetrainer.pages.dev (`site/`, Cloudflare Pages, deployed by build-apk.yml, see below): the
newest release behind "Download the latest version", the Novation section, all equipment in one frame (Amazon tag
markomantresh-21), the Salamander credit, the Tibetan bowl, the favicon = the app icon. Linked from the biography's Kod tab.

## Next, if he asks

A bellows speed or volume control; a drone that keeps sounding with the screen off (needs a foreground service of
the mediaPlayback type); just intonation instead of equal temperament (a shruti box is often tuned to Sa and Pa).

## v11–v13 in the code (22.10.2026)

- `Midi.kt`: every MIDI device that sends, opened as it appears; (channel, note, velocity), 0 = released.
- `Reeds.kt`: `Mixer` has the 13 valves (moved by `octave`) and 128 free reeds (`setNote`) for keys outside the screen's
  octave; `crossfadeS` slows every swell and fade (the sequencer's XFADE); `Shruti.valveInWindow`, `octaveFor`, `noteFreq`.
- `Auto.kt`: `AutoRun.walk` (MIDI notes through the trainer's range), `DIFFICULTIES`, `PIANO`; `SeqRun` (one beat a step).
- CUSTOM presets are JSON in the preferences (`custom`: name and valves relative to Sa).

## The download follows every release by itself (1.11.2026)

- The button's address stays `download/shruti-box-and-voice-trainer-latest.apk` (no version, never edited; the page names
  no version, version-visible.md). The APK is no longer in git.
- build-apk.yml, step *Put the release on the page*: on main, when *Publish* made a new release (or on Run workflow, which
  re-puts the existing release of appVersion), it downloads that release's APK, `scripts/publish_page.sh` copies it to the
  fixed address, writes its size into the `app-size` spans and deploys site/public with wrangler.
- **Blocked on the secrets:** the repository has no CLOUDFLARE_API_TOKEN / CLOUDFLARE_ACCOUNT_ID (only the keystore
  secrets). Until they are added (MANTRA_MANIFEST apis/cloudflare.md §6) the step prints a notice and skips; the live page
  keeps the hand-deployed v27 file (checked 1.11.2026: identical to the release, 3437229 bytes). Proven path so far:
  run 41277771678 (workflow_dispatch, green, skip notice). After the secrets: Actions → Run workflow on main, then check
  the live file and the size "3 MB" on the button.
- A new version: raise appVersion, push to main. Never deploy site/public by hand (git holds no APK).
