from pathlib import Path

root = Path(__file__).resolve().parents[1]
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text(encoding="utf-8")

assert "android.permission.INTERNET" not in manifest
assert "import android.webkit.WebView" not in main
assert "EditText" in main
assert "TextWatcher" in main
assert "afterTextChanged" in main
assert "Hledat podle názvu nebo popisu" in main
assert 'product.name + " " + product.subtitle' in main
assert "renderSearchResults" in main
assert "searchable.contains(query)" in main
assert "MOJE DOTAZY" in main
assert "ODEBRAT" in main
assert "SharedPreferences" in main
assert "ZOBRAZIT VŠE" in main
print("STEP 4 search static checks: OK")
