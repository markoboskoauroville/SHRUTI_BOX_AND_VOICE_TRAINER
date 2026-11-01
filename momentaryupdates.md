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

**Status:** done in the app (v11). Marko played soft and hard: every press reached the phone as velocity 127, so the Launchkey is set to a fixed velocity on the keyboard itself. Status before: The controller is a Novation Launchkey Mini MK4 25 on the Pixel's USB; the first build (test branch, build 53) opened it and the low C reached the box, but toggled. The second (held keys) tried by Marko 22.10.2026: "It works".

## v12 — requested 22.10.2026: the whole keyboard at its own pitch, and ◀ ▶ for the octave

> "Now back to the Shruti box. Please map the full keyboard to the Shruti. It means the octaves are matching, so it's not repeating even if I go over the what's on the screen. The app can produce the sound, the screen shows only one octave, but if the keyboard has more octave then keyboard is not lighting up on the screen of phone, but I can play it. And now talking about future updates because of this keyboarding instead of the list of all the keys at the bottom, just add one two buttons arrow left and right which are going to shift the display of the keys and also the playing on the screen octave up or down." (Marko, 22.10.2026)

1. Every key of the MIDI keyboard sounds at its own pitch: no octave folding. Keys inside the octave on the screen light its valves; keys outside it sound without lighting anything.
2. At the bottom, instead of the row of keys C … B: two arrows, ◀ and ▶, that move the octave on the screen (what is shown, and what the valves play) down or up.

> "Also fix the design for the setting icon. It should look exactly like Android setting icon." (Marko, 22.10.2026)

3. The settings button is Android's own settings gear (Material "settings").

> "Or even better, let's make this: if I play a MIDI keyboard and I'm in a different octave, then the view shifts to that octave automatically." (Marko, 22.10.2026)

4. The screen follows the keyboard: a key in another octave moves the box there (what is shown and what the valves play), and lights its valve. While other keys are held, a key outside sounds at its own pitch without moving the box, so held notes never jump.

**Status:** v12 22.10.2026, built and tested (free reeds, octave window, Android's gear from Material Icons' path); ◀ ▶ tried by adb on v13. The keyboard's own octaves not yet played by Marko.

## v13 — requested 22.10.2026: the trainer's difficulty and range, and CUSTOM presets

> "More updates in the settings for voice trainer. You need to put difficulties. The hardest part is 20% plus minus, then 40%, then 60%. Different difficulties for the voice trainer. And when I press auto for voice trainer, the C is starting automatically. Also inside the settings for the voice trainer, you need to put the range of training. So now we have from CC1 until 88 keys of the actual piano keys. That's the one thing. Second thing I want to have: custom. We have drones, we have ragas, we have chords, we have custom. So user can store its own presets and also rename them. So how we save the presets? When user presses keys next to the reset button, the keys are displayed. Then after the keys there will be a save icon, and then this icon saves the new preset of what's there on the screen. Let's build that." (Marko, 22.10.2026)

1. VOICE TRAINER in settings: DIFFICULTY, how close the voice must be to count as the note: ±20 % (hardest), ±40 %, ±60 %.
2. AUTO starts at once on C (Sa) by itself.
3. VOICE TRAINER in settings: the RANGE of training, chosen within the 88 keys of a piano (A0 … C8).
4. A CUSTOM tab beside DRONES, RAGAS, CHORDS: his own presets, renamed as he likes.
5. Saving one: after the note buttons beside RESET, a save icon stores what sounds on the screen as a new CUSTOM preset.

> "A long press on auto sets its difficulty." (Marko, 22.10.2026)

6. A long press on AUTO sets the difficulty (±20 / ±40 / ±60 %), as well as settings.

> "Please also rename the repository and the application. It's called Shruti Box and Voice Trainer." (Marko, 22.10.2026)

7. The app and its repository are named Shruti Box and Voice Trainer.

> "Please in the custom chords please build a simple sequencer. When user similar is in same logic in now what we build opening for Chrome browser user press on each preset and then it gets the number, and then when user press sequence button this sequences is starting and the speed of the sequence in beats per minute needs to be set in the sequencer itself." (Marko, 22.10.2026)

8. A sequencer in CUSTOM: as in Opening and Closing's Chrome profiles, a tap on a preset gives it the next number; SEQUENCE plays them in that order, round and round; its speed in beats per minute is set in the sequencer itself.

> "In the sequencer itself, add a crossfade button so it's not hard sequencing. Inside the app, inside because it's Shruti Box, it needs to have a smooth transition. One octave or one combination, one let's call it the one chord is playing, and then the sequences go to the second one. So they are fading in and out. Time adjustable." (Marko, 22.10.2026)

9. XFADE in the sequencer: one chord fades out while the next fades in, over a time he sets there.

**Status:** v13 22.10.2026, built (test branch), installed on the Pixel 7 and tried by adb taps: the save icon after the notes, the name asked at once ("Morning Drone", "Evening Fourth"), the CUSTOM tab with SEQ, ▶ SEQUENCE / ■ STOP, 30 BPM, XFADE 1.5 s (the fade seen mid-way on a screenshot), the numbers 1 and 2, AUTO ±20, settings' DIFFICULTY and RANGE C3–C4, ▶ to Sa = C4. Released as v13 from main. Not tried by Marko's hand yet; the two test presets stay on his phone (✎ deletes them).

## The release and its page — requested 22.10.2026

> "After you've done everything, now this application is ready to be released. Create a page in the Cloudflare domain name is mantrashrutiadvoicetrainer.pages.dev and make it as a product advertisement campaign to sell this one for free now. But you need to quickly give all the features in a nutshell, then go into the details, and in the end history of Shruti Box and how they come from France to India and how they are used for recited mantras. Then you can give few examples of mantras and a few YouTube videos where people are playing the mantras with Shruti Box." (Marko, 22.10.2026)

> "Take the screenshot of the app to be published on the website." (Marko, 22.10.2026)

1. Release the app once v13 is done.
2. A product page on Cloudflare Pages, free download: the features in a nutshell, then the details, then the history of the shruti box (from France to India) and its use with recited mantras, a few mantras, and a few YouTube videos of mantras played with a shruti box.
3. Screenshots of the app on the page.

> "Give some also lifestyle images and connection diagrams for a MIDI keyboard. Exactly the same setup I have: open the Chrome browser and use Nano Banana to generate images for free." (Marko, 22.10.2026)

4. Lifestyle images and a connection diagram of his setup (Launchkey Mini MK4 25 into the Pixel 7 by USB), made with Nano Banana (Gemini) in Chrome.

> "Know very well how to do this to Gemini in my profile of ... auroville.community." (Marko, 22.10.2026)

> "When the page is done building, open it automatically with the Cloud extension in Chrome." (Marko, 22.10.2026)

5. The page opens in Chrome through Claude in Chrome as soon as it is live.

> "When you finish everything, write in the code box a message to Svemir in Croatian speaking language. Say thank you for inspiration. I created ShrutiBox app. If you ever need a shruti, you can download it from here. You can also just point to the website. That's it. I actually have my WhatsApp right in auroville Community in my quick bookmarks bar, and you can just trigger it from there and find Svemir. Then just not sending message, just pasted there." (Marko, 22.10.2026)

> "Also, please edit the image. The image is great, but it has an extra cable going out of it that's wrong. There is only one USB-C cable at the top of the device, this one which is on the side. It's an AI error. Please fix it." — "I'm talking about lifestyle and hero image." (Marko, 22.10.2026)

7. The hero lifestyle image: remove the extra cable; only the one USB-C cable, from the top of the keyboard to the phone. — **Done 22.10.2026:** Gemini edits and new generations kept a cable along the front; Marko edited the original in imgtoimg.ai's AI Image Editor himself; that image is the hero (published, checked byte for byte).

> "Also, one part is missing. To charge the phone at the same time we're using the MIDI controller, I want to promote this device. Look at this." (Marko, 22.10.2026, with a screenshot of the StarTech 4-Port USB-C Hub HB31CM4CPD3: 4× USB-C, 10 Gbps, 100 W Power Delivery pass-through, 32 cm cable)

> "Also find me alternatives to this device on Amazon Germany. You can open Amazon Germany with Chrome extension for Claude, and then you find the same device with metal build. This is plastic build. Power delivery is very important, and it's very important to have only USB-C ports. Find me an alternative." (Marko, 22.10.2026)

> "Please learn from a repository called Elevate Media. When we do this kind of sheets for characters, sheets for props, sheets for spaces, and also pencil sketches, you need to work in this way. Give reference for everything you can even find web images of these devices so everything looks like in real life. You need to write references. Read all the manifest from that Elevate Media." (Marko, 22.10.2026)

> "On the website we need to promote the right USB hub, which is the metal one and has a good price on Amazon. This one, the plastic one, is unfortunately a scam." (Marko, 22.10.2026)

> "please fix my web page. Download should be download latest version, not v13. We don't specify version because we are always updating. Put this in manifest so never repeats again, and it will open the GitHub page with the latest version" (Marko, 22.10.2026)

12. The download button says "Download the latest version", no version anywhere on the page; it opens GitHub's latest release (needs the repository public: key scan and Marko's yes, going-public.md); until then the page serves the newest APK under a fixed name.

11. The page promotes the metal StarTech 309B-USBC-HUB (amazon.de, €37.81 on 22.10.2026), not the plastic HB31CM4CPD3. — **Done 22.10.2026:** section "Play for hours: charge while you play" with the three-step hookup and the amazon.de link; the plastic hub is not named.

10. Images are made the Elevate Media way: reference sheets (characters, props, spaces, pencil sketches), real web photos of the devices as references, and the references written down.

9. Alternatives on amazon.de: a metal (aluminium) hub, Power Delivery pass-through, only USB-C ports.

8. On the page: play and charge at once, the StarTech HB31CM4CPD3 hub between the phone, the MIDI keyboard and a USB-C charger, with its diagram.

6. At the end: a message to Svemir in spoken Croatian (thanks for the inspiration, the app, the page's link), shown in a code box, and pasted into his WhatsApp chat (WhatsApp Web from the bookmarks bar, Chrome profile auroville.community) without sending it.

**Status:** v13 released (22.10.2026). The page is live at https://mantrashrutiandvoicetrainer.pages.dev (features, details, history from France to India with sources, six mantras, six verified YouTube videos, the v13 APK served from the page, two Nano Banana images and four screenshots), opened in Chrome. The message to Svemir is pasted in his WhatsApp chat, not sent. The hero image's extra cable: being fixed.

## v14 — requested 22.10.2026: landscape is the box alone, full screen

> "Now the lifestyle image showed me something I don't have in my app, actually, and we need to build that in the app. Shruti box and that is in the landscape mode. There should be only keyboard, nothing additional. There landscape mode is for full screen keyboard. That's it. In a setting, you can just have settings in landscape mode, additional panels so user can add if he wants something extra. But by default it's empty. It's just a keyboard in immersive view with our Shruti box." (Marko, 22.10.2026)

1. Landscape is the shruti box alone, filling the screen, immersive: nothing else by default.
2. Settings, LANDSCAPE PANELS: the extra parts he may add beside the box there (notes, trainer, presets, octave), all off by default.

**Status:** asked.

## v15 — requested 22.10.2026: the octave on every valve

> "Please also fix my ShrutiBox. When I switch between octaves, I need to see on the keyboard itself that there is C1, C2, and so on. So every name of the note you can add it to only European notation: SA RE GA MA PA DA NI SA. I can stay so I know where I am by just looking at my keyboard. I don't need to look down there in the switching part at the bottom of the page." (Marko, 22.10.2026)

1. Each valve's Western name carries its octave number (C3, D3 … C4 for Sa'), moving with ◀ ▶ and with the keyboard; the Indian names stay as they are.

**Status:** asked.

## The donation bowl — requested 22.10.2026

> "please build in donation Tibetan bubble. We are going to kind of emulate the donation after my sound bots. When we put Tibetan bubble to donate to me mantra for my work, it needs to be connected to my account on Revolut and donations come directly there to my Revolut account. Can we build this so right in the manifest when Marko Mantra asked for donation, there will be Tibetan bowl image and user can click and put the money inside" (Marko, 22.10.2026)

1. A Tibetan singing bowl on the page, as after a sound bath: a tap on it rings the bowl and opens Marko's Revolut payment link, so the money goes straight to his Revolut account.
2. In the manifest: whenever Marko asks for a donation on a page, this bowl is used.

**Status:** live 22.10.2026 on mantrashrutiandvoicetrainer.pages.dev (#donate): the bowl is a photoreal Nano Banana Pro render; a tap rings a synthesised singing bowl, drops a coin and opens https://revolut.me/mbosko0612. Kit and rules in MANTRA_MANIFEST (donation-bowl.md, payment-links.md).

## The Facebook post — requested 22.10.2026

> "please write a post for facebook to promote this page, both in english and in croatian" — then: "Facebook ad is very bad. You need to write in natural writing style. Please see my writing style and put one screenshot and that's it. Make it very normal, not overhyped, please. And both languages in one post, please. First English, then Croatian. Like human wrote it, please. My Facebook page is opened in my web browser Chrome under my profile, aurorawheel.community. And please make a post. Don't send it. I'm going to send it. Attach the screenshots and make it normal, please. Not overhyped." (Marko, 22.10.2026)

1. One Facebook post on his profile (Chrome, auroville.community), English first, then Croatian, in his own plain writing style read from his posts: no hype, no emoji lists, no hashtags.
2. One screenshot of the app attached. Composed and left unsent: Marko posts it.

**Status:** composed 22.10.2026 in the Create post dialog on his profile (Chrome): plain text in his style, English then Croatian, the page link, the app screenshot (the Major chord sounding). Not posted. The audience was "Only me", left for him to change.

## The download above the bowl — requested 22.10.2026

> "now fix the Shruti box page by putting a download link at the bottom, just above the Tibetan ball. People need to scroll down to find it themselves" (Marko, 22.10.2026)

1. At the bottom of the page the download comes first, then the Tibetan bowl, so whoever scrolls to the end meets the download before the bowl.

> "top download icon for downloading the application should be removed. Only button one exists next to Tibetan ball" (Marko, 22.10.2026)

2. The only download button on the page is the one above the bowl; the one at the top goes.

**Status:** done 22.10.2026: the download section stands just above the bowl (checked on the live page); the top button is removed and its install note moved under the remaining button.

## Affiliate links — requested 22.10.2026

> "please make affiliate links for this hub and open my shopping basket in my Pixel 7 and see which one I choose to buy. Actually, I deleted others, so there are two options. On Amazon there are also affiliate links for this midi controller" (Marko, 22.10.2026)

1. His basket (Pixel 7, 22.10.2026) kept two of the candidates: the SATECHI USB-C Hub 4-in-1 (100 W PD, 4× USB-C, aluminium) and the GL.iNet Opal. The page promotes the hub he chose, the Satechi.
2. Amazon affiliate links (his Associates tracking ID) for the hub and for the Novation Launchkey Mini MK4 25 on the page.

**Status:** done 22.10.2026: Marko signed up for Amazon.de Associates himself (tracking ID markomantresh-21); the page links SATECHI 4-in-1, StarTech 309B and the Launchkey Mini 25 MK4 with the tag and rel=sponsored, and carries Amazon's disclosure line. Payment and tax details in PartnerNet: Marko's, still to do.

## The Mantra payment gateway — requested 22.10.2026

> "Please fix the mantra payment gateway first. What is seen on the page is the title itself, anti-Tibetan ball. When the user clicks on the Tibetan ball, there is a Tibetan ball sound and then comes the technical interface to donate without the ball itself." (Marko, 22.10.2026)

1. On the page: only the title and the Tibetan bowl.
2. A tap: the bowl rings, then the bowl gives way to the giving panel (Revolut button, his QR code, a way back); the bowl is not shown with the panel.

> "You need to write under the ball. Click on the ball to donate." (Marko, 22.10.2026)

3. Under the bowl: "Click on the bowl to donate".

**Status:** live 22.10.2026, both steps tested in Chrome on a local copy (the bowl with its line; after the click the bowl fades and the panel shows the Give with Revolut button, the QR @mbosko0612 and "back to the bowl"). On the way the download section was cut by a replacement and put back above the bowl (checked live); the line under the bowl was black on black until the button inherited the page colour.

## v16 — requested 22.10.2026: the Launchkey's octaves and the app's octaves as one

> "please go back to my Pixel phone through ADB and use Novation Control. There is a switch on Novation Octave Plus and Minus. You need to find a way to change this switch through this application, and then test all octaves so they are compatible with the application. Right now not everything is displayed, some goes out of the range of this application, so the application doesn't snap to that range. Please check all the ranges from the bottom to the top, and they will be displayed in the same language as this keyboard is displaying it. For example, the lowest octave is 4 C2 C0. You can see matrix display now with camera. Please make this app compatible with this controller, so when I switch octaves with my left and right arrows at the bottom of the screen, it is also triggering the same mechanism inside my MIDI controller. It's shifting the MIDI controller as well to different octaves" (Marko, 22.10.2026)

1. Every octave the Launchkey's Octave − / + reaches is inside the app's range: the box snaps to it, from the lowest to the highest.
2. Note names in the app as the keyboard's own display writes them.
3. ◀ ▶ in the app also shift the Launchkey's octave (the controller follows the app), found through the controller's own control interface.

**Status:** asked.

## v17 — requested 22.10.2026: the whole Launchkey drives the app, as it drives Ableton Live

> "are there some other open-source programs which control Novation keyboards? Please search through GitHub and find the codes source code / we can even write to Novation to give us data we need for developing our app to control Novation keyboard Also, Novation keyboard is controlled via Ableton Live and it can control Ableton Live. Are there some protocols we can use from Ableton Live? Ableton Live protocols are very well documented." (Marko, 22.10.2026)

> "ableton Live is controlled also with all these knobs and pads and everything. Let's see what we can use to control our own application with this Novation keyboard using the tricks for Ableton Live ableton Live our application on the phone" (Marko, 22.10.2026)

1. Learn from open-source code that drives Novation keyboards, above all Ableton Live's own Launchkey MK4 MIDI Remote Script.
2. The app takes the Launchkey in DAW mode, as Ableton does: knobs, pads, arrows, Play and the other buttons control the app; the app lights the pads and writes the display.
3. A letter to Novation asking for the octave message (drafted for Marko).

> "for now from Novation Control, I only want to control the octave button. That must work. Pitch and modulations ribbons must work. There are eight knobs at the top. They should work with adjusting velocity of each button on the Shruti box on each valve. And we have sequencer also, so the pads are for sequencer. They will light up as the sequencer is running. And also I can trigger the sequencer each step of the sequencer with these sequencing buttons. And it can also light up. So it's off when I select preset, then it's become on. First preset first pad is on. Second preset second pad is on. And then I can trigger them by pressing the tabs itself" (Marko, 22.10.2026)

4. The Launchkey's Octave − / + work: the box follows every octave.
5. The pitch and modulation strips work (pitch bends the reeds, modulation adds vibrato).
6. The 8 knobs set the loudness of the valves.
7. The 16 pads are the sequencer's steps: a pad is dark until a preset gets that number in SEQ (first numbered preset lights pad 1, the second pad 2 …), it glows brighter while its step plays, and a press triggers that step.

**Status:** v17–v20 tested on the Pixel 7 with Marko's hand, 22.10.2026: DAW mode switched on by the app (the Launchkey answers 9F 0C 7F; again after a restart); knob 1 = CC 21 on the DAW port, Sa's loudness, shown on the display; modulation strip = CC 1 (port 0), vibrato; pitch strip = bends on port 0, shown on the display (v20); pads = notes 96–103 / 112–119 on the DAW port, blue while numbered, white while sounding (seen through the camera, the light walking pad 1 → 2 at 30 BPM), a press triggers its step; Oct + followed (note 108 → Sa = C7, names as on the Launchkey). Not yet tried: knobs 2–8 and Shift + knobs. SEQ numbering is not kept across restarts yet.

## The Novation note on the page — requested 22.10.2026

> "please add a note on the website about integration with Novation and what we have implemented" (Marko, 22.10.2026)

1. A section on the page: what the app does with a Novation Launchkey Mini MK4, released together with the build that has it.

> "web page update is the last thing we are doing at the end of the session. You need to ask me now, we are just developing. I just want to put it in your mind so I don't forget to tell you when it's finished. So you need to update" (Marko, 22.10.2026)

2. The page is updated at the end of the session, not now: before closing, ask Marko and then publish the release, the latest APK and the Novation section together.

> "on the website, all equipment which is affiliate links should be in the same frame. Now my keyboard gets special treatment. Everything should be the same. Thank you" (Marko, 22.10.2026)

3. On the page all affiliate equipment (the two hubs and the Launchkey) sits in one frame, each item the same.

**Status:** waiting for the end of the session (ask Marko first): the release with the newest APK, the Novation section, and the equipment in one frame.

## v19 — 22.10.2026: high notes clean (from Marko's octave test)

> "I press the low C key. I mean, first I switch octave, and then I press the low C key, and the sound is very strange" (Marko, 22.10.2026, Launchkey at Octave +5, the low C = C7, note 108)

1. A reed's tone carries only the harmonics that fit under the digital limit at its pitch: at C7 the 28 harmonics reached 117 kHz and folded back as harsh tones (aliasing). Nine tones (28 … 1 harmonics), the richest clean one chosen per reed each block.

**Status:** tested 22.10.2026 through the Pixel's own microphone (scrcpy --audio-source=mic): C7 = 4186 Hz with its harmonics at exactly 2× and 3×, nothing folded back.

## v20 — 22.10.2026: the pitch strip on the display

> "I did it. I don't know what's wrong with pitch. When I'm moving modulation slider, the matrix display is showing the readings, but for this one everything is dead" (Marko, 22.10.2026)

1. The pitch strip did reach the app (bends 772 … 1131, back to 8192), but the Launchkey's own display does not show it; the app now writes "Pitch" over its value in semitones.

**Status:** tested 22.10.2026: "Pitch is registering in the display" (Marko).

## v21 — requested 22.10.2026: the 88 piano keys, names without minus, Oct ± moves the box at once

> "just fix these labels on the buttons. You put minus c vision keyboard one remove minus. Please also two way communication. If I go to my octa plus or octa minus, it's triggering the application directly. It's shifting the display man. And the lowest c is too low. Please find a range which makes sense. Please see the keyboard, the regular piano keys. What is the frequency of the lowest key? What is the frequency of the highest key? So we have 88 keys matching the piano" (Marko, 22.10.2026)

1. The range is the piano's 88 keys: A0 (27.5 Hz, MIDI 21) to C8 (4186 Hz, MIDI 108); the box from Sa = C1 to Sa = C7 (its Sa' is C8); keys outside the piano are not played.
2. Names without a minus: the piano's own names, A0 … C8, middle C = C4.
3. The Launchkey's Oct − / + move the box at once (if the keyboard reports them), not only when a key is played.

**Status:** asked; Oct ± being tested.

## v22 — requested 22.10.2026: a piano beside the shruti box

> "also turn this up in the piano. Find free piano samples which are the best quality. Which industry is a praising or you can maybe use wave table. Something which doesn't take much space. Try to find some good balance between size and quality. That's app which people download, so no big files. Piano and Shruti Box 2 sounds" (Marko, 22.10.2026)

1. Two sounds: Shruti Box (the reeds) and Piano.
2. The piano from the best free samples the industry praises (or a wavetable), small enough for a downloaded app: a balance of size and quality.

> "salamander" (Marko, 22.10.2026, choosing between Salamander Grand Piano V3 and Upright Piano KW)

**Status:** v22 built: Salamander Grand Piano V3 (Yamaha C5, CC BY 3.0), its own 30 notes A0 … C8 every minor third × 2 layers (v5 soft, v13 strong), downloaded alone (88 MB of FLAC instead of the whole set, on the Download Monitor), cut to 5 s with a 1.5 s fade, mono AAC 64 kbps 32 kHz: 2.5 MB in assets/piano. SOUND in settings: SHRUTI BOX | PIANO; the piano plays from the valves, the keys, the presets, the pads and the sequencer, bent by the strips; the credit in settings and the licence beside the samples.

## v25 — requested 22.10.2026: the Launchkey's velocity set from the app

> "Piano sounds good, but I think the issue is velocity is disabled in the settings of the keyboard itself. Read the manual and let me know where I can enable velocity. Or maybe you can in Dove mode. Maybe you can enable and disable velocity of the keys. Then we can do it remotely through the settings in the app" (Marko, 22.10.2026)

1. Novation's Programmer's Reference v2, feature controls (sent to the DAW port, channel 7; queried on channel 8; reply on channel 7): CC 104 (68h) "Keys velocity curve / Fixed velocity select", CC 106 (6Ah) "Fixed velocity value"; both non-volatile (kept across power cycles). The values are not listed in the guide.
2. The app asks the keyboard for CC 104 at connect (logged) and offers KEYBOARD VELOCITY in settings (SOFT / NORMAL / HARD / FIXED), sent as CC 104; the meaning of each value to be confirmed by playing.

**Status:** v25 tested 22.10.2026: the Launchkey answered CC 104 = 3 (Fixed) and CC 106 = 127 (the fixed value) — the reason soft and hard sounded the same; NORMAL from the app's settings → the keyboard confirmed CC 104 = 1; Marko played soft (velocities 3–11) and hard (80–123). So 0 Soft, 1 Normal, 2 Hard, 3 Fixed, as the Launchkey's own menu. Piano v22–v24 "sounds good" (Marko).

## v26 — requested 22.10.2026: it plays on in the background; ✕ releases everything

> "please make my app not release anything in the background, even the MIDI control the sound. Basically it can work in the background fully functional if it's not in the focus in my Android phone Pixel 7. Then what I want you to do is add X next to the gear icon, because if it's working in background, how can I disable it only by going to the settings of the app and then force killing it? That's not practical for everyday user, my dear. What is practical to have that X button like we have in our Swift apps, and then you just press X and everything is released" (Marko, 22.10.2026)

1. Out of focus (Home, another app, the screen off) the app keeps everything: the sound, the MIDI keyboard, the sequencer and the pads (a media-playback foreground service with its notification).
2. ✕ beside the gear: everything is released (sound, MIDI, the Launchkey back to standalone, the service) and the app closes; the notification has the same stop.
3. Back on the box sends the app to the background instead of closing it.

**Status:** v26 tested on the Pixel 7, 22.10.2026: a valve opened, Home pressed → the launcher in front, ShrutiService foreground (type media playback, its notification with Stop), the app's AudioTrack still "started"; ✕ beside the gear → the app closed, no service left, no audio player, the Launchkey released (DAW mode off, "closed"). The sequencer and AUTO now run on their own 8 ms clock instead of screen frames, and the pads are lit from it. Marko confirmed by hand: "I can confirm it works in the background" (the keyboard playing with the app out of focus). (POST_NOTIFICATIONS granted by adb for the test.)

## v27 — 22.10.2026: the sequencer's numbers kept

> "all unfinished tasks: please finish" (Marko, 22.10.2026)

1. The SEQ numbers (which CUSTOM preset is step 1, 2 …) are kept across restarts, as the presets are; the Launchkey's pads light again at once after a restart.

**Status:** tested 22.10.2026 (numbered 1 and 2, force-stopped, reopened: still 1 and 2); released as v27 with everything since v15.


## 22.10.2026: every page has a favicon, the app's own icon

> "please in manifest it's written, but you're not doing this. Every page needs fav icon. Please put it on shruti box on shruti page. Of course we will make it the same as application icon. Now that makes sense. Also for the dj app it makes sense, so please do that now and in the future" (Marko, 22.10.2026)

1. mantrashrutiandvoicetrainer.pages.dev gets a favicon made from the app's launcher icon; the DJ app page the same with DJ Mantra's icon; every page from now on.

**Status:** done 22.10.2026: favicon.svg from the launcher icon (MANTRA_MANIFEST kits/favicon/vector2svg.py), apple-touch-icon.png, favicon.ico; live, all three 200. The rule is in design-language.md §16.

## 1.11.2026: the page order (banner, introduction, lifestyle, technical, donation)

> "the page is perfect. Let's do it all pages" (Marko, 1.11.2026, about mantramiditools.pages.dev)

1. Every public app page in the order of MANTRA_MANIFEST modules/app-page-order.md: the banner (the app at work, name and one line on it), a short introduction with the download button and the version line, the lifestyle picture, the technical part, the donation bowl.

**Status:** done 1.11.2026 (site/public/index.html): banner = img/lifestyle.jpg (the singer with the phone and the MIDI keyboard) with the icon and the name on it, the introduction with Download the latest version (no version number, version-visible.md), the technical part, the bowl. A second picture is still needed for the lifestyle place (a placeholder comment marks it), to be made in Gemini.
**Status, 1.11.2026 (later):** the lifestyle picture is in (made in Gemini, lifestyle2.jpg); the page order is complete.

## 1.11.2026: download links that follow every new version by themselves
> "yes, fix download links so every website gets each new version by itself" (Marko, 1.11.2026)

**Status:** built 1.11.2026, not yet live: build-apk.yml puts each new release's APK at download/shruti-box-and-voice-trainer-latest.apk and deploys the page (scripts/publish_page.sh). Waiting for the repository secrets CLOUDFLARE_API_TOKEN and CLOUDFLARE_ACCOUNT_ID; until then the step skips with a notice and the page keeps the v27 file (identical to the release).

## 1.11.2026: My studio setup, a link above the bowl

> "Just above the Tibetan bowl for donation, you can just make one link, and then it will open the page with all lifestyle images and images of these products which I'm using in my development studio ... For this link, you can say "My studio setup." ... It is opening a new page, which is going to call studio setup mantra.pages.dev" (Marko, 1.11.2026)

1. One centred link "My studio setup" right above the donation bowl, opening https://studiosetupmantra.pages.dev in a new tab, with the line "The gear these apps are made with · affiliate links on Amazon.de".

**Status:** committed 1.11.2026, NOT pushed or deployed: waiting until Marko registers studiosetupmantra.pages.dev in Amazon PartnerNet and the studio page is live.
