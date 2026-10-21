"""The structural checks a compiler will not run (MANTRA_MANIFEST android-app.md §1). Run on the desk and in CI."""
import pathlib, re, sys
root = pathlib.Path(__file__).resolve().parent.parent
src = root / "app/src/main/java/com/mantra/shrutibox"
fails = []
reeds = (src / "Reeds.kt").read_text()
if re.search(r"^import android", reeds, re.M):
    fails.append("Reeds.kt imports Android: Test 1 could no longer run it on a plain JVM")
v = re.search(r"^appVersion=(\S+)", (root / "gradle.properties").read_text(), re.M)
if not v or not v.group(1).isdigit():
    fails.append("appVersion is not one whole number (versioning.md)")
keys = re.compile(r"(gsk_|AIza|ghp_|github_pat_|sk-ant-|xox[baprs]-)[A-Za-z0-9_-]{20,}")
files = [p for p in root.rglob("*") if p.is_file() and p.suffix in {".kt", ".kts", ".xml", ".properties", ".yml", ".md", ".py"} and "build" not in p.parts]
for p in files:
    if keys.search(p.read_text(errors="ignore")):
        fails.append(f"a key shape in {p.relative_to(root)}")
ui = (src / "MainActivity.kt").read_text()
for words in ("press to", "tap to", "click to"):
    if words in ui.lower():
        fails.append(f'a sentence on a control ("{words}"): one word, the control shows the state (mantra-design-language.md §1.3)')
print(f"files examined: {len(files)}")
for f in fails: print("FAIL:", f)
sys.exit(1 if fails else 0)
