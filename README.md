# Mantra Shruti Box

A shruti box for Android, drawn in vectors from Marko's photo (`reference-shruti-box.png`): the teak box, the blue
bellows that breathe, the dark reed plate with its thirteen valve tabs. **Tap a valve and its reed sounds; tap it again
and it closes.** Open as many as you like for the drone. Built 21.10.2026 for Marko's request (momentaryupdates.md).

## On the screen

- **The box.** Eight tabs on the lower row: Sa Re Ga ma Pa Dha Ni Sa'. Five on the upper row, between them, as on a
  keyboard: re ga Ma dha ni. An open valve slides out of the plate and glows while its reed sounds.
- **KEY**, under the box: C to B moves Sa (C3 to B3), remembered.
- **OFF**, top right, closes every valve. **vN** beside it opens the latest build (version-visible.md).
- The screen stays on while a reed sounds. The sound stops when the app leaves the screen.

## The sound

Synthesised on the phone, nothing recorded or downloaded: each reed is one cycle of a free-reed tone (28 harmonics,
the odd ones stronger), played as two voices 1.5 cents apart for the beating of a real reed; each valve fades in over
0.12 s and out over 0.35 s so a tap never clicks; the bellows breathe the whole box at 0.11 Hz, ±16 %; the level
follows the square root of the open valves, and a soft clip keeps every sample inside -1..1. `Reeds.kt`, which imports
nothing from Android, is the whole mechanism and Test 1 runs it on a plain JVM.

## Build and install

Only GitHub Actions builds the APK (MANTRA_MANIFEST android-app.md §1a): push to `main`, the workflow runs the gates
and Test 1, signs with the app's one permanent key (secrets `SHRUTIBOX_KEYSTORE`, `SHRUTIBOX_KEYSTORE_PASSWORD`; the
PKCS12 also in `~/.mantra-shrutibox-signing` on the Mac) and publishes release `vN` with `N-shrutibox-vN.apk`. The
version is `appVersion` in `gradle.properties`, a whole number, nowhere else.
