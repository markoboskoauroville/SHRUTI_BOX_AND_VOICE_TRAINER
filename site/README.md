# The product page

https://mantrashrutiandvoicetrainer.pages.dev — the free download of Shruti Box and Voice Trainer, its features, the
shruti box's history and mantras to sing over it (Marko, 22.10.2026). Static files in `public/`; the APK is served from
`public/download/` because this repository is private. Images: two from Nano Banana (Gemini, auroville.community),
four screenshots of v13 on the Pixel 7. Deploy: `npx wrangler@3 pages deploy public --project-name
mantrashrutiandvoicetrainer --branch main`, the Cloudflare token and account from his key file in the environment,
never printed. A new release: copy its APK over the one in `public/download/` and change the version on the page.
