from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
main = (root / "app/src/main/java/cz/kspol/stableapp/resetv8/MainActivity.java").read_text()
manifest = (root / "app/src/main/AndroidManifest.xml").read_text()
gradle = (root / "app/build.gradle").read_text()

assert "import android.webkit.WebView" not in main
assert 'android.permission.INTERNET' in manifest
assert "versionCode 807" in gradle
assert "versionName '8.0-step7-shop-links-admin-entry'" in gradle

on_create = re.search(
    r"protected void onCreate\(Bundle savedInstanceState\) \{(.*?)\n    \}", main, re.S
).group(1)
assert "showHomeScreen();" in on_create
assert "downloadCatalog" not in on_create
assert "HttpURLConnection" not in on_create

assert 'tag.equals("link")' in main
assert "productUrl" in main
assert "ZOBRAZIT V E-SHOPU ↗" in main
assert "Intent.ACTION_VIEW" in main
assert "Uri.parse(productUrl)" in main
assert 'host.equals("zbrane-kspol.cz")' in main
assert 'host.endsWith(".zbrane-kspol.cz")' in main

assert "Dotaz odeslán. Číslo dotazu:" in main
assert "Dotaz uložen. Číslo dotazu:" not in main
assert "Číslo dotazu" in main

for feature in [
    "PRO ZAMĚSTNANCE",
    "Přístup pouze pro zaměstnance +K spol. s r.o.",
    "zbrane.kspol@gmail.com",
    "123456",
    "PŘIHLÁSIT",
    "ADMINISTRAČNÍ ROZHRANÍ",
    "ZMĚNIT HESLO",
    "ODHLÁSIT",
    "TEST REŽIM",
]:
    assert feature in main

assert "https://www.zbrane-kspol.cz/admin" not in main
assert "NAČÍST DALŠÍ" in main
assert "Všechny kategorie e-shopu" in main
print("STEP 7 shop links and admin entry checks: OK")
