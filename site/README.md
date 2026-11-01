# The product page

https://mantrashrutiandvoicetrainer.pages.dev — the free download of Shruti Box and Voice Trainer, its features, the
shruti box's history and mantras to sing over it (Marko, 22.10.2026). Static files in `public/`; the APK is served from
`public/download/` because this repository is private. Images: two from Nano Banana (Gemini, auroville.community),
four screenshots of v13 on the Pixel 7. Deploy (since 1.11.2026, MANTRA_MANIFEST download-always-latest.md): **by build-apk.yml only**, in the run on main that
publishes a new release: `scripts/publish_page.sh` takes that release's own APK, puts it at
`public/download/shruti-box-and-voice-trainer-latest.apk` (the button's address, never with a version, never edited),
writes its size into the `app-size` spans, and runs wrangler. Actions → Run workflow on main puts the existing release of
appVersion on the page again. It needs the repository secrets CLOUDFLARE_API_TOKEN and CLOUDFLARE_ACCOUNT_ID; without
them the step says so and skips. git holds no APK, so never deploy `public/` by hand from the Mac (it would take the
download off the page). The page never names a version (MANTRA_MANIFEST version-visible.md, 22.10.2026); when the
repository goes public the button links `releases/latest` instead.
