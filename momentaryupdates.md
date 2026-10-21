# momentaryupdates: every request, word for word, before any code

## v1 — requested 21.10.2026 (11:27, with a photo of a shruti box: reference-shruti-box.png)

> Please build the project: open the repository in my GitHub. You call it Mantra Shruti Box, and it should be an emulation of a Shruti Box. When I tap the picture of the Shruti Box, which you draw in vectors, it should open the valves of certain sounds. Very simple. Please make the APK now and give it to me to download. Read the Mantra Manifest before building the Mantra Shruti Box emulator.

1. An Android app, Mantra Shruti Box: a shruti box drawn in vectors, as in the photo (wooden box, bellows, the reed
   plate with its row of valve tabs); a tap on a valve opens it and its reed sounds, a tap again closes it; the bellows
   keep the drone going. Very simple.
2. The APK built by GitHub Actions, published as a release, the link given to Marko.
   **Status:** done, v1 (21.10.2026, 11:35–11:52). Built by GitHub Actions at the first push (Test 1: 20 cases, 0
   failures; signed with the permanent PKCS12 key, SHA-256 92:BC:8F:89…), release v1 `1-shrutibox-v1.apk` (729 KB).
   Tried on the Pixel 7 emulator in portrait: the box draws, Sa and Pa open, glow and sound (one float track at 48 kHz,
   underruns flat after the start), KEY moves to D, OFF closes all. Waiting for Marko's ear and thumb.

## v2 — requested 21.10.2026 (with a web page for the layout: reference-v2-layout.html)

> Please fix the user interface from Shruti Box. It needs to fill up the whole screen, so it works only in portrait mode, and it should have a very simple technical design with curves in a dark mode. It should not emulate the actual design of Shruti Box. Only the placement of the buttons stays the same. The action stays the same as well, opening the valves, but it should use the whole area of the screen efficiently, with a very minimalistic design. The background will be black, and the valves will be light blue and glowing. Please make them glow so that they look like they are coming from another dimension, from the reality of angels.

1. No more wooden box: the whole portrait screen is the instrument, black ground, a simple technical design with curves.
2. The placement stays: eight natural valves along the bottom (Sa Re Ga Ma Pa Dha Ni Sa'), the five between them above
   (re ga Ma dha ni), as in the reference page. The action stays: a tap opens a valve, a tap again closes it.
3. The valves light blue and glowing, a soft light from another dimension, the reality of angels.
   **Status:** done, v2 (21.10.2026). Built by GitHub Actions (green, release v2 `2-shrutibox-v2.apk`, 713 KB, same
   key). Tried on the Pixel 7 emulator in portrait: the bars hide, the thirteen capsules fill the screen, Sa, Pa and
   the upper Ma opened and glowed (blurred halo, white heart, rising light), OFF closed them, no crash. Waiting for
   Marko's eye and thumb on the phone.

## v3 — requested 21.10.2026

> The buttons are too tall. It doesn't matter if there's empty space on the screen, that's okay. Just make them more square, maybe a 2:1 ratio. Basically, look at the web page I uploaded and copy that interface, the same. with the new color scheme

1. The valves as on reference-v2-layout.html: the eight a little over 2 tall to 1 wide (70 × 140 on the page's
   scale), the five narrower and higher, overlapping the top of the eight as on the page; empty space is fine.
2. Each valve shows its Western note and its Indian name, as on the page; under the box the Reset button, the current
   notes and what they make (an interval or a chord), as on the page.
3. The v2 colours stay: black ground, light-blue glowing valves.
   **Status:** done, v3 (21.10.2026). GitHub Actions green (Test 1: 27 cases, 0 failures), release v3
   `3-shrutibox-v3.apk`. On the Pixel 7 emulator: the box as on the page in the v2 colours, valves 70 × 140 and 40 × 100
   in the page's units, Western note over Indian name; Sa Ga Pa in D glowed and read "D major", dha over Pa opened and
   closed (the upper valves win where they overlap), RESET closed all, no crash. Waiting for Marko on the phone.

## v4 — requested 21.10.2026

> Please add all other features from my web app iAudio with different scale presets. You can redesign them in the style of this app.

("iAudio" read as the web app he uploaded, reference-v2-layout.html: no project of that name is on the Mac.)

1. The page's presets, all of them, in five tabs: Basic, Intermediate and Advanced drones, Ragas, Western chords; each
   with its name, level, description and where it is used. A tap closes all valves and opens the preset's.
2. Redesigned in this app's style: black, light blue, rounded, monospace; the preset that sounds now glows.
3. What the page has that a phone does not need: the light theme (this app is dark by request) and the computer
   keyboard shortcuts.
   **Status:** done, v4 (21.10.2026). Every preset of the page in `Presets.kt`: 5 tabs, 41 cards (5, 6, 6, 12, 12, as
   on the page), the page's notes in C turned into valves so they follow the key; names written as the app writes them
   (ma plain, Ma sharp). GitHub Actions green (Test 1: 31 cases, 0 failures), release v4. On the Pixel 7 emulator in D:
   CHORDS → Major opened Sa Ga Pa, the line read "D major", the card lit; the same card again closed them; no crash.
   The light theme and the keyboard shortcuts of the page were left out (dark only by request; a phone has no keyboard).

## v5 — requested 21.10.2026

> please fully test the Shruti box in the real phone which is now attached through USB. Pixel 7 phone, open it with screen copy and test it truly. There is a bug in the ragas when I open the ragas. One raga opens all ragas, so I cannot select individual ragas. That's the greatest bug. Test fully this app before release also, I don't need to have Shruti and v4 inside the main interface. Instead of v4, there should be a setting button where I see version And different options for user interface, different color scheme. This is a blue cold color scheme. We need to have also orange warm color scheme. And also parts of the interface can be hidden from the settings, and user can fix layout to portrait or landscape inside the settings.

1. The bug: ten ragas share the drone Sa Pa Sa', so a tap on one lit all ten. Each card is chosen by itself.
2. Tested fully on the real Pixel 7 over USB with scrcpy, before the release: a build goes to a test branch first
   (the APK from that run, no release), is tested on the phone, and only then is merged to main and released.
3. "SHRUTI" and "v4" leave the main screen; a settings button takes their place. In settings: the version, the colour
   scheme (cold blue as now, warm orange), which parts of the screen show, and the layout fixed to portrait or landscape.
   **Status:** done, v5 (21.10.2026). Built on the test branch (no release), tried on Marko's real Pixel 7 over USB
   (scrcpy on the MacBook screen, sound on the phone): RAGAS → Yaman lit Yaman only, Bhairav lit Bhairav only, a second
   tap closed it, Malkauns and Bageshri (both Sa ma Sa') each alone; a key change kept the card; valves by hand, an
   upper valve over Pa, RESET; settings: WARM, LANDSCAPE (valves and sound carried through the turn), hiding names,
   notes, presets and keys, Back to leave; force-stop and restart kept every setting; Home stops the sound thread,
   coming back starts it; no crash, no ANR. One fix from the test: landscape with the presets hidden left half the
   screen empty, now the box is centred and fills the height (build 14, tried again). Then merged to main and released.

## v6 — requested 21.10.2026

> add a new feature to this app, which is a voice trainer. It's very simple: you need to play the shruti, stop the shruti, and then the microphone becomes active. Then you need to repeat the sound, and the application gives you how much percentage you are off plus or minus. It's the same scale you can see on different tuning devices or software. So we're going to have small oval buttons in the middle when it's the exact pitch, and then these LEDs are going left or right depending on your error, and the middle one is of course the last button pressed. A voice trainer. Add this to the app

1. A voice trainer: play the shruti (a valve), stop it, and the microphone opens; sing the note back.
2. The error shown plus or minus in percent, as on a tuner: a percent of a semitone, so ±50 % is half a semitone
   (the cent of a tuner). Any octave counts: a man and a woman sing the same note an octave apart.
3. A row of small oval LEDs: the middle one is the exact pitch and carries the name of the last valve pressed; flat
   lights to the left, sharp to the right.
4. The microphone only while the trainer is on and every valve is closed and silent, so it never hears the shruti.

## v6, second round — 21.10.2026 (Marko sang into the test build)

> yes, I did it but it's not good. You need to get the code for tuning the guitar or any other software. The LEDs are moving too slow. They are not optimized. They need to react. You need to do FFT in high resolution or something, because it's too slow. It needs to react immediately and very fast. And the second bug is that when I press voice, the sound should automatically be disengaged from the Shruti box. If you record the Shruti box sound, the pitch will be perfect. That's not what we want. We want to stop the Shruti when voice is active

> search for the best guitar tuner on GitHub. What has the most forks? What is the most popular one? And get the code from any platform and convert to Android code in Kotlin

5. The LEDs react immediately, as on a guitar tuner: the pitch method of the most popular open tuner on GitHub,
   converted to Kotlin; a reading every few milliseconds, not every quarter second.
6. VOICE stops the shruti at once: pressing it silences every reed, and the microphone never hears the box.
