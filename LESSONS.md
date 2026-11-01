# LESSONS: Mantra Shruti Box

1. **The repository came first, then the key, then the code** (21.10.2026): the request was pushed word for word
   before any code (requests-saved-first), and the PKCS12 key was made and stored as secrets before the first build,
   so v1 is already signed with the key every later version must carry (updatable.md).
2. **Lint is told why, not switched off**: the portrait lock trips LockedOrientationActivity; the manifest says
   `tools:ignore` on that one activity with the reason beside it. The network-version lint checks are off in
   build.gradle.kts, because they turn a new release at Google into a red build that says nothing about this app.
3. **The emulator said `unauthorized`** (21.10.2026): its quick-boot snapshot kept an old adb authorisation; a cold
   boot (`-no-snapshot-load`, headless with `-no-window -no-audio`) fixed it. 60 % CPU on the emulator is its software
   drawing of one frame per tick; worth watching on the phone.

## 22.10.2026
- The Launchkey Mini MK4 can be set fully from the app in DAW mode: feature controls are CCs on channel 7 of its DAW port (asked on channel 8). CC 104 = keys velocity curve (0 soft, 1 normal, 2 hard, 3 fixed) — his keyboard was on Fixed, which is why every note came at 127.
- The Oct −/+ buttons send nothing over MIDI (Ableton reads them only from the notes after); follow the octave from the incoming note numbers.
- A run loop on withFrameNanos stops when the screen is off; the sequencer needs its own clock (delay loop) to play in the background.
- Naive wavetables alias above ~C6; one table per octave band (harmonics ≤ 0.45 fs) fixed it.

## 1.11.2026, download links by themselves
- The publish step skips when the tag exists, so the page step must not hang on "published" alone: a Run workflow on
  main re-puts the existing release, which lets the page be fixed or checked without a new version.
- The button said "APK · 1 MB" for a 3.4 MB file: a size typed by hand goes stale like a version; the run writes it now.
