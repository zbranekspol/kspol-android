from pathlib import Path

root = Path(__file__).resolve().parents[1]
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
activity = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text(encoding="utf-8")

assert "android.permission.INTERNET" not in manifest
assert "WebView" not in activity
assert "http://" not in activity and "https://" not in activity
assert activity.count('"KRÁTKÉ ZBRANĚ"') == 1
assert '"ZOBRAZIT VŠE"' in activity
assert "R.drawable.kspol_logo" in activity
print("STEP 1 static stability checks: OK")
