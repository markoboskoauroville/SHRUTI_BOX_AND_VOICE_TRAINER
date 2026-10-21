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
