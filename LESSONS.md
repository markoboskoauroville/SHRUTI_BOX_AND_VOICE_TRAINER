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
