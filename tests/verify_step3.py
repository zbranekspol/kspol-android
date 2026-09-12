from pathlib import Path

root = Path(__file__).resolve().parents[1]
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text(encoding="utf-8")

assert "android.permission.INTERNET" not in manifest
assert "import android.webkit.WebView" not in main
assert "PŘIDAT DO POPTÁVKY" in main
assert "ODEBRAT" in main
assert "MOJE DOTAZY" in main
assert "SharedPreferences" in main
assert 'SimpleDateFormat("dd.MM."' in main
assert 'String.format(Locale.ROOT, "%03d", sequence)' in main
assert "saveLocalInquiry" in main
assert "loadLocalInquiries" in main
assert "ZOBRAZIT VŠE" in main
print("STEP 3 static checks: OK")
