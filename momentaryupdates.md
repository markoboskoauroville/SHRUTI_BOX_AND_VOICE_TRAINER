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

## v6, third round — 21.10.2026 (after Marko sang into the tuner build: "now the tuner is much better")

> now the tuner is much better. Please make this logic: when I press voice to record, it stops the playing of the shruti, and if I play shruti during voice recording, it stops voice recording

7. VOICE stops the shruti (done in the second round); opening a valve or a preset while VOICE is on switches VOICE off.
   **Status:** done, v6 (21.10.2026). Tried on Marko's Pixel 7: Pa playing, VOICE closed it at once and the
   microphone opened; Pa while VOICE switched VOICE off and the microphone closed; VOICE again; Yaman while VOICE
   switched it off and opened Sa Pa Sa'; no crash. Marko sang into the tuner build: "now the tuner is much better".
   Released from main.

## v7 — requested 21.10.2026

> please redo all pages with presets for keys and ragas. They are completely wrong, and please check if other pages are wrong. Check each preset if it's working fine. You need to research if it's true or if it's lying. You need to do the ground truth and check my app for truth and precision Please work so my presets are the right ones.

1. Every preset checked against sources (ground truth), not taken from the reference page: the ragas (their drone,
   thaat, notes, time), the drone combinations, the chords. What is false goes, what is true stays, with its source.
2. Each preset tried on the phone: it opens exactly the valves it names, in every key.

> Please make new icon for this app which reflects its looks, how the actual buttons, its colors, how they look like. So the style of keys needs to be icon.

3. A new icon in the app's own look: the valves as they are on the screen (the eight below, the upper ones over the
   gaps), black ground, light-blue glowing keys; the version number stays under it (version-visible.md).

> Also built a new feature which is auto voice. So when I press the note button, I test the voice. As soon as I hit the right pitch, you jump to the next note, and then again after 4 seconds you activate the voice recording. And when I hit the right pitch, you go to the next one, and that's how we go back and forth until I reach the end of the scale. Of course, when I turn off auto, everything stops.

4. AUTO beside VOICE. With AUTO on, a tap on a valve starts a walk up the scale from that note: the note plays
   4 seconds, then the shruti stops and the microphone listens; when the voice is on the note (within ±10 %, held a
   moment) the next note plays 4 seconds, then the microphone again, and so on to the end of the scale (the chosen
   raga's notes up to Sa', or Sa Re Ga ma Pa Dha Ni Sa'). AUTO off stops everything.
   **Status (21.10.2026, v7 on the test branch, not released):** the presets rebuilt from sources (DRONES 8, RAGAS 24,
   CHORDS 11; sources in Presets.kt); Test 1 checks that no raga's drone sounds a note the raga lacks, the known
   tunings (Puriya, Marwa on Ni; Malkauns, Bageshri on ma) and every chord's name. On Marko's Pixel 7 a script tapped
   all 43 cards: each opened exactly its valves and closed on the second tap, 43 of 43. AUTO built and unit-tested
   (the walk with its own clock); the new icon in. Marko asked why most ragas are the same C G C: the drone is the
   same by tradition, the raga is in the notes sung over it; proposed to him: the raga's notes marked on the valves
   and a ▶ SCALE that plays aroha and avaroha. Waiting for his yes.

## v7, second round — 21.10.2026

> yes, mark ragas, and now you are the master session and compile all updates and push them to main

5. The chosen raga's notes marked on the valves (a brighter outline and a dot of light at the foot), Sa' with Sa.
6. ▶ SCALE on the chosen raga's card: its aroha then its avaroha as the card writes them (turns and all), one valve
   at a time; ■ STOP or any other touch ends it, and the drone comes back when it ends.
7. The icon finished: Sa and Pa in the halo the screen throws, and a themed (monochrome) icon of its own, open valves
   filled and closed ones outlined, so Android 13+ no longer draws seven blobs.
8. Everything of v7 gathered (the presets, AUTO, the icon, the marks, ▶ SCALE) and merged to main, released as v7.

## v8 — requested 21.10.2026

> Yes, the scale is already done. It's pushed to main. It's done. We need to build the new features, please. A new feature is arpeggio. When I click arpeggio, any preset chord will play as arpeggio, one note after the other. And then we need to also improve auto mode. When I press auto, you start from the beginning of scale to train my voice automatically. So you play the note for 3 seconds And then I sing it. If I hover inside the 20% of correction of the pitch, so middle note plus minus 20% for 2 seconds, you go to the next note. Please make it like this. And when I press out auto again, it stops, start from the beginning. I can jump if I don't like it start from the beginning. Once it starts to play, I can manually overwrite and just tell what is the new beginning, and then you start from that note.

1. ARP: with it on, a preset plays as an arpeggio, its notes one after the other, round and round, instead of together.
2. AUTO starts at once from the first note of the scale (the chosen raga's, or Sa Re Ga ma Pa Dha Ni Sa'): the note
   plays 3 s, then the voice sings it; within ±20 % of the note for 2 s goes to the next.
3. AUTO again stops it; the next AUTO starts from the beginning. While it runs, a tap on a valve makes that note the
   new beginning and the walk goes on from there.
   **Status:** released as v8 from main (21.10.2026) on Marko's word, "no need to test, just push". GitHub Actions green
   on the test branch; ARP tried on the Pixel 7 (C E G in turn, ARP off brings the chord back); the new AUTO not
   tried on the phone before the release.

## v9 — requested 21.10.2026

> A new feature is 3 levels of loudness of each note. Long press on the button opens up a menu and user can choose from 100%, which is the loudest, or we can make it in dB. 0 dB, then -12 dB and -24 dB, and the last option is off to turn off the note. Long press, 2 seconds.

1. A valve held for 2 seconds opens its loudness menu (a line fills along its foot while it is held); a short tap
   still opens and closes it.
2. Four choices: 0 dB (100 %, the loudest), −12 dB, −24 dB, OFF. OFF keeps that note silent even when its valve or a
   preset opens it. A valve below 0 dB shows its level near its top; every valve's level is kept across restarts.

## v10 — requested 21.10.2026 (after trying v9: "I am long pressing, nothing is happening")

> I am long pressing, nothing is happening, but that's not a problem. I have idea for better feature because when we press any notes that is next to reset button, there is a list of notes. You just need to make these buttons bigger and they become buttons, representation of the original buttons, and then I can click and change the volume in 3 different levels. So this is much better than long press because this is not a long press, this is single short press. And then you can make like a different, like imagine we are in Fruity Loops. So basically full volume is full button, level 2 is half full button with some color, and level 3 is just one line at the bottom. So we change line, lines inside, like there are 3 LEDs but the continuous ones. Thank you.

1. The list of notes beside RESET becomes a row of buttons, one per sounding note, like the valves.
2. A short tap on a button steps its note: full (0 dB, the button full of light) → half (−12 dB, half full) → a line
   at its foot (−24 dB) → full again. The long press and its menu (and OFF) are gone.

## v11 — requested 22.10.2026: a MIDI controller plays the box

> "we are working on Shruti app. Please see wireless debugging. There is Pixel phone 7 ready to test camera is also there active and it is pointed to MIDI controller. You can also use my mantra camera. You have all the data about it because we programmed it and now make it also available to fully control through ADB. So you can, for example, if its picture is too dark, you can turn on the light on my camera. If picture is in wrong exposure, you can adjust exposure parameters in the camera. If there is wrong white balance, you can adjust that. So make ADB controller for my mantra camera which is already installed and check if it's latest version. And then for Shruti box, you will see on the camera there is a MIDI controller through camera you can see what it is. Find the right model and make this MIDI controller control my Shruti box. Let's do that first" (Marko, 22.10.2026)

1. The MIDI controller in front of the Pixel 7's camera (its model found from the camera) plays the shruti box: its keys or pads open and close the valves.

> "I did press the low C. It keeps the valve opened. It should basically not keep when I press it's open, and when I stop pressing it's closed. Thank you." (Marko, 22.10.2026, after trying the first v11 build)

2. A key is held, not toggled: pressed, its valve opens; released, it closes.

> "I did press the C key two times. It works, it switches on and off the wall. Please also make it velocity sensitive. All my keys in my application Shruti Box." (Marko, 22.10.2026)

3. Velocity: how hard a key is struck sets how loud its reed sounds, for every key.

**Status:** in progress. The controller is a Novation Launchkey Mini MK4 25 on the Pixel's USB; the first build (test branch, build 53) opened it and the low C reached the box, but toggled. The second (held keys) tried by Marko 22.10.2026: "It works".

## v12 — requested 22.10.2026: the whole keyboard at its own pitch, and ◀ ▶ for the octave

> "Now back to the Shruti box. Please map the full keyboard to the Shruti. It means the octaves are matching, so it's not repeating even if I go over the what's on the screen. The app can produce the sound, the screen shows only one octave, but if the keyboard has more octave then keyboard is not lighting up on the screen of phone, but I can play it. And now talking about future updates because of this keyboarding instead of the list of all the keys at the bottom, just add one two buttons arrow left and right which are going to shift the display of the keys and also the playing on the screen octave up or down." (Marko, 22.10.2026)

1. Every key of the MIDI keyboard sounds at its own pitch: no octave folding. Keys inside the octave on the screen light its valves; keys outside it sound without lighting anything.
2. At the bottom, instead of the row of keys C … B: two arrows, ◀ and ▶, that move the octave on the screen (what is shown, and what the valves play) down or up.

> "Also fix the design for the setting icon. It should look exactly like Android setting icon." (Marko, 22.10.2026)

3. The settings button is Android's own settings gear (Material "settings").

> "Or even better, let's make this: if I play a MIDI keyboard and I'm in a different octave, then the view shifts to that octave automatically." (Marko, 22.10.2026)

4. The screen follows the keyboard: a key in another octave moves the box there (what is shown and what the valves play), and lights its valve. While other keys are held, a key outside sounds at its own pitch without moving the box, so held notes never jump.

**Status:** asked.

