#!/usr/bin/env bash
# The page gets the released APK by itself (MANTRA_MANIFEST modules/download-always-latest.md, 1.11.2026).
# Usage: scripts/publish_page.sh <released.apk>
# Puts the APK at site/public/download/shruti-box-and-voice-trainer-latest.apk (the button's address, no version, written
# once in index.html), writes its size into the page, and deploys site/public to mantrashrutiandvoicetrainer.pages.dev.
# The page names no version (version-visible.md, Marko 22.10.2026). Run by build-apk.yml right after the release;
# CLOUDFLARE_API_TOKEN and CLOUDFLARE_ACCOUNT_ID in the environment.
set -euo pipefail
APK="$1"
cd "$(dirname "$0")/.."
PAGE=site/public/index.html
OUT=site/public/download/shruti-box-and-voice-trainer-latest.apk

[ "$(head -c 2 "$APK")" = "PK" ] || { echo "not an APK: $APK"; exit 1; }
BYTES=$(stat -c%s "$APK" 2>/dev/null || stat -f%z "$APK")
[ "$BYTES" -lt 26214400 ] || { echo "APK is $BYTES bytes, over Cloudflare Pages' 25 MB per file"; exit 1; }
MB=$(( (BYTES + 524288) / 1048576 )); [ "$MB" -ge 1 ] || MB=1

mkdir -p site/public/download
cp "$APK" "$OUT"
sed -i.bak -E "s#(<span class=\"app-size\">)[0-9]+(</span>)#\1${MB}\2#g" "$PAGE"; rm -f "$PAGE.bak"
grep -q 'href="download/shruti-box-and-voice-trainer-latest.apk"' "$PAGE" || { echo "the button does not link the fixed address"; exit 1; }
echo "page: $OUT = $BYTES bytes ($MB MB)"

npx --yes wrangler@4 pages deploy site/public --project-name mantrashrutiandvoicetrainer --branch main --commit-dirty=true
